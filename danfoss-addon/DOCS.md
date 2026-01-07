# Configuration Guide

Complete reference for all add-on configuration options and advanced Home Assistant integration.

---

## Configuration Options

Configure these options in the add-on's **Configuration** tab.

### General Settings

| Parameter | Default | Description |
|-----------|---------|-------------|
| `haUpdatePeriodInSeconds` | `60` | How often to sync data with Home Assistant (1-86400 seconds) |
| `sensorNameFmt` | `sensor.danfoss_%d_temperature` | Sensor entity naming pattern ([format syntax](https://docs.oracle.com/javase/21/docs/api/java/util/Formatter.html#syntax)) |
| `port` | `9199` | HTTP port for the embedded web server |
| `logLevel` | `info` | Log verbosity: `trace`, `debug`, `info`, `warn`, `error` |

### MQTT Settings

| Parameter | Default | Description |
|-----------|---------|-------------|
| `mqttEnabled` | `false` | Enable MQTT auto-discovery for climate entities |
| `mqttHost` | `core-mosquitto` | MQTT broker hostname |
| `mqttPort` | `1883` | MQTT broker port |
| `mqttKeepAlive` | `60` | MQTT keep-alive interval in seconds |
| `mqttUsername` | _(empty)_ | MQTT authentication username |
| `mqttPassword` | _(empty)_ | MQTT authentication password |

### Advanced Settings

| Parameter | Default | Description |
|-----------|---------|-------------|
| `m4FixEnabled` | `false` | Enable JVM fix for Apple M4 Macs (`-XX:UseSVE=0`). Only enable if experiencing startup issues on M4 hardware. |

---

## Home Assistant Integration

### Option 1: MQTT (Recommended)

The easiest way to integrate thermostats as climate entities.

1. Enable `mqttEnabled` in add-on configuration
2. Configure MQTT broker credentials if required
3. Restart the add-on

All thermostats will automatically appear as climate entities in Home Assistant.

### Option 2: Manual Setup (Without MQTT)

If MQTT is not available, you can manually create climate entities using REST commands and templates.

#### Step 1: Create REST Commands

Add to your `configuration.yaml`:

```yaml
rest_command:
  set_danfoss_home_temp:
    url: http://localhost:9199/command
    method: POST
    headers:
      accept: "application/json"
    payload: '{"command":"setHomeTemperature","value":{{ temperature }},"roomNumber":{{ roomNumber }}}'
    content_type: "application/json; charset=utf-8"

  set_danfoss_away_temp:
    url: http://localhost:9199/command
    method: POST
    headers:
      accept: "application/json"
    payload: '{"command":"setAwayTemperature","value":{{ temperature }},"roomNumber":{{ roomNumber }}}'
    content_type: "application/json; charset=utf-8"
```

#### Step 2: Test the Commands

Call the service from **Developer Tools → Services**:

```yaml
service: rest_command.set_danfoss_home_temp
data:
  temperature: 22.5
  roomNumber: 0
```

> **Note:** `roomNumber` is available as an attribute on each Danfoss temperature sensor entity.

#### Step 3: Create Climate Entities

Install the [Template Climate](https://github.com/jcwillox/hass-template-climate) integration, then add to `configuration.yaml`:

```yaml
climate:
  - platform: climate_template
    name: Living Room Thermostat
    unique_id: danfoss_0_thermostat
    min_temp: 5
    max_temp: 35
    temp_step: 0.5
    modes:
      - "off"
      - "heat"
    hvac_mode_template: "{{ state_attr('sensor.danfoss_0_temperature', 'mode') }}"
    current_temperature_template: "{{ states('sensor.danfoss_0_temperature') }}"
    target_temperature_template: "{{ state_attr('sensor.danfoss_0_temperature', 'temperature_home') }}"
    set_temperature:
      service: rest_command.set_danfoss_home_temp
      data:
        temperature: "{{ temperature }}"
        roomNumber: "{{ state_attr('sensor.danfoss_0_temperature', 'room_number') }}"
```

Repeat for each room, changing `danfoss_0` to match the room number.

---

## API Reference

The add-on exposes a REST API on the configured port.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/health` | GET | Health check (returns `OK`) |
| `/rooms` | GET | List all rooms with current state |
| `/rooms/{name}` | GET | Get specific room by name |
| `/command` | POST | Send command to thermostat |
| `/discover` | POST | Initiate pairing (Web UI form) |

### Command Payload

```json
{
  "command": "setHomeTemperature",
  "value": 22.5,
  "roomNumber": 0
}
```

Available commands:
- `setHomeTemperature` - Set target temperature for home mode
- `setAwayTemperature` - Set target temperature for away mode

---

## Support

This is a fork of [soundvibe/ha-danfoss](https://github.com/soundvibe/ha-danfoss). If you find this add-on useful, please consider supporting the original author.
