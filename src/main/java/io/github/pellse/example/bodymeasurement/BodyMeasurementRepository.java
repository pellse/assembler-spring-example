package io.github.pellse.example.bodymeasurement;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface BodyMeasurementRepository extends ReactiveMongoRepository<BodyMeasure, Integer> {

    Flux<BodyMeasure> findByPatientIdIn(List<Integer> patientIds);
}
