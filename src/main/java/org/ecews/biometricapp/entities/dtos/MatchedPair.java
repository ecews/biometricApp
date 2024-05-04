package org.ecews.biometricapp.entities.dtos;

import lombok.Data;

@Data
public class MatchedPair {
    public String matchedPatientId;
    public String enrolledFingerId;
    public String matchedFingerId;
    public String enrolledPatientFingerType;
    public String matchedPatientFingerType;
    public Integer score;
    public Integer matchedRecapture;
}
