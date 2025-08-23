package org.ecews.biometricapp.services;

import org.ecews.biometricapp.entities.BackupBiometric;
import org.ecews.biometricapp.entities.dtos.CapturedBiometricDto;
import org.ecews.biometricapp.repositories.BackupBiometricRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BackupBiometricService {

    private final BackupBiometricRepository biometricRepository;

    public BackupBiometricService(BackupBiometricRepository biometricRepository) {
        this.biometricRepository = biometricRepository;
    }

    public Integer getMaxLevel (String personUuid) {
        var bb = biometricRepository.findByPersonUuid(personUuid);
        var maxLevel = bb.stream()
                .map(BackupBiometric::getLevel)
                .max(Integer::compareTo);

        return maxLevel.orElse(0);
    }

    public void saveBackupBiometric (List<CapturedBiometricDto> capturedBiometricDtoList, String personUuid) {
        var maxLevel = getMaxLevel(personUuid);
        var backup = capturedBiometricDtoList.stream().map(m -> {
           var bb = new BackupBiometric();
           bb.setId(m.getId());
           bb.setDateCreated(LocalDate.now());
           // bb.setBase64Image(m.getBase64Image());
           bb.setHashed(m.getHashed());
           bb.setImageQuality(m.getImageQuality());
           bb.setTemplate(m.getTemplate());
           bb.setTemplateType(m.getTemplateType());
           bb.setPersonUuid(personUuid);
           bb.setUsed(Boolean.FALSE);
           bb.setLevel(maxLevel + 1);
           bb.setDevice(m.getDevice());
            return bb;
        }).toList();
        biometricRepository.saveAll(backup);
    }
}
