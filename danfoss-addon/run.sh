#!/usr/bin/with-contenv bashio

echo "Starting app..."

M4_FIX_ENABLED="$(bashio::config 'm4FixEnabled')"

if [[ "$M4_FIX_ENABLED" == "true" ]]; then
  export JAVA_TOOL_OPTIONS="-XX:UseSVE=0"
fi

java --enable-preview \
  -XX:+UseContainerSupport \
  -Xms48m \
  -Xmx80m \
  -XX:MaxMetaspaceSize=48m \
  -Xss256k \
  -XX:+UseSerialGC \
  -XX:+TieredCompilation -XX:TieredStopAtLevel=1 \
  -XX:-UsePerfData \
  -XX:+ExitOnOutOfMemoryError \
  -jar app.jar