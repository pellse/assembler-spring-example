package io.github.pellse.patientobservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.consul.ConsulContainer;
import org.testcontainers.kafka.KafkaContainer;
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
        return new PostgreSQLContainer<>(parse("postgres:latest"));
    }

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer(parse("mongo:latest"));
    }

    @Bean
    @ServiceConnection
    public KafkaContainer kafkaContainer() {
        return new KafkaContainer(parse("apache/kafka:latest"));
    }

    @SuppressWarnings("resource")
    @Bean
    ConsulContainer consulContainer() {
        return new ConsulContainer(parse("hashicorp/consul:latest"))
                .withExposedPorts(8500);
    }

    @Bean
    public DynamicPropertyRegistrar consulContainerProperties(ConsulContainer container) {
        return (properties) -> {
            properties.add("spring.cloud.consul.host", container::getHost);
            properties.add("spring.cloud.consul.port", container::getFirstMappedPort);
        };
    }
}
