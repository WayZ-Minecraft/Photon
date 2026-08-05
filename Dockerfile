FROM eclipse-temurin:21-jre-alpine

# Install timezone data, SQLite, and C-compatibility libraries needed by JNA
RUN apk add --no-cache tzdata sqlite-dev libc6-compat gcompat
ENV TZ=Europe/Brussels

# Creating a new user without any privileges
RUN addgroup -S photon_group && adduser -S photon_user -G photon_group

WORKDIR /photon_server

# Attributing permissions to "photon_group"
RUN chown -R photon_user:photon_group /photon_server

# Create explicit JNA temporary directory to prevent execution crashes
RUN mkdir -p /photon_server/tmp && chown -R photon_user:photon_group /photon_server

COPY build/libs/*.jar /photon_server/photon.jar
RUN chown photon_user:photon_group /photon_server/photon.jar

# Set the user to the newly created "photon_user"
USER photon_user

# Set JVM flag to force JNA native extraction to /photon_server/tmp
ENV JAVA_TOOL_OPTIONS="-Djna.tmpdir=/photon_server/tmp"

# Javalin port, by default it's : 7070
EXPOSE 7070

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/photon_server/photon.jar"]