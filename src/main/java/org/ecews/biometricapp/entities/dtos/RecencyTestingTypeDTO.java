package org.ecews.biometricapp.entities.dtos;
import java.time.LocalDate;

public interface RecencyTestingTypeDTO {
	//@XmlElement(name = "TestName", required = true)
	 String getTestName();
	//@XmlElement(name = "TestDate", required = true)
	LocalDate getTestDate();
	//@XmlElement(name = "SampleType", required = true)
	 String getSampleType();
	//@XmlElement(name = "DateSampleCollected", required = true)

	LocalDate getDateSampleCollected();
	//@XmlElement(name = "DateSampleSent", required = true)

	LocalDate getDateSampleSent();
	//@XmlElement(name = "PCRLab", required = true)
	 String getPcrLab();
	//@XmlElement(name = "RapidRecencyAssay", required = true)
	 String getRapidRecencyAssay();
	 Double getViralLoadConfirmationResult();
	 LocalDate getViralLoadConfirmationTestDate();
	//@XmlElement(name = "FinalRecencyTestResult", required = true)
	 String getFinalRecencyTestResult();
	//@XmlElement(name = "Consent", required = true)

	 //YNCodeType
	 String	 getConsent();
	//@XmlElement(name = "RecencyNumber", required = true)
	 String getRecencyNumber();
	//@XmlElement(name = "ControlLine", required = true)

	 //YNCodeType
	 String	 getControlLine();
	//@XmlElement(name = "VerificationLine", required = true)
	
	// YNCodeType
	String	 getVerificationLine();
	//@XmlElement(name = "LongTermLine", required = true)
	
	//YNCodeType
	 String getLongTermLine();
	//@XmlElement(name = "RecencyInterpretation", required = true)
	 String getRecencyInterpretation();
	//@XmlElement(name = "ViralLoadRequest", required = true)

	//YNCodeType
	 String getViralLoadRequest();
	//@XmlElement(name = "SampleReferenceNumber", required = true)
	 String getSampleReferenceNumber();
	//@XmlElement(name = "ViralLoadClassification", required = true)
	 String getViralLoadClassification();
}
