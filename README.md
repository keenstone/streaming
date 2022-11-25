## Streaming
* для начала был использован гайд [Confluent docker-compose guide](https://developer.confluent.io/quickstart/kafka-docker/)
* для kafka producer api был использован [JavaDoc](https://kafka.apache.org/33/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html)
- для билда  [docker multi-stage build](https://docs.docker.com/build/building/multi-stage/)

### ~~шаг 1~~
собрать и запустить докер контейнер с кафкой и минимальным работающим кодом

### ~~шаг 2~~
в докер контейнере запустить код продсюсера для кафки:
1. стартуем кафку:
   ```
   docker-compose up -d
   ```
2. создаем топик (он автоматически не создается):
   ```
   docker exec broker kafka-topics --bootstrap-server broker:29092 --create --topic quickstart
   ```
3. запускаем консьюмера:
   ```
   docker exec --interactive --tty broker kafka-console-consumer --bootstrap-server broker:29092 --topic quickstart --from-beginning
   ```
5. собираем приложение:
   ```
   docker image build -t simpleapp:1.0 .
   ```
5. запускаем собранный образ в одной сетке с кафкой:
   ```
   docker container run -it --net streaming_kafka simpleapp:1.0
   ```
6. проверяем, что в консоли консьюмера появились сообщения вида
  >value93
  value94
  value95
  value96
  value97
  value98
  value99
  
Можно было бы сразу с докер-композ засунуть билд стейдж и сборку докерфайла и, например, параметрами сборки\запуска рулить какое поведение мы хотим в конкретном случае, но это оставим на позже.    
Есть момент с настройкой кафки в докер образе. Это откуда клиенту  доступен брокер кафка. Для понимания читаем https://rmoff.net/2018/08/02/kafka-listeners-explained/ и https://www.baeldung.com/kafka-docker-connection
Мы сейчас настроили, что клиент может обращаться или с локалхота или из внутренней сетки. 

### ~~шаг 3~~
сделать продьюсера многопоточным, чтобы в дальнейшем можно было проверять более интересные кейсы.<br>
из-за того, что продьюсер потоко безопасен его можно шарить между потоками. так и делаем.

### ~~шаг 4~~
~~прикрутить schema registry.~~ пришлось отказаться от фичи валидации схемы ибо она доступна в докер образе cp-server. это то, что раньше называлось enterprise kafka.<br>
я же хочу работать с open source т.е. cp-kafka) да и потребности в этой фиче для нужд лабы нет.<br><br>
за основу взять  https://github.com/confluentinc/cp-demo/blob/7.3.0-post/docker-compose.yml <br>
старые гайды с конфигами не всегда актуальны.</br>
Если выдруг забыли, что такое confluent schema registry: 
> Confluent Schema Registry provides a serving layer for your metadata. It provides a RESTful interface for storing and retrieving your Avro®, JSON Schema, and Protobuf schemas. It stores a versioned history of all schemas based on a specified subject name strategy, provides multiple compatibility settings and allows evolution of schemas according to the configured compatibility settings and expanded support for these schema types. It provides serializers that plug into Apache Kafka® clients that handle schema storage and retrieval for Kafka messages that are sent in any of the supported formats.</br>
Schema Registry lives outside of and separately from your Kafka brokers. Your producers and consumers still talk to Kafka to publish and read data (messages) to topics. Concurrently, they can also talk to Schema Registry to send and retrieve schemas that describe the data models for the messages. <br>

далее про то, как работает версионирование схем: 
>A Kafka topic contains messages, and each message is a key-value pair. Either the message key or the message value, or both, can be serialized as Avro, JSON, or Protobuf. A schema defines the structure of the data format. The Kafka topic name can be independent of the schema name. Schema Registry defines a scope in which schemas can evolve, and that scope is the subject. The name of the subject depends on the configured subject name strategy, which by default is set to derive subject name from topic name.

про subject name strategy читаем [тут](https://docs.confluent.io/platform/current/schema-registry/serdes-develop/index.html#subject-name-strategy)
<br>Вот лучшее пояснение, что я нашел:
>As a practical example, let’s say a retail business is streaming transactions in a Kafka topic called transactions. A producer is writing data with a schema Payment to that Kafka topic transactions. If the producer is serializing the message value as Avro, then Schema Registry has a subject called transactions-value. If the producer is also serializing the message key as Avro, Schema Registry would have a subject called transactions-key, but for simplicity, in this tutorial consider only the message value. That Schema Registry subject transactions-value has at least one schema called Payment. The subject transactions-value defines the scope in which schemas for that subject can evolve and Schema Registry does compatibility checking within this scope. In this scenario, if developers evolve the schema Payment and produce new messages to the topic transactions, Schema Registry checks that those newly evolved schemas are compatible with older schemas in the subject transactions-value and adds those new schemas to the subject.

все наши subject будут в topic по-умолчанию _schemas

Создать новый топик с валидацией и дефолтной subject name strategy=TopicNameStrategy можно командой вида:
```
docker exec broker kafka-topics --create --bootstrap-server broker:29092 --replication-factor 1 --partitions 1 --topic my-new-topic --config confluent.value.schema.validation=true
```
Создать новый топик с валидацией и subject name strategy=RecordNameStrategy можно командой вида:
```
docker exec broker kafka-topics --create --bootstrap-server broker:29092 --replication-factor 1 --partitions 1 --topic my-very-new-topic --config confluent.value.schema.validation=true --config confluent.value.subject.name.strategy=io.confluent.kafka.serializers.subject.RecordNameStrategy
```
или добавить валидацию для существующего c дефолтной subject name strategy=TopicNameStrategy: 
```
docker exec broker kafka-configs --bootstrap-server broker:29092 --alter --entity-type topics --entity-name my-existing-topic --add-config confluent.value.schema.validation=true
```
или добавить валидацию для существующего с subject name strategy=RecordNameStrategy:
```
docker exec broker kafka-configs --bootstrap-server broker:29092 --alter --entity-type topics --entity-name my-another-existing-topic --add-config confluent.value.schema.validation=true
docker exec broker kafka-configs --bootstrap-server broker:29092 --alter --entity-type topics --entity-name my-another-existing-topic --add-config confluent.key.subject.name.strategy=io.confluent.kafka.serializers.subject.RecordNameStrategy
```
получить список топиков
```
docker exec broker kafka-topics --list --bootstrap-server broker:29092
```
получить описание топика. в последнем столбце будут переопределенные свойства топика. если что там можно найти то, что мы на переопределяли
```
docker exec broker kafka-topics --describe --topic quickstart --bootstrap-server broker:29092
```

ну а далее из-за того, что у нас выключено автосоздание схем (и это хорошо) нам нужно создать описание схемы. зарегистрировать его. Подключаем нужные сериалайзеры\десириалайзеры и живем.<br>
в теории если вы правите схему (про настройки совместимости схем можно почитать [тут](https://docs.confluent.io/platform/current/schema-registry/avro.html#using-compatibility-types)), то должны при обновлении удовлетворять политикам которые настроены для неё. и  при сериализации\десиарилизации видимо тоже будет проверка.<br>

