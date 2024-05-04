package org.ecews.biometricapp;

import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.repositories.ReportRepository;
import org.ecews.biometricapp.services.ReportService;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class ReportServiceTest {

    @Autowired
    ReportService reportService;

    @Autowired
    ReportRepository reportRepository;

    @Test
    void testSummaryReport () {
        var report = reportService.generateSummaryReport(DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE);
        log.info("Report generated successfully ************** {}", report.toString());
    }

    @Test
    void testSummaryRepository () {
        var data = reportRepository.getSummaryReport(DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE);
        log.info("Data size is ******* {}", data.size());
    }
}
