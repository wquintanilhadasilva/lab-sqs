FROM openjdk:17.0-jdk-slim-buster as application
ENV TZ="America/Sao_Paulo"
ENV APP_NAME="sqs-sqs-consumer"
ENTRYPOINT ["/entrypoint.sh"]
WORKDIR /app

# Run as non-root
RUN useradd -ms /bin/bash lab &&\
    mkdir -p /var/lib /app/logs /app/config &&\
    chown lab:root /var/lib /app/logs /app/config &&\
    chmod 775 /var/lib
USER lab

COPY --chown=oobj --chmod=0755 ./etc/docker/entrypoint.sh /entrypoint.sh
COPY --chown=oobj --chmod=0777 ./target/${APP_NAME}.jar ./${APP_NAME}.jar
