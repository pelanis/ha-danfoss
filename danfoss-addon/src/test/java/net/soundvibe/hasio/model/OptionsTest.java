package net.soundvibe.hasio.model;

import net.soundvibe.hasio.Json;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OptionsTest {

    @TempDir
    Path tempDir;

    @Test
    void should_return_defaults_when_file_does_not_exist() {
        var options = Options.fromPath(tempDir.resolve("nonexistent.json"));

        assertEquals(1, options.haUpdatePeriodInSeconds());
        assertEquals("sensor.danfoss_%d_temperature", options.sensorNameFmt());
        assertEquals(9199, options.port());
        assertFalse(options.mqttEnabled());
        assertEquals("core-mosquitto", options.mqttHost());
        assertEquals(1883, options.mqttPort());
        assertEquals(60, options.mqttKeepAlive());
        assertEquals("", options.mqttUsername());
        assertEquals("", options.mqttPassword());
        assertEquals("info", options.logLevel());
    }

    @Test
    void should_parse_valid_config() throws IOException {
        var configJson = """
            {
                "haUpdatePeriodInSeconds": 30,
                "sensorNameFmt": "sensor.test_%d",
                "port": 8080,
                "mqttEnabled": true,
                "mqttHost": "localhost",
                "mqttPort": 1884,
                "mqttKeepAlive": 120,
                "mqttUsername": "user",
                "mqttPassword": "pass",
                "logLevel": "debug"
            }
            """;
        var configPath = tempDir.resolve("options.json");
        Files.writeString(configPath, configJson);

        var options = Options.fromPath(configPath);

        assertEquals(30, options.haUpdatePeriodInSeconds());
        assertEquals("sensor.test_%d", options.sensorNameFmt());
        assertEquals(8080, options.port());
        assertTrue(options.mqttEnabled());
        assertEquals("localhost", options.mqttHost());
        assertEquals(1884, options.mqttPort());
        assertEquals(120, options.mqttKeepAlive());
        assertEquals("user", options.mqttUsername());
        assertEquals("pass", options.mqttPassword());
        assertEquals("debug", options.logLevel());
    }

    @Test
    void should_ignore_unknown_fields_for_backward_compatibility() throws IOException {
        var configJson = """
            {
                "haUpdatePeriodInSeconds": 60,
                "sensorNameFmt": "sensor.danfoss_%d_temperature",
                "port": 9199,
                "webui_host": "192.168.1.100",
                "webui_use_port": false,
                "webui_port": "8080",
                "mqttEnabled": false,
                "mqttHost": "core-mosquitto",
                "mqttPort": 1883,
                "mqttKeepAlive": 60,
                "mqttUsername": "",
                "mqttPassword": "",
                "logLevel": "info"
            }
            """;
        var configPath = tempDir.resolve("old_config.json");
        Files.writeString(configPath, configJson);

        var options = Options.fromPath(configPath);

        assertNotNull(options);
        assertEquals(60, options.haUpdatePeriodInSeconds());
        assertEquals(9199, options.port());
        assertFalse(options.mqttEnabled());
    }

    @Test
    void should_serialize_to_json() {
        var options = new Options(60, "sensor.test_%d", 9199,
                true, "mqtt.local", 1883, 60, "user", "pass", "debug");

        var json = Json.toJsonString(options);

        assertNotNull(json);
        assertTrue(json.contains("\"haUpdatePeriodInSeconds\":60"));
        assertTrue(json.contains("\"mqttEnabled\":true"));
    }
}
