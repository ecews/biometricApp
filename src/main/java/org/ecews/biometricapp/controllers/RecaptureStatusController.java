package org.ecews.biometricapp.controllers;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.RecaptureStatus;
import org.ecews.biometricapp.services.RecaptureStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Controller
@Slf4j
public class RecaptureStatusController {

    @Autowired
    RecaptureStatusService recaptureStatusService;
    @PostMapping("/upload-recapture-status")
    @SneakyThrows
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (file.isEmpty()) {
            model.addAttribute("errorMessage", "FIle not selected, please select a file");
            return "recapture-status";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<RecaptureStatus> recaptureStatusList = new CsvToBeanBuilder<RecaptureStatus>(reader)
                    .withType(RecaptureStatus.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSkipLines(1)
                    .build()
                    .parse();
            recaptureStatusService.saveStatus(recaptureStatusList);
            model.addAttribute("successMessage", "Upload was successful");
            return "recapture-status";
            // return new ResponseEntity<>(recaptureStatusList, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/recapture-status";
            // return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // return "redirect:/recapture-status";
    }
}
