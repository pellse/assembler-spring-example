package io.github.pellse.example.patientobservation.bodymeasurement;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public record BodyMeasurement(
        @Id String id,
        int patientId,
        String healthCardNumber,
        int height,
        int weight,
        LocalDateTime time) {
}
