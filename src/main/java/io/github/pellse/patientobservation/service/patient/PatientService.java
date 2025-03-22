package io.github.pellse.patientobservation.service.patient;

import io.github.pellse.patientobservation.common.Patient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Flux<Patient> findAllPatients() {
        return patientRepository.findAll();
    }

    public Flux<Patient> findPatientsById(List<Integer> patientIds) {
        return patientRepository.findAllById(patientIds);
    }

    public Flux<Patient> findPatientsByHealthCardNumber(List<String> healthCardNumbers) {
        return patientRepository.findAllByHealthCardNumberIn(healthCardNumbers);
    }
}
