package org.ecews.biometricapp.entities.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CurrentRecaptureDTO {
    private String lga;
    private String state;
    private String patientId;
    private String hospitalNumber;
    private String uniqueId;
    private Integer age;
    private String sex;
    private LocalDate dateOfBirth;
    private String facilityName;
    private String datimId;
    private String targetGroup;
    private String enrollmentSetting;
    private LocalDate artStartDate;
    private String regimenAtArtStart;
    private LocalDate dateOfRegistration;
    private String surname;
    private String firstName;
    private String lgaOfResidence;
    private LocalDate dateOfCurrentRecapture;
    private Integer currentRecapture;
}
