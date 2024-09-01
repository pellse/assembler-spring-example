package io.github.pellse.example.patientobservation.spo2;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public record SpO2(
        @Id Long id,
        int patientId,
        int spO2Value,
        LocalDateTime time) {
}