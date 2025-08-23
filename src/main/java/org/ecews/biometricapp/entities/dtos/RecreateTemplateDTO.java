package org.ecews.biometricapp.entities.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RecreateTemplateDTO {
    public String personUuid;
    public LocalDate dateOfEnrollment;
    public int use;
    public int create;
}
