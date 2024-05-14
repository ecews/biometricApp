package org.ecews.biometricapp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.Biometric;
import org.ecews.biometricapp.entities.dtos.BiometricFullDTO;
import org.ecews.biometricapp.entities.dtos.RecreateTemplateDTO;
import org.ecews.biometricapp.repositories.BiometricRepository;
import org.ecews.biometricapp.repositories.projections.FingerCountProjection;
import org.ecews.biometricapp.repositories.queries.SetupQueries;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.springframework.beans.BeanUtils;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class BiometricService {
    private final JdbcTemplate jdbcTemplate;
    private final BiometricRepository biometricRepository;

    public Optional<Biometric> getPersonRecaptureTemplate (String personUuid, String templateType, Integer recapture) {
        return biometricRepository.getPersonRecaptureTemplate(personUuid, templateType, recapture);
    }
    public Set<String> getClientForIntervention(String deduplicationType, Double percentage, LocalDate dateOfDeduplication) {
        return biometricRepository.getClientForIntervention(deduplicationType, percentage, dateOfDeduplication);
    }

    public List<FingerCountProjection> getFingerCountForDate(LocalDate dateOfDeduplication, String deduplicationType) {
        return biometricRepository.getFingerCountForDate(dateOfDeduplication, deduplicationType);
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

    @SneakyThrows
    public List<RecreateTemplateDTO> readDTOsFromFile(MultipartFile file) throws Exception {
        List<RecreateTemplateDTO> dtos = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean skipFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (skipFirstLine) {
                    skipFirstLine = false; // Set the flag to false after skipping the first line
                    continue; // Skip the first line
                }
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    // Handle invalid line
                    continue;
                }
                String person = parts[0].trim();
                String encounterDateString = parts[1].trim();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate encounterDate = null;
                try {
                    encounterDate = LocalDate.parse(encounterDateString, formatter);
                } catch (Exception e) {
                    encounterDate = LocalDate.now();
                }
                log.info("String Date is ************************** {}", encounterDate);

                int use = Integer.parseInt(parts[2].trim());
                int create = Integer.parseInt(parts[3].trim());

                RecreateTemplateDTO dto = new RecreateTemplateDTO();
                dto.setPersonUuid(person);
                dto.setDateOfEnrollment(encounterDate);
                dto.setUse(use);
                dto.setCreate(create);

                dtos.add(dto);
            }
        }
        log.error("Length of DTO ******* {}", dtos.size());

        return dtos;
    }

    @SneakyThrows
    public ByteArrayOutputStream downloadFile(List<RecreateTemplateDTO> dtos) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        var biometrics = new ArrayList<BiometricFullDTO>();
        dtos.stream().parallel().forEach(d-> {
            Set<Integer> recapture = new HashSet<>();
            recapture.add(d.getCreate());
            var biometric = getPersonBiometrics(d.getPersonUuid(), recapture).stream().map(m-> {
                BiometricFullDTO biometricFullDTO = new BiometricFullDTO();
                BeanUtils.copyProperties(m, biometricFullDTO);
                biometricFullDTO.setTemplate(Base64.getEncoder().encodeToString(m.getTemplate()));
                return biometricFullDTO;
            }).toList();
            biometrics.addAll(biometric);
        });
        var json = objectMapper.writeValueAsString(biometrics);
        byte[] jsonBytes = json.getBytes();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(jsonBytes);
        return outputStream;
    }

    public void uploadTemplate(List<BiometricFullDTO> dtos) {
        dtos.stream().parallel()
                .forEach(d -> {
                    Biometric biometric = new Biometric();
                    BeanUtils.copyProperties(d, biometric);
                    biometric.setTemplate(Base64.getDecoder().decode(d.getTemplate()));
                    biometricRepository.save(biometric);
                });
    }

    @PostConstruct
    private void runSetupQueries() {
        jdbcTemplate.execute(SetupQueries.CLEAN_BIOMETRIC_RECORD);
        log.info("Done executing cleanup queries ***** ");
    }
}
