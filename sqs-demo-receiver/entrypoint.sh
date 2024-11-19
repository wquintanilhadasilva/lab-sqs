#!/bin/bash
echo "Iniciando o serviço..."

## Permite a alteração do arquivo ws-config.xml por varivel de ambiente;
## A Variável de ambiente WS_CONFIG_REPLACEMENT deve ser preenchida com uma lista de substituição
## Exemplo: WS_CONFIG_REPLACEMENT="http://taxauthority::http://mock,http://qrcode-taxauthority::http://qrcode-mock"
## Neste formado as URL's sem mock serão substituidas pelas com mock.
#
#if [ -n "$WS_CONFIG_REPLACEMENT" ]; then
#  printf "Sobreescrevendo ws-config.xml\n"
#  # ADDR = http://taxauthority::http://mock,http://qrcode-taxauthority::http://qrcode-mock
#  IFS=',' read -ra ADDR <<< "$WS_CONFIG_REPLACEMENT"
#  # i = http://taxauthority::http://mock
#  for i in "${ADDR[@]}"; do
#    # OLD = http://taxauthority
#    sed -i "s#$OLD#$NEW#g" ./config/ws-config.xml
#  done
#fi

echo "Executando o comando: java ${JAVA_OPTS} --enable-preview -Djava.security.egd=file:/dev/./urandom -jar ${APP_NAME}.jar"
exec java ${JAVA_OPTS} --enable-preview -Djava.security.egd=file:/dev/./urandom -jar ${APP_NAME}.jar "$@"
