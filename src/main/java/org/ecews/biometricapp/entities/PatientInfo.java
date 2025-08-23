package org.ecews.biometricapp.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientInfo {
    private String uuid;
    private String surname;
    private String firstName;
    private LocalDate dateOfBirth;
    private String sex;
    private LocalDate dateStarted;
}
