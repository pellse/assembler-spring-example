package io.github.pellse.example.patientobservation.patient;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends R2dbcRepository<Patient, Integer> {
}
