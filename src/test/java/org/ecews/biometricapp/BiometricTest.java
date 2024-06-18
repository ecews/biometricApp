package org.ecews.biometricapp;

import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.Biometric;
import org.ecews.biometricapp.services.BiometricService;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@SpringBootTest
@Slf4j
public class BiometricTest {
    @Autowired
    BiometricService biometricService;

    @Value("${intervention.enabled:false}")
    private boolean interventionEnabled;

    @Value("${intervention.deduplication:false}")
    private boolean interventionDeduplication;

    @Value("${percentage:0.9}")
    private double percentage;

    @Test
    public void testGetNoMatchFingerprints () {
        List<Biometric> biometricList = biometricService.getNoMatchFingerprints(1L, DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE);
        log.info("LIST SIZE IS ***** {} ", biometricList.size());
    }

    @Test
    public void testGetFingerprints (){
        List<Biometric> biometricList = biometricService.getFingerprints(0L, DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE);
        log.info("LIST SIZE IS ***** {} ", biometricList.size());
    }

    @Test
    public void testGroupFingerprints () {
        List<Biometric> biometricList2 = biometricService.getNoMatchFingerprints(1L, DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE);
        List<Biometric> biometricList1 = biometricService.getFingerprints(0L, DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE);
        var map = biometricService.groupFingerprints(biometricList1, biometricList2);
        var filter = biometricService.filterClientWithoutBiometric(map, biometricList2);
        log.info("Data is ****** {}", map.size());
    }

    @Test
    public void testGetClientForIntervention() {
        Set<String> strings = biometricService.getClientForIntervention("RECAPTURE_TWO_AND_ONE", 1.0, LocalDate.now());
        if(strings.contains("d6b206a8-c6ee-4538-bbe9-9f099b6eacb9")) {
            log.info("I found the patient ****** ");
        }
        log.info("Size of strings is ***** {}", strings.size());
    }

    @Test
    public void getClientPrintsFOrIntervention() {
        Set<String> strings = biometricService.getClientForIntervention("RECAPTURE_ONE_AND_BASELINE", 0.85, LocalDate.now());
        Set<Integer> recaptures = new HashSet<>();
        recaptures.add(0);
        recaptures.add(1);
        var biometrics = biometricService.getClientPrintsForIntervention(strings, recaptures);
        log.info("BIOMETRIC SIZE ******** {}", biometrics.size());
    }

    @Test
    public void getInterventionPrintsForDeduplication() {
        Set<Integer> recaptures = new HashSet<>();
        recaptures.add(0);
        recaptures.add(2);
        var clients = biometricService.getInterventionPrintsForDeduplication(LocalDate.now(), recaptures, "RECAPTURE_ONE_AND_BASELINE");
        log.info("BIOMETRIC SIZE ******** {}", clients.size());
    }

    @Test
    public void testGetAllBiometrics () {
        String start = "2024-01-01";
        String end = "2024-01-31";

        var bios = biometricService.getAllBiometrics(1, LocalDate.parse(start), LocalDate.parse(end));
        log.info("Biometric size = {}", bios.size());
    }

    @Test
    public void getIdsForNDR(){
        String start = "2024-01-01";
        String end = "2024-01-31";
        var bios = biometricService.getIdsForNDR(LocalDate.parse(start), LocalDate.parse(end));
        log.info("Biometric size = {}", bios.size());
    }

    @Test
    public void testApplicationPV(){
        log.info("Enabled *************** {}", interventionEnabled);
        log.info("Deduplication *************** {}", interventionDeduplication);
        log.info("Percentage *************** {}", percentage);
    }
}
