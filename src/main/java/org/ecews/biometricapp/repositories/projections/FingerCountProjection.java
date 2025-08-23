package org.ecews.biometricapp.repositories.projections;

public interface FingerCountProjection {
    String getPersonUuid();
    Long getFingerCount();
}
