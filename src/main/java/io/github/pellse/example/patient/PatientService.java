package io.github.pellse.example.patient;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Flux<Patient> retrieveAllPatients() {
        return patientRepository.findAll();
    }
}
