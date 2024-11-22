# Teste de ambiente AWS SQS

Simular envio e recebimento de mensagens no SQS com rotação de mensagens controlado o lock pelo Redis

[Referência sobre SQS & SpringBoot](https://docs.awspring.io/spring-cloud-aws/docs/3.1.0/reference/html/index.html#sqs-integration)

# Ambiente AWS LOCALSTACK

Simular os serviços da aws localmente, use a localstck:

[Material de Referência](https://www.youtube.com/watch?v=yOdp0wz5NeI)

[Documentação](https://docs.localstack.cloud/getting-started/installation/)

## Configuração

Copie para o diretório do usário, na pasta `~/.localstack`.

Coloque essa pasta na variável de ambiente *PATH*

Verifique se está ok digitando no console: `localstack --version`

## Execução
Execute o docker daemon e depois:

```
localstack start
```

> Porta padrão: `http://localhost:4566/`
 
### Usando o aws cli

Para utilizar o aws cli conectando à localstack e não à aws use:

```
aws --endpoint="http://localhost:4566" s3 ls
```
> Lista todos os buckets criados na localstack
 
#### Exemplo para criar fila no SQS

```
aws --endpoint="http://localhost:4566" sqs create-queue --queue-name minha-fila
```


# Ambiente Desenvolvimento

Spring-cloud AWS

```
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.awspring.cloud</groupId>
            <artifactId>spring-cloud-aws-dependencies</artifactId>
            <version>{project-version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>


```

[Referência](https://docs.awspring.io/spring-cloud-aws/docs/3.1.0/reference/html/index.html#starter-dependencies)

# Enviar dados para teste

## Producer

Envia um texto plano na Api do Produtor
```
curl --request POST \
  --url http://localhost:8080/producer/send \
  --header 'Content-Type: text/plain' \
  --data 'teste de envio'
```

## Receiver

Envia um json na API do Receiver, que não é o consumer mas o serviço externo chamado pelo consumer após receber a mensagem:

```
curl --request POST \
  --url http://localhost:8082/receiver/process \
  --header 'Content-Type: application/json' \
  --data '{   "id": "12345",   "firstQueueDateTime": "2024-11-19T10:30:00Z",   "lastQueueDateTime": "2024-11-19T12:45:00Z",   "textMessage": "Hello, this is a test message.",   "equeueCount": 3 }'
```

Ajustar a configuração do receiver informando que é para rejeitar com erro as requisições de process

```
curl --request POST \
  --url http://localhost:8082/receiver/reject-enabled
```

Ajustar a configuração do receiver informando que é para permitir e aceitar as requisições de process

```
curl --request POST \
  --url http://localhost:8082/receiver/reject-disabled
```

# Importantes:

[Entrega duplicada](https://repost.aws/knowledge-center/lambda-function-process-sqs-messages)

