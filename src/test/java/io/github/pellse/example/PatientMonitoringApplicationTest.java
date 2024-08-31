package io.github.pellse.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.testcontainers.utility.DockerImageName.parse;

@TestConfiguration(proxyBeanMethods = false)
public class PatientMonitoringApplicationTest {

    public static void main(String[] args) {

        SpringApplication
                .from(PatientMonitoringApplication::main)
                .with(PatientMonitoringApplicationTest.class)
                .run(args);
    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(parse("postgres:16.4-alpine"));
    }

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer(parse("mongo:7.0.3"));
    }

    @Bean
    public KafkaContainer kafka(DynamicPropertyRegistry registry) {
        var kafka = new KafkaContainer(parse("confluentinc/cp-kafka:7.4.6"));
        registry.add("spring.kafka.properties.bootstrap.servers", () -> "%s:%s".formatted(kafka.getHost(), kafka.getFirstMappedPort()));
        return kafka;
    }
}
