package io.github.pellse.example.patientobservation;

import io.github.pellse.assembler.Rule.BatchRule;
import io.github.pellse.example.patientobservation.bodymeasurement.BodyMeasurement;
import io.github.pellse.example.patientobservation.bodymeasurement.BodyMeasurementService;
import io.github.pellse.example.patientobservation.patient.Patient;
import io.github.pellse.example.patientobservation.patient.PatientService;
import io.github.pellse.example.patientobservation.spo2.SpO2;
import io.github.pellse.example.patientobservation.spo2.SpO2StreamingService;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static io.github.pellse.assembler.Rule.withIdResolver;
import static io.github.pellse.assembler.RuleMapper.oneToMany;
import static io.github.pellse.assembler.RuleMapper.oneToOne;
import static io.github.pellse.assembler.caching.AutoCacheFactory.autoCache;
import static io.github.pellse.assembler.caching.CacheFactory.cached;
import static io.github.pellse.assembler.caching.CacheFactory.cachedMany;

@Controller
public class PatientObservationGraphQLController {

    private final PatientService patientService;

    private final BatchRule<Patient, BodyMeasurement> bodyMeasurementBatchRule;
    private final BatchRule<Patient, List<SpO2>> spO2BatchRule;

    PatientObservationGraphQLController(
            PatientService patientService,                                        // Connects to PostgreSQL, Patient Demographics
            BodyMeasurementService bodyMeasurementService,   // Connects to MongoDB, Body Height and Weight Patient Observation
            SpO2StreamingService spO2StreamingService) {           // Connects to Kafka, Real-time Oxygen Saturation from pulse oximeter device (IOT)

        this.patientService = patientService;

        this.bodyMeasurementBatchRule = withIdResolver(Patient::id)
                .createRule(BodyMeasurement::patientId, oneToOne(cached(bodyMeasurementService::retrieveBodyMeasurements)));

        this.spO2BatchRule = withIdResolver(Patient::id)
                .createRule(SpO2::patientId, oneToMany(SpO2::id, cachedMany(autoCache(spO2StreamingService::spO2Flux))));
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
}