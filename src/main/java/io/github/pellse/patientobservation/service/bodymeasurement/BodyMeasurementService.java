package io.github.pellse.patientobservation.service.bodymeasurement;

import io.github.pellse.patientobservation.common.BodyMeasurement;
import io.github.pellse.patientobservation.common.Patient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class BodyMeasurementService {

    private final BodyMeasurementRepository bodyMeasurementRepository;

    public BodyMeasurementService(BodyMeasurementRepository bodyMeasurementRepository) {
        this.bodyMeasurementRepository = bodyMeasurementRepository;
    }

    public Flux<BodyMeasurement> retrieveBodyMeasurements(List<Patient> patients) {
        return getBodyMeasurements(patients.stream().map(Patient::id).toList());
    }

    public Flux<BodyMeasurement> getBodyMeasurements(List<Integer> patientIds) {
        return bodyMeasurementRepository.findByPatientIdIn(patientIds);
    }
}