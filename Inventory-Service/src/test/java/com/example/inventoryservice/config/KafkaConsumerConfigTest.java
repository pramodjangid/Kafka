package com.example.inventoryservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConsumerConfigTest {

    private final KafkaConsumerConfig config = new KafkaConsumerConfig();

    @Test
    void objectMapperBean_shouldNotBeNull() {
        ObjectMapper objectMapper = config.objectMapper();
        assertNotNull(objectMapper);
    }

    @Test
    void consumerFactory_shouldContainCorrectProperties() {
        ConsumerFactory<String, String> factory = config.consumerFactory();
        assertNotNull(factory);
        assertTrue(factory instanceof DefaultKafkaConsumerFactory);

        Map<String, Object> props = factory.getConfigurationProperties();
        assertEquals("localhost:9092", props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("inventory-group", props.get(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals(StringDeserializer.class, props.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
        assertEquals(StringDeserializer.class, props.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
    }

    @Test
    void kafkaListenerContainerFactory_shouldBeConfiguredProperly() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(1000L, 3));

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                config.kafkaListenerContainerFactory(errorHandler);

        assertNotNull(factory);
        assertNotNull(factory.getConsumerFactory());
        assertEquals(DefaultKafkaConsumerFactory.class, factory.getConsumerFactory().getClass());
    }
}
