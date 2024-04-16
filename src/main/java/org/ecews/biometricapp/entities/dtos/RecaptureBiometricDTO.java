package org.ecews.biometricapp.entities.dtos;

import java.time.LocalDate;

public interface RecaptureBiometricDTO {
	 String getTemplateType();
	 String getTemplateTypeHash();
	 Integer getQuality();
	 Integer getCount();
	 byte[] getTemplate();
	 LocalDate getEnrollmentDate();
}
