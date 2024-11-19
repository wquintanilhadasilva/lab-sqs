# Teste de ambiente AWS SQS

Simular envio e recebimento de mensagens no SQS com rotação de mensagens controlado o lock pelo Redis

# Ambiente AWS LOCALSTACK

Simular os serviços da aws localmente, use a localstck:

[Material de Referência](https://www.youtube.com/watch?v=yOdp0wz5NeI)

[Documentação](https://docs.localstack.cloud/getting-started/installation/)

## Configuração

Copie para o diretório do usário, na pasta `~/./localstack`.

Verifique se está ok: `localstack --version`

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

