# Changelog

## [0.5.1-fork] - Ingress Support

- Enabled Home Assistant ingress for reverse proxy support
- Removed unused webui_host, webui_use_port, webui_port configuration options
- Simplified configuration by relying on HA ingress routing

## [0.5.0-fork] - Fork Release

**This is a forked version with significant enhancements**

- Fixed NullPointerException when IconMaster data is not yet available during startup
- Added null-safety checks for IconMaster state reporting to both Home Assistant and MQTT
- Redesigned web UI with modern Danfoss branding and improved UX
- Added styled success/error pages for pairing process
- Added copy-to-clipboard functionality for configuration JSON
- Improved form validation and loading states in web interface
- Added GitHub Actions workflow for automated build validation
- Updated README with fork installation instructions

## [0.4.5]

- Added new config option `m4FixEnabled` which could be enabled to fix Mac M4 startup bug.

## [0.4.4]

- Reverted M4 Mac bugfix because it breaks some other cpu architectures. Will fix in later releases.

## [0.4.3]

- Trying to fix M4 Mac bug.

## [0.3.9]

- Trying to fix MQTT subscriptions and app rediscovery.

## [0.3.8]

- Fixed reporting and switching between heating/cooling modes.

## [0.3.7]

- Added debug logging for unused heating/cooling states.

## [0.3.5]

- Add support for cooling mode.

## [0.3.4]

- Use set point home for target temp high and set point away for target temp low.

## [0.3.3]

- HA update interval changed to seconds instead of minutes.

## [0.3.2]

- Can update log level

## [0.3.1]

- Minor improvements

## [0.3.0]

- MQTT integration added

## [0.2.5]

- Made port configurable

## [0.2.4]

- Try to create config dir before writing to it during pairing process 

## [0.2.3]

- Updated master controller state (`sensor.danfoss_master_controller_last_updated`)

## [0.2.2]

- Exposed master controller state (`sensor.danfoss_master_controller`)

## [0.2.1]

- Improved logging

## [0.2.0]

- Exposed more sensor data

## [0.1.8]

- Updated documentation

## [0.1.7]

- Added commands RESTful endpoint
- Added documentation how to set temperature from HA

## [0.1.3]

- Added translations for configuration
- Docs

## [0.1.2]

- Added configuration options

## [0.1.1]

- Added unique_id attribute to entities

## [0.1.0]

- Initial working version.
- Thermostats are exposed to HA.