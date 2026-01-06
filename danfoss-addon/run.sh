#!/usr/bin/with-contenv bashio

echo "Starting app..."

M4_FIX_ENABLED="$(bashio::config 'm4FixEnabled')"

if [[ "$M4_FIX_ENABLED" == "true" ]]; then
  export JAVA_TOOL_OPTIONS="-XX:UseSVE=0"
fi

java --enable-preview \
  -Xms32m -Xmx128m \
  -XX:MaxMetaspaceSize=64m \
  -XX:+UseSerialGC \
  -XX:+TieredCompilation -XX:TieredStopAtLevel=1 \
  -jar app.jar