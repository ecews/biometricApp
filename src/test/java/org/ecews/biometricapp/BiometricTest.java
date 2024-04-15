package org.ecews.biometricapp;

import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.Biometric;
import org.ecews.biometricapp.services.BiometricService;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
public class BiometricTest {
    @Autowired
    BiometricService biometricService;

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
}
