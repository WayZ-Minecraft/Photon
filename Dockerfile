FROM eclipse-temurin:21-jre

ENV TZ=Europe/Brussels

# Create non-root system group and user using Debian/Ubuntu syntax
RUN groupadd --system photon_group && useradd --system --gid photon_group --no-create-home photon_user

WORKDIR /photon_server

# Create temp directory and set permissions for the workdir before copying files
RUN mkdir -p /photon_server/tmp && chown -R photon_user:photon_group /photon_server

# Copy jar as the target user directly (eliminates an extra RUN layer for chown)
COPY --chown=photon_user:photon_group build/libs/*.jar /photon_server/photon.jar

# Set the user
USER photon_user

# Set JVM flag to force JNA native extraction to /photon_server/tmp
ENV JAVA_TOOL_OPTIONS="-Djna.tmpdir=/photon_server/tmp"

# Javalin port
EXPOSE 7070

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/photon_server/photon.jar"]