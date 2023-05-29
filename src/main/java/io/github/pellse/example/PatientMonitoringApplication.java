package io.github.pellse.example;

import io.github.pellse.example.bodymeasurement.BodyMeasurement;
import io.github.pellse.example.bodymeasurement.BodyMeasurementRepository;
import io.github.pellse.example.patient.Patient;
import io.github.pellse.example.patient.PatientRepository;
import io.github.pellse.example.spO2.SpO2;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static java.lang.Math.random;
import static java.time.Duration.ofSeconds;
import static java.time.LocalDateTime.now;

@SpringBootApplication
public class PatientMonitoringApplication implements ApplicationListener<ApplicationReadyEvent> {

    private final PatientRepository patientRepository;
    private final BodyMeasurementRepository bodyMeasurementRepository;

    PatientMonitoringApplication(PatientRepository patientRepository, BodyMeasurementRepository bodyMeasurementRepository) {

        this.patientRepository = patientRepository;
        this.bodyMeasurementRepository = bodyMeasurementRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(PatientMonitoringApplication.class, args);
    }

    private static SpO2 randomSpO2(int spO2Id, int patientId) {
        return new SpO2(spO2Id, patientId, (int) (random() * 8) + 92, now()); // Oxygen Saturation between 92% and 100%
    }

    @Bean
    public Supplier<Flux<SpO2>> sendSpO2() {

        var spO2Id = new AtomicInteger(1);

        return () -> Flux.<SpO2>generate(sink -> {
                    sink.next(randomSpO2(spO2Id.getAndIncrement(), 1));
                    sink.next(randomSpO2(spO2Id.getAndIncrement(), 2));
                    sink.next(randomSpO2(spO2Id.getAndIncrement(), 3));
                })
                .delayElements(ofSeconds(1));
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {

        patientRepository.saveAll(List.of(
                        new Patient(null, "Claire Gabriel"),
                        new Patient(null, "Erick Daria"),
                        new Patient(null, "Brenden Jacob")))
                .blockLast();

        bodyMeasurementRepository.saveAll(List.of(
                        new BodyMeasurement(null, 1, 170, 65, now().minusWeeks(2)),
                        new BodyMeasurement(null, 2, 165, 62, now().minusWeeks(3)),
                        new BodyMeasurement(null, 3, 175, 76, now().minusWeeks(4))
                ))
                .blockLast();
    }
}









