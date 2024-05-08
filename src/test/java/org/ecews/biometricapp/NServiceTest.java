package org.ecews.biometricapp;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.dtos.Device;
import org.ecews.biometricapp.services.NInterventionService;
import org.ecews.biometricapp.services.NService;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
@Slf4j
public class NServiceTest {

    @Autowired
    NService nService;
    @Autowired
    NInterventionService nInterventionService;

    @Test
    public void testRecaptureOneAndBaseline (){
        nService.recaptureOneAndBaseline(DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE, LocalDate.now());
        log.info("Recapture deduplication with baseline ****** ");
    }

    @Test
    public void testRecaptureOneDuplicateCheck () {
        nService.recaptureOneDuplicateCheck(DeDuplicationConfigs.RECAPTURE_ONE_DUPLICATE_CHECK);
        log.info("Done recapture on duplicate check ****** ");
    }

    @Test
    public void testDoIntervention() {
        nInterventionService.doIntervention(DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE, 0.85, 0, 1,Boolean.FALSE,  LocalDate.now());
    }

    @Test
    public void testGeDevice () {
       var devices = nService.getReaders();
       log.info("Device length ****** {}", devices);
    }
}
