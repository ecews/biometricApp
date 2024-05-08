package org.ecews.biometricapp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.Biometric;
import org.ecews.biometricapp.entities.IdentificationResponse;
import org.ecews.biometricapp.entities.SysBackup;
import org.ecews.biometricapp.entities.dtos.BiometricFullDTO;
import org.ecews.biometricapp.entities.dtos.ExportFileDTO;
import org.ecews.biometricapp.repositories.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;

@Service
@Slf4j
@AllArgsConstructor
public class ExportFileService {
    private final MPositionRepository mPositionRepository;
    private final InterventionResponseRepository interventionResponseRepository;
    private final IdentificationResponseRepository identificationResponseRepository;
    private final RecaptureStatusRepository recaptureStatusRepository;
    private final SysBackupRepository sysBackupRepository;

    @SneakyThrows
    public ByteArrayOutputStream generateExportFile() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        var exportFile = new ExportFileDTO();

        var positions = mPositionRepository.findAll();
        exportFile.setMPositionList(positions);

        var identifications = identificationResponseRepository.findAll();
        exportFile.setIdentificationResponses(identifications);

        var interventions = interventionResponseRepository.findAll();
        exportFile.setInterventionResponses(interventions);

        var recaptureStatus = recaptureStatusRepository.findAll();
        exportFile.setRecaptureStatuses(recaptureStatus);

        var sysBackup = sysBackupRepository.findAll();
        var biometric = sysBackup.stream().map(m-> {
            BiometricFullDTO biometricFullDTO = new BiometricFullDTO();
            BeanUtils.copyProperties(m, biometricFullDTO);
            biometricFullDTO.setTemplate(Base64.getEncoder().encodeToString(m.getTemplate()));
            return biometricFullDTO;
        }).toList();
        exportFile.setSysBackups(biometric);

        var json = objectMapper.writeValueAsString(exportFile);
        byte[] jsonBytes = json.getBytes();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(jsonBytes);
        return outputStream;
    }
    public void ingestExportFile(ExportFileDTO exportFileDTO){
        mPositionRepository.saveAll(exportFileDTO.getMPositionList());
        identificationResponseRepository.saveAll(exportFileDTO.getIdentificationResponses());
        interventionResponseRepository.saveAll(exportFileDTO.getInterventionResponses());
        recaptureStatusRepository.saveAll(exportFileDTO.getRecaptureStatuses());
        exportFileDTO.getSysBackups().stream().parallel()
                .forEach(d -> {
                    SysBackup biometric = new SysBackup();
                    BeanUtils.copyProperties(d, biometric);
                    biometric.setTemplate(Base64.getDecoder().decode(d.getTemplate()));
                    sysBackupRepository.save(biometric);
                });
    }
}
