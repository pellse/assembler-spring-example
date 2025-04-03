package io.github.pellse.patientobservation.common;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public record BP(
        @Id String id,
        int patientId,
        int systolic,
        int diastolic,
        LocalDateTime time) {
}
