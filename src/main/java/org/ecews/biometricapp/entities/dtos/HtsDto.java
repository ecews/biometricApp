package org.ecews.biometricapp.entities.dtos;

import java.time.LocalDate;

public interface HtsDto {
	String getClientCode();
	String getVisitId();
	String getSetting();
	String getFirstTimeVisit();
	LocalDate getVisitDate();

//	protected String sessionType;

//	protected String referredFrom;

//	protected String maritalStatus;

//	protected Integer noOfOwnChildrenLessThan5Years;

//	protected Integer noOfAllWives;

//	protected String isIndexClient;

//	protected String indexType;

//	protected String indexClientId;

//	protected String reTestingForResultVerification;

//	protected PreTestInformationType preTestInformation;

//	protected HIVTestResultType hivTestResult;

//	protected PostTestCounsellingType postTestCounselling;

//	protected String syphilisTestResult;

//	protected String hbvTestResult;

//	protected String hcvTestResult;

//	protected IndexNotificationServicesType indexNotificationServices;

//	protected String completedBy;

//	protected String dateCompleted;
	
	String getHivRiskAssessment();
	String getKnowledgeAssessment();
	String getPostTestCounselling();
	String getTestResult();
	String getRecency();
	String getSyndromicStiScreening();
	
}
