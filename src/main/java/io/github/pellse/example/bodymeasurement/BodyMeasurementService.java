package io.github.pellse.example.bodymeasurement;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class BodyMeasurementService {

    private final BodyMeasurementRepository bodyMeasurementRepository;

    public BodyMeasurementService(BodyMeasurementRepository bodyMeasurementRepository) {
        this.bodyMeasurementRepository = bodyMeasurementRepository;
    }

    public Flux<BodyMeasurement> retrieveBodyMeasurement(List<Integer> patientIds) {
        return bodyMeasurementRepository.findByPatientIdIn(patientIds);
    }
}