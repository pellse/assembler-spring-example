package io.github.pellse.patientobservation.service;

import io.github.pellse.assembler.Assembler;
import io.github.pellse.patientobservation.common.BodyMeasurement;
import io.github.pellse.patientobservation.client.DiscoverableRestClient;
import io.github.pellse.patientobservation.common.Patient;
import io.github.pellse.patientobservation.common.SpO2;
import io.github.pellse.patientobservation.service.spo2.SpO2StreamingService;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

import static io.github.pellse.assembler.AssemblerBuilder.assemblerOf;
import static io.github.pellse.assembler.Rule.rule;
import static io.github.pellse.assembler.RuleMapper.oneToOne;
import static io.github.pellse.assembler.caching.CacheFactory.cached;
import static io.github.pellse.assembler.caching.caffeine.CaffeineCacheFactory.caffeineCache;
import static java.time.Duration.ofSeconds;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@RestController
public class SpO2MonitoringStreamController {

    record SpO2Reading(SpO2 spO2, Patient patient, BodyMeasurement bodyMeasurement) {
    }

    private final Assembler<SpO2, SpO2Reading> spO2ReadingAssembler;
    private final SpO2StreamingService spO2StreamingService;
    private final DiscoverableRestClient restClient;

    SpO2MonitoringStreamController(
            SpO2StreamingService spO2StreamingService,
            DiscoverableRestClient restClient) {

        spO2ReadingAssembler = assemblerOf(SpO2Reading.class)
                .withCorrelationIdResolver(SpO2::patientId)
                .withRules(
                        rule(Patient::id, oneToOne(cached(this::findPatients, caffeineCache()))),
                        rule(BodyMeasurement::patientId, oneToOne(cached(this::findBodyMeasurements, caffeineCache()))),
                        SpO2Reading::new)
                .build();

        this.spO2StreamingService = spO2StreamingService;
        this.restClient = restClient;
    }

    @GetMapping(value = "/spO2/stream", produces = TEXT_EVENT_STREAM_VALUE)
    @SubscriptionMapping
    Flux<SpO2Reading> spO2Reading() {
        return spO2StreamingService.spO2Flux()
                .window(3)
                .flatMapSequential(spO2ReadingAssembler::assemble)
                .delayElements(ofSeconds(1));
    }

    private Flux<Patient> findPatients(List<SpO2> spO2List) {
        return restClient.retrieveData("patient-observation", "/patient/find", "ids", spO2List, SpO2::patientId, Patient.class);
    }

    private Flux<BodyMeasurement> findBodyMeasurements(List<SpO2> spO2List) {
        return restClient.retrieveData(
                "patient-observation",
                "/body-measurement/find-by-patient",
                "patient-ids",
                spO2List,
                SpO2::patientId,
                BodyMeasurement.class);
    }
}