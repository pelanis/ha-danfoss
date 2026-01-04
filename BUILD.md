# Building and Deploying Danfoss Icon Addon

This guide explains how to build and deploy your own version of the Danfoss Icon Home Assistant addon.

## Prerequisites

- Java 21 (Temurin recommended)
- Maven 3.8 or higher
- Docker
- Docker Buildx (for multi-architecture builds)
- Git
- Home Assistant instance

## Quick Start

### 1. Build the Java Application

```bash
cd danfoss-addon
mvn clean package
```

This creates `ha-danfoss-addon-0.0.1.jar` in the `target/` directory.

### 2. Test Locally

Run the application locally for testing:

```bash
java -jar target/ha-danfoss-addon-0.0.1.jar
```

Access the web interface at `http://localhost:9199`

## Docker Build

### Single Architecture Build (for testing)

```bash
cd danfoss-addon
docker build -t ha-danfoss-local:latest .
```

### Multi-Architecture Build (for production)

Home Assistant supports multiple architectures. Build for all supported platforms:

```bash
docker buildx create --use --name danfoss-builder
docker buildx build \
  --platform linux/amd64,linux/arm64,linux/arm/v7 \
  -t yourusername/ha-danfoss:latest \
  --push \
  .
```

**Note:** Replace `yourusername` with your Docker Hub username.

## Deployment Options

### Option 1: Local Addon (Development)

Install the addon directly from your local filesystem:

1. Copy the entire `danfoss-addon` folder to your Home Assistant addons directory:
   ```bash
   scp -r danfoss-addon root@homeassistant.local:/addons/danfoss-local/
   ```

2. In Home Assistant:
   - Settings → Add-ons → Add-on Store
   - Click ⋮ (menu) → Check for updates
   - The local addon should appear in the store

### Option 2: GitHub Repository

Use your forked repository as an addon source:

1. **Update Version Number**

   Edit `danfoss-addon/config.yaml`:
   ```yaml
   version: "0.4.6"  # Increment version
   ```

2. **Update Repository Info**

   Edit `repository.json`:
   ```json
   {
     "name": "Danfoss Icon Controller Add-on (Fork)",
     "url": "https://github.com/YOUR_USERNAME/ha-danfoss",
     "maintainer": "Your Name <your.email@example.com>"
   }
   ```

3. **Build and Push Docker Image**

   ```bash
   cd danfoss-addon
   mvn clean package

   docker buildx build \
     --platform linux/amd64,linux/arm64,linux/arm/v7 \
     -t yourusername/ha-danfoss:0.4.6 \
     -t yourusername/ha-danfoss:latest \
     --push \
     .
   ```

4. **Update Image Reference**

   Edit `danfoss-addon/config.yaml`:
   ```yaml
   image: "yourusername/ha-danfoss"
   ```

5. **Commit and Push**

   ```bash
   git add .
   git commit -m "Release version 0.4.6"
   git tag v0.4.6
   git push origin main --tags
   ```

6. **Add Repository to Home Assistant**

   - Settings → Add-ons → Add-on Store
   - Click ⋮ (menu) → Repositories
   - Add: `https://github.com/YOUR_USERNAME/ha-danfoss`
   - Install the addon from your repository

### Option 3: Automated GitHub Actions

Use GitHub Actions to automatically build and publish on every release.

1. **Create Docker Hub Access Token**

   - Go to https://hub.docker.com/settings/security
   - Create new access token
   - Save the token securely

2. **Add GitHub Secrets**

   In your GitHub repository:
   - Settings → Secrets and variables → Actions
   - Add secrets:
     - `DOCKER_USERNAME`: Your Docker Hub username
     - `DOCKER_TOKEN`: Your Docker Hub access token

3. **Create Workflow File**

   The workflow file `.github/workflows/build-and-publish.yml` is already created.

4. **Trigger Build**

   Create a new release:
   ```bash
   git tag v0.4.6
   git push origin v0.4.6
   ```

   Or create a release through GitHub UI:
   - Releases → Create a new release
   - Tag: `v0.4.6`
   - Publish release

   GitHub Actions will automatically:
   - Build the Java application
   - Build Docker images for all architectures
   - Push to Docker Hub

## Configuration

### Reverse Proxy Setup

If you're using a reverse proxy (nginx, Traefik, etc.) and the addon's web interface isn't accessible:

1. **Option A: Use Ingress**

   Edit `danfoss-addon/config.yaml`:
   ```yaml
   ingress: true
   ingress_port: 9199
   panel_icon: mdi:thermostat
   ```

   Remove or comment out:
   ```yaml
   # webui: "http://[HOST]:[PORT:9199]"
   ```

2. **Option B: Custom WebUI URL**

   Edit `danfoss-addon/config.yaml`:
   ```yaml
   webui: "http://your-custom-domain.com/danfoss"
   ```

3. **Configure Options**

   In Home Assistant addon configuration, you can customize:
   - `webui_host`: Custom host for webui (empty = auto-detect)
   - `webui_use_port`: Whether to include port in URL (default: true)
   - `webui_port`: Custom port (empty = use main port)

## Troubleshooting

### Build Fails

**Maven build error:**
```
[ERROR] Failed to execute goal
```

Solution:
- Verify Java 21 is installed: `java -version`
- Clean build: `mvn clean install`

**Docker build error:**
```
failed to solve with frontend dockerfile.v0
```

Solution:
- Ensure JAR is copied to danfoss-addon directory:
  ```bash
  cp target/ha-danfoss-addon-0.0.1.jar danfoss-addon/
  ```

### Addon Won't Start

**Check logs:**
```bash
docker logs addon_local_danfoss
```

**Common issues:**

1. **NullPointerException on startup**
   - Fixed in recent versions with null-safety checks
   - Wait 5-10 seconds for Danfoss device to respond

2. **Configuration file missing**
   - Access `http://[HA_IP]:9199` to pair with Danfoss Icon
   - Configuration saves to `/share/danfoss-icon/danfoss_config.json`

3. **Port already in use**
   - Change port in addon configuration
   - Default: 9199

### Version Conflicts

If Home Assistant doesn't show your updated version:

1. Increment version in `config.yaml`
2. Clear browser cache
3. Restart Home Assistant
4. Check for updates in Add-on Store

## Updating the Addon

### Manual Update

```bash
cd danfoss-addon
mvn clean package

docker buildx build \
  --platform linux/amd64,linux/arm64,linux/arm/v7 \
  -t yourusername/ha-danfoss:0.4.7 \
  -t yourusername/ha-danfoss:latest \
  --push \
  .

# Update version in config.yaml
# Commit and push
git add .
git commit -m "Release version 0.4.7"
git tag v0.4.7
git push origin main --tags
```

### Automated Update (with GitHub Actions)

```bash
# Update version in danfoss-addon/config.yaml
git add danfoss-addon/config.yaml
git commit -m "Bump version to 0.4.7"
git tag v0.4.7
git push origin main --tags
```

GitHub Actions handles the rest automatically.

## Development Tips

### Local Development Without Docker

```bash
cd danfoss-addon
mvn clean package
java -Dfile.encoding=UTF-8 -jar target/ha-danfoss-addon-0.0.1.jar
```

Set environment variable for Home Assistant API:
```bash
export SUPERVISOR_TOKEN=your_token
```

### Test Configuration Changes

Before deploying to production:

1. Build locally: `mvn clean package`
2. Test locally: `java -jar target/ha-danfoss-addon-0.0.1.jar`
3. Verify web interface: `http://localhost:9199`
4. Test pairing with Danfoss Icon
5. Build Docker image and test in Home Assistant development instance

## Additional Resources

- [Home Assistant Add-on Development](https://developers.home-assistant.io/docs/add-ons)
- [Docker Buildx Documentation](https://docs.docker.com/buildx/working-with-buildx/)
- [Maven Documentation](https://maven.apache.org/guides/)

## Support

For issues with the forked version, please open an issue on your GitHub repository.
For issues with the original addon, see: https://github.com/soundvibe/ha-danfoss
