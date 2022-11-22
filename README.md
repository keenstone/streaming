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
  
Можно было бы сразу с докер-композ засунуть билд стейдж и сборку докерфайла и, например, параметрами сборки\запуска рулить какое поведение мы хотим в конкретном случае, но это оставим на позже.    
Есть момент с настройкой кафки в докер образе. Это откуда клиенту  доступен брокер кафка. Для понимания читаем https://rmoff.net/2018/08/02/kafka-listeners-explained/ и https://www.baeldung.com/kafka-docker-connection
Мы сейчас настроили, что клиент может или с локалхота  или из внутренней сетки. 

### ~~шаг 3~~
сделать продьюсера многопоточным, чтобы в дальнейшем можно было проверять более интересные кейсы. <br>
из-за того, что продьюсер потоко безопасен его можно шарить между потоками. так и делаем.

### шаг 4
прикрутить schema registry