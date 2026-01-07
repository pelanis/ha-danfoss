<div align="center">

# Danfoss Icon Controller Add-on (Fork)

**Version 0.5.5-fork**

Integrate your Danfoss Icon floor heating system with Home Assistant

[![Home Assistant](https://img.shields.io/badge/Home%20Assistant-Add--on-blue?logo=homeassistant)](https://www.home-assistant.io/)

</div>

---

## Features

| Feature | Description |
|---------|-------------|
| **Automatic Discovery** | MQTT auto-discovery creates climate entities automatically |
| **Real-time Updates** | Temperature and battery status synced every 60 seconds |
| **Ingress Support** | Access the web UI directly from Home Assistant |
| **Low Memory** | Optimized for Raspberry Pi and constrained environments |

## Quick Start

### 1. Add Repository

In Home Assistant, go to **Settings → Add-ons → Add-on Store → ⋮ → Repositories**

Add this URL:
```
https://github.com/pelanis/ha-danfoss
```

### 2. Install Add-on

Find **Danfoss Icon (Fork)** in the add-on store and click **Install**.

> **Note:** First installation takes 5-10 minutes as Home Assistant builds the container.

### 3. Pair Your Controller

Open the add-on **Web UI** and enter your credentials from the Danfoss Icon Android app.

See [Setup Guide](danfoss-addon/README.md) for detailed instructions.

---

## Documentation

| Document | Description |
|----------|-------------|
| [Setup Guide](danfoss-addon/README.md) | Installation and pairing instructions |
| [Configuration](danfoss-addon/DOCS.md) | All configuration options and advanced setup |
| [Changelog](danfoss-addon/CHANGELOG.md) | Version history and release notes |

## What's New in This Fork

- Fixed startup crashes (NullPointerException)
- Fixed memory issues for constrained environments
- Modern web interface with Danfoss branding
- Home Assistant ingress support
- Automated CI/CD build validation

## Credits

- **Original addon:** [soundvibe/ha-danfoss](https://github.com/soundvibe/ha-danfoss)
- **Fork maintained by:** [Pelanis](https://github.com/pelanis)

---

<div align="center">
<sub>This is an independent fork. For the original addon, see <a href="https://github.com/soundvibe/ha-danfoss">soundvibe/ha-danfoss</a></sub>
</div>
