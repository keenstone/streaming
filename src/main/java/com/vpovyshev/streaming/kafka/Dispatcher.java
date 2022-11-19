package com.vpovyshev.streaming.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dispatcher implements Runnable {

    private final Producer<String, String> producer;
    private final String topicName;

    private final String name;
    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    Dispatcher(Producer<String, String> producer, String topicName, String name) {
        this.producer = producer;
        this.topicName = topicName;
        this.name = name;
    }

    @Override
    public void run() {
        logger.info("Start processing " + name + "...");

        for(int i = 0; i < 100; i++) {
            producer.send(new ProducerRecord<>(topicName, Integer.toString(i),
                    name+":value:"+i));
        }

    }
}
