package io.github.pellse.example.patientobservation;

import io.github.pellse.assembler.Assembler;
import io.github.pellse.assembler.Rule.BatchRule;
import io.github.pellse.example.patientobservation.bodymeasurement.BodyMeasurement;
import io.github.pellse.example.patientobservation.bodymeasurement.BodyMeasurementService;
import io.github.pellse.example.patientobservation.patient.Patient;
import io.github.pellse.example.patientobservation.patient.PatientService;
import io.github.pellse.example.patientobservation.spo2.SpO2;
import io.github.pellse.example.patientobservation.spo2.SpO2StreamingService;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static io.github.pellse.assembler.AssemblerBuilder.assemblerOf;
import static io.github.pellse.assembler.Rule.rule;
import static io.github.pellse.assembler.Rule.withIdResolver;
import static io.github.pellse.assembler.RuleMapper.oneToMany;
import static io.github.pellse.assembler.RuleMapper.oneToOne;
import static io.github.pellse.assembler.RuleMapperSource.call;
import static io.github.pellse.assembler.caching.AutoCacheFactory.autoCache;
import static io.github.pellse.assembler.caching.CacheFactory.cached;
import static io.github.pellse.assembler.caching.CacheFactory.cachedMany;
import static java.time.Duration.ofSeconds;

@Controller
public class PatientObservationGraphQLController {

    record SpO2Reading(SpO2 spO2, Patient patient, BodyMeasurement bodyMeasurement) {
    }

    private final PatientService patientService;
    private final SpO2StreamingService spO2StreamingService;

    private final BatchRule<Patient, BodyMeasurement> bodyMeasurementBatchRule;
    private final BatchRule<Patient, List<SpO2>> spO2BatchRule;

    private final Assembler<SpO2, SpO2Reading> spO2ReadingAssembler;

    PatientObservationGraphQLController(
            PatientService patientService,                                        // Connects to PostgreSQL, Patient Demographics
            BodyMeasurementService bodyMeasurementService,   // Connects to MongoDB, Body Height and Weight Patient Observation
            SpO2StreamingService spO2StreamingService) {           // Connects to Kafka, Real-time Oxygen Saturation from pulse oximeter device (IOT)

        this.patientService = patientService;

        this.bodyMeasurementBatchRule = withIdResolver(Patient::id)
                .createRule(BodyMeasurement::patientId, oneToOne(cached(bodyMeasurementService::retrieveBodyMeasurements)));

        this.spO2BatchRule = withIdResolver(Patient::id)
                .createRule(SpO2::patientId, oneToMany(SpO2::id, cachedMany(autoCache(spO2StreamingService::spO2Flux))));

        this.spO2StreamingService = spO2StreamingService;

        this.spO2ReadingAssembler = assemblerOf(SpO2Reading.class)
                .withCorrelationIdResolver(SpO2::patientId)
                .withRules(
                        rule(Patient::id, oneToOne(cached(call(SpO2::healthCardNumber, patientService::findPatientsByHealthCardNumber)))),
                        rule(BodyMeasurement::patientId, oneToOne(cached(call(bodyMeasurementService::getBodyMeasurements)))),
                        SpO2Reading::new)
                .build();
    }

    @QueryMapping
    Flux<Patient> patients() {
        return patientService.findAllPatients();
    }

    @BatchMapping
    Mono<Map<Patient, BodyMeasurement>> bodyMeasurement(List<Patient> patients) {
        return bodyMeasurementBatchRule.toMono(patients);
    }

    @BatchMapping
    Flux<List<SpO2>> spO2(List<Patient> patients) {
        return spO2BatchRule.toFlux(patients);
    }

    @SubscriptionMapping
    Flux<SpO2Reading> spO2Reading() {

        return spO2StreamingService.spO2Flux()
                .window(3)
                .flatMapSequential(spO2ReadingAssembler::assemble)
                .delayElements(ofSeconds(1));
    }
}