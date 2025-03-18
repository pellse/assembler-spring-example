package io.github.pellse.example.patientobservation;

import io.github.pellse.assembler.Assembler;
import io.github.pellse.example.patientobservation.bodymeasurement.BodyMeasurement;
import io.github.pellse.example.patientobservation.bodymeasurement.BodyMeasurementService;
import io.github.pellse.example.patientobservation.patient.Patient;
import io.github.pellse.example.patientobservation.patient.PatientService;
import io.github.pellse.example.patientobservation.spo2.SpO2;
import io.github.pellse.example.patientobservation.spo2.SpO2StreamingService;
import org.springframework.cache.CacheManager;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static io.github.pellse.assembler.AssemblerBuilder.assemblerOf;
import static io.github.pellse.assembler.Rule.rule;
import static io.github.pellse.assembler.RuleMapper.oneToOne;
import static io.github.pellse.assembler.RuleMapperSource.call;
import static io.github.pellse.assembler.caching.CacheFactory.cached;
import static io.github.pellse.assembler.caching.spring.SpringCacheFactory.springCache;
import static io.github.pellse.example.config.CaffeineCacheConfig.BODY_MEASUREMENT_CACHE_2;
import static io.github.pellse.example.config.CaffeineCacheConfig.PATIENT_CACHE;
import static java.time.Duration.ofSeconds;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@RestController
public class SpO2MonitoringStreamController {

    record SpO2Reading(SpO2 spO2, Patient patient, BodyMeasurement bodyMeasurement) {
    }

    private final Assembler<SpO2, SpO2Reading> spO2ReadingAssembler;

    private final SpO2StreamingService spO2StreamingService;

    SpO2MonitoringStreamController(
            PatientService patientService,                                        // Connects to PostgreSQL, Patient Demographics
            BodyMeasurementService bodyMeasurementService,   // Connects to MongoDB, Body Height and Weight Patient Observation
            SpO2StreamingService spO2StreamingService,              // Connects to Kafka, Real-time Oxygen Saturation from pulse oximeter device (IOT)
            CacheManager cacheManager) {

        final var patientCache = cacheManager.getCache(PATIENT_CACHE);
        final var bodyMeasurementCache = cacheManager.getCache(BODY_MEASUREMENT_CACHE_2);

        this.spO2StreamingService = spO2StreamingService;

        spO2ReadingAssembler = assemblerOf(SpO2Reading.class)
                .withCorrelationIdResolver(SpO2::patientId)
                .withRules(
                        rule(Patient::id, oneToOne(cached(call(patientService::findPatientsById), springCache(patientCache)))),
                        rule(BodyMeasurement::patientId, oneToOne(cached(call(bodyMeasurementService::getBodyMeasurements), springCache(bodyMeasurementCache)))),
                        SpO2Reading::new)
                .build();
    }

    @GetMapping(value = "/spO2/stream", produces = TEXT_EVENT_STREAM_VALUE)
    @SubscriptionMapping
    Flux<SpO2Reading> spO2Reading() {

        return spO2StreamingService.spO2Flux()
                .window(3)
                .flatMapSequential(spO2ReadingAssembler::assemble)
                .delayElements(ofSeconds(1));
    }
}