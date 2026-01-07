package net.soundvibe.hasio;

import io.javalin.Javalin;
import net.soundvibe.hasio.model.Options;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class BootstrapperTest {

    @Mock
    private Javalin app;

    @Mock
    private MqttClient mqttClient;

    @Mock
    private IMqttToken mqttToken;

    private Bootstrapper bootstrapper;
    private Options options;

    @BeforeEach
    void setUp() {
        options = new Options(1, "sensor.test_%d", 9199,
                false, "localhost", 1883, 60, "", "", "info");
        bootstrapper = new Bootstrapper(app, options);
    }

    @Test
    void should_close_mqtt_client_and_clear_subscribers() throws Exception {
        setMqttClientRef(mqttClient);
        Map<String, IMqttToken> subscribers = getSubscribersMap();
        subscribers.put("topic1", mqttToken);
        subscribers.put("topic2", mqttToken);

        invokeCloseMqttClient();

        verify(mqttClient).unsubscribe("topic1");
        verify(mqttClient).unsubscribe("topic2");
        verify(mqttClient).disconnect();
        verify(mqttClient).close();
        assertTrue(subscribers.isEmpty(), "Subscribers should be cleared");
        assertNull(getMqttClientRef(), "MQTT client reference should be null");
    }

    @Test
    void should_handle_null_mqtt_client_gracefully() throws Exception {
        setMqttClientRef(null);

        assertDoesNotThrow(this::invokeCloseMqttClient);
    }

    @Test
    void should_continue_cleanup_even_if_unsubscribe_fails() throws Exception {
        setMqttClientRef(mqttClient);
        Map<String, IMqttToken> subscribers = getSubscribersMap();
        subscribers.put("topic1", mqttToken);
        doThrow(new MqttException(1)).when(mqttClient).unsubscribe("topic1");

        invokeCloseMqttClient();

        verify(mqttClient).disconnect();
        verify(mqttClient).close();
        assertTrue(subscribers.isEmpty());
    }

    @Test
    void should_have_static_executor_services() throws Exception {
        Field executorField = Bootstrapper.class.getDeclaredField("executorService");
        Field schedulerField = Bootstrapper.class.getDeclaredField("scheduler");

        assertTrue(java.lang.reflect.Modifier.isStatic(executorField.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isFinal(executorField.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isStatic(schedulerField.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isFinal(schedulerField.getModifiers()));
    }

    private void setMqttClientRef(MqttClient client) throws Exception {
        Field field = Bootstrapper.class.getDeclaredField("mqttClientRef");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        AtomicReference<MqttClient> ref = (AtomicReference<MqttClient>) field.get(bootstrapper);
        ref.set(client);
    }

    private MqttClient getMqttClientRef() throws Exception {
        Field field = Bootstrapper.class.getDeclaredField("mqttClientRef");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        AtomicReference<MqttClient> ref = (AtomicReference<MqttClient>) field.get(bootstrapper);
        return ref.get();
    }

    @SuppressWarnings("unchecked")
    private Map<String, IMqttToken> getSubscribersMap() throws Exception {
        Field field = Bootstrapper.class.getDeclaredField("subscribers");
        field.setAccessible(true);
        return (Map<String, IMqttToken>) field.get(bootstrapper);
    }

    private void invokeCloseMqttClient() throws Exception {
        Method method = Bootstrapper.class.getDeclaredMethod("closeMqttClient");
        method.setAccessible(true);
        method.invoke(bootstrapper);
    }
}
