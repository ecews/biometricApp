package org.ecews.biometricapp.entities.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BiometricLongitudinalDTO {
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
    
    private Integer baselineCount;
    private LocalDate baselineDate;
    private Integer recaptureOneCount;
    private LocalDate recaptureOneDate;
    private Integer recaptureTwoCount;
    private LocalDate recaptureTwoDate;
    private Integer recaptureThreeCount;
    private LocalDate recaptureThreeDate;
    private Integer recaptureFourCount;
    private LocalDate recaptureFourDate;
    private Integer recaptureFiveCount;
    private LocalDate recaptureFiveDate;
    private Integer recaptureSixCount;
    private LocalDate recaptureSixDate;
    private Integer recaptureSevenCount;
    private LocalDate recaptureSevenDate;
    private Integer recaptureEightCount;
    private LocalDate recaptureEightDate;
    private Integer recaptureNineCount;
    private LocalDate recaptureNineDate;
    private Integer recaptureTenCount;
    private LocalDate recaptureTenDate;

}
