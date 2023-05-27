package io.github.pellse.example;

import io.github.pellse.example.bodymeasurement.BodyMeasure;
import io.github.pellse.example.bodymeasurement.BodyMeasureService;
import io.github.pellse.example.patient.Patient;
import io.github.pellse.example.patient.PatientService;
import io.github.pellse.example.spo2.SpO2;
import io.github.pellse.example.spo2.SpO2Service;
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
import static io.github.pellse.reactive.assembler.caching.CacheFactory.cached;

@Controller
public class PatientMonitoringGraphQLController {

    private final PatientService patientService;
    private final BatchRule<Patient, BodyMeasure> bodyMeasureBatchRule;
    private final BatchRule<Patient, List<SpO2>> spO2BatchRule;

    PatientMonitoringGraphQLController(PatientService patientService, BodyMeasureService bodyMeasureService, SpO2Service spO2Service) {

        this.patientService = patientService;

        this.bodyMeasureBatchRule = batchRule(BodyMeasure::patientId, oneToOne(cached(bodyMeasureService::retrieveBodyMeasure)))
                .withIdExtractor(Patient::id);

        this.spO2BatchRule = batchRule(SpO2::patientId, oneToMany(SpO2::id, cached(spO2Service::retrieveSpO2)))
                .withIdExtractor(Patient::id);
    }

    @QueryMapping
    Flux<Patient> patients() {
        return patientService.retrieveAllPatients();
    }

    @BatchMapping
    Mono<Map<Patient, BodyMeasure>> bodyMeasurement(List<Patient> patients) {
        return bodyMeasureBatchRule.executeToMono(patients);
    }

    @BatchMapping
    Flux<List<SpO2>> spO2(List<Patient> patients) {
        return spO2BatchRule.executeToFlux(patients);
    }
}
