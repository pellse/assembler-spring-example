package io.github.pellse.example.domain.patient;

import org.springframework.data.annotation.Id;

public record Patient(@Id Integer id, String name) {
}
