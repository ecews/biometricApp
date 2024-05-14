package org.ecews.biometricapp.services;

import ch.qos.logback.core.testUtil.RandomUtil;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.standards.CBEFFBDBFormatIdentifiers;
import com.neurotec.biometrics.standards.CBEFFBiometricOrganizations;
import com.neurotec.biometrics.standards.FMRecord;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.devices.NFScanner;
import com.neurotec.io.NBuffer;
import com.neurotec.licensing.NLicense;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.Biometric;
import org.ecews.biometricapp.entities.IdentificationResponse;
import org.ecews.biometricapp.entities.InterventionResponse;
import org.ecews.biometricapp.entities.dtos.Device;
import org.ecews.biometricapp.entities.dtos.HandledResponse;
import org.ecews.biometricapp.entities.dtos.MatchedPair;
import org.ecews.biometricapp.repositories.MPositionRepository;
import org.ecews.biometricapp.utils.LibraryManager;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NService {

    @Value("${intervention.enabled:false}")
    private boolean interventionEnabled;

    @Value("${percentage:1.0}")
    private double percentage;
    private NDeviceManager deviceManager;
    private final BiometricService biometricService;
    private final IdentificationResponseService identificationResponseService;
    private final NInterventionService nInterventionService;
    private final InterventionResponseService interventionResponseService;
    private static final Boolean replacement = Boolean.FALSE;
    public NService(BiometricService biometricService, IdentificationResponseService identificationResponseService, NInterventionService nInterventionService, MPositionRepository mPositionRepository, InterventionResponseService interventionResponseService) {
        this.biometricService = biometricService;
        this.identificationResponseService = identificationResponseService;
        this.nInterventionService = nInterventionService;
        this.interventionResponseService = interventionResponseService;
    }

    public void recaptureOneAndBaseline(String deduplicationType, LocalDate deduplicationDate) {
        // Getting all baseline prints
        var baselinePrints = biometricService.getFingerprints(1L);
        var recaptureOneNMPrints = biometricService.getNoMatchFingerprints(1L, deduplicationType);
        prepareForDeduplication(deduplicationType, baselinePrints, recaptureOneNMPrints, 0,1);
        // Doing intervention after deduplication
        if (interventionEnabled) {
            nInterventionService.doIntervention(deduplicationType, percentage, 0, 1, Boolean.FALSE, deduplicationDate);
        }

    }
    public void recaptureOneDuplicateCheck(String deduplicationType){
        var recaptureOnePrints = biometricService.getMatchedFingerprints(1L, deduplicationType);
        var groupedBiometrics = recaptureOnePrints.stream()
                .collect(Collectors.groupingBy(Biometric::getPersonUuid));
        var baselinePrints = biometricService.getFingerprints(0L);
        // Filtering baseline prints for all clients that are NO_MATCH recapture one
        var fingers = biometricService.removeBiometrics(baselinePrints, recaptureOnePrints);
        fingers.addAll(recaptureOnePrints);
        doIdentification(fingers, groupedBiometrics, deduplicationType);
    }

    public void recaptureTwoAndRecaptureOne(String deduplicationType, LocalDate deduplicationDate) {

        var baselinePrints = biometricService.getFingerprints(2L);
        var recaptureTwoPrints = biometricService.getFingerprints(2L, deduplicationType);

        prepareForDeduplication(deduplicationType, baselinePrints, recaptureTwoPrints, 1,2);

        if (interventionEnabled) {
            nInterventionService.doIntervention(deduplicationType, percentage, 0, 2, Boolean.FALSE, deduplicationDate);
        }

    }
    private void callForDeduplication(String deduplicationType, Map<String, List<Biometric>> biometrics,
                                      Integer base, Integer recapture) {
        biometrics.entrySet().stream().parallel()
                .forEach(value -> {
                    var patientBaselinePrints = value.getValue().stream().filter(f -> f.getRecapture() <= base).toList();
                    var patientRecaptureOnePrints = value.getValue().stream().filter(f -> Objects.equals(f.getRecapture(), recapture)).toList();
                    doIdentification(patientBaselinePrints, patientRecaptureOnePrints, value.getKey(), deduplicationType);
                });
    }

    private void prepareForDeduplication(String deduplicationType, List<Biometric> baselinePrints, List<Biometric> recaptureTwoPrints,
                                         Integer base, Integer recapture) {
        var recapturePersonsUuids = recaptureTwoPrints.stream()
                .map(Biometric::getPersonUuid)
                .toList();
        baselinePrints = baselinePrints.stream()
                .filter(biometric -> recapturePersonsUuids.contains(biometric.getPersonUuid()))
                .toList();
        var groupedBiometrics = baselinePrints.stream()
                .collect(Collectors.groupingBy(Biometric::getPersonUuid));
        callForDeduplication(deduplicationType, groupedBiometrics, base, recapture);
    }

    public void recaptureTwoDuplicateCheck(String deduplicationType){
        var recaptureTwoPrints = biometricService.getMatchedFingerprints(2L, deduplicationType);
        var groupedBiometrics = recaptureTwoPrints.stream()
                .collect(Collectors.groupingBy(Biometric::getPersonUuid));
        var baselinePrints = biometricService.getFingerprints(0L);
        // Filtering all recapture two prints from baseline prints
        var fingers = biometricService.removeBiometrics(baselinePrints, recaptureTwoPrints);
        fingers.addAll(recaptureTwoPrints);

        doIdentification(fingers, groupedBiometrics, deduplicationType);
    }
    /*This method help you compare recaptu
    * */
    public void recaptureThreeAndRecaptureTwo(String deduplicationType, LocalDate deduplicationDate) {
        var baselinePrints = biometricService.getFingerprints(3L);
        var recaptureThreePrints = biometricService.getFingerprints(3L, deduplicationType);
        prepareForDeduplication(deduplicationType, baselinePrints, recaptureThreePrints, 2,3);
        if (interventionEnabled) {
            nInterventionService.doIntervention(deduplicationType, percentage, 0, 3, Boolean.FALSE, deduplicationDate);
        }
    }

    public void recaptureThreeDuplicateCheck(String deduplicationType){
        var recaptureThreePrints = biometricService.getMatchedFingerprints(3L, deduplicationType);
        var groupedBiometrics = recaptureThreePrints.stream()
                .collect(Collectors.groupingBy(Biometric::getPersonUuid));
        var baselinePrints = biometricService.getFingerprints(0L);
        // Filtering baseline prints for all clients that are NO_MATCH recapture one
        var fingers = biometricService.removeBiometrics(baselinePrints, recaptureThreePrints);
        fingers.addAll(recaptureThreePrints);
        doIdentification(fingers, groupedBiometrics, deduplicationType);
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
        log.info("SIZE OF THE MAP ****** {}", identifierSubjects.size());

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
            // Finger level loop
            subs.stream().parallel().forEach(subject -> {
                NBiometricStatus s = client.identify(subject);
                HandledResponse r = handleIdentificationResponse(s, handleDResponse, subject, subjects, deduplicationType);
                handleDResponse.setMatchedPairs(r.getMatchedPairs());
            });

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

    private void doIdentification (
            List<Biometric> biometrics, List<Biometric> identifiers,
            String personUuid, String deduplicationType
    ) {
        var client = createNBiometricClient();
        var subjects = createSubjects(biometrics);
        var identifierSubjects = createSubjects(identifiers);

        NBiometricTask task = client.createTask(EnumSet.of(NBiometricOperation.ENROLL), null);
        performNTask(client, subjects, task);

        var  identificationResponse = new IdentificationResponse();
        identificationResponse.setPersonUuid(personUuid);
        identificationResponse.setDateOfDeduplication(LocalDate.now());
        identificationResponse.setDeduplicationType(deduplicationType);
        identificationResponse.setSubjectCount(subjects.size());
        identificationResponse.setIdentifierCount(identifierSubjects.size());

        HandledResponse handleDResponse = new HandledResponse();
        for (NSubject finger : identifierSubjects) {
            NBiometricStatus s = client.identify(finger);
            HandledResponse r = handleIdentificationResponse(s, handleDResponse, finger, subjects, deduplicationType);
            handleDResponse.setMatchedPairs(r.getMatchedPairs());
        }

        identificationResponse.setMatchedPairs(handleDResponse.getMatchedPairs());

        Set<String> recapturedIds = identifierSubjects.stream()
                .map(NSubject::getId)
                .collect(Collectors.toSet());

        identificationResponse.setDeduplicatedIds(recapturedIds);
        identificationResponseService.saveIdentificationResponses(identificationResponse);

        client.clear();
    }

    private HandledResponse handleIdentificationResponse (NBiometricStatus s, HandledResponse handleDResponse,
                                                          NSubject finger, List<NSubject> subjects, String deduplicationType) {
        var templateType = finger.getProperty("templateType").toString();
        var id = finger.getProperty("id").toString();
        var personUUid = finger.getProperty("personUuid").toString();
        if (s.equals(NBiometricStatus.OK)) {
            // NSubject.MatchingResultCollection nMatchingResults = finger.getMatchingResults();

            var matchedPairs = handleDResponse.getMatchedPairs();

            for(int i = 0; i < finger.getMatchingResults().size(); i++) {
                if (matchedPairs == null) {
                    matchedPairs = new ArrayList<MatchedPair>();
                }
                var matchedPair = new MatchedPair();
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
                if(!matchedPair.getMatchedFingerId().equals(matchedPair.getEnrolledFingerId())){
                    matchedPairs.add(matchedPair);
                }
                handleDResponse.setMatchedPairs(matchedPairs);
            }
        }
        return handleDResponse;
    }

    public NBiometricClient createNBiometricClient() {
        NBiometricClient client = null;
        client = new NBiometricClient();
        client.setMatchingThreshold(144);
        client.setFingersMatchingSpeed(NMatchingSpeed.LOW);
        client.setMatchingWithDetails(true);
        client.setMatchingMaximalResultCount(100);
        client.setFingersReturnBinarizedImage(true);

        return client;
    }

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

    public NDeviceManager.DeviceCollection getDevices() {
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

    @PostConstruct
    public void init() {
        LibraryManager.initLibraryPath();
        initDeviceManager();
        obtainLicense("Biometrics.FingerExtraction");
        obtainLicense("Biometrics.Standards.FingerTemplates");
        obtainLicense("Biometrics.FingerMatching");
    }

    public List<Device> getReaders() {
        //GET - http://localhost:8282/api/v1/biometrics//reader
        List<Device> devices = new ArrayList<>();
        getDevices().forEach(device -> {
            Device d = new Device();
            d.setDeviceName(device.getDisplayName());
            d.setId(device.getId());
            devices.add(d);
        });
        return devices;
    }

    public boolean scannerIsNotSet(String reader, NBiometricClient client) {
        for (NDevice device : getDevices()) {
            log.info("Device is **** {} and Reader is ***** {}", device.getDisplayName(), reader);
            if (device.getDisplayName().contains(reader)) {
                log.info("Scanner is set ******* ");
                client.setFingerScanner((NFScanner) device);
                return false;
            } else{
                log.info("Scanner is not set ******* ");
            }
        }
        return true;
    }

    public ResponseEntity<?> verifyPersonWithBiometric(String personUuid, String recaptureType, String reader) {
        Set<Integer> recaptures = new HashSet<>();

        switch (recaptureType) {
            case "all" -> {
                recaptures.add(0);
                recaptures.add(1);
                recaptures.add(2);
            }
            case "0" -> recaptures.add(0);
            case "1" -> recaptures.add(1);
            case "2" -> recaptures.add(2);
        }

        var personBiometrics = biometricService.getPersonBiometrics(personUuid, recaptures);
        var subjects = createSubjects(personBiometrics);
        log.info("Subject size is ******** {}", subjects.size());

        var biometricClient = createNBiometricClient();
        short q = 60;
        biometricClient.setMatchingThreshold((byte)q);

        Map<String, Object> response = new HashMap<>();;

        try {
            reader = URLDecoder.decode(reader, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ignored) {
        }

        for (NDevice device : getDevices()) {
            if (device.getDisplayName().equals(reader)) {
                biometricClient.setFingerScanner((NFScanner) device);
            }
        }

        NBiometricTask task = biometricClient.createTask(EnumSet.of(NBiometricOperation.ENROLL), null);
        performNTask(biometricClient, subjects, task);

        try (NSubject subject = new NSubject()) {
            final NFinger finger = new NFinger();
            finger.setPosition(NFPosition.UNKNOWN);
            subject.getFingers().add(finger);

            if (scannerIsNotSet(reader, biometricClient)) {
                response.put("error", "Biometrics Scanner not found");
                response.put("status", HttpStatus.OK.value());
                return new ResponseEntity<>(response, HttpStatus.OK);
            }

            NBiometricStatus status = biometricClient.capture(subject);

            if (status.equals(NBiometricStatus.OK)) {
                status = biometricClient.createTemplate(subject);
                if (status.equals(NBiometricStatus.OK)) {
                    byte[] isoTemplate = subject.getTemplateBuffer(CBEFFBiometricOrganizations.ISO_IEC_JTC_1_SC_37_BIOMETRICS,
                            CBEFFBDBFormatIdentifiers.ISO_IEC_JTC_1_SC_37_BIOMETRICS_FINGER_MINUTIAE_RECORD_FORMAT,
                            FMRecord.VERSION_ISO_20).toByteArray();

                    NSubject nSubject = new NSubject();
                    nSubject.setTemplateBuffer(new NBuffer(isoTemplate));
                    nSubject.setId(String.valueOf(1L));

                    status = biometricClient.identify(nSubject);
                    var matchResponse = new ArrayList<>();
                    if(status.equals(NBiometricStatus.OK)){
                        for(int i = 0; i < nSubject.getMatchingResults().size(); i++) {
                            Map<String, Object> map = new HashMap<>();
                            String fingerId = nSubject.getMatchingResults().get(i).getId();
                            Optional<NSubject> sb = subjects.stream().filter(sub -> sub.getId().equals(fingerId))
                                    .findFirst();
                            sb.ifPresent(sub -> {
                                var matchTemplateType = sub.getProperty("templateType").toString();
                                var recapture = sub.getProperty("recapture").toString();
                                log.info("ID ******* {}", sub.getProperty("id").toString());
                                map.put("templateType", matchTemplateType);
                                map.put("recapture", recapture);
                            });
                            map.put("score", nSubject.getMatchingResults().get(i).getScore());
                            matchResponse.add(map);
                        }
                        response.put("type", "SUCCESS");
                        response.put("matches", matchResponse);
                    } else {
                        response.put("type", "SUCCESS_NOT_FOUND");
                        response.put("message", "Could not match this fingerprint to any of the client existing set of fingerprints");
                        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
                    }
                } else {
                    response.put("error", "Could not create template from fingerprint");
                    response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
                }
            } else {
                response.put("error", "Could not capture fingerprint");
                response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
