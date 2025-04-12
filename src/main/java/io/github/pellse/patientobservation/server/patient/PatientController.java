package io.github.pellse.patientobservation.server.patient;

import io.github.pellse.patientobservation.common.Patient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping(value = "/all")
    Flux<Patient> findAllPatients() {
        return patientService.findAllPatients();
    }

    @GetMapping(value = "/find")
    Flux<Patient> findPatients(@RequestParam("ids") List<Integer> patientIds) {
        return patientService.findPatientsById(patientIds);
    }
}