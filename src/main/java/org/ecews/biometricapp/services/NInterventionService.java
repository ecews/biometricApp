package org.ecews.biometricapp.services;

import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.standards.BDIFStandard;
import com.neurotec.biometrics.standards.FMRFingerView;
import com.neurotec.biometrics.standards.FMRecord;
import com.neurotec.io.NBuffer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.ecews.biometricapp.entities.*;
import org.ecews.biometricapp.entities.dtos.HandledResponse;
import org.ecews.biometricapp.entities.dtos.MatchedPair;
import org.ecews.biometricapp.repositories.MPositionRepository;
import org.ecews.biometricapp.repositories.SysBackupRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NInterventionService {

    private final BiometricService biometricService;
    private final MPositionRepository mPositionRepository;
    private final MPositionService mPositionService;
    private final SysBackupRepository sysBackupRepository;
    private final JdbcTemplate jdbcTemplate;
    private final InterventionResponseService interventionResponseService;

    @Value("${intervention.deduplication:false}")
    private Boolean interventionDeduplication;

    public NInterventionService(BiometricService biometricService, MPositionRepository mPositionRepository, MPositionService mPositionService, SysBackupRepository sysBackupRepository, JdbcTemplate jdbcTemplate, InterventionResponseService interventionResponseService) {
        this.biometricService = biometricService;
        this.mPositionRepository = mPositionRepository;
        this.mPositionService = mPositionService;
        this.sysBackupRepository = sysBackupRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.interventionResponseService = interventionResponseService;
    }

    public void doIntervention (String deduplicationType, Double percentage,
                                Integer base, Integer recapture,
                                Boolean isAPI, LocalDate dateOfDeduplication) {

        Set<Integer> recaptures = new HashSet<>();
        recaptures.add(base);
        recaptures.add(recapture);

        var clients = biometricService.getClientForIntervention(deduplicationType, percentage, dateOfDeduplication);

        if(isAPI) {
            var countBase = biometricService.getFingerCountForDate(dateOfDeduplication, deduplicationType);
            var countReplaced = sysBackupRepository.getFingerCountForDate(dateOfDeduplication);
            if (!countReplaced.isEmpty()) {
                countBase.stream().parallel()
                        .forEach(c -> {
                            countReplaced.forEach(ct -> {
                                if (c.getPersonUuid().equals(ct.getPersonUuid()) && Objects.equals(c.getFingerCount(), ct.getFingerCount())){
                                    clients.remove(c.getPersonUuid());
                                }
                            });
                        });
            }
        }

        var  biometrics = biometricService.getClientPrintsForIntervention(clients, recaptures);
        var groupBiometrics = biometrics.stream().collect(Collectors.groupingBy(Biometric::getPersonUuid));

        groupBiometrics.entrySet().stream()
                // removing duplicate keys
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existingValue, newValue) -> existingValue))
                .forEach((key, value) -> {
                    var baseBiometric = value.stream().filter(f -> Objects.equals(f.getRecapture(), base)).toList();
                    var recaptureBiometric = value.stream().filter(f -> Objects.equals(f.getRecapture(), recapture)).toList();
                    var replacedRecaptured = new ArrayList<Biometric>();
                    baseBiometric.forEach(bb -> {
                        log.info("Recapture count ****** {}", bb.getRecapture());
                        recaptureBiometric.forEach(rb -> {
                            if (StringUtils.equals(bb.getTemplateType().trim().toLowerCase(), rb.getTemplateType().trim().toLowerCase())) {
                                // log.info("Template position is equal ******");
                                byte[] template = bb.getTemplate();
                                if (template.length >= 25) {
                                    template[25] = 0x00;
                                    // Create a new print from the older one
                                    NFRecord record = convertTemplateToNFRecord(template);

                                    if (record != null) {
                                        int size = record.getMinutiae().size();
                                        assert bb.getId() != null;
                                        Optional<MPosition> position = mPositionRepository.findById(bb.getId());
                                        int[] indexes = {};
                                        if (position.isPresent()) {
                                            indexes = convertStringToArray(position.get().getMIndex());
                                        }
                                        int index = getIndex(size, indexes);
                                        byte[] newTemplate = createNewTemplate(index, record);

                                        if (newTemplate != null) {
                                            updateBiometric(rb.getId(), bcryptHash(newTemplate), newTemplate, record.getQuality());
                                            indexes = ArrayUtils.add(indexes, index);
                                            MPosition mPosition = new MPosition();
                                            mPosition.setId(bb.getId());
                                            mPosition.setMIndex(Arrays.toString(indexes));
                                            mPositionService.saveUpdatePosition(mPosition);

                                            SysBackup sysBackup = new SysBackup();
                                            BeanUtils.copyProperties(rb, sysBackup);
                                            sysBackup.setBackupDate(LocalDate.now());
                                            sysBackupRepository.save(sysBackup);

                                            rb.setTemplate(newTemplate);
                                            rb.setHashed(bcryptHash(newTemplate));
                                            rb.setImageQuality((int) record.getQuality());

                                            replacedRecaptured.add(rb);

                                        }else {
                                            log.info("New template created is null *********** {}", bb.getId());
                                        }

                                    } else {
                                        log.error("Creation of NRecord failed for biometric id: ******** {}", bb.getId());
                                    }
                                } else {
                                    log.error("Template length is less than 25 ***** ");
                                }
                            }
                        });
                    });
            // doIdentificationForIntervention(baseBiometric, replacedRecaptured, key, deduplicationType);
            log.info("Done intervention deduplication using subject as *****");
        });
        // Doing deduplication for the replaced prints
        if (interventionDeduplication) {
            getInterventionClients(LocalDate.now(), base, recapture, deduplicationType);
        }

    }

    private void setNSubjectParams(Biometric print, NSubject nSubject) {
        byte [] template = print.getTemplate();
        template[25] = 0x00;
        nSubject.setTemplateBuffer(new NBuffer(template));
        nSubject.setId(print.getId());

        nSubject.setProperty("templateType", print.getTemplateType());
        nSubject.setProperty("personUuid", print.getPersonUuid());
        nSubject.setProperty("id", print.getId());
        nSubject.setProperty("recapture", print.getRecapture());
    }

    private NSubject instantiateNSubject(){return new NSubject();}
    private List<NSubject> createSubjects (List<Biometric> prints) {
        var subjects = new ArrayList<NSubject>();
        prints
                .forEach(fingerPrint -> {
                    if (fingerPrint.getTemplate().length > 25){
                        NSubject nSubject = instantiateNSubject();
                        setNSubjectParams(fingerPrint, nSubject);
                        subjects.add(nSubject);
                    }
                });
        return subjects;
    }

    private void performNTask(NBiometricClient client, List<NSubject> subjects, NBiometricTask task) {
        subjects
                .forEach(nSubject -> {
                    try {
                        task.getSubjects().add(nSubject);
                    } catch (Exception e) {
                        task.getSubjects().remove(nSubject);
                        log.error("Error add subject to task ******** {}", e.getMessage());
                    }
                });

        try {
            client.performTask(task);
        } catch (Exception e){
            log.error("Error performing task ****** {}", e.getMessage());
        }
    }

    public void getInterventionClients(LocalDate backupDate, Integer base, Integer recapture, String deduplicationType) {
        Set<Integer> recaptures = new HashSet<>();
        recaptures.add(base); recaptures.add(recapture);
        var clients = biometricService.getInterventionPrintsForDeduplication(backupDate, recaptures, deduplicationType);
        log.info("Client size **** {}", clients.size());
        var groupClients = clients.stream().collect(Collectors.groupingBy(Biometric::getPersonUuid));

        log.info("About to do intervention deduplication ***** ");
        groupClients.entrySet().stream()
                // removing duplicate keys
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existingValue, newValue) -> existingValue))
                .forEach((key, value) -> {
                    var baseBiometric = value.stream().filter(f -> Objects.equals(f.getRecapture(), base)).toList();
                    var recaptureBiometric = value.stream().filter(f -> Objects.equals(f.getRecapture(), recapture)).toList();

                    doDeduplicationForIntervention(baseBiometric, recaptureBiometric, key, deduplicationType);
                });
    }

    public void doDeduplicationForIntervention (
            List<Biometric> biometrics, List<Biometric> identifiers,
            String personUuid, String deduplicationType
    ) {
        var client = createNBiometricClient();
        var subjects = createSubjects(biometrics);
        var identifierSubjects = createSubjects(identifiers);

        NBiometricTask task = client.createTask(EnumSet.of(NBiometricOperation.ENROLL), null);
        performNTask(client, subjects, task);

        var  interventionResponse = new InterventionResponse();
        interventionResponse.setPersonUuid(personUuid);
        interventionResponse.setDateOfDeduplication(LocalDate.now());
        interventionResponse.setDeduplicationType(deduplicationType);
        interventionResponse.setSubjectCount(subjects.size());
        interventionResponse.setIdentifierCount(identifierSubjects.size());

        HandledResponse handleDResponse = new HandledResponse();
        for (NSubject finger : identifierSubjects) {
            NBiometricStatus s = client.identify(finger);
            HandledResponse r = handleIdentificationResponse(s, handleDResponse, finger, subjects, deduplicationType);
            handleDResponse.setMatchedPairs(r.getMatchedPairs());
        }

        interventionResponse.setMatchedPairs(handleDResponse.getMatchedPairs());

        Set<String> recapturedIds = identifierSubjects.stream()
                .map(NSubject::getId)
                .collect(Collectors.toSet());

        interventionResponse.setDeduplicatedIds(recapturedIds);

        interventionResponseService.saveInterventionResponses(interventionResponse);

        client.clear();
    }

    private HandledResponse handleIdentificationResponse (NBiometricStatus s, HandledResponse handleDResponse,
                                                          NSubject finger, List<NSubject> subjects, String deduplicationType) {
        var templateType = finger.getProperty("templateType").toString();
        var id = finger.getProperty("id").toString();
        var personUUid = finger.getProperty("personUuid").toString();
        if (s.equals(NBiometricStatus.OK)) {
            NSubject.MatchingResultCollection nMatchingResults = finger.getMatchingResults();

            List<MatchedPair> matchedPairs = handleDResponse.getMatchedPairs();

            for(int i = 0; i < finger.getMatchingResults().size(); i++) {
                if (matchedPairs == null) {
                    matchedPairs = new ArrayList<>();
                }
                MatchedPair matchedPair = new MatchedPair();
                matchedPair.setEnrolledFingerId(id);
                matchedPair.setEnrolledPatientFingerType(templateType);

                String fingerId = finger.getMatchingResults().get(i).getId();
                Optional<NSubject> sb = subjects.stream().filter(sub -> sub.getId().equals(fingerId)).findFirst();
                sb.ifPresent(nSubject -> {
                    var matchId = nSubject.getProperty("id").toString();
                    var matchPersonUUid = nSubject.getProperty("personUuid").toString();
                    var matchTemplateType = nSubject.getProperty("templateType").toString();

                    matchedPair.setMatchedFingerId(matchId);
                    matchedPair.setMatchedPatientFingerType(matchTemplateType);
                    matchedPair.setMatchedPatientId(matchPersonUUid);
                });
                matchedPair.setScore(finger.getMatchingResults().get(i).getScore());

                if(!matchedPair.getMatchedFingerId().equals(matchedPair.getEnrolledFingerId())){
                    matchedPairs.add(matchedPair);
                }
                handleDResponse.setMatchedPairs(matchedPairs);
            }
        }
        return handleDResponse;
    }

    public byte[] convertNFRecordToFMRecord(NFRecord nfRecord){
        try {
            FMRecord newR = new FMRecord(nfRecord, BDIFStandard.ISO, FMRecord.VERSION_ISO_20);
            return newR.save().toByteArray();
        } catch (Exception e){
            log.error("Error converting template from NFRecord to FMRecord ");
            return null;
        }
    }

    public NBiometricClient createNBiometricClient() {
        var client = new NBiometricClient();
        client.setMatchingThreshold(144);
        client.setFingersMatchingSpeed(NMatchingSpeed.LOW);
        client.setMatchingWithDetails(true);
        client.setMatchingMaximalResultCount(100);
        client.setFingersReturnBinarizedImage(true);

        return client;
    }

    public NFRecord convertTemplateToNFRecord(byte[] template){
        try {
            FMRecord fmRecord = new FMRecord(new NBuffer(template), BDIFStandard.ISO);
            FMRFingerView fmrFingerView = fmRecord.getFingerViews().getFirst();
            return fmrFingerView.toNFRecord();
        }catch (Exception e){
            log.error("Error in conversion ******* {}", e.getMessage());
            return null;
        }
    }

    public int getIndex(int size, int[] indexes){
        Random random = new Random();
        if(indexes == null){
            return random.nextInt(size);
        }
        int index;
        do {
            index  = random.nextInt(size);
        }while (Arrays.binarySearch(indexes, index) >= 0);
        return index;
    }

    public NBiometricStatus doIdentification(NBiometricClient client, NSubject nSubject) {
        return client.identify(nSubject);
    }

    @SneakyThrows
    public byte[] createNewTemplate(int index, NFRecord record){
        record.getMinutiae().remove(index);
        record.setQuality(getRandomPercentage());
        return convertNFRecordToFMRecord(record);
    }

    public int[] convertStringToArray(String stringArray){
        String[] parts = stringArray.substring(1, stringArray.length() - 1).split(",");
        int[] arr = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            arr[i] = Integer.parseInt(parts[i].trim());
        }
        return arr;
    }

    public short getRandomPercentage() {
        short minPercentage = 70;
        short maxPercentage = 97;
        return (short) ThreadLocalRandom.current().nextInt(minPercentage, maxPercentage + 1);
    }

    public String bcryptHash(byte[] template) {
        String encoded = Base64.getEncoder().encodeToString(template);
        return BCrypt.hashpw(encoded, "$2a$12$MklNDNgs4Agd50cSasj91O");
    }

    private void updateBiometric(String patientUUID, String hashed, byte[] template, int quality){
        // Saving match information to the database
        jdbcTemplate.update("UPDATE biometric SET template = ?, hashed = ?, image_quality = ? where id = ? ",
                template, hashed, quality, patientUUID);
    }
}
