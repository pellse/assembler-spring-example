package io.github.pellse.patientobservation.common;

import java.util.List;

public record Vitals(HR heartRate, Patient patient, List<BP> bloodPressures) {
}