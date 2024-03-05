FROM --platform=linux/arm/v7 eclipse-temurin:17-jammy
EXPOSE 8080
WORKDIR /app

COPY target/raspberrypi-device-hub-1.jar .
ENTRYPOINT ["java","-jar","./raspberrypi-device-hub-1.jar"]