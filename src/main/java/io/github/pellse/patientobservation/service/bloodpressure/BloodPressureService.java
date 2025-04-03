package io.github.pellse.patientobservation.service.bloodpressure;

import io.github.pellse.patientobservation.common.BP;
import io.github.pellse.patientobservation.common.Patient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class BloodPressureService {

    private final BloodPressureRepository bloodPressureRepository;

    public BloodPressureService(BloodPressureRepository bloodPressureRepository) {
        this.bloodPressureRepository = bloodPressureRepository;
    }

    public Flux<BP> retrieveBloodPressures(List<Patient> patients) {
        return getBloodPressures(patients.stream().map(Patient::id).toList());
    }

    public Flux<BP> getBloodPressures(List<Integer> patientIds) {
        return bloodPressureRepository.findByPatientIdIn(patientIds);
    }
}