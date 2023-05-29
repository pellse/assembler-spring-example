package io.github.pellse.example.bodymeasurement;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public record BodyMeasure(@Id String id, Integer patientId, int height, int weight, LocalDateTime time) {
}
