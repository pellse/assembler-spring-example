package io.github.pellse.example.patient;

import org.springframework.data.annotation.Id;

public record Patient(@Id Integer id, String name) {
}
