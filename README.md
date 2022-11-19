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
   docker exec broker kafka-topics --bootstrap-server broker:9092 --create --topic quickstart
   ```
3. запускаем консьюмера:
   ```
   docker exec --interactive --tty broker kafka-console-consumer --bootstrap-server broker:9092 --topic quickstart --from-beginning
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
  
  Есть момент с настройкой кафки в докер образе. Это клиенту откуда доступен брокер кафка. Для понимания читаем https://rmoff.net/2018/08/02/kafka-listeners-explained/ и https://www.baeldung.com/kafka-docker-connection
  
### шаг 3
сделать продьюсера много поточным, чтобы в дальнейшем можно было проверять более интересные кейсы