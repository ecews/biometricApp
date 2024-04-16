package org.ecews.biometricapp.entities.dtos;

import java.time.LocalDate;


public interface PatientDemographicDTO {
	Long getPersonId();
	String getPersonUuid();
	String getPatientIdentifier();
	LocalDate getDiagnosisDate();
	String getHospitalNumber();
	String getStatusAtRegistration();
	String getCareEntryPoint();
	
	Integer getAge();
	String getPatientSexCode();
	LocalDate getPatientDateOfBirth();
	String getFacilityTypeCode();
	String getFacilityName();
	String getLga();
	String getState();
	String getFacilityId();
	LocalDate getArtStartDate();
	LocalDate getDateOfBirth();
	String getFirstARTRegimenCodeDescTxt();
	String getFirstARTRegimenCode();
	String getLgaCode();
	String getStateCode();
	String getCountryCode();
	String getPatientOccupationCode();
	String getPatientMaritalStatusCode();
	String getStateOfNigeriaOriginCode();
	String getPatientEducationLevelCode();
	String getFunctionalStatusStartART();
	String getWHOClinicalStageART();
	LocalDate getEnrolledInHIVCareDate();
	String getClientCode();
	String getHtsUuid();
	String getTbStatus();
	String getCauseOfDeath();
}    
