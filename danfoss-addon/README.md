# Danfoss Icon Add-on Setup Guide

Integrate your [Danfoss Icon Controller](https://www.danfoss.com/en-gb/products/dhs/smart-heating/smart-heating/danfoss-icon/) floor heating system with Home Assistant.

---

## Requirements

- Home Assistant with Supervisor
- Danfoss Icon Master Controller connected to your network
- **Danfoss Icon Android app** (required for pairing)

> **Important:** Only the Android app is supported for pairing. The iPhone app uses a different protocol that is not compatible with this add-on.

---

## Pairing Process

### Step 1: Get Your Pairing Code

1. Open the **Danfoss Icon** app on your Android device
2. Go to **Settings → Share house**
3. Copy the one-time code (enter it **without dashes**)

### Step 2: Connect to Home Assistant

1. Start the add-on and open the **Web UI**
2. Enter your **one-time code** from Step 1
3. Enter a **username** (can be any name you choose)
4. Click **Connect**

If successful, you'll see a confirmation message with your house name.

> **Tip:** If you only have an iPhone, borrow an Android device temporarily for pairing. Once complete, the Android app is no longer needed.

---

## After Pairing

### Configuration File

The add-on saves connection credentials to:
```
/share/danfoss-icon/danfoss_config.json
```

As long as this file exists, the add-on will automatically reconnect on restart without needing to pair again.

### Home Assistant Integration

**With MQTT (Recommended):**
Enable MQTT in the add-on configuration. All thermostats will automatically appear as climate entities in Home Assistant.

**Without MQTT:**
Thermostats are exposed as temperature sensors:
- Entity ID: `sensor.danfoss_{room_number}_temperature`
- Attributes: `battery_level`, `room_number`, `temperature_home`, `temperature_away`

See [Configuration Guide](DOCS.md) for manual climate entity setup without MQTT.

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Add-on won't start | Check logs for memory errors. Try increasing container memory. |
| Pairing fails | Verify the code is from Android app and entered without dashes. |
| Web UI unstyled | Clear browser cache and reload. |
| Thermostats not appearing | Enable MQTT or wait 60 seconds for sensor discovery. |

---

## More Information

- [Configuration Options](DOCS.md) - All settings and advanced setup
- [Changelog](CHANGELOG.md) - Version history

## Credits

Fork of [soundvibe/ha-danfoss](https://github.com/soundvibe/ha-danfoss). Please support the original author if you find this add-on useful.
