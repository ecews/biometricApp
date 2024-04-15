package org.ecews.biometricapp.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeduplicationSummary {
    String personUuid;
    LocalDate dateOfDeduplication;
    Integer identifierCount;
    Integer matchCount;
    Integer noMatchCount;
    Integer subjectCount;
    String lga;
    String state;
    String patientId;
    String hospitalNumber;
    String uniqueId;
    Integer age;
    String sex;
    LocalDate dateOfBirth;
    String facilityName;
    String datimId;
    String targetGroup;
    String enrollmentSetting;
    LocalDate artStartDate;
    String regimenAtArtStart;
    LocalDate dateOfRegistration;
    String surname;
    String firstName;
    String lgaOfResidence;
}
