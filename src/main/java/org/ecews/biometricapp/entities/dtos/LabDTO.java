package org.ecews.biometricapp.entities.dtos;

import java.time.LocalDateTime;

public interface LabDTO {
	String getVisitId();
	String getLabTestName();
	String getResultReported();
	LocalDateTime getDateSampleCollected();
	LocalDateTime getDateAssayed();
	LocalDateTime getResultDate();


}
