package io.github.pellse.example.patientobservation;

import io.github.pellse.assembler.BatchRule;
import io.github.pellse.example.patientobservation.bodymeasurement.BodyMeasurement;
import io.github.pellse.example.client.DiscoverableRestClient;
import io.github.pellse.example.patientobservation.patient.Patient;
import io.github.pellse.example.patientobservation.spo2.SpO2;
import io.github.pellse.example.patientobservation.spo2.SpO2StreamingService;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static io.github.pellse.assembler.BatchRule.withIdResolver;
import static io.github.pellse.assembler.RuleMapper.oneToMany;
import static io.github.pellse.assembler.RuleMapper.oneToOne;
import static io.github.pellse.assembler.caching.CacheFactory.cached;
import static io.github.pellse.assembler.caching.CacheFactory.cachedMany;
import static io.github.pellse.assembler.caching.StreamTableFactory.streamTable;
import static io.github.pellse.assembler.caching.caffeine.CaffeineCacheFactory.caffeineCache;

@Controller
public class PatientObservationController {

    private final BatchRule<Patient, BodyMeasurement> bodyMeasurementBatchRule;
    private final BatchRule<Patient, List<SpO2>> spO2BatchRule;
    private final DiscoverableRestClient restClient;

    PatientObservationController(
            SpO2StreamingService spO2StreamingService,
            DiscoverableRestClient restClient) {

        this.bodyMeasurementBatchRule = withIdResolver(Patient::id)
                .createRule(BodyMeasurement::patientId, oneToOne(cached(this::findBodyMeasurements, caffeineCache())));

        this.spO2BatchRule = withIdResolver(Patient::id)
                .createRule(SpO2::patientId, oneToMany(SpO2::id, cachedMany(caffeineCache(), streamTable(spO2StreamingService::spO2Flux))));

        this.restClient = restClient;
    }

    @QueryMapping
    Flux<Patient> patients() {
        return findAllPatients();
    }

    @BatchMapping
    Mono<Map<Patient, BodyMeasurement>> bodyMeasurement(List<Patient> patients) {
        return bodyMeasurementBatchRule.toMono(patients);
    }

    @BatchMapping
    Flux<List<SpO2>> spO2(List<Patient> patients) {
        return spO2BatchRule.toFlux(patients);
    }

    private Flux<Patient> findAllPatients() {
        return restClient.retrieveData("patient-observation", "/patient/all", Patient.class);
    }

    private Flux<BodyMeasurement> findBodyMeasurements(List<Patient> patients) {
        return restClient.retrieveData(
                "patient-observation",
                "/body-measurement/find-by-patient",
                "patient-ids",
                patients,
                Patient::id,
                BodyMeasurement.class);
    }
}