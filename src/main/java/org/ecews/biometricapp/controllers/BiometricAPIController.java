package org.ecews.biometricapp.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.Biometric;
import org.ecews.biometricapp.entities.PatientInfo;
import org.ecews.biometricapp.entities.RecaptureStatus;
import org.ecews.biometricapp.entities.dtos.BiometricFullDTO;
import org.ecews.biometricapp.entities.dtos.CapturedBiometricDto;
import org.ecews.biometricapp.entities.dtos.Device;
import org.ecews.biometricapp.entities.dtos.ExportFileDTO;
import org.ecews.biometricapp.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
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
    private final BiometricService biometricService;
    private final CreateTemplateService createTemplateService;
    private final ExportFileService exportFileService;
    private final RecaptureStatusService recaptureStatusService;

    public BiometricAPIController(NService nService, NInterventionService nInterventionService, PatientInfoService patientInfoService,
                                  BackupBiometricService backupBiometricService, BiometricService biometricService,
                                  CreateTemplateService createTemplateService, ExportFileService exportFileService, RecaptureStatusService recaptureStatusService) {
        this.nService = nService;
        this.nInterventionService = nInterventionService;
        this.patientInfoService = patientInfoService;
        this.backupBiometricService = backupBiometricService;
        this.biometricService = biometricService;
        this.createTemplateService = createTemplateService;
        this.exportFileService = exportFileService;
        this.recaptureStatusService = recaptureStatusService;
    }

    @GetMapping("/patient")
    public List<PatientInfo> getPatientInfo (@RequestParam String search) {
        return patientInfoService.getPatientInfo(search);
    }

    @SneakyThrows
    @GetMapping("/export-file/{fileName}")
    public void exportFile(@PathVariable("fileName") String fileName, HttpServletResponse response) {

        var baos = exportFileService.generateExportFile();
        response.setHeader ("Content-Type", "application/octet-stream");
        response.setHeader ("Content-Disposition", "attachment;filename=" + fileName + ".json");
        response.setHeader ("Content-Length", Integer.toString (baos.size ()));
        OutputStream outputStream = response.getOutputStream ();
        outputStream.write (baos.toByteArray ());
        outputStream.close ();
        response.flushBuffer ();
    }

    @PostMapping("/ingest-files")
    public ResponseEntity<String> ingestFiles (@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            var dtos = objectMapper.readValue(file.getInputStream(), new TypeReference<ExportFileDTO>() {});
            exportFileService.ingestExportFile(dtos);
            // log.info("DTOS is count {}", dtos.size());
            return new ResponseEntity<>("Done recreating templates ....", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SneakyThrows
    @PostMapping("/create-template")
    public String createTemplate(@RequestParam("file") MultipartFile file) {

        var dtos = biometricService.readDTOsFromFile(file);
        log.info("DTOS is count {}", dtos.size());
        try {
            dtos.stream().parallel().forEach(createTemplateService::recreateTemplate);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("An error occur ************* {}", e.getMessage());
        }
        log.info("Done creating prints **** ");
        return "Done recreating templates ....";
    }

    @PostMapping("/recapture-status")
    public ResponseEntity<String> recaptureStatus(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<RecaptureStatus> recaptureStatusList = new CsvToBeanBuilder<RecaptureStatus>(reader)
                    .withType(RecaptureStatus.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSkipLines(1)
                    .build()
                    .parse();
            recaptureStatusService.saveStatus(recaptureStatusList);
            return new ResponseEntity<>("Done recreating templates ....", HttpStatus.OK);
            // return new ResponseEntity<>(recaptureStatusList, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/upload-template")
    public ResponseEntity<String> uploadTemplate (@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            var dtos = objectMapper.readValue(file.getInputStream(), new TypeReference<List<BiometricFullDTO>>() {});
            biometricService.uploadTemplate(dtos);
            log.info("DTOS is count {}", dtos.size());
            return new ResponseEntity<>("Done recreating templates ....", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SneakyThrows
    @PostMapping("/download-template/{fileName}")
    public void downloadTemplate(@RequestParam("file") MultipartFile file, @PathVariable("fileName") String fileName, HttpServletResponse response) {

        var dtos = biometricService.readDTOsFromFile(file);
        var baos = biometricService.downloadFile (dtos);
        response.setHeader ("Content-Type", "application/octet-stream");
        response.setHeader ("Content-Disposition", "attachment;filename=" + fileName + ".json");
        response.setHeader ("Content-Length", Integer.toString (baos.size ()));
        OutputStream outputStream = response.getOutputStream ();
        outputStream.write (baos.toByteArray ());
        outputStream.close ();
        response.flushBuffer ();
    }

    @GetMapping("/download/{file}")
    public void downloadFile(@PathVariable String file, HttpServletResponse response) throws IOException {

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

    @GetMapping("/intervention/{type}/{percentage}/{base}/{recapture}/{deduplicationDate}")
    public String intervention(
            @PathVariable("type") String type, @PathVariable("percentage") Double percentage,
            @PathVariable("base") Integer base, @PathVariable("recapture") Integer recapture,
            @PathVariable("deduplicationDate") LocalDate deduplicationDate
    ) {
        nInterventionService.doIntervention(type, percentage, base, recapture, Boolean.TRUE, deduplicationDate);
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
