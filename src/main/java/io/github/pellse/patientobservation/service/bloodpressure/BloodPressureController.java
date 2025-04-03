package io.github.pellse.patientobservation.service.bloodpressure;

import io.github.pellse.patientobservation.common.BP;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/blood-pressure")
public class BloodPressureController {

    private final BloodPressureService bloodPressureService;

    public BloodPressureController(BloodPressureService bloodPressureService) {
        this.bloodPressureService = bloodPressureService;
    }

    @GetMapping(value = "/find-by-patient")
    Flux<BP> getBloodPressures(@RequestParam("patient-ids") List<Integer> patientIds) {
        return bloodPressureService.getBloodPressures(patientIds);
    }
}
