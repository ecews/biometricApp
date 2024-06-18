package org.ecews.biometricapp.controllers.apis;

import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.services.ReportService;
import org.ecews.biometricapp.utils.Constants;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
public class ReportAPIController {

    private final ReportService reportService;

    public ReportAPIController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping(value = "/generate-reports-others", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> generateCSVReport(@RequestParam("reportType") String reportType){
        ByteArrayInputStream inputStream = null;

        if (reportType.equalsIgnoreCase(Constants.CURRENT_RECAPTURE)){
            inputStream = reportService.generateRecaptureReport();
            // Create a Resource from the ByteArrayInputStream
            InputStreamResource resource = new InputStreamResource(inputStream);
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + Constants.CURRENT_RECAPTURE + LocalDate.now() + ".csv");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .headers(headers)
                    .body(resource);
        } else if (reportType.equalsIgnoreCase(Constants.BIOMETRIC_LONGITUDINAL_REPORT)) {
            inputStream = reportService.generateLongitudinalReport();
            // Create a Resource from the ByteArrayInputStream
            InputStreamResource resource = new InputStreamResource(inputStream);
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + Constants.BIOMETRIC_LONGITUDINAL_REPORT + LocalDate.now() + ".csv");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .headers(headers)
                    .body(resource);
        }

        return null;
    }

    @GetMapping(value = "/generate-reports", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> generateCSVReport(
            @RequestParam("reportType") String reportType, @RequestParam("reportLevel") String reportLevel
    ) {
        ByteArrayInputStream inputStream = null;

        switch (reportLevel) {
            case DeDuplicationConfigs.REPORT_LEVEL_SUMMARY -> {
                switch (reportType) {
                    case DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE_REPORT -> {
                        inputStream = reportService.generateSummaryReport(DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE_REPORT + "_SUMMARY" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                    /*case DeDuplicationConfigs.RECAPTURE_ONE_DUPLICATE_CHECK_REPORT -> {
                        CompletableFuture<InputStream> future = CompletableFuture.supplyAsync(() -> reportService.generateSummaryReport(DeDuplicationConfigs.RECAPTURE_ONE_DUPLICATE_CHECK));

                        // Wait for the future to complete and get the result
                        InputStream input = future.join();
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(input);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_ONE_DUPLICATE_CHECK_REPORT + "_SUMMARY" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }*/
                    case DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE_REPORT -> {
                        log.info("About to generate recapture two and one *****8 ");
                        inputStream = reportService.generateSummaryReport(DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE_REPORT + "_SUMMARY" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                    case DeDuplicationConfigs.RECAPTURE_THREE_AND_TWO_REPORT -> {
                        inputStream = reportService.generateSummaryReport(DeDuplicationConfigs.RECAPTURE_THREE_AND_TWO);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_THREE_AND_TWO_REPORT + "_SUMMARY" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                    /*case DeDuplicationConfigs.RECAPTURE_TWO_DUPLICATE_CHECK_REPORT -> {
                        inputStream = reportService.generateSummaryReport(DeDuplicationConfigs.RECAPTURE_TWO_DUPLICATE_CHECK);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_TWO_DUPLICATE_CHECK_REPORT + "_SUMMARY" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }*/
                    case DeDuplicationConfigs.RECAPTURE_FOUR_REPORT -> {
                        inputStream = reportService.generateSummaryReport(DeDuplicationConfigs.RECAPTURE_FOUR);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_FOUR_REPORT + "_SUMMARY" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                    case DeDuplicationConfigs.RECAPTURE_FIVE_REPORT -> {
                        inputStream = reportService.generateSummaryReport(DeDuplicationConfigs.RECAPTURE_FIVE);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_FIVE_REPORT + "_SUMMARY" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                    case DeDuplicationConfigs.RECAPTURE_SIX_REPORT -> {
                        inputStream = reportService.generateSummaryReport(DeDuplicationConfigs.RECAPTURE_SIX);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_SIX_REPORT + "_SUMMARY" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                    case DeDuplicationConfigs.RECAPTURE_SEVEN_REPORT -> {
                        inputStream = reportService.generateSummaryReport(DeDuplicationConfigs.RECAPTURE_SEVEN);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_SEVEN_REPORT + "_SUMMARY" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                    case DeDuplicationConfigs.RECAPTURE_EIGHT_REPORT -> {
                        inputStream = reportService.generateSummaryReport(DeDuplicationConfigs.RECAPTURE_EIGHT);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_EIGHT_REPORT + "_SUMMARY" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                    case DeDuplicationConfigs.RECAPTURE_NINE_REPORT -> {
                        inputStream = reportService.generateSummaryReport(DeDuplicationConfigs.RECAPTURE_NINE);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_NINE_REPORT + "_SUMMARY" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                    case DeDuplicationConfigs.RECAPTURE_TEN_REPORT -> {
                        inputStream = reportService.generateSummaryReport(DeDuplicationConfigs.RECAPTURE_TEN);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_TEN_REPORT + "_SUMMARY" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                    /*case DeDuplicationConfigs.RECAPTURE_THREE_DUPLICATE_CHECK_REPORT -> {
                        inputStream = reportService.generateSummaryReport(DeDuplicationConfigs.RECAPTURE_THREE_DUPLICATE_CHECK);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_THREE_DUPLICATE_CHECK_REPORT + "_SUMMARY" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }*/
                }
            }
            case DeDuplicationConfigs.REPORT_LEVEL_DETAIL -> {
                switch (reportType) {
                    case DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE_REPORT -> {
                        inputStream = reportService.generateDetailReport(DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE_REPORT + "_DETAIL" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                    case DeDuplicationConfigs.RECAPTURE_ONE_DUPLICATE_CHECK_REPORT -> {
                        inputStream = reportService.generateDetailReport(DeDuplicationConfigs.RECAPTURE_ONE_DUPLICATE_CHECK);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_ONE_DUPLICATE_CHECK_REPORT + "_DETAIL" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                    case DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE_REPORT -> {
                        inputStream = reportService.generateDetailReport(DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE_REPORT + "_DETAIL" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                    case DeDuplicationConfigs.RECAPTURE_TWO_DUPLICATE_CHECK_REPORT -> {
                        inputStream = reportService.generateDetailReport(DeDuplicationConfigs.RECAPTURE_TWO_DUPLICATE_CHECK);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_TWO_DUPLICATE_CHECK_REPORT + "_DETAIL" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                    case DeDuplicationConfigs.RECAPTURE_THREE_AND_TWO_REPORT -> {
                        inputStream = reportService.generateDetailReport(DeDuplicationConfigs.RECAPTURE_THREE_AND_TWO);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_THREE_AND_TWO_REPORT + "_DETAIL" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                    case DeDuplicationConfigs.RECAPTURE_THREE_DUPLICATE_CHECK_REPORT -> {
                        inputStream = reportService.generateDetailReport(DeDuplicationConfigs.RECAPTURE_THREE_DUPLICATE_CHECK);
                        // Create a Resource from the ByteArrayInputStream
                        InputStreamResource resource = new InputStreamResource(inputStream);
                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + DeDuplicationConfigs.RECAPTURE_THREE_DUPLICATE_CHECK_REPORT + "_DETAIL" + LocalDate.now() + ".csv");
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .headers(headers)
                                .body(resource);
                    }
                }
            }
            default -> {

            }
        }
        return null;
    }
}
