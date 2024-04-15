package org.ecews.biometricapp.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeduplicationDetail {
    private String enrolledLga;
    private String enrolledState;
    private String enrolledPatientId;
    private String enrolledHospitalNumber;
    private String enrolledUniqueId;
    private String enrolledSex;
    private LocalDate enrolledDateOfBirth;
    private String enrolledPatientFingerType;
    private String enrolledFacilityName;
    private String enrolledDatimId;
    private String enrolledTargetGroup;
    private LocalDate enrolledArtStartDate;
    private String enrolledSurname;
    private String enrolledFirstName;
    private String enrolledLgaOfResidence;

    private String matchedLga;
    private String matchedState;
    private String matchedPatientId;
    private String matchedHospitalNumber;
    private String matchedUniqueId;
    private String matchedSex;
    private LocalDate matchedDateOfBirth;
    private String matchedPatientFingerType;
    private String matchedFacilityName;
    private String matchedDatimId;
    private String matchedTargetGroup;
    private LocalDate matchedArtStartDate;
    private String matchedSurname;
    private String matchedFirstName;
    private String matchedLgaOfResidence;
    private Integer score;
    private LocalDate dateOfDeduplication;
}
