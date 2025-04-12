package io.github.pellse.patientobservation.server;

import io.github.pellse.assembler.BatchRule;
import io.github.pellse.patientobservation.common.BP;
import io.github.pellse.patientobservation.client.DiscoverableRestClient;
import io.github.pellse.patientobservation.common.Patient;
import io.github.pellse.patientobservation.common.HR;
import io.github.pellse.patientobservation.server.heartrate.HeartRateStreamingService;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static io.github.pellse.assembler.BatchRule.withIdResolver;
import static io.github.pellse.assembler.RuleMapper.oneToMany;
import static io.github.pellse.assembler.caching.factory.CacheFactory.cachedMany;
import static io.github.pellse.assembler.caching.factory.StreamTableFactory.streamTable;
import static io.github.pellse.assembler.caching.caffeine.CaffeineCacheFactory.caffeineCache;

@Controller
public class PatientObservationController {

    private final BatchRule<Patient, List<BP>> bloodPressureBatchRule;
    private final BatchRule<Patient, List<HR>> heartRateBatchRule;
    private final DiscoverableRestClient restClient;

    PatientObservationController(
            HeartRateStreamingService heartRateStreamingService,
            DiscoverableRestClient restClient) {

        this.bloodPressureBatchRule = withIdResolver(Patient::id)
                .createRule(BP::patientId, oneToMany(BP::id, cachedMany(this::getBPs, caffeineCache())));

        this.heartRateBatchRule = withIdResolver(Patient::id)
                .createRule(HR::patientId, oneToMany(HR::id, cachedMany(caffeineCache(), streamTable(heartRateStreamingService::stream))));

        this.restClient = restClient;
    }

    @QueryMapping
    Flux<Patient> patients() {
        return findAllPatients();
    }

    @BatchMapping
    Mono<Map<Patient, List<BP>>> bloodPressures(List<Patient> patients) {
        return bloodPressureBatchRule.toMono(patients);
    }

    @BatchMapping
    Flux<List<HR>> heartRate(List<Patient> patients) {
        return heartRateBatchRule.toFlux(patients);
    }

    private Flux<Patient> findAllPatients() {
        return restClient.retrieveData("patient-observation", "/patient/all", Patient.class);
    }

    private Flux<BP> getBPs(List<Patient> patients) {
        return restClient.retrieveData(
                "patient-observation",
                "/blood-pressure/find-by-patient",
                "patient-ids",
                patients,
                Patient::id,
                BP.class);
    }
}