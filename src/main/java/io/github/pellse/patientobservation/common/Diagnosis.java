package io.github.pellse.patientobservation.common;

public record Diagnosis(Integer patientId, String diagnosis) {

    public Diagnosis(Vitals vitals) {
        this(vitals.patient().id());
    }

    public Diagnosis(Integer patientId) {
        this(patientId, "Unavailable");
    }
}