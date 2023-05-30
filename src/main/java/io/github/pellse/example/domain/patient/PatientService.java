package io.github.pellse.example.domain.patient;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public Flux<Patient> findPatients(List<Integer> patientIds) {
        return patientRepository.findAllById(patientIds);
    }

    public Mono<Patient> findPatient(int patientId) {
        return patientRepository.findById(patientId);
    }
}
