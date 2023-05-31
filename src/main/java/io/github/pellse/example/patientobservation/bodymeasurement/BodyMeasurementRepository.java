package io.github.pellse.example.patientobservation.bodymeasurement;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface BodyMeasurementRepository extends ReactiveMongoRepository<BodyMeasurement, Integer> {

    Flux<BodyMeasurement> findByPatientIdIn(List<Integer> patientIds);
}
