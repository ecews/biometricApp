package org.ecews.biometricapp.entities.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientInfo {
    private String state;
    private String lga;
    private String facilityName;
    private String datimId;
    private String patientId;
    private String hospitalNumber;
    private String uniqueId;
    private String sex;
    private LocalDate dateOfBirth;
    private String targetGroup;
    private String enrollmentSetting;
    private LocalDate artStartDate;
    private LocalDate dateOfRegistration;
    private String surname;
    private String firstName;
    private String lgaOfResidence;

    // Getters and Setters
}
