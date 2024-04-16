package org.ecews.biometricapp.entities.dtos;

import java.time.LocalDate;

public interface PatientDemographics {
	Long getFacilityId();
	Long getId();
	String getDatimId();
	
	String getFacilityName();
	
	String getState();
	
	String getLga();
	
	String getPersonUuid();
	
	String getHospitalNumber();
	
	
	String getSurname();
	
	String getOtherName();
	
	String getFirstName();
	
	LocalDate getDateOfBirth();
	
	Integer getAge();
	
	String getSex();
	String getTown();
	
	String getMaritalStatus();
	
	String getEducation();
	
	String getOccupation();
	
	String getResidentialState();
	
	String getResidentialLga();
	
	String getAddress();
	
	String getPhone();
	
	String getCareEntryPoint();
	
	LocalDate getDateOfConfirmedHIVTest();
	
	LocalDate getDateOfRegistration();
	
	String getStatusAtRegistration();
	
}
