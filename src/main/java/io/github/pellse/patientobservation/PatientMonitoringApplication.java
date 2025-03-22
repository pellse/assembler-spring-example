package io.github.pellse.patientobservation;

import io.github.pellse.patientobservation.common.BodyMeasurement;
import io.github.pellse.patientobservation.service.bodymeasurement.BodyMeasurementRepository;
import io.github.pellse.patientobservation.common.Patient;
import io.github.pellse.patientobservation.service.patient.PatientRepository;
import io.github.pellse.patientobservation.common.SpO2;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
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

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {

        var patients = List.of(
                new Patient(null, "Claire Gabriel", "GABC 6709 1206"),
                new Patient(null, "Erick Daria", "DARE 7802 2112"),
                new Patient(null, "Brenden Jacob", "JACB 8206 1405"));

        patientRepository
                .saveAll(patients)
                .map(PatientMonitoringApplication::randomBodyMeasurement)
                .transform(bodyMeasurementRepository::saveAll)
                .blockLast();
    }

    @Bean
    public Supplier<Flux<SpO2>> sendSpO2() {
        var spO2Id = new AtomicLong(1);

        return () -> Flux.<SpO2, Integer>generate(() -> 1, (patientId, sink) -> {
                    sink.next(randomSpO2(spO2Id.getAndIncrement(), patientId));
                    return (patientId % 3) + 1;
                })
                .delayElements(ofSeconds(1));
    }

    private static SpO2 randomSpO2(long spO2Id, int patientId) {
        return new SpO2(spO2Id, patientId, (int) (random() * 25) + 75, now()); // Oxygen Saturation between 75% and 100%
    }

    private static BodyMeasurement randomBodyMeasurement(Patient patient) {
        return new BodyMeasurement(
                null,
                patient.id(),
                patient.healthCardNumber(),
                (int) (random() * 20) + 160, // Between 160 cm and 180 cm
                (int) (random() * 20) + 60, // Between 60 kg and 80 kg
                now().minusWeeks((long) (random() * 4) + 1)); // Now minus 1 to 5 weeks
    }
}