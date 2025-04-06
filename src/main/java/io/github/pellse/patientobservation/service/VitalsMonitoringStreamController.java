package io.github.pellse.patientobservation.service;

import io.github.pellse.assembler.Assembler;
import io.github.pellse.patientobservation.common.BP;
import io.github.pellse.patientobservation.client.DiscoverableRestClient;
import io.github.pellse.patientobservation.common.Patient;
import io.github.pellse.patientobservation.common.HR;
import io.github.pellse.patientobservation.common.Vitals;
import io.github.pellse.patientobservation.service.heartrate.HeartRateStreamingService;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

import static io.github.pellse.assembler.AssemblerBuilder.assemblerOf;
import static io.github.pellse.assembler.Rule.rule;
import static io.github.pellse.assembler.RuleMapper.oneToMany;
import static io.github.pellse.assembler.RuleMapper.oneToOne;
import static io.github.pellse.assembler.caching.CacheFactory.cached;
import static io.github.pellse.assembler.caching.CacheFactory.cachedMany;
import static io.github.pellse.assembler.caching.caffeine.CaffeineCacheFactory.caffeineCache;
import static java.time.Duration.ofSeconds;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@RestController
public class VitalsMonitoringStreamController {

    private final Assembler<HR, Vitals> vitalsAssembler;
    private final HeartRateStreamingService heartRateStreamingService;
    private final DiscoverableRestClient restClient;

    VitalsMonitoringStreamController(
            HeartRateStreamingService heartRateStreamingService,
            DiscoverableRestClient restClient) {

        vitalsAssembler = assemblerOf(Vitals.class)
                .withCorrelationIdResolver(HR::patientId)
                .withRules(
                        rule(Patient::id, oneToOne(cached(this::getPatients, caffeineCache()))),
                        rule(BP::patientId, oneToMany(BP::id, cachedMany(this::getBPs, caffeineCache()))),
                        Vitals::new)
                .build();

        this.heartRateStreamingService = heartRateStreamingService;
        this.restClient = restClient;
    }

    @GetMapping(value = "/vitals/stream", produces = TEXT_EVENT_STREAM_VALUE)
    @SubscriptionMapping
    Flux<Vitals> vitals() {
        return heartRateStreamingService.stream()
                .window(3)
                .flatMapSequential(vitalsAssembler::assemble)
                .delayElements(ofSeconds(1));
    }

    private Flux<Patient> getPatients(List<HR> heartRates) {
        return restClient.retrieveData("patient-observation", "/patient/find", "ids", heartRates, HR::patientId, Patient.class);
    }

    private Flux<BP> getBPs(List<HR> heartRates) {
        return restClient.retrieveData(
                "patient-observation",
                "/blood-pressure/find-by-patient",
                "patient-ids",
                heartRates,
                HR::patientId,
                BP.class);
    }
}