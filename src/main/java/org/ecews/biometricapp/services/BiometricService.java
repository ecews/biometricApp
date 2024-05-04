package org.ecews.biometricapp.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.Biometric;
import org.ecews.biometricapp.repositories.BiometricRepository;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class BiometricService {
    private final JdbcTemplate jdbcTemplate;

    private final BiometricRepository biometricRepository;

    public Set<String> getClientForIntervention(String deduplicationType, Double percentage) {
        return biometricRepository.getClientForIntervention(deduplicationType, percentage);
    }

    public List<Biometric> getPersonBiometrics(String personUuid, Set<Integer> recaptures) {
        return biometricRepository.getPersonBiometrics(personUuid, recaptures);
    }
    public List<Biometric> getInterventionPrintsForDeduplication (LocalDate backupDate, Set<Integer> recaptures, String deduplicationType){
        return biometricRepository.getInterventionPrintsForDeduplication(backupDate, recaptures, deduplicationType);
    }

    public void updateBiometric(String id, byte[] template, String hashed) {
        biometricRepository.updateBiometric(id, template, hashed);
    }
    public List<Biometric> getClientPrintsForIntervention(Set<String> clients, Set<Integer> recaptures){
        return biometricRepository.getClientPrintsForIntervention(clients, recaptures);
    }

    public List<Biometric> getNoMatchFingerprints (Long recapture, String deduplicationType) {
        return biometricRepository.getNoMatchFingerprints(recapture, deduplicationType);
    }

    public List<Biometric> getFingerprints(Long recapture, String deduplicationType){
        return biometricRepository.getFingerprints(recapture, deduplicationType);
    }

    public List<Biometric> getFingerprints(Long recapture){
        return biometricRepository.getFingerprints(recapture);
    }


    public List<Biometric> getFingerprintsByNDRStatus(Long recapture, String status){
        return biometricRepository.getFingerprintsByNDRStatus(recapture, status);
    }

    public List<Biometric> getMatchedFingerprints(Long recapture, String deduplicationType){
        return biometricRepository.getMatchedFingerprints(recapture, deduplicationType);
    }

    public List<Biometric> filterBiometricByRecapture (List<Biometric> biometrics, Integer recapture) {
        return biometrics.stream()
                .filter(biometric -> biometric.getRecapture() != null && biometric.getRecapture() <= recapture)
                .collect(Collectors.toList());
    }

    public List<Biometric> filterAllBiometricByRecapture (List<Biometric> biometrics, Integer recapture) {
        return biometrics.stream()
                .filter(biometric -> biometric.getRecapture() != null && biometric.getRecapture() <= recapture)
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

    public List<Biometric> removeBiometrics(List<Biometric> biometric1, List<Biometric> biometric2) {
        List<String> personUuidsToRemove = biometric2.stream()
                .map(Biometric::getPersonUuid)
                .toList();

        return biometric1.stream()
                .filter(biometric -> !personUuidsToRemove.contains(biometric.getPersonUuid()))
                .collect(Collectors.toList());
    }
}
