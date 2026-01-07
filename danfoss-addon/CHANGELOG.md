# Changelog

All notable changes to this project are documented in this file.

---

## Fork Releases

### [0.5.5-fork] - OOM Fix & Version Management

- Fixed OOM killer during startup by using explicit heap limits instead of percentage-based:
  - `-Xms48m -Xmx80m` instead of `-XX:MaxRAMPercentage=50.0`
  - Reduced metaspace from 64MB to 48MB
  - Reduced thread stack from 512KB to 256KB
- Fixed CSS not loading when accessed through Home Assistant ingress (reverse proxy)
  - Changed root-relative paths (`/styles.css`) to relative paths (`styles.css`)
  - Added explicit MIME type configuration for CSS/JS/HTML files
- Added Maven resource filtering for automatic version synchronization
  - pom.xml is now the single source of truth for version number
  - config.yaml is generated from template during build

### [0.5.4-fork] - Memory Leak Fixes

- Fixed OkHttpClient leak - now using static singleton instead of creating per request
- Added shutdown hooks for executor services to ensure clean termination
- Fixed MQTT client leak on configuration reload - properly disconnect and close old client
- Reuse HomeAssistantClient instance instead of creating new one per scheduled update
- Fixed GridConnection leak - properly close on connection failure
- Cleared MQTT subscribers map on client disposal
- Added unit tests for memory leak fixes (Mockito integration)

### [0.5.3-fork] - Container Memory Optimization

- Switched from openjdk21 to openjdk21-jre-headless (smaller image, less memory)
- Added container-aware JVM settings:
  - `-XX:+UseContainerSupport` for proper cgroup memory detection
  - `-XX:MaxRAMPercentage=25.0` to use 25% of container memory
  - `-Xss256k` reduced thread stack size
  - `-XX:-UsePerfData` to disable unneeded performance counters
- Reduced metaspace from 64MB to 48MB
- Added ExitOnOutOfMemoryError for clean shutdown on OOM

### [0.5.2-fork] - Memory & Startup Fixes

- Fixed Javalin "created but never started" warning by reordering startup sequence
- Reduced JVM memory footprint for constrained HA environments (Raspberry Pi)
  - Heap: 256MB → 128MB max, 64MB → 32MB initial
  - Added 64MB metaspace limit
  - Switched to SerialGC for lower overhead
  - Tiered compilation level 1 for faster startup
- Reduced thread pool sizes (16+8 → 4+2) to match actual workload

### [0.5.1-fork] - Ingress Support

- Enabled Home Assistant ingress for reverse proxy support
- Removed unused webui_host, webui_use_port, webui_port configuration options
- Simplified configuration by relying on HA ingress routing

### [0.5.0-fork] - Initial Fork Release

- Fixed NullPointerException when IconMaster data is not yet available during startup
- Added null-safety checks for IconMaster state reporting to both Home Assistant and MQTT
- Redesigned web UI with modern Danfoss branding and improved UX
- Added styled success/error pages for pairing process
- Added copy-to-clipboard functionality for configuration JSON
- Improved form validation and loading states in web interface
- Added GitHub Actions workflow for automated build validation
- Updated README with fork installation instructions

---

## Original Releases

### [0.4.5]

- Added new config option `m4FixEnabled` which could be enabled to fix Mac M4 startup bug.

### [0.4.4]

- Reverted M4 Mac bugfix because it breaks some other CPU architectures

### [0.4.3]

- Attempted fix for M4 Mac startup issue

### [0.3.9]

- Fixed MQTT subscriptions and app rediscovery

### [0.3.8]

- Fixed reporting and switching between heating/cooling modes

### [0.3.7]

- Added debug logging for heating/cooling states

### [0.3.5]

- Added support for cooling mode

### [0.3.4]

- Use set point home for target temp high, set point away for target temp low

### [0.3.3]

- Changed HA update interval to seconds instead of minutes

### [0.3.2]

- Added configurable log level

### [0.3.1]

- Minor improvements

### [0.3.0]

- Added MQTT integration

### [0.2.5]

- Made port configurable

### [0.2.4]

- Create config directory before writing during pairing

### [0.2.3]

- Added `sensor.danfoss_master_controller_last_updated` state

### [0.2.2]

- Exposed master controller state (`sensor.danfoss_master_controller`)

### [0.2.1]

- Improved logging

### [0.2.0]

- Exposed additional sensor data

### [0.1.8]

- Updated documentation

### [0.1.7]

- Added commands REST endpoint
- Added documentation for setting temperature from HA

### [0.1.3]

- Added configuration translations

### [0.1.2]

- Added configuration options

### [0.1.1]

- Added unique_id attribute to entities

### [0.1.0]

- Initial release
- Thermostats exposed to Home Assistant
