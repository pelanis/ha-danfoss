<div align="center">
<h1>Danfoss Icon Controller Add-on (Fork)</h1>
<p><strong>Version 0.5.0-fork</strong> - Enhanced fork with improved UI, stability fixes, and reverse proxy support</p>
</div>

## What's New in This Fork

- ✅ **Fixed NPE crashes** - No more NullPointerException on startup
- 🎨 **Modern UI** - Redesigned web interface with Danfoss branding
- 🔧 **Reverse proxy support** - New configuration options for custom hosts/ports
- 📚 **Comprehensive documentation** - Build and deployment guides
- 🔄 **Automated build validation** - CI/CD workflow

See [CHANGELOG](danfoss-addon/CHANGELOG.md) for full details.

## Installation

Add the repository URL under **Settings → Add-ons → Add-on Store** in your Home Assistant:

    https://github.com/pelanis/ha-danfoss

Then install **Danfoss Icon (Fork)** from the add-on store.

**Note:** First install takes 5-10 minutes as Home Assistant builds the addon locally.

## Configuration

The addon supports additional configuration options for reverse proxy scenarios:

```yaml
webui_host: ""           # Custom host (leave empty for auto-detect)
webui_use_port: true     # Include port in WebUI URL
webui_port: ""           # Custom port (leave empty to use main port)
```

See [addon README](danfoss-addon/README.md) for full configuration options and [CHANGELOG](danfoss-addon/CHANGELOG.md) for version history.

## Credits

- **Original addon:** [soundvibe/ha-danfoss](https://github.com/soundvibe/ha-danfoss)
- **Fork maintained by:** Pelanis

---

**Note:** This is a personal fork maintained independently. For the original addon, see [soundvibe/ha-danfoss](https://github.com/soundvibe/ha-danfoss).

