package org.ecews.biometricapp.controllers;

import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFPosition;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.standards.CBEFFBDBFormatIdentifiers;
import com.neurotec.biometrics.standards.CBEFFBiometricOrganizations;
import com.neurotec.biometrics.standards.FMRecord;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NFScanner;
import com.neurotec.images.NImage;
import com.neurotec.io.NBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.PatientInfo;
import org.ecews.biometricapp.entities.dtos.CapturedBiometricDto;
import org.ecews.biometricapp.entities.dtos.Device;
import org.ecews.biometricapp.services.BackupBiometricService;
import org.ecews.biometricapp.services.NInterventionService;
import org.ecews.biometricapp.services.NService;
import org.ecews.biometricapp.services.PatientInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@RestController
@Slf4j
@RequestMapping("api/v1/biometric")
public class BiometricAPIController {
    private final NService nService;
    private final NInterventionService nInterventionService;
    private final PatientInfoService patientInfoService;
    private final BackupBiometricService backupBiometricService;

    public BiometricAPIController(NService nService, NInterventionService nInterventionService, PatientInfoService patientInfoService, BackupBiometricService backupBiometricService) {
        this.nService = nService;
        this.nInterventionService = nInterventionService;
        this.patientInfoService = patientInfoService;
        this.backupBiometricService = backupBiometricService;
    }

    @GetMapping("/patient")
    public List<PatientInfo> getPatientInfo (@RequestParam String search) {
        return patientInfoService.getPatientInfo(search);
    }

    @GetMapping("/devices")
    public List<Device> getReaders() {
        return nService.getReaders();
    }

    @GetMapping("/backup-level")
    public ResponseEntity<?> getBackupLevel (@RequestParam String personUuid) {
        var level = backupBiometricService.getMaxLevel(personUuid);
        log.info("Level is ****** {}", level);
        return new ResponseEntity<>("Capturing backup level " + (level + 1), HttpStatus.OK);
    }

    @PostMapping ( "/save-capture")
    public ResponseEntity<?> saveCapture (@RequestParam String personUuid,
                                          @RequestBody List<CapturedBiometricDto> capturedBiometricDtoList) {
        try {
            backupBiometricService.saveBackupBiometric(capturedBiometricDtoList, personUuid);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping ( "/capture")
    public ResponseEntity<?> capture(
            @RequestParam String reader,
            @RequestParam String fingerType,
            @RequestBody List<CapturedBiometricDto> capturedBiometricDtoList
    ){
        log.info("I got her into capturing ... readr **** {}", reader);
        var client = nService.createNBiometricClient();
        client.setFingersCheckForDuplicatesWhenCapturing(true);
        short q = 60;
        client.setMatchingThreshold((byte)q);

        Map<String, Object> response = new HashMap<>();;

        try {
            reader = URLDecoder.decode(reader, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ignored) {
        }

        for (NDevice device : nService.getDevices()) {
            if (device.getDisplayName().equals(reader)) {
                client.setFingerScanner((NFScanner) device);
            }
        }

        try (NSubject subject = new NSubject()) {
            final NFinger finger = new NFinger();
            finger.setPosition(NFPosition.UNKNOWN);
            subject.getFingers().add(finger);

            if (nService.scannerIsNotSet(reader, client)) {
                response.put("ERROR", "Biometrics Scanner not found");
                response.put("status", HttpStatus.OK.value());
                return new ResponseEntity<>(response, HttpStatus.OK);
            }

            NBiometricStatus status = client.capture(subject);

            if (status.equals(NBiometricStatus.OK)) {
                status = client.createTemplate(subject);
                if (status.equals(NBiometricStatus.OK)) {
                    var capturedBiometricDto = new CapturedBiometricDto();
                    capturedBiometricDto.setTemplateType(fingerType);
                    capturedBiometricDto.setDevice(reader);
                    capturedBiometricDto.setId(UUID.randomUUID().toString());
                    // Converting template to ISO format
                    byte[] isoTemplate = subject.getTemplateBuffer(CBEFFBiometricOrganizations.ISO_IEC_JTC_1_SC_37_BIOMETRICS,
                            CBEFFBDBFormatIdentifiers.ISO_IEC_JTC_1_SC_37_BIOMETRICS_FINGER_MINUTIAE_RECORD_FORMAT,
                            FMRecord.VERSION_ISO_20).toByteArray();
                    capturedBiometricDto.setTemplate(isoTemplate);
                    capturedBiometricDto.setHashed(nInterventionService.bcryptHash(isoTemplate));
                    int imageQuality = subject.getFingers().getFirst().getObjects().getFirst().getQuality();
                    capturedBiometricDto.setImageQuality(imageQuality);

                    NImage image = subject.getFingers().getFirst().getImage();
                    NBuffer buffer = image.save();
                    byte[] array = buffer.toByteArray();
                    String encodeImage = Base64.getEncoder().withoutPadding().encodeToString(array);
                    capturedBiometricDto.setBase64Image("data:image/png;base64,".concat(encodeImage));

                    capturedBiometricDtoList.add(capturedBiometricDto);
                    return new ResponseEntity<>(capturedBiometricDtoList, HttpStatus.OK);
                } else {
                    log.info("Could not create template");
                }
            } else {
                log.info("Could not capture template");
            }

        } catch (Exception e) {
            log.error("Error creating subject ****** {}", e.getMessage());
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/intervention/{type}/{percentage}/{base}/{recapture}")
    public String intervention(
            @PathVariable("type") String type, @PathVariable("percentage") Double percentage,
            @PathVariable("base") Integer base, @PathVariable("recapture") Integer recapture
    ) {
        nInterventionService.doIntervention(type, percentage, base, recapture);
        return "Done with intervention ...";
    }

    @GetMapping("/intervention/deduplication/{backupDate}/{type}/{base}/{recapture}")
    public String interventionDeduplication(
            @PathVariable("backupDate") LocalDate backupDate,  @PathVariable("type") String type,
            @PathVariable("base") Integer base, @PathVariable("recapture") Integer recapture
    ) {
        nInterventionService.getInterventionClients(backupDate, base, recapture, type);
        return "Done with intervention deduplication ...";
    }

    @GetMapping("/verification")
    public ResponseEntity<?> verification (
            @RequestParam String personUuid, @RequestParam String reader,
            @RequestParam String recaptureType
    ){
        return nService.verifyPersonWithBiometric(personUuid, recaptureType, reader);
    }
}
