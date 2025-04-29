package io.github.pellse.patientobservation;

import io.github.pellse.patientobservation.common.BP;
import io.github.pellse.patientobservation.server.bloodpressure.BloodPressureRepository;
import io.github.pellse.patientobservation.common.Patient;
import io.github.pellse.patientobservation.server.patient.PatientRepository;
import io.github.pellse.patientobservation.common.HR;
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

import static io.github.pellse.patientobservation.common.Patient.Sex.F;
import static io.github.pellse.patientobservation.common.Patient.Sex.M;
import static java.lang.Math.random;
import static java.time.Duration.ofSeconds;
import static java.time.LocalDateTime.now;

@SpringBootApplication
public class PatientMonitoringApplication implements ApplicationListener<ApplicationReadyEvent> {

    private final PatientRepository patientRepository;
    private final BloodPressureRepository bloodPressureRepository;

    PatientMonitoringApplication(PatientRepository patientRepository, BloodPressureRepository bloodPressureRepository) {
        this.patientRepository = patientRepository;
        this.bloodPressureRepository = bloodPressureRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(PatientMonitoringApplication.class, args);
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {

        var patients = List.of(
                new Patient(null, "Claire Gabriel", "GABC 6709 1206", 35, F),
                new Patient(null, "Erick Daria", "DARE 7802 2112", 43, M),
                new Patient(null, "Brenden Jacob", "JACB 8206 1405", 62, M));

        patientRepository
                .saveAll(patients)
                .flatMapIterable(PatientMonitoringApplication::randomBloodPressures)
                .transform(bloodPressureRepository::saveAll)
                .doOnError(Throwable::printStackTrace)
                .blockLast();
    }

    @Bean
    public Supplier<Flux<HR>> sendHeartRates() {
        var heartRateId = new AtomicLong(1);

        return () -> Flux.<HR, Integer>generate(() -> 1, (patientId, sink) -> {
                    sink.next(randomHeartRates(heartRateId.getAndIncrement(), patientId));
                    return (patientId % 3) + 1;
                })
                .delayElements(ofSeconds(1));
    }

    private static HR randomHeartRates(long heartRateId, int patientId) {
        return new HR(heartRateId, patientId, (int) (random() * 100) + 50, now()); // Between 50 bpm and 150 bpm
    }

    private static List<BP> randomBloodPressures(Patient patient) {

        Supplier<BP> bpSupplier = () -> new BP(
                null,
                patient.id(),
                (int) (random() * 40) + 100, // Systolic between 100 mmHg and 140 mmHg
                (int) (random() * 40) + 60, // Diastolic between 60 mmHg and 100 mmHg
                now().minusWeeks((long) (random() * 4) + 1)); // Now minus 1 to 5 weeks

        return List.of(bpSupplier.get(), bpSupplier.get());
    }
}