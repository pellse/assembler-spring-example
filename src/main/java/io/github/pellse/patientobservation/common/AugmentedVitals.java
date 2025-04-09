package io.github.pellse.patientobservation.common;

import java.util.List;

public record AugmentedVitals(Patient patient, HR heartRate, List<BP> bloodPressures, String diagnosis) {

    public AugmentedVitals(Vitals vitals, Diagnosis diagnosis) {
        this(vitals.patient(), vitals.heartRate(), vitals.bloodPressures(), diagnosis.diagnosis());
    }
}
