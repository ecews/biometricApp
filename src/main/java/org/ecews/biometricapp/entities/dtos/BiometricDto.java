package org.ecews.biometricapp.entities.dtos;

import java.time.LocalDate;


public interface BiometricDto {
    String getTemplateType();

    byte[] getTemplate();

    LocalDate getEnrollmentDate();
    
    Integer getQuality();
}
