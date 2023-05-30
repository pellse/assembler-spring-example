package io.github.pellse.example.domain.spo2;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public record SpO2(@Id Integer id, Integer patientId, int percentage, LocalDateTime time) {
}