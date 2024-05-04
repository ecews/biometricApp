package org.ecews.biometricapp;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.ecews.biometricapp.entities.PatientInfo;
import org.ecews.biometricapp.services.BackupBiometricService;
import org.ecews.biometricapp.services.PatientInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
class BiometricAppApplicationTests {

    @Autowired
    PatientInfoService patientInfoService;
    @Autowired
    BackupBiometricService backupBiometricService;
    @Test
    void contextLoads() {
    }

    @Test
    void testGetPatient() {
        List<PatientInfo> patientInfo = patientInfoService.getPatientInfo("AKS/003/1881");
        log.info("Patient details ****** {}", patientInfo.size());
    }

    @Test
    void testGetMaxLevel () {
        var level = backupBiometricService.getMaxLevel("eae3eb4a-b1c1-477a-9be0-9971d019a3f4");
        log.info("Max level ****** {}", level);
    }

}
