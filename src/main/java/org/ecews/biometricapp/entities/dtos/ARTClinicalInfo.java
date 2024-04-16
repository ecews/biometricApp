package org.ecews.biometricapp.entities.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ARTClinicalInfo {
	Long getClinicId();
	
	Long getFacilityId();
	
	LocalDate getVisitDate();
	
	Long getCd4Percentage();
	
	Long getCd4();
	
	//Boolean isCommencement();
	
	Long getFunctionalStatusId();
	
	Long getClinicalStageId();
	
	String getclinicalUuid();
	
	long getRegimenId();
	
	long getRegimenTypeId();
	
	Long getArtStatusId();
	
	Long getWhoStagingId();
	
	String getAdherenceLevel();
	
	LocalDate getNextAppointment();
	
	String getFamilyPlaning();
	
	String getOnFamilyPlaning();
	
	String getLevelOfAdherence();
	
	String getTbStatus();
	
	String getTbPrevention();
	
	String getCd4Count();
	
	String getPregnancyStatus();
	
	Double getBodyWeight();
		
	Double getDiastolic();
	
	LocalDateTime getCaptureDate();
		
	Double getHeight();
		
	Double getTemperature();
		
	Double getPulse();
		
	Double getRespiratoryRate();
	
	String getVisitId();
		
	Double getSystolic();
	
}