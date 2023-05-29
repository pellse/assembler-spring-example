package io.github.pellse.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
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
    @RestartScope
    PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:15.1-alpine");
    }

    @Bean
    @ServiceConnection
    @RestartScope
    MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer("mongo:5.0");
    }

    @Bean
    @ServiceConnection
    @RestartScope
    public KafkaContainer kafka() {
        return new KafkaContainer(parse("confluentinc/cp-kafka:7.4.0"));
    }
}
