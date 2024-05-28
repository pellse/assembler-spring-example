package io.github.pellse.example;

import io.github.pellse.example.patientobservation.bodymeasurement.BodyMeasurement;
import io.github.pellse.example.patientobservation.bodymeasurement.BodyMeasurementRepository;
import io.github.pellse.example.patientobservation.patient.Patient;
import io.github.pellse.example.patientobservation.patient.PatientRepository;
import io.github.pellse.example.patientobservation.spo2.SpO2;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.Math.random;
import static java.time.Duration.ofSeconds;
import static java.time.LocalDateTime.now;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toMap;

@SpringBootApplication
public class PatientMonitoringApplication implements ApplicationListener<ApplicationReadyEvent> {

    private static final Map<Integer, Patient> PATIENT_MAP = Stream.of(
                    entry(1, new Patient(null, "Claire Gabriel", "GABC 6709 1206")),
                    entry(2, new Patient(null, "Erick Daria", "DARE 7802 2112")),
                    entry(3, new Patient(null, "Brenden Jacob", "JACB 8206 1405")))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (o, o2) -> o2, LinkedHashMap::new));

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
        return new SpO2(spO2Id, patientId, PATIENT_MAP.get(patientId).healthCardNumber(), (int) (random() * 8) + 92, now()); // Oxygen Saturation between 92% and 100%
    }

    @Bean
    public Supplier<Flux<SpO2>> sendSpO2() {

        var spO2Id = new AtomicInteger(1);

        return () -> Flux.<SpO2, Integer>generate(() -> 1, (patientId, sink) -> {
                    sink.next(randomSpO2(spO2Id.getAndIncrement(), patientId));
                    return (patientId % 3) + 1;
                })
                .delayElements(ofSeconds(1));
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {

        patientRepository
                .saveAll(PATIENT_MAP.values())
                .collectList()
                .flatMapMany(patients -> bodyMeasurementRepository.saveAll(List.of(
                        new BodyMeasurement(null, patients.getFirst().id(), patients.getFirst().healthCardNumber(), 170, 65, now().minusWeeks(2)),
                        new BodyMeasurement(null, patients.get(1).id(), patients.get(1).healthCardNumber(), 165, 62, now().minusWeeks(3)),
                        new BodyMeasurement(null, patients.get(2).id(), patients.get(2).healthCardNumber(), 175, 76, now().minusWeeks(4))
                )))
                .blockLast();
    }
}
