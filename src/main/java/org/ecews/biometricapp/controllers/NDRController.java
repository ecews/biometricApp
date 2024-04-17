package org.ecews.biometricapp.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.services.RecaptureBiometricService;
import org.ecews.biometricapp.services.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
@Slf4j
public class NDRController {
    private  final RecaptureBiometricService biometricService;
    private final ReportService reportService;
    @GetMapping("/generate_xml")
    public String generate(Model model) {
        model.addAttribute("facilities", reportService.getDistinctFacilities());
        return "generate-xml";
    }

    @GetMapping("/download_xml")
    public String download(Model model) {
        model.addAttribute("files", biometricService.getNdrStatus());
        return "download-xml";
    }
}
