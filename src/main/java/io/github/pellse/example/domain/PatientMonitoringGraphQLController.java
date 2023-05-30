package io.github.pellse.example.domain;

import io.github.pellse.example.domain.bodymeasurement.BodyMeasurement;
import io.github.pellse.example.domain.bodymeasurement.BodyMeasurementService;
import io.github.pellse.example.domain.patient.Patient;
import io.github.pellse.example.domain.patient.PatientRepository;
import io.github.pellse.example.domain.spo2.SpO2;
import io.github.pellse.example.domain.spo2.SpO2StreamingService;
import io.github.pellse.reactive.assembler.Rule.BatchRule;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static io.github.pellse.reactive.assembler.Rule.batchRule;
import static io.github.pellse.reactive.assembler.RuleMapper.oneToMany;
import static io.github.pellse.reactive.assembler.RuleMapper.oneToOne;
import static io.github.pellse.reactive.assembler.caching.AutoCacheFactory.autoCache;
import static io.github.pellse.reactive.assembler.caching.CacheFactory.cached;

@Controller
public class PatientMonitoringGraphQLController {

    private final PatientRepository patientRepository;
    private final BatchRule<Patient, BodyMeasurement> bodyMeasurementBatchRule; // Body Height and Weight
    private final BatchRule<Patient, List<SpO2>> spO2BatchRule; // Oxygen Saturation from pulse oximeter device (IOT)

    PatientMonitoringGraphQLController(PatientRepository pr, BodyMeasurementService bms, SpO2StreamingService spO2ss) {

        this.patientRepository = pr;

        this.bodyMeasurementBatchRule = batchRule(BodyMeasurement::patientId, oneToOne(cached(bms::retrieveBodyMeasurement)))
                .withIdExtractor(Patient::id);

        this.spO2BatchRule = batchRule(SpO2::patientId, oneToMany(SpO2::id, cached(autoCache(spO2ss::spO2Flux))))
                .withIdExtractor(Patient::id);
    }

    @QueryMapping
    Flux<Patient> patients() {
        return patientRepository.findAll();
    }

    @BatchMapping
    Mono<Map<Patient, BodyMeasurement>> bodyMeasurement(List<Patient> patients) {
        return bodyMeasurementBatchRule.executeToMono(patients);
    }

    @BatchMapping
    Flux<List<SpO2>> spO2(List<Patient> patients) {
        return spO2BatchRule.executeToFlux(patients);
    }
}
