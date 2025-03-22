package io.github.pellse.example.patientobservation.bodymeasurement;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/body-measurement")
public class BodyMeasurementController {

    private final BodyMeasurementService bodyMeasurementService;

    public BodyMeasurementController(BodyMeasurementService bodyMeasurementService) {
        this.bodyMeasurementService = bodyMeasurementService;
    }

    @GetMapping(value = "/find-by-patient")
    Flux<BodyMeasurement> getBodyMeasurement(@RequestParam("patient-ids") List<Integer> patientIds) {
        return bodyMeasurementService.getBodyMeasurements(patientIds);
    }
}
