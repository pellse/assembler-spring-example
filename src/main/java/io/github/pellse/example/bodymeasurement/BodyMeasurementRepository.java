package io.github.pellse.example.bodymeasurement;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface BodyMeasurementRepository extends ReactiveMongoRepository<BodyMeasurement, Integer> {

    Flux<BodyMeasurement> findByPatientIdIn(List<Integer> patientIds);
}
