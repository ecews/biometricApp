package org.ecews.biometricapp;

import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.services.NService;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class NServiceTest {

    @Autowired
    NService nService;
    @Test
    public void testRecaptureOneAndBaseline (){
        nService.recaptureOneAndBaseline(DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE);
        log.info("Recapture deduplication with baseline ****** ");
    }

    @Test
    public void testRecaptureOneDuplicateCheck () {
        nService.recaptureOneDuplicateCheck(DeDuplicationConfigs.RECAPTURE_ONE_DUPLICATE_CHECK);
        log.info("Done recapture on duplicate check ****** ");
    }
}
