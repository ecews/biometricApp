package org.ecews.biometricapp.entities.dtos;

import java.time.LocalDate;

public interface HtsReportDto extends
        SyndromicSTIScreeningTypeDto,
        KnowledgeAssessmentTypeDto,
        HIVRiskAssessmentTypeDTO,
        ClinicalTBScreeningTypeDto,
        TestResultTypeDTO,
        RecencyTestingTypeDTO,
        PostTestCounsellingTypeDto{
    String getClientCode();
    String getVisitId();
    String getSetting();
    String getFirstTimeVisit();
    LocalDate getVisitDate();
}
