package com.vpovyshev.streaming.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;



public class testApp {
    private static final Logger logger = LoggerFactory.getLogger(testApp.class);

    public static void main(String[] args){

        //Assign topicName to string variable
        String topicName = "quickstart";

        // create instance for properties to access producer configs
        Properties props = new Properties();

        //Assign localhost id
        props.put("bootstrap.servers", "broker:29092");

        //Set acknowledgements for producer requests.
        props.put("acks", "all");

        //If the request fails, the producer can automatically retry,
        props.put("retries", 0);

        //Specify buffer size in config
        props.put("batch.size", 16384);

        //Reduce the no of requests less than 0
        props.put("linger.ms", 1);

        //The buffer.memory controls the total amount of memory available to the producer for buffering.
        props.put("buffer.memory", 33554432);

        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        int threadAmount = 5;
        Producer<String, String> producer = new KafkaProducer<>(props);
        Thread[] dispatchers = new Thread[threadAmount];
        for (int i = 0; i < threadAmount; i++) {
            dispatchers[i] = new Thread(new Dispatcher(producer, topicName, "Thread"+i));
            dispatchers[i].start();
        }

        try {
            for (Thread t : dispatchers)
                t.join();
        } catch (InterruptedException e) {
            logger.error("Thread Interrupted ");
        } finally {
            producer.close();
        }

    }
}