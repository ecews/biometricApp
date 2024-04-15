package org.ecews.biometricapp.services;

import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.io.NBuffer;
import com.neurotec.licensing.NLicense;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.Biometric;
import org.ecews.biometricapp.entities.IdentificationResponse;
import org.ecews.biometricapp.entities.dtos.HandledResponse;
import org.ecews.biometricapp.entities.dtos.MatchedPair;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.ecews.biometricapp.utils.LibraryManager;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCrypt;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NService {
    private NDeviceManager deviceManager;
    private final BiometricService biometricService;
    private final IdentificationResponseService identificationResponseService;

    public NService(BiometricService biometricService, IdentificationResponseService identificationResponseService) {
        this.biometricService = biometricService;
        this.identificationResponseService = identificationResponseService;
    }

    public void recaptureOneAndBaseline(String deduplicationType) {
        // Getting all baseline prints
        var baselinePrints = biometricService.getFingerprints(0L, deduplicationType);
        // Getting all recapture one fingerprints
        var recaptureOnePrints = biometricService.getNoMatchFingerprints(1L, deduplicationType);
        // Grouping baselinePrints and recaptureOnePrints using personUuid
        var biometrics = biometricService.groupFingerprints(baselinePrints, recaptureOnePrints);
        // Filtering keys in biometrics that do not have a recapture one print
        var filteredBiometrics = biometricService.filterClientWithoutBiometric(biometrics, recaptureOnePrints);
        log.info("Filter Biometric Size ******* {}", filteredBiometrics.size());

        for (String personUuid : filteredBiometrics.keySet()) {
            List<Biometric> prints = filteredBiometrics.get(personUuid);
            List<Biometric> patientBaselinePrints = biometricService.filterBiometricByRecapture(prints, 0);
            List<Biometric> patientRecaptureOnePrints = biometricService.filterBiometricByRecapture(prints, 1);

            // Do identification
            doIdentification(patientBaselinePrints, patientRecaptureOnePrints, personUuid, deduplicationType);
        }

    }

    public void recaptureOneDuplicateCheck(String deduplicationType){
        var recaptureOnePrints = biometricService.getNoMatchFingerprints(1L, deduplicationType);
        var groupedBiometrics = recaptureOnePrints.stream()
                .collect(Collectors.groupingBy(Biometric::getPersonUuid));

        doIdentification(recaptureOnePrints, groupedBiometrics, deduplicationType);
    }
    private Map<String, List<NSubject>> createSubjects (Map<String, List<Biometric>> prints) {
        Map<String, List<NSubject>> subjects = new HashMap<>();

        for (Map.Entry<String, List<Biometric>> entry : prints.entrySet()) {
            var personUuid = entry.getKey();
            var biometrics = entry.getValue();
            var nBiometrics = biometrics.stream().parallel().map(print -> {
                NSubject nSubject = instantiateNSubject();
                if (print.getTemplate().length > 25){
                    setNSubjectParams(print, nSubject);
                }
                return nSubject;
            }).toList();
            subjects.put(personUuid, nBiometrics);
        }
        return subjects;
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
        List<NSubject> subjects = new ArrayList<>();
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

    private void doIdentification (List<Biometric> biometrics, Map<String, List<Biometric>> identifiers, String deduplicationType) {
        var client = createNBiometricClient();
        var subjects = createSubjects(biometrics);
        var identifierSubjects = createSubjects(identifiers);

        var task = client.createTask(EnumSet.of(NBiometricOperation.ENROLL), null);
        performNTask(client, subjects, task);

        for (Map.Entry<String, List<NSubject>> entry : identifierSubjects.entrySet()) {
            var personUuid = entry.getKey();
            var subs = entry.getValue();

            IdentificationResponse identificationResponse = new IdentificationResponse();
            identificationResponse.setPersonUuid(personUuid);
            identificationResponse.setDateOfDeduplication(LocalDate.now());
            identificationResponse.setDeduplicationType(deduplicationType);
            identificationResponse.setSubjectCount(subjects.size());
            identificationResponse.setIdentifierCount(subs.size());

            HandledResponse handleDResponse = new HandledResponse();
            subs.stream().parallel().forEach(subject -> {
                NBiometricStatus s = client.identify(subject);
                HandledResponse r = handleIdentificationResponse(s, handleDResponse, subject, subjects, deduplicationType);
                handleDResponse.setMatchCount(r.getMatchCount());
                handleDResponse.setNoMatchCount(r.getNoMatchCount());
                handleDResponse.setMatchedPairs(r.getMatchedPairs());
            });

            identificationResponse.setNoMatchCount(handleDResponse.getNoMatchCount());
            identificationResponse.setMatchCount(handleDResponse.getMatchCount());
            identificationResponse.setMatchedPairs(handleDResponse.getMatchedPairs());
            Set<String> recapturedIds = subs.stream()
                    .map(NSubject::getId)
                    .collect(Collectors.toSet());
            identificationResponse.setDeduplicatedIds(recapturedIds);
            identificationResponseService.saveIdentificationResponses(identificationResponse);
            log.info("Identification Response ********* {}", identificationResponse);
        }

        client.clear();

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

    private void doIdentification (List<Biometric> biometrics, List<Biometric> identifiers, String personUuid, String deduplicationType) {
        var client = createNBiometricClient();
        var subjects = createSubjects(biometrics);
        var identifierSubjects = createSubjects(identifiers);

        NBiometricTask task = client.createTask(EnumSet.of(NBiometricOperation.ENROLL), null);
        performNTask(client, subjects, task);

        IdentificationResponse identificationResponse = new IdentificationResponse();
        identificationResponse.setPersonUuid(personUuid);
        identificationResponse.setDateOfDeduplication(LocalDate.now());
        identificationResponse.setDeduplicationType(deduplicationType);
        identificationResponse.setSubjectCount(subjects.size());
        identificationResponse.setIdentifierCount(identifierSubjects.size());

        HandledResponse handleDResponse = new HandledResponse();
        for (NSubject finger : identifierSubjects) {
            NBiometricStatus s = client.identify(finger);
            HandledResponse r = handleIdentificationResponse(s, handleDResponse, finger, subjects, deduplicationType);
            handleDResponse.setMatchCount(r.getMatchCount());
            handleDResponse.setNoMatchCount(r.getNoMatchCount());
            handleDResponse.setMatchedPairs(r.getMatchedPairs());
        }
        identificationResponse.setNoMatchCount(handleDResponse.getNoMatchCount());
        identificationResponse.setMatchCount(handleDResponse.getMatchCount());
        identificationResponse.setMatchedPairs(handleDResponse.getMatchedPairs());

        Set<String> recapturedIds = identifierSubjects.stream()
                .map(NSubject::getId)
                .collect(Collectors.toSet());

        identificationResponse.setDeduplicatedIds(recapturedIds);
        log.info("Identification Response ********* {}", identificationResponse);
        identificationResponseService.saveIdentificationResponses(identificationResponse);

        client.clear();
    }

    private HandledResponse handleIdentificationResponse (NBiometricStatus s, HandledResponse handleDResponse,
                                                          NSubject finger, List<NSubject> subjects, String deduplicationType) {
        var templateType = finger.getProperty("templateType").toString();
        var id = finger.getProperty("id").toString();
        var personUUid = finger.getProperty("personUuid").toString();
        if (s.equals(NBiometricStatus.OK)) {

            NSubject.MatchingResultCollection nMatchingResults = finger.getMatchingResults();
            for(int i = 0; i < finger.getMatchingResults().size(); i++) {
                List<MatchedPair> matchedPairs = handleDResponse.getMatchedPairs();
                MatchedPair matchedPair = new MatchedPair();
                matchedPair.setEnrolledFingerId(id);
                matchedPair.setEnrolledPatientFingerType(templateType);

                String fingerId = finger.getMatchingResults().get(i).getId();
                Optional<NSubject> sb = subjects.stream().filter(sub -> sub.getId().equals(fingerId))
                        .findFirst();
                sb.ifPresent(nSubject -> {
                    var matchId = nSubject.getProperty("id").toString();
                    var matchPersonUUid = nSubject.getProperty("personUuid").toString();
                    var matchTemplateType = nSubject.getProperty("templateType").toString();

                    matchedPair.setMatchedFingerId(matchId);
                    matchedPair.setMatchedPatientFingerType(matchTemplateType);
                    matchedPair.setMatchedPatientId(matchPersonUUid);
                });
                matchedPair.setScore(finger.getMatchingResults().get(i).getScore());

                if (matchedPairs == null) {
                    // If matchedPairs is null, create a new ArrayList to hold the matched pairs
                    matchedPairs = new ArrayList<>();
                    if (deduplicationType.equals(DeDuplicationConfigs.RECAPTURE_ONE_DUPLICATE_CHECK)) {
                        if (!personUUid.equals(matchedPair.getMatchedPatientId())) {
                            matchedPairs.add(matchedPair);
                        }
                    } else if (deduplicationType.equals(DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE )) {
                        matchedPairs.add(matchedPair);
                        handleDResponse.setMatchCount(handleDResponse.getMatchCount() + 1);
                    } else {
                        log.info("This is not a valid deduplication type ****** ");
                    }

                }
                handleDResponse.setMatchedPairs(matchedPairs);
            }

        } else {
            handleDResponse.setNoMatchCount(handleDResponse.getNoMatchCount() + 1);
        }
        return handleDResponse;
    }

    public NBiometricClient createNBiometricClient() {
        NBiometricClient client = null;
        client = new NBiometricClient();
        client.setMatchingThreshold(144);
        client.setFingersMatchingSpeed(NMatchingSpeed.LOW);
        client.setMatchingWithDetails(true);
        // client.setFingersCheckForDuplicatesWhenCapturing(true);
        client.setMatchingMaximalResultCount(100);
        client.setFingersReturnBinarizedImage(true);

        return client;
    }

    /*private boolean scannerIsNotSet(String reader) {
        log.info("Reader from REST **** {}", reader);
        for (NDevice device : getDevices()) {
            if (device.getDisplayName().equals(reader)) {
                client.setFingerScanner((NFScanner) device);
                return false;
            } else if (reader.equals("Futronic FS80H #1")){
                client.setFingerScanner((NFScanner) device);
                return false;
            }
        }
        return true;
    }*/

    private void initDeviceManager() {
        try {
            deviceManager = new NDeviceManager();
        } catch (Exception e) {
            log.error("Error ********* {}", e.getMessage());
        }

        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.FINGER_SCANNER));
        deviceManager.setAutoPlug(true);
        deviceManager.initialize();
    }

    private NDeviceManager.DeviceCollection getDevices() {
        return deviceManager.getDevices();
    }

    private void obtainLicense(String component) {
        try {
            boolean result = NLicense.obtainComponents("/local", "5000", component);
            log.info("Obtaining license: {}: {}", component, result);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /*private void createClient() {
        client = new NBiometricClient();
        client.setMatchingThreshold(96);
        client.setFingersMatchingSpeed(NMatchingSpeed.LOW);
        client.setFingersTemplateSize(NTemplateSize.LARGE);
        client.initialize();
    }*/

    @PostConstruct
    public void init() {
        LibraryManager.initLibraryPath();
        initDeviceManager();

        obtainLicense("Biometrics.FingerExtraction");
        obtainLicense("Biometrics.Standards.FingerTemplates");
        obtainLicense("Biometrics.FingerMatching");

        // createClient();
    }

    /*public Boolean emptyStoreByPersonId(Long personId){
        Boolean hasCleared = false;
        if(!BiometricStoreDTO.getPatientBiometricStore().isEmpty() && BiometricStoreDTO.getPatientBiometricStore().get(personId) != null){
            BiometricStoreDTO.getPatientBiometricStore().remove(personId);
            hasCleared = true;
        }
        return hasCleared;
    }
*/
    public String bcryptHash(byte[] template) {
        String encoded = Base64.getEncoder().encodeToString(template);
        return BCrypt.hashpw(encoded, "$2a$12$MklNDNgs4Agd50cSasj91O");
    }
}
