package io.github.pellse.patientobservation.common;

import org.springframework.data.annotation.Id;

public record Patient(
        @Id Integer id,
        String name,
        String healthCardNumber) {
}
