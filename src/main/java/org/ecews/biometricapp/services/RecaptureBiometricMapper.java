package org.ecews.biometricapp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ecews.biometricapp.entities.NdrMessageLog;
import org.ecews.biometricapp.entities.dtos.PatientDemographics;
import org.ecews.biometricapp.entities.dtos.RecaptureBiometricDTO;
import org.ecews.biometricapp.recapture.*;
import org.ecews.biometricapp.repositories.NDRCodeSetRepository;
import org.ecews.biometricapp.repositories.NdrMessageLogRepository;
import org.ecews.biometricapp.utils.DateUtil;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeConfigurationException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class RecaptureBiometricMapper {
    private final NdrMessageLogRepository ndrMessageLogRepository;
    private final NDRCodeSetRepository ndrCodeSetRepository;

    public Container getRecaptureBiometricMapper(PatientDemographics demographics, Integer recaptureType) {
        log.info("Start fetching and mapping finger-print for patient with hospital number {}", demographics.getHospitalNumber());
        ObjectFactory objectFactory = new ObjectFactory();

        MessageSendingOrganisationType messageSendingOrganisationType =
                getMessageSendingOrganisationType(objectFactory.createMessageSendingOrganisationType(),
                        demographics.getFacilityName(), demographics.getDatimId());

        MessageHeaderType messageHeaderType =
                getMessageHeaderType(objectFactory.createMessageHeaderType(), messageSendingOrganisationType);

        PatientDemographicsType patientDemographicsType = objectFactory.createPatientDemographicsType();
        patientDemographicsType.setPatientIdentifier(demographics.getDatimId().concat("_").concat(demographics.getPersonUuid()));
        boolean fingerPrintsAdded =
                addFingerPrintType(objectFactory, demographics.getPersonUuid(), patientDemographicsType, recaptureType);
        if (fingerPrintsAdded) {
            log.info("updated finger prints where fetched successfully");
            Container container = new Container();
            container.setEmrType("LAMISPlus");
            container.setMessageHeader(messageHeaderType);
            container.setPatientDemographics(patientDemographicsType);
            return container;
        }
        return null;
    }


    public MessageSendingOrganisationType getMessageSendingOrganisationType(MessageSendingOrganisationType messageSendingOrganisationType,
                                                                            String facilityName,
                                                                            String datimId) {
        messageSendingOrganisationType.setFacilityName(facilityName);
        messageSendingOrganisationType.setFacilityTypeCode("FAC");
        messageSendingOrganisationType.setFacilityID(datimId);
        return messageSendingOrganisationType;
    }

    public MessageHeaderType getMessageHeaderType(
            MessageHeaderType messageHeaderType,
            MessageSendingOrganisationType messageSendingOrganisationType) {
        messageHeaderType.setMessageSendingOrganisation(messageSendingOrganisationType);
        messageHeaderType.setXmlType("fingerprintsvalidation");
        messageHeaderType.setMessageUniqueID(generateUniqueString());
        messageHeaderType.setMessageVersion(1.0f);
        try {
            messageHeaderType.setMessageCreationDateTime(DateUtil.getXmlDateTime(new Date()));
        } catch (DatatypeConfigurationException e) {
            log.error("An error occurred while setting the message creation date time errorMessage {}", e.getMessage());
        }
        return messageHeaderType;
    }


    public boolean addFingerPrintType(
            ObjectFactory objectFactory,
            String patientUuid,
            PatientDemographicsType patientDemographicsType, Integer recaptureType) {

        //offset

        String patientIdentifier = patientDemographicsType.getPatientIdentifier();
        Optional<NdrMessageLog> messageLog =
                ndrMessageLogRepository.findFirstByIdentifierAndFileType(patientIdentifier, "recaptured-biometric");
        List<RecaptureBiometricDTO> biometricDTOList = new ArrayList<>();
        if (messageLog.isPresent() && messageLog.get().getFile().contains(String.valueOf(recaptureType))) {
            // find the new value;
            LocalDate lastUpdated =
                    messageLog.get().getLastUpdated().toLocalDate();
            log.info("last recapture Date " + lastUpdated);
            log.info("file {}", messageLog.get().getFile());
            log.info("recaptureType " + recaptureType);
            biometricDTOList =
                    ndrCodeSetRepository.getPatientRecapturedBiometricByPatientUuid(patientUuid, recaptureType, lastUpdated);


        } else {
            biometricDTOList =
                    ndrCodeSetRepository.getPatientRecapturedBiometricByPatientUuid(patientUuid, recaptureType);
        }

        if (biometricDTOList.isEmpty()) {
            log.info("No biometric recapture found for the patient with uuid " + patientUuid);
            return false;
        }
        if (biometricDTOList.size() < 6) {
            log.info("incomplete biometric recapture found for the patient with uuid " + patientUuid);
            return false;
        }
        RecaptureBiometricDTO metaData = biometricDTOList.get(0);
        LocalDate visitDate = metaData.getEnrollmentDate();
        if (visitDate == null) {
            log.error("Captured date can not be null patient uuid : " + patientUuid);
            return false;
        }
        try {
            String visitId = visitDate.toEpochDay() + patientUuid;
            Integer count = metaData.getCount();
            FingerPrintType fingerPrintType = objectFactory.createFingerPrintType();
            fingerPrintType.setVisitId(visitId);
            fingerPrintType.setVisitDate(DateUtil.getXmlDateTime(java.sql.Date.valueOf(visitDate)));
            fingerPrintType.setDateCaptured(DateUtil.getXmlDateTime(java.sql.Date.valueOf(visitDate)));
            if (count == null) {
                count = 1;
            }
            fingerPrintType.setCaptureCount(count);
            //get right fingers
            List<RecaptureBiometricDTO> rightFingers =
                    biometricDTOList.stream().filter(finger -> finger.getTemplateType().contains("Right"))
                            .collect(Collectors.toList());

            RightHandType rightHandType = getRightHandType(rightFingers, objectFactory.createRightHandType());
            fingerPrintType.setRightHand(rightHandType);
            //get left fingers
            List<RecaptureBiometricDTO> leftFingers =
                    biometricDTOList.stream().filter(finger -> finger.getTemplateType().contains("Left"))
                            .collect(Collectors.toList());
            LeftHandType leftHandType = getLeftHandType(leftFingers, objectFactory.createLeftHandType());
            fingerPrintType.setLeftHand(leftHandType);

            patientDemographicsType.getFingerPrints().add(fingerPrintType);
        } catch (Exception e) {
            log.error("An exception occurred while trying to print the fingerprint patient uuid {} error {}, ",
                    patientUuid, e.getMessage());
            //e.printStackTrace();
            return false;
        }

        return true;
    }

    private LeftHandType getLeftHandType(List<RecaptureBiometricDTO> leftList, LeftHandType leftHandType) {

        if (leftList.isEmpty()) {
            throw new IllegalArgumentException("At least one left hand finger is required");
        }
        for (RecaptureBiometricDTO finger : leftList) {
            String type = finger.getTemplateType();
            String template = Base64.getEncoder().encodeToString(finger.getTemplate());
            if (StringUtils.containsIgnoreCase(type, "Thumb")) {
                leftHandType.setLeftThumb(template);
                leftHandType.setLeftThumbQuality(finger.getQuality());
                leftHandType.setHashedLeftThumb(finger.getTemplateTypeHash());
            } else if (StringUtils.containsIgnoreCase(type, "Index")) {
                leftHandType.setLeftIndex(template);
                leftHandType.setLeftIndexQuality(finger.getQuality());
                leftHandType.setHashedLeftIndex(finger.getTemplateTypeHash());
            } else if (StringUtils.containsIgnoreCase(type, "Middle")) {
                leftHandType.setLeftMiddle(template);
                leftHandType.setLeftMiddleQuality(finger.getQuality());
                leftHandType.setHashedLeftMiddle(finger.getTemplateTypeHash());
            } else if (StringUtils.containsIgnoreCase(type, "Little")) {
                leftHandType.setLeftSmall(template);
                leftHandType.setLeftSmallQuality(finger.getQuality());
                leftHandType.setHashedLeftSmall(finger.getTemplateTypeHash());
            } else {
                leftHandType.setLeftWedding(template);
                leftHandType.setLeftWeddingQuality(finger.getQuality());
                leftHandType.setHashedLeftWedding(finger.getTemplateTypeHash());
            }
        }
        return leftHandType;
    }

    private RightHandType getRightHandType(List<RecaptureBiometricDTO> rightList, RightHandType rightHandType) {
        if (rightList.isEmpty()) {
            throw new IllegalArgumentException("At least one right hand finger is required");
        }
        rightList.forEach(finger -> {
            String type = finger.getTemplateType();
            String template = Base64.getEncoder().encodeToString(finger.getTemplate());
            if (StringUtils.containsIgnoreCase(type, "Thumb")) {
                rightHandType.setRightThumb(template);
                rightHandType.setRightThumbQuality(finger.getQuality());
                rightHandType.setHashedRightThumb(finger.getTemplateTypeHash());
            } else if (StringUtils.containsIgnoreCase(type, "Index")) {
                rightHandType.setRightIndex(template);
                rightHandType.setRightIndexQuality(finger.getQuality());
                rightHandType.setHashedRightIndex(finger.getTemplateTypeHash());
            } else if (StringUtils.containsIgnoreCase(type, "Middle")) {
                rightHandType.setRightMiddle(template);
                rightHandType.setRightMiddleQuality(finger.getQuality());
                rightHandType.setHashedRightMiddle(finger.getTemplateTypeHash());
            } else if (StringUtils.containsIgnoreCase(type, "Little")) {
                rightHandType.setRightSmall(template);
                rightHandType.setRightSmallQuality(finger.getQuality());
                rightHandType.setHashedRightSmall(finger.getTemplateTypeHash());
            } else {
                rightHandType.setRightWedding(template);
                rightHandType.setRightWeddingQuality(finger.getQuality());
                rightHandType.setHashedRightWedding(finger.getTemplateTypeHash());
            }
        });
        return rightHandType;
    }

    private static String generateUniqueString() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }


}
