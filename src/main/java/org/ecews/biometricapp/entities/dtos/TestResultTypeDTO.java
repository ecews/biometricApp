package org.ecews.biometricapp.entities.dtos;

import java.time.LocalDate;

public interface TestResultTypeDTO {
	//@XmlElement(name = "ScreeningTestResult", required = true)
	 String getScreeningTestResult();
	//@XmlElement(name = "ScreeningTestResultDate", required = true)
	//@XmlSchemaType(name = "date")
	 LocalDate getScreeningTestResultDate();
	//@XmlElement(name = "ConfirmatoryTestResult", required = true)
	 String getConfirmatoryTestResult();
	//@XmlElement(name = "ConfirmatoryTestResultDate", required = true)
	//@XmlSchemaType(name = "date")
	LocalDate getConfirmatoryTestResultDate();
	//@XmlElement(name = "TieBreakerTestResult", required = true)
     String getTieBreakerTestResult();
	//@XmlElement(name = "TieBreakerTestResultDate", required = true)
	//@XmlSchemaType(name = "date")
	 LocalDate getTieBreakerTestResultDate();
	//@XmlElement(name = "FinalTestResult", required = true)
	 String getFinalTestResult();
}
