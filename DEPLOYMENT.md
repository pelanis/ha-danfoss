# Quick Deployment Guide

## What Was Fixed

### Issue 1: NullPointerException Crash ✅
The addon was crashing on startup due to null values in IconMaster data before the Danfoss device responded.

**Solution:** Added null-safety checks and default values throughout the code.

### Issue 2: Ugly UI ✅
The web interface had no styling and looked unprofessional.

**Solution:** Created modern, Danfoss-branded UI with:
- Clean card-based design
- Danfoss red (#DC0019) color scheme
- Responsive layout
- Loading states
- Proper error messages
- Copy-to-clipboard for configuration

### Issue 3: Reverse Proxy Support ✅
The addon's WebUI link didn't work with reverse proxy setups.

**Solution:** Added configuration options in addon settings:
- `webui_host`: Custom host (leave empty for auto-detect)
- `webui_use_port`: Whether to include port in URL
- `webui_port`: Custom port number

### Issue 4: Build Instructions ✅
No documentation for building and deploying forked versions.

**Solution:** Created comprehensive BUILD.md with multiple deployment options.

## Quick Start: Using Your Fork

### Option 1: Quick Test (Recommended First)

1. **Build locally:**
   ```bash
   cd danfoss-addon
   mvn clean package
   ```

2. **Test the JAR:**
   ```bash
   java -jar target/ha-danfoss-addon-0.0.1.jar
   ```

3. **Access:** http://localhost:9199

### Option 2: Deploy via GitHub (Recommended for Production)

1. **Update repository.json with your details:**
   ```bash
   # Edit repository.json and replace:
   # - YOUR_USERNAME with your GitHub username
   # - Your Name with your name
   # - your.email@example.com with your email
   ```

2. **Set up Docker Hub:**
   - Create account at https://hub.docker.com
   - Create access token: Settings → Security → New Access Token
   - Save the token

3. **Add GitHub secrets:**
   - Go to your GitHub repo → Settings → Secrets → Actions
   - Add `DOCKER_USERNAME` (your Docker Hub username)
   - Add `DOCKER_TOKEN` (your Docker Hub access token)

4. **Build and push:**
   ```bash
   cd danfoss-addon
   mvn clean package

   docker buildx create --use
   docker buildx build \
     --platform linux/amd64,linux/arm64,linux/arm/v7 \
     -t YOUR_DOCKER_USERNAME/ha-danfoss:0.4.6 \
     -t YOUR_DOCKER_USERNAME/ha-danfoss:latest \
     --push \
     .
   ```

5. **Update config.yaml:**
   Add this line after `slug`:
   ```yaml
   image: "YOUR_DOCKER_USERNAME/ha-danfoss"
   ```

6. **Commit and tag:**
   ```bash
   git add .
   git commit -m "Release version 0.4.6 with improvements"
   git tag v0.4.6
   git push origin main --tags
   ```

7. **Add to Home Assistant:**
   - Settings → Add-ons → Add-on Store
   - Click ⋮ → Repositories
   - Add: `https://github.com/YOUR_USERNAME/ha-danfoss`
   - Install your addon

### Option 3: Automated (Use GitHub Actions)

GitHub Actions will automatically build when you create a release:

1. **Set up secrets** (as in Option 2)

2. **Create a release:**
   ```bash
   git tag v0.4.6
   git push origin v0.4.6
   ```

3. **Or use GitHub UI:**
   - Go to Releases → Create new release
   - Tag: `v0.4.6`
   - Publish

GitHub Actions builds and publishes automatically!

## Configuration for Reverse Proxy

If using a reverse proxy (nginx, Traefik, etc.):

### Method 1: Use Ingress (Recommended)

Edit `danfoss-addon/config.yaml`:
```yaml
ingress: true
ingress_port: 9199
panel_icon: mdi:thermostat
```

Remove or comment out the webui line:
```yaml
# webui: "http://[HOST]:[PORT:9199]"
```

### Method 2: Custom URL

In Home Assistant addon configuration:
```yaml
webui_host: "your-domain.com"
webui_use_port: false
webui_port: ""
```

Or edit `config.yaml`:
```yaml
webui: "https://your-domain.com/danfoss"
```

## Testing Your Changes

1. **Start the addon** in Home Assistant
2. **Check logs** for any errors
3. **Access WebUI** - should show modern Danfoss interface
4. **Pair with Danfoss Icon** - verify pairing works
5. **Verify no crashes** - wait 60 seconds, check logs

## Troubleshooting

### Addon won't start
Check logs: `docker logs addon_xxxx_danfoss`

Common issues:
- Port 9199 already in use → Change in addon config
- Config file missing → Pair via WebUI first
- NPE errors → Fixed in this version, but wait 5-10 seconds on first start

### WebUI not accessible
- Check firewall settings
- Verify port mapping in addon config
- Try ingress mode (see above)
- Check reverse proxy configuration

### Version not updating
1. Increment version in `config.yaml`
2. Clear browser cache
3. Restart Home Assistant
4. Check for updates in Add-on Store

## Next Steps

1. Test locally first
2. Choose deployment method (GitHub recommended)
3. Set up Docker Hub + GitHub secrets
4. Build and deploy
5. Add repository to Home Assistant
6. Install and test

For detailed instructions, see [BUILD.md](BUILD.md)

## Support

- Original addon issues: https://github.com/soundvibe/ha-danfoss
- Fork-specific issues: Create issue in your fork
