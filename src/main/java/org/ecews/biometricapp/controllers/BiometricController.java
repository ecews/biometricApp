package org.ecews.biometricapp.controllers;

import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.services.NService;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class BiometricController {

    @Autowired
    NService nService;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/deduplication")
    public String deduplication() {
        return "deduplication";
    }

    @GetMapping("/recapture-status")
    public String recaptureStatus() {
        return "recapture-status";
    }

    @GetMapping("/run-deduplication")
    public String runDeduplication(@RequestParam("deduplicationType") String deduplicationType, Model model) {

        if(deduplicationType.equals(DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE)) {
            nService.recaptureOneAndBaseline(DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE);
            model.addAttribute("successMessage", "Done running recapture one baseline deduplication");
            return "deduplication";
        } else if (deduplicationType.equals(DeDuplicationConfigs.RECAPTURE_ONE_DUPLICATE_CHECK)) {
            nService.recaptureOneDuplicateCheck(DeDuplicationConfigs.RECAPTURE_ONE_DUPLICATE_CHECK);
            model.addAttribute("successMessage", "Done running recapture one duplicate check deduplication");
            return "deduplication";
        } else {
            model.addAttribute("errorMessage", "No deduplication type selected");
            return "deduplication";
        }

    }
}
