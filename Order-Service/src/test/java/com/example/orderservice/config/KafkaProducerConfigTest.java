package com.example.orderservice.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KafkaProducerConfigTest {

    private final KafkaProducerConfig config = new KafkaProducerConfig();

    @Test
    void testProducerFactoryConfig() {
        ProducerFactory<String, Object> producerFactory = config.producerFactory();
        assertNotNull(producerFactory);

        Map<String, Object> configMap = producerFactory.getConfigurationProperties();
        assertEquals("localhost:9092", configMap.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(StringSerializer.class, configMap.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(JsonSerializer.class, configMap.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        assertEquals(true, configMap.get(JsonSerializer.ADD_TYPE_INFO_HEADERS));
    }

    @Test
    void testKafkaTemplateBeanCreated() {
        KafkaTemplate<String, Object> kafkaTemplate = config.kafkaTemplate();
        assertNotNull(kafkaTemplate);
        assertNotNull(kafkaTemplate.getProducerFactory());
    }

}
