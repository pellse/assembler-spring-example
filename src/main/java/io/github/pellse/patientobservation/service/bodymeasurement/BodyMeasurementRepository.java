package io.github.pellse.patientobservation.service.bodymeasurement;

import io.github.pellse.patientobservation.common.BodyMeasurement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface BodyMeasurementRepository extends ReactiveMongoRepository<BodyMeasurement, Integer> {

    Flux<BodyMeasurement> findByPatientIdIn(List<Integer> patientIds);
}
