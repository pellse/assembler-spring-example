package io.github.pellse.patientobservation.common;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public record HR(
        @Id Long id,
        int patientId,
        int heartRateValue,
        LocalDateTime time) {
}