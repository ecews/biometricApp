package org.ecews.biometricapp.entities.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RecaptureDetails {
    Integer count;
    LocalDate enrollmentDate;
}
