package org.ecews.biometricapp.entities.dtos;

public interface PostTestCounsellingTypeDto {
	 String  getTestedForHIVBeforeWithinThisYear();
	 boolean getHivRequestAndResultFormSignedByTester();
	 boolean getHivRequestAndResultFormFilledWithCTIForm();
	 boolean getClientRecievedHIVTestResult();
	 boolean getPostTestCounsellingDone();
	 boolean getRiskReductionPlanDeveloped();
	 boolean getPostTestDisclosurePlanDeveloped();
	 boolean getWillBringPartnerForHIVTesting();
	 boolean getWillBringOwnChildrenForHIVTesting();
	 boolean getProvidedWithInformationOnFPandDualContraception();
	 boolean getClientOrPartnerUseFPMethodsOtherThanCondoms();
	 boolean getClientOrPartnerUseCondomsAsOneFPMethods();
	 boolean getCorrectCondomUseDemonstrated();
	 boolean getCondomsProvidedToClient();
	 boolean getClientReferredToOtherServices();
}
