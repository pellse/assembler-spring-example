package io.github.pellse.patientobservation.server.bloodpressure;

import io.github.pellse.patientobservation.common.BP;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface BloodPressureRepository extends ReactiveMongoRepository<BP, Integer> {

    Flux<BP> findByPatientIdIn(List<Integer> patientIds);
}
