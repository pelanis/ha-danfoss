package net.soundvibe.hasio;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import net.soundvibe.hasio.danfoss.protocol.IconMasterHandler;
import net.soundvibe.hasio.danfoss.protocol.IconRoomHandler;
import net.soundvibe.hasio.danfoss.protocol.config.AppConfig;
import net.soundvibe.hasio.ha.HomeAssistantClient;
import net.soundvibe.hasio.ha.model.MQTTSetState;
import net.soundvibe.hasio.model.Command;
import net.soundvibe.hasio.model.Options;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.function.Predicate.not;
import static net.soundvibe.hasio.danfoss.protocol.config.DanfossBindingConstants.ICON_MAX_ROOMS;

public class Bootstrapper {

    private static final Logger logger = LoggerFactory.getLogger(Bootstrapper.class);
    private static final String STATE_TOPIC_FMT = "danfoss/icon/%d/state";
    private static final String SET_TOPIC_FMT = "danfoss/icon/%d/set";
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4, Thread.ofVirtual().factory());
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, Thread.ofVirtual().factory());

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down executor services...");
            executorService.shutdown();
            scheduler.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            logger.info("Executor services shut down");
        }));
    }

    private final Options options;
    private final Map<String, IMqttToken> subscribers = new ConcurrentHashMap<>(ICON_MAX_ROOMS * 2);
    private final AtomicBoolean opened = new AtomicBoolean(false);
    private final Javalin app;
    private final AtomicReference<IconMasterHandler> masterHandler = new AtomicReference<>();
    private final AtomicReference<ScheduledFuture<?>> scheduleHAUpdates = new AtomicReference<>();
    private final AtomicReference<ScheduledFuture<?>> scheduleMQTTUpdates = new AtomicReference<>();
    private final AtomicReference<MqttClient> mqttClientRef = new AtomicReference<>();
    private final AtomicReference<HomeAssistantClient> haClientRef = new AtomicReference<>();

    public Bootstrapper(Javalin app, Options options) {
        this.app = app;
        this.options = options;
    }

    public void load(AppConfig appConfig) {
        if (this.scheduleHAUpdates.get() != null) {
            this.scheduleHAUpdates.get().cancel(true);
        }
        if (scheduleMQTTUpdates.get() != null) {
            scheduleMQTTUpdates.get().cancel(true);
        }
        closeMqttClient();

        var masterHandler = new IconMasterHandler(appConfig.privateKey(), executorService);
        masterHandler.scanRooms(appConfig.peerId());
        logger.info("rooms scanned: {}", appConfig.peerId());
        this.masterHandler.set(masterHandler);

        var token = resolveToken();
        logger.info("SUPERVISOR_TOKEN: {}", token);
        if (token.isEmpty()) {
            logger.warn("authorization token not found");
        } else {
            if (haClientRef.get() == null) {
                haClientRef.set(new HomeAssistantClient(token));
            }
            logger.info("scheduling HA state updater");
            this.scheduleHAUpdates.set(scheduleHomeAssistantUpdates(options));
        }

        if (options.mqttEnabled()) {
            scheduleMQTTUpdates.set(scheduleMQTTUpdates(options));
        }

        if (opened.get()) {
            return;
        }

        app.get("/rooms", ctx -> {
            var rooms = this.masterHandler.get().listRooms();
            ctx.json(rooms);
        });
        app.get("/rooms/{roomName}", ctx -> {
            var roomName = ctx.queryParam("roomName");
            this.masterHandler.get().listRooms().stream()
                    .filter(iconRoom -> iconRoom.name().equals(roomName))
                    .findAny()
                    .ifPresentOrElse(ctx::json, () -> ctx.status(HttpStatus.NOT_FOUND));
        });
        app.post("/command", ctx -> {
            try {
                var command = Json.fromString(ctx.body(), Command.class);
                executeCommand(command);
                ctx.status(200)
                        .result("""
            { "status": "OK" }""")
                        .contentType("application/json");
            } catch (Throwable e) {
                ctx.status(500)
                        .result(STR."""
                        "status": "error", "error": "\{ e.getMessage() }"
                        """)
                        .contentType("application/json");
            }
        });
        opened.set(true);
    }

    private void executeCommand(Command cmd) {
        logger.info("executing cmd: {}", cmd.command());

        var maybeRoom = this.masterHandler.get().roomHandlerByNumber(cmd.roomNumber());
        if (maybeRoom.isEmpty()) {
            logger.info("room not found: {}", cmd.roomNumber());
            return;
        }
        var roomHandler =  maybeRoom.get();

        switch (cmd.command()) {
            case "setHomeTemperature": {
                roomHandler.setHomeTemperature(cmd.value());
                break;
            }
            case "setAwayTemperature": {
                roomHandler.setAwayTemperature(cmd.value());
                break;
            }
            case "setSleepTemperature": {
                roomHandler.setSleepTemperature(cmd.value());
                break;
            }
            default:
                logger.warn("unknown command: {}", cmd.command());
        }
    }

    private ScheduledFuture<?> scheduleHomeAssistantUpdates(Options options) {
        return scheduler.scheduleAtFixedRate(() -> {
            var handler = this.masterHandler.get();
            var haClient = this.haClientRef.get();
            if (handler == null || haClient == null) {
                return;
            }
            try {
                for (var room : handler.listRooms()) {
                    var sensorName = String.format(options.sensorNameFmt(), room.number());
                    var state = room.toState();
                    haClient.upsertState(state, sensorName);
                }

                var iconMaster = handler.iconMaster();
                if (iconMaster != null && iconMaster.houseName() != null) {
                    haClient.upsertState(iconMaster.toState(), "sensor.danfoss_master_controller_last_updated");
                    logger.info("sensors updated successfully");
                } else {
                    logger.debug("IconMaster data not yet available, skipping master controller update");
                }
            } catch (Exception e) {
                logger.error("sensor update error", e);
            }
        }, 1, options.haUpdatePeriodInSeconds(), TimeUnit.SECONDS);
    }

    private String resolveToken() {
        var token = System.getenv("SUPERVISOR_TOKEN");
        if (token != null && !token.isEmpty()) {
            return token;
        }

        return System.getProperty("SUPERVISOR_TOKEN", "");
    }

    private void closeMqttClient() {
        var oldClient = mqttClientRef.getAndSet(null);
        if (oldClient != null) {
            try {
                for (var topic : subscribers.keySet()) {
                    try {
                        oldClient.unsubscribe(topic);
                    } catch (MqttException e) {
                        logger.debug("Failed to unsubscribe from {}", topic);
                    }
                }
                subscribers.clear();
                oldClient.disconnect();
                oldClient.close();
                logger.info("MQTT client closed");
            } catch (MqttException e) {
                logger.warn("Error closing MQTT client: {}", e.getMessage());
            }
        }
    }

    private ScheduledFuture<?> scheduleMQTTUpdates(Options options) {
        String clientID = UUID.randomUUID().toString();
        try {
            var mqttClient = new MqttClient(STR."tcp://\{options.mqttHost()}:\{options.mqttPort()}", clientID);
            var mqttConnOptions = new MqttConnectOptions();
            mqttConnOptions.setAutomaticReconnect(true);
            mqttConnOptions.setCleanSession(true);
            mqttConnOptions.setConnectionTimeout(10);
            mqttConnOptions.setKeepAliveInterval(options.mqttKeepAlive());
            mqttConnOptions.setUserName(options.mqttUsername());
            mqttConnOptions.setPassword(options.mqttPassword().toCharArray());
            mqttClient.connect(mqttConnOptions);
            mqttClientRef.set(mqttClient);
            logger.info("MQTT connection established successfully");
            return scheduler.scheduleAtFixedRate(() -> {
                var handler = this.masterHandler.get();
                var client = this.mqttClientRef.get();
                if (handler == null || client == null || !client.isConnected()) {
                    return;
                }
                try {
                    var iconMaster = handler.iconMaster();
                    if (iconMaster == null || iconMaster.houseName() == null) {
                        logger.debug("IconMaster data not yet available, skipping MQTT update");
                        return;
                    }

                    for (var room : handler.listRooms()) {
                        var thermostatID = STR."danfoss_icon_thermostat_room_\{room.number()}";
                        var entityTopic = STR."homeassistant/climate/\{thermostatID}/config";
                        var climateEntity = room.toMQTTClimateEntity(thermostatID, STATE_TOPIC_FMT, SET_TOPIC_FMT, iconMaster);
                        client.publish(entityTopic, Json.toJsonBytes(climateEntity), 0, false);

                        var stateTopic = String.format(STATE_TOPIC_FMT, room.number());
                        var state = room.toState();
                        client.publish(stateTopic, Json.toJsonBytes(state), 0, false);

                        var setTopic = String.format(SET_TOPIC_FMT, room.number());
                        var mqttToken = subscribeToTopic(setTopic, client);
                        if (!mqttToken.isComplete()) {
                            client.unsubscribe(setTopic);
                            subscribers.remove(setTopic);
                            logger.info("MQTT subscriber removed = {}", setTopic);
                        }
                    }
                    logger.info("MQTT sensors updated successfully");
                } catch (Exception e) {
                    logger.error("MQTT sensor update error", e);
                }
            }, 1, options.haUpdatePeriodInSeconds(), TimeUnit.SECONDS);
        } catch (MqttException e) {
            logger.error("unable to connect to MQTT broker", e);
            throw new RuntimeException(e);
        }
    }

    private IMqttToken subscribeToTopic(String setTopic, MqttClient mqttClient) {
        return subscribers.compute(setTopic, (key, listener) -> {
            if (listener != null) {
                return listener;
            }

            IMqttMessageListener newListener = (_, message) -> {
                try {
                    var setState = Json.fromString(message.toString(), MQTTSetState.class);
                    // get room preset
                    masterHandler.get().roomHandlerByNumber(setState.room_number())
                            .map(IconRoomHandler::toIconRoom)
                            .map(room -> switch (room.roomMode()) {
                                case HOME -> "setHomeTemperature";
                                case AWAY -> "setAwayTemperature";
                                case SLEEP -> "setSleepTemperature";
                                default -> "";
                            })
                            .filter(not(String::isEmpty))
                            .map(cmdName -> new Command(cmdName, setState.room_number(), setState.temperature_target()))
                            .ifPresent(this::executeCommand);
                } catch (Throwable e) {
                    logger.warn("got error on topic listener", e);
                }
            };
            try {
                var token = mqttClient.subscribeWithResponse(key, newListener);
                if (token.getException() != null) {
                    throw token.getException();
                }
                logger.info("subscribed to MQTT topic {} successfully", key);
                return token;
            } catch (MqttException e) {
                logger.error("unable to subscribe to MQTT topic", e);
                throw new RuntimeException(e);
            }
        });
    }
}
