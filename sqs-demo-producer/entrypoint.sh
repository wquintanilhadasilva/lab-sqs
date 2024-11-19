#!/bin/bash
echo "Iniciando o serviço..."

echo "Executando o comando: java ${JAVA_OPTS} --enable-preview -Djava.security.egd=file:/dev/./urandom -jar ${APP_NAME}.jar"
exec java ${JAVA_OPTS} --enable-preview -Djava.security.egd=file:/dev/./urandom -jar ${APP_NAME}.jar "$@"
