package com.example.mongofilesmicroservice.configuration;

import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

//    @Bean
//    public ProducerFactory<String, FileKafka> producerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.2.152:9092");
//        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, FileKafkaSerializer.class); // Use custom serializer
//
//        return new DefaultKafkaProducerFactory<>(configProps);
//    }

    @Bean
    public NewTopic createFile() {
        return TopicBuilder.name("createfile").build();
    }

    @Bean
    public NewTopic deleteFile() {
        return TopicBuilder.name("deletefile").build();
    }

    @Bean
    public NewTopic createDir() {
        return TopicBuilder.name("createdir").build();
    }

}
