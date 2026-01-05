ARG BUILD_FROM
FROM $BUILD_FROM
#FROM ghcr.io/home-assistant/aarch64-base:latest

RUN mkdir -p /share/danfoss-icon/

RUN apk add --no-cache openjdk21

WORKDIR /

COPY run.sh /
RUN chmod a+x /run.sh

# Copy the JAR file to the container
COPY ha-danfoss-addon.jar /app.jar

CMD [ "/run.sh" ]