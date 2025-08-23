package org.ecews.biometricapp.services;

import com.neurotec.biometrics.NFRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.ecews.biometricapp.entities.Biometric;
import org.ecews.biometricapp.entities.MPosition;
import org.ecews.biometricapp.entities.dtos.RecreateTemplateDTO;
import org.ecews.biometricapp.repositories.BiometricRepository;
import org.ecews.biometricapp.repositories.MPositionRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.swing.text.Position;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class CreateTemplateService {
    private final BiometricService biometricService;
    private final NService nService;
    private final NInterventionService nInterventionService;
    private final MPositionRepository mPositionRepository;
    private final BiometricRepository biometricRepository;

    public CreateTemplateService(BiometricService biometricService, NService nService, NInterventionService nInterventionService, MPositionRepository mPositionRepository, BiometricRepository biometricRepository) {
        this.biometricService = biometricService;
        this.nService = nService;
        this.nInterventionService = nInterventionService;
        this.mPositionRepository = mPositionRepository;
        this.biometricRepository = biometricRepository;
    }

    public void recreateTemplate(RecreateTemplateDTO recreateTemplateDTO) {
        Set<Integer> recaptures = new HashSet<>();
        log.info("Create ******** {}", recreateTemplateDTO);
        recaptures.add(recreateTemplateDTO.getUse());
        List<Biometric> biometrics = biometricService.getPersonBiometrics(recreateTemplateDTO.personUuid, recaptures);
        LocalDate date = recreateTemplateDTO.getDateOfEnrollment();
        LocalDateTime localDateTime = getRandomDateTimeWithinDay(date);
        AtomicLong nano = new AtomicLong();
        log.info("Biometric size ****** {}", biometrics.size());
        if (biometrics.size() > 5) {
            biometrics.forEach(b -> {
                Optional<Biometric> personBiometric = biometricService.getPersonRecaptureTemplate(recreateTemplateDTO.getPersonUuid(), b.getTemplateType(), recreateTemplateDTO.getCreate());
                personBiometric.ifPresentOrElse(pb -> {
                    // log.info("Template {} for {} already exist with recapture count {}", b.getTemplateType(), recreateTemplateDTO.getPersonUuid(), recreateTemplateDTO.getCreate());
                }, () -> {
                    Biometric biometric = new Biometric();
                    BeanUtils.copyProperties(b, biometric);
                    String uuid = UUID.randomUUID().toString();
                    biometric.setId(uuid);
                    biometric.setDate(date);
                    biometric.setRecapture(recreateTemplateDTO.getCreate());
                    // Create a new print from the older one
                    byte[] template = biometric.getTemplate();
                    // log.info("Template length ******* {}", template.length);

                    if(template.length > 24) {
                        template[25] = 0x00;
                        NFRecord record = nInterventionService.convertTemplateToNFRecord(template);
                        if(record != null) {
                            int size = record.getMinutiae().size();
                            assert biometric.getId() != null;
                            Optional<MPosition> position = mPositionRepository.findById(biometric.getId());
                            int[] indexes = {};
                            if(position.isPresent()){
                                indexes = nInterventionService.convertStringToArray(position.get().getMIndex());
                            }
                            int index = nInterventionService.getIndex(size, indexes);
                            byte[] newTemplate = nInterventionService.createNewTemplate(index, record);

                            indexes = ArrayUtils.add(indexes, index);

                            biometric.setTemplate(newTemplate);
                            biometric.setHashed(nInterventionService.bcryptHash(newTemplate));
                            biometric.setImageQuality((int) record.getQuality());

                            biometric.setCreatedDate(localDateTime.plusNanos(nano.get()));
                            biometric.setLastModifiedDate(localDateTime.plusNanos(nano.get()));
                            biometric.setCreatedBy("ecewsACE5");
                            biometric.setLastModifiedBy("ecewsACE5");

                            // Save newly created template
                            biometricRepository.save(biometric);
                            log.info("Done saving template *****");
                            // Saving position
                            MPosition p = new MPosition();
                            assert b.getId() != null;
                            p.setId(String.valueOf(UUID.fromString(b.getId())));
                            p.setPersonUuid(biometric.getPersonUuid());
                            // p.setRecapture(biometric.getRecapture());
                            p.setMIndex(Arrays.toString(indexes));
                            mPositionRepository.save(p);

                            nano.set(nano.get() + 3);
                        } else {
                            log.error("Record created is null  ****** ");
                        }
                    } else {
                        log.error("Template length is lesser than 25 ***** ");
                    }
                });

            });
        }else {
            log.error("Patient has less than six for this fingerprints set ");
        }
        log.info("Out of loop, done ****** ");

    }

    public static LocalDateTime getRandomDateTimeWithinDay(LocalDate date) {
        // Define the range of hours (from 9:00 AM to 5:00 PM)
        int minHour = 9;
        int maxHour = 17;

        // Generate random values for hour, minute, second, and nanosecond within the specified range
        int hour = ThreadLocalRandom.current().nextInt(minHour, maxHour);
        int minute = ThreadLocalRandom.current().nextInt(0, 60);
        int second = ThreadLocalRandom.current().nextInt(0, 60);
        int nano = ThreadLocalRandom.current().nextInt(0, 1_000_000_000);

        // Construct and return the random LocalDateTime within the given date
        return LocalDateTime.of(date, java.time.LocalTime.of(hour, minute, second, nano));
    }
}
