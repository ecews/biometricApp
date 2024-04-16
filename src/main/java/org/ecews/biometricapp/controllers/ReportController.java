package org.ecews.biometricapp.controllers;

import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.services.ReportService;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Controller
@Slf4j
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public String reports() {
        return "reports";
    }

    @GetMapping(value = "/generate-reports", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> generateCSVReport(
            @RequestParam("reportType") String reportType, @RequestParam("reportLevel") String reportLevel, Model model
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
                    case DeDuplicationConfigs.RECAPTURE_ONE_DUPLICATE_CHECK_REPORT -> {
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
                    }
                    case DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE_REPORT -> {
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
                    case DeDuplicationConfigs.RECAPTURE_TWO_DUPLICATE_CHECK_REPORT -> {
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
                    case DeDuplicationConfigs.RECAPTURE_THREE_DUPLICATE_CHECK_REPORT -> {
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
                    }
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
