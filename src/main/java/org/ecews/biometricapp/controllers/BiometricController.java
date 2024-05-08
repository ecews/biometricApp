package org.ecews.biometricapp.controllers;

import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.services.NService;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@Slf4j
public class BiometricController {

    @Autowired
    NService nService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("devices", nService.getReaders());
        return "home";
    }

    @GetMapping("/deduplication")
    public String deduplication() {
        return "deduplication";
    }

    @GetMapping("/import-export")
    public String recaptureStatus() {
        return "recapture-status";
    }

    @GetMapping("/capturing")
    public String capturing (Model model) {
        model.addAttribute("devices", nService.getReaders());
        return "capturing";
    }

    @GetMapping("/create-template")
    public String createTemplate (Model model) {
        return "create-template";
    }

    @GetMapping("/run-deduplication")
    public String runDeduplication(@RequestParam("deduplicationType") String deduplicationType, Model model) {

        switch (deduplicationType) {
            case DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE -> {
                nService.recaptureOneAndBaseline(DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE, LocalDate.now());
                model.addAttribute("successMessage", "Done running recapture one baseline deduplication");
                return "deduplication";
            }
            case DeDuplicationConfigs.RECAPTURE_ONE_DUPLICATE_CHECK -> {
                nService.recaptureOneDuplicateCheck(DeDuplicationConfigs.RECAPTURE_ONE_DUPLICATE_CHECK);
                model.addAttribute("successMessage", "Done running recapture one duplicate check deduplication");
                return "deduplication";
            }
            case DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE -> {
                nService.recaptureTwoAndRecaptureOne(DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE, LocalDate.now());
                model.addAttribute("successMessage", "Done running recapture two and one deduplication");
                return "deduplication";
            }
            case DeDuplicationConfigs.RECAPTURE_THREE_AND_TWO -> {
                nService.recaptureThreeAndRecaptureTwo(DeDuplicationConfigs.RECAPTURE_THREE_AND_TWO, LocalDate.now());
                model.addAttribute("successMessage", "Done running recapture three and two deduplication");
                return "deduplication";
            }
            case DeDuplicationConfigs.RECAPTURE_TWO_DUPLICATE_CHECK -> {
                nService.recaptureTwoDuplicateCheck(DeDuplicationConfigs.RECAPTURE_TWO_DUPLICATE_CHECK);
                model.addAttribute("successMessage", "Done running recapture two duplicate check");
                return "deduplication";
            }
            case DeDuplicationConfigs.RECAPTURE_THREE_DUPLICATE_CHECK -> {
                nService.recaptureThreeDuplicateCheck(DeDuplicationConfigs.RECAPTURE_THREE_DUPLICATE_CHECK);
                model.addAttribute("successMessage", "Done running recapture three duplicate check");
                return "deduplication";
            }
            default -> {
                model.addAttribute("errorMessage", "No deduplication type selected");
                return "deduplication";
            }
        }

    }
}
