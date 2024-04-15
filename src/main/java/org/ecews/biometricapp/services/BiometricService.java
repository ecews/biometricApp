package org.ecews.biometricapp.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.Biometric;
import org.ecews.biometricapp.repositories.BiometricRepository;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class BiometricService {
    private final JdbcTemplate jdbcTemplate;

    private final BiometricRepository biometricRepository;

    public List<Biometric> getNoMatchFingerprints (Long recapture, String deduplicationType) {
        return biometricRepository.getNoMatchFingerprints(recapture, deduplicationType);
    }

    public List<Biometric> getFingerprints(Long recapture, String deduplicationType){
        return biometricRepository.getFingerprints(recapture, deduplicationType);
    }

    public List<Biometric> filterBiometricByRecapture (List<Biometric> biometrics, Integer recapture) {
        return biometrics.stream()
                .filter(biometric -> biometric.getRecapture() != null && biometric.getRecapture().equals(recapture))
                .collect(Collectors.toList());
    }

    public Map<String, List<Biometric>> groupFingerprints (List<Biometric> biometricList1, List<Biometric> biometricList2) {
        var groupedBiometrics = biometricList1.stream()
                .collect(Collectors.groupingBy(Biometric::getPersonUuid));

        biometricList2.forEach(biometric -> groupedBiometrics.merge(biometric.getPersonUuid(), List.of(biometric),
                (baselineList, recaptureList) -> {
                    baselineList.addAll(recaptureList);
                    return baselineList;
                }));
        return groupedBiometrics;
    }

    public Map<String, List<Biometric>> filterClientWithoutBiometric (Map<String, List<Biometric>> groupedBiometrics, List<Biometric> biometrics) {
        // Filter baseline biometrics that do not have a corresponding recapture biometric
        List<String> recapturePersonUuids = biometrics.stream()
                .map(Biometric::getPersonUuid)
                .toList();
        // Removing fingerprints of client that do not have recapture fingerprints
        groupedBiometrics.keySet().removeIf(personUuid -> !recapturePersonUuids.contains(personUuid));

        return groupedBiometrics;
    }
}
