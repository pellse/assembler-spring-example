package io.github.pellse.patientobservation.common;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public record SpO2(
        @Id Long id,
        int patientId,
        int spO2Value,
        LocalDateTime time) {
}