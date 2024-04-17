package org.ecews.biometricapp.entities.dtos;

public interface PatientRedactedDemographicDTO {
    String getPersonUuid();
    String getPatientIdentifier();
    String getHospitalNumber();
    String getFacilityName();
    String getFacilityId();
    String getLgaCode();
    String getStateCode();
    String getRedactedPatient();
    String getRedactedPatientReason();
    String getRedactedVisit();
    String getReason();
}
