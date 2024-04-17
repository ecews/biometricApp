package org.ecews.biometricapp.entities.dtos;

public interface KnowledgeAssessmentTypeDto {
	 Boolean getPreviouslyTestedHIVNegative();
	Boolean getClientInformedAboutHIVTransmissionRoutes();
	Boolean getClientPregnant();
	Boolean getClientInformedOfHIVTransmissionRiskFactors();
	Boolean getClientInformedAboutPreventingHIV();
	Boolean getClientInformedAboutPossibleTestResults();
	Boolean getInformedConsentForHIVTestingGiven();
}
