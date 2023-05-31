package io.github.pellse.example.patientobservation.bodymeasurement;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public record BodyMeasurement(@Id String id, Integer patientId, int height, int weight, LocalDateTime time) {
}
