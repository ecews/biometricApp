package org.ecews.biometricapp.entities.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class NDRMessageLogDTO {
    Integer id;
    String message;
    String patientIdentifier;
    String FileName;
    LocalDate dateCreated;
}
