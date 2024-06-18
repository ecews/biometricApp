package org.ecews.biometricapp.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ecews.biometricapp.entities.dtos.BiometricLongitudinalDTO;
import org.ecews.biometricapp.entities.dtos.CurrentRecaptureDTO;
import org.ecews.biometricapp.entities.dtos.DeduplicationDetail;
import org.ecews.biometricapp.entities.dtos.DeduplicationSummary;
import org.ecews.biometricapp.repositories.ReportRepository;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public ReportService(ReportRepository reportRepository, JdbcTemplate jdbcTemplate) {
        this.reportRepository = reportRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getDistinctFacilities() {
        String sql = "SELECT DISTINCT pp.facility_id AS facilityId, bac.name AS facilityName " +
                "FROM patient_person pp " +
                "JOIN base_organisation_unit bac ON bac.id = pp.facility_id";

        return jdbcTemplate.queryForList(sql);
    }

    public ByteArrayInputStream  generateSummaryReport (String deduplicationType) {
        var data = reportRepository.getSummaryReport(deduplicationType);
        return writeDeduplicationResultsToCsvInMemory(data, deduplicationType);
    }

    public ByteArrayInputStream  generateDetailReport (String deduplicationType) {
        var data = reportRepository.getDetailReport(deduplicationType);
        var filtered = data.stream().filter(f -> f.getMatchedPatientId() != null).collect(Collectors.toList());
        return writeDeduplicationDetailReport(filtered);
    }

    private static String toCamelCase(String s) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : s.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    sb.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
    }

    public ByteArrayInputStream generateRecaptureReport () {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
            List<String> headersList = new ArrayList<>(Arrays.asList(
                    "State", "LGA", "Facility", "Datim Code", "Patient Id", "Surname", "First Name", "Sex", "LGA of Residence",
                    "Hospital Number", "Unique Id", "Date of Birth", "ART Start Date", "Target Group", "Date of Current Recapture",
                    "Current Recapture"
            ));
            String[] headers = headersList.toArray(new String[0]);
            writer.writeNext(headers);
            var patientInfo = reportRepository.getPatientData();
            log.info("Data size ******* {}", patientInfo.size());

            for (CurrentRecaptureDTO result: patientInfo) {
                List<String> rowData = new ArrayList<>(Arrays.asList(
                        result.getState(), result.getLga(), result.getFacilityName(),
                        result.getDatimId(), result.getPatientId(), result.getSurname(), result.getFirstName(), result.getSex(),
                        result.getLgaOfResidence(), result.getHospitalNumber(), result.getUniqueId(), String.valueOf(result.getDateOfBirth()),
                        String.valueOf(result.getArtStartDate()), result.getTargetGroup(), String.valueOf(result.getDateOfCurrentRecapture()),
                        String.valueOf(result.getCurrentRecapture())
                ));
                writer.writeNext(rowData.toArray(new String[0]));
            }
            writer.flush();
            return new ByteArrayInputStream(outputStream.toByteArray());
        }catch (Exception e) {
            log.error("Error ***** {}", e.getMessage());
            return null;
        }
    }

    public ByteArrayInputStream generateLongitudinalReport () {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
            List<String> headersList = new ArrayList<>(Arrays.asList(
                    "State", "LGA", "Facility", "Datim Code", "Patient Id", "Surname", "First Name", "Sex", "LGA of Residence",
                    "Hospital Number", "Unique Id", "Date of Birth", "Target Group", "Baseline Finger Count", "Baseline Enrollment Date",
                    "Recapture One Finger Count", "Recapture One Enrollment Date", "Recapture Two Finger Count", "Recapture Two Enrollment Date",
                    "Recapture Three Finger Count", "Recapture Three Enrollment Date", "Recapture Four Finger Count", "Recapture Four Enrollment Date",
                    "Recapture Five Finger Count", "Recapture Five Enrollment Date", "Recapture Five Finger Count", "Recapture Five Enrollment Date",
                    "Recapture Six Finger Count", "Recapture Six Enrollment Date", "Recapture Seven Finger Count", "Recapture Seven Enrollment Date",
                    "Recapture Eight Finger Count", "Recapture Eight Enrollment Date", "Recapture Nine Finger Count", "Recapture Nine Enrollment Date",
                    "Recapture Ten Finger Count", "Recapture ten Enrollment Date"
            ));
            String[] headers = headersList.toArray(new String[0]);
            writer.writeNext(headers);
            var patientInfo = reportRepository.getLongitudinalRecaptureReport();
            log.info("Data size ******* {}", patientInfo.size());

            for (BiometricLongitudinalDTO result: patientInfo) {
                List<String> rowData = new ArrayList<>(Arrays.asList(
                        result.getState(), result.getLga(), result.getFacilityName(),
                        result.getDatimId(), result.getPatientId(), result.getSurname(), result.getFirstName(), result.getSex(),
                        result.getLgaOfResidence(), result.getHospitalNumber(), result.getUniqueId(), String.valueOf(result.getDateOfBirth()),
                        result.getTargetGroup(), String.valueOf(result.getBaselineCount()),
                        (result.getBaselineDate() != null ) ? String.valueOf(result.getBaselineDate()) : "",
                        (result.getRecaptureOneCount() != null) ? String.valueOf(result.getRecaptureOneCount()) : "",
                        result.getRecaptureOneDate() != null ? String.valueOf(result.getRecaptureOneDate()) : "",
                        result.getRecaptureTwoCount() != null ? String.valueOf(result.getRecaptureTwoCount()) : "",
                        result.getRecaptureTwoDate() != null ? String.valueOf(result.getRecaptureTwoDate()) : "",
                        result.getRecaptureThreeCount() != null ? String.valueOf(result.getRecaptureThreeCount()): "",
                        result.getRecaptureThreeDate() != null ? String.valueOf(result.getRecaptureThreeDate()) : "",
                        result.getRecaptureFourCount() != null ? String.valueOf(result.getRecaptureFourCount()) : "",
                        result.getRecaptureFourDate() != null ? String.valueOf(result.getRecaptureFourDate()) : "",
                        result.getRecaptureFiveCount() != null ? String.valueOf(result.getRecaptureFiveCount()) : "",
                        result.getRecaptureFiveDate() != null ? String.valueOf(result.getRecaptureFiveDate()) : "",
                        result.getRecaptureSixCount() != null ? String.valueOf(result.getRecaptureSixCount()) : "",
                        result.getRecaptureSixDate() != null ?String.valueOf(result.getRecaptureSixDate()) : "",
                        result.getRecaptureSevenCount() != null ? String.valueOf(result.getRecaptureSevenCount()) : "",
                        result.getRecaptureSevenDate() != null ? String.valueOf(result.getRecaptureSevenDate()) : "",
                        result.getRecaptureEightCount() != null ? String.valueOf(result.getRecaptureEightCount()): "",
                        result.getRecaptureEightDate() != null ? String.valueOf(result.getRecaptureEightDate()) : "",
                        result.getRecaptureNineCount() != null ? String.valueOf(result.getRecaptureNineCount()) : "",
                        result.getRecaptureNineDate() != null ? String.valueOf(result.getRecaptureNineDate()) : "",
                        result.getRecaptureTenCount() != null ? String.valueOf(result.getRecaptureTenCount()) : "",
                        result.getRecaptureTenDate() != null ? String.valueOf(result.getRecaptureTenDate()) : ""
                ));
                writer.writeNext(rowData.toArray(new String[0]));
            }
            writer.flush();
            return new ByteArrayInputStream(outputStream.toByteArray());
        }catch (Exception e) {
            log.error("Error ***** {}", e.getMessage());
            return null;
        }
    }

    private ObjectNode convertAndSortJsonNodeKeys(JsonNode jsonNode) {
        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            ObjectNode sortedNode = objectMapper.createObjectNode();
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            Map<String, JsonNode> sortedFields = new TreeMap<>();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                sortedFields.put(entry.getKey(), entry.getValue());
            }
            sortedFields.forEach(sortedNode::set);
            return sortedNode;
        } else {
            throw new IllegalArgumentException("JsonNode is not an ObjectNode");
        }
    }

    public ByteArrayInputStream writeDeduplicationResultsToCsvInMemory(List<DeduplicationSummary> results, String deduplicationType) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
            List<String> headersList = new ArrayList<>(Arrays.asList(
                    "State", "LGA", "Facility", "Datim Code", "Patient Id", "Surname", "First Name", "Sex", "LGA of Residence",
                    "Hospital Number", "Unique Id", "Date of Birth", "ART Start Date", "Target Group", "Date of Deduplication",
                    "Match Count", "Subject Count", "Identifier Count"
            ));

            switch (deduplicationType) {
                case DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE:
                    headersList.add("Baseline Match Count");
                    break;
                case DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE:
                    headersList.add("Baseline Match Count");
                    headersList.add("Recapture One Match Count");
                    break;
                case DeDuplicationConfigs.RECAPTURE_THREE_AND_TWO:
                    headersList.add("Baseline Match Count");
                    headersList.add("Recapture One Match Count");
                    headersList.add("Recapture Two Match Count");
                    break;
                case DeDuplicationConfigs.RECAPTURE_FOUR:
                    headersList.add("Baseline Match Count");
                    headersList.add("Recapture One Match Count");
                    headersList.add("Recapture Two Match Count");
                    headersList.add("Recapture Three Match Count");
                    break;
                case DeDuplicationConfigs.RECAPTURE_FIVE:
                    headersList.add("Baseline Match Count");
                    headersList.add("Recapture One Match Count");
                    headersList.add("Recapture Two Match Count");
                    headersList.add("Recapture Three Match Count");
                    headersList.add("Recapture Four Match Count");
                    break;
                case DeDuplicationConfigs.RECAPTURE_SIX:
                    headersList.add("Baseline Match Count");
                    headersList.add("Recapture One Match Count");
                    headersList.add("Recapture Two Match Count");
                    headersList.add("Recapture Three Match Count");
                    headersList.add("Recapture Four Match Count");
                    headersList.add("Recapture Five Match Count");
                    break;
                case DeDuplicationConfigs.RECAPTURE_SEVEN:
                    headersList.add("Baseline Match Count");
                    headersList.add("Recapture One Match Count");
                    headersList.add("Recapture Two Match Count");
                    headersList.add("Recapture Three Match Count");
                    headersList.add("Recapture Four Match Count");
                    headersList.add("Recapture Five Match Count");
                    headersList.add("Recapture Six Match Count");
                    break;
                case DeDuplicationConfigs.RECAPTURE_EIGHT:
                    headersList.add("Baseline Match Count");
                    headersList.add("Recapture One Match Count");
                    headersList.add("Recapture Two Match Count");
                    headersList.add("Recapture Three Match Count");
                    headersList.add("Recapture Four Match Count");
                    headersList.add("Recapture Five Match Count");
                    headersList.add("Recapture Six Match Count");
                    headersList.add("Recapture Seven Match Count");
                    break;
                case DeDuplicationConfigs.RECAPTURE_NINE:
                    headersList.add("Baseline Match Count");
                    headersList.add("Recapture One Match Count");
                    headersList.add("Recapture Two Match Count");
                    headersList.add("Recapture Three Match Count");
                    headersList.add("Recapture Four Match Count");
                    headersList.add("Recapture Five Match Count");
                    headersList.add("Recapture Six Match Count");
                    headersList.add("Recapture Seven Match Count");
                    headersList.add("Recapture Eight Match Count");
                    break;
                case DeDuplicationConfigs.RECAPTURE_TEN:
                    headersList.add("Baseline Match Count");
                    headersList.add("Recapture One Match Count");
                    headersList.add("Recapture Two Match Count");
                    headersList.add("Recapture Three Match Count");
                    headersList.add("Recapture Four Match Count");
                    headersList.add("Recapture Five Match Count");
                    headersList.add("Recapture Six Match Count");
                    headersList.add("Recapture Seven Match Count");
                    headersList.add("Recapture Eight Match Count");
                    headersList.add("Recapture ten Match Count");
                    break;
                default:
                    break;
            }

            String[] headers = headersList.toArray(new String[0]);
            writer.writeNext(headers);

            // Writing data
            for (DeduplicationSummary result : results) {
                List<String> rowData = new ArrayList<>(Arrays.asList(
                        result.getState(), result.getLga(), result.getFacilityName(),
                        result.getDatimId(), result.getPatientId(), result.getSurname(), result.getFirstName(), result.getSex(),
                        result.getLgaOfResidence(), result.getHospitalNumber(), result.getUniqueId(), String.valueOf(result.getDateOfBirth()),
                        String.valueOf(result.getArtStartDate()), result.getTargetGroup(), String.valueOf(result.getDateOfDeduplication()),
                        String.valueOf(result.getMatchCount()), String.valueOf(result.getSubjectCount()),
                        String.valueOf(result.getIdentifierCount())
                ));

                switch (deduplicationType) {
                    case DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE:
                        rowData.add(String.valueOf(result.getBaselineCount()));
                        break;
                    case DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE:
                        rowData.add(String.valueOf(result.getBaselineCount()));
                        rowData.add(String.valueOf(result.getRecaptureOneCount()));
                        break;
                    case DeDuplicationConfigs.RECAPTURE_THREE_AND_TWO:
                        rowData.add(String.valueOf(result.getBaselineCount()));
                        rowData.add(String.valueOf(result.getRecaptureOneCount()));
                        rowData.add(String.valueOf(result.getRecaptureTwoCount()));
                        break;
                    case DeDuplicationConfigs.RECAPTURE_FOUR:
                        rowData.add(String.valueOf(result.getBaselineCount()));
                        rowData.add(String.valueOf(result.getRecaptureOneCount()));
                        rowData.add(String.valueOf(result.getRecaptureTwoCount()));
                        rowData.add(String.valueOf(result.getRecaptureThreeCount()));
                        break;
                    case DeDuplicationConfigs.RECAPTURE_FIVE:
                        rowData.add(String.valueOf(result.getBaselineCount()));
                        rowData.add(String.valueOf(result.getRecaptureOneCount()));
                        rowData.add(String.valueOf(result.getRecaptureTwoCount()));
                        rowData.add(String.valueOf(result.getRecaptureThreeCount()));
                        rowData.add(String.valueOf(result.getRecaptureFourCount()));
                        break;
                    case DeDuplicationConfigs.RECAPTURE_SIX:
                        rowData.add(String.valueOf(result.getBaselineCount()));
                        rowData.add(String.valueOf(result.getRecaptureOneCount()));
                        rowData.add(String.valueOf(result.getRecaptureTwoCount()));
                        rowData.add(String.valueOf(result.getRecaptureThreeCount()));
                        rowData.add(String.valueOf(result.getRecaptureFourCount()));
                        rowData.add(String.valueOf(result.getRecaptureFiveCount()));
                        break;
                    case DeDuplicationConfigs.RECAPTURE_SEVEN:
                        rowData.add(String.valueOf(result.getBaselineCount()));
                        rowData.add(String.valueOf(result.getRecaptureOneCount()));
                        rowData.add(String.valueOf(result.getRecaptureTwoCount()));
                        rowData.add(String.valueOf(result.getRecaptureThreeCount()));
                        rowData.add(String.valueOf(result.getRecaptureFourCount()));
                        rowData.add(String.valueOf(result.getRecaptureFiveCount()));
                        rowData.add(String.valueOf(result.getRecaptureSixCount()));
                        break;
                    case DeDuplicationConfigs.RECAPTURE_EIGHT:
                        rowData.add(String.valueOf(result.getBaselineCount()));
                        rowData.add(String.valueOf(result.getRecaptureOneCount()));
                        rowData.add(String.valueOf(result.getRecaptureTwoCount()));
                        rowData.add(String.valueOf(result.getRecaptureThreeCount()));
                        rowData.add(String.valueOf(result.getRecaptureFourCount()));
                        rowData.add(String.valueOf(result.getRecaptureFiveCount()));
                        rowData.add(String.valueOf(result.getRecaptureSixCount()));
                        rowData.add(String.valueOf(result.getRecaptureSevenCount()));
                        break;
                    case DeDuplicationConfigs.RECAPTURE_NINE:
                        rowData.add(String.valueOf(result.getBaselineCount()));
                        rowData.add(String.valueOf(result.getRecaptureOneCount()));
                        rowData.add(String.valueOf(result.getRecaptureTwoCount()));
                        rowData.add(String.valueOf(result.getRecaptureThreeCount()));
                        rowData.add(String.valueOf(result.getRecaptureFourCount()));
                        rowData.add(String.valueOf(result.getRecaptureFiveCount()));
                        rowData.add(String.valueOf(result.getRecaptureSixCount()));
                        rowData.add(String.valueOf(result.getRecaptureSevenCount()));
                        rowData.add(String.valueOf(result.getRecaptureEightCount()));
                        break;
                    case DeDuplicationConfigs.RECAPTURE_TEN:
                        rowData.add(String.valueOf(result.getBaselineCount()));
                        rowData.add(String.valueOf(result.getRecaptureOneCount()));
                        rowData.add(String.valueOf(result.getRecaptureTwoCount()));
                        rowData.add(String.valueOf(result.getRecaptureThreeCount()));
                        rowData.add(String.valueOf(result.getRecaptureFourCount()));
                        rowData.add(String.valueOf(result.getRecaptureFiveCount()));
                        rowData.add(String.valueOf(result.getRecaptureSixCount()));
                        rowData.add(String.valueOf(result.getRecaptureSevenCount()));
                        rowData.add(String.valueOf(result.getRecaptureEightCount()));
                        rowData.add(String.valueOf(result.getRecaptureNineCount()));
                        break;
                    default:
                        break;
                }

                writer.writeNext(rowData.toArray(new String[0]));
            }
            writer.flush();
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            log.error("Error ***** {}", e.getMessage());
            return null;
        }
    }

    public ByteArrayInputStream writeDeduplicationDetailReport(List<DeduplicationDetail> results) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
            // Writing header
            String[] headers = new String[]{
                    "Enrolled State", "Matched State",
                    "Enrolled LGA", "Matched LGA",
                    "Enrolled Facility Name", "Matched Facility Name",
                    "Enrolled DATIM ID", "Matched DATIM ID",
                    "Enrolled Patient ID", "Matched Patient ID",
                    "Enrolled Hospital Number", "Matched Hospital Number",
                    "Enrolled Unique ID", "Matched Unique ID",
                    "Enrolled Sex", "Matched Sex",
                    "Enrolled Date of Birth", "Matched Date of Birth",
                    "Enrolled Patient Finger Type", "Matched Patient Finger Type",
                    "Enrolled Target Group", "Matched Target Group",
                    "Enrolled ART Start Date", "Matched ART Start Date",
                    "Enrolled Surname", "Matched Surname",
                    "Enrolled First Name", "Matched First Name",
                    "Enrolled LGA of Residence", "Matched LGA of Residence",
                    "Score", "Date Of Deduplication"
            };
            writer.writeNext(headers);

            // Writing data
            for (DeduplicationDetail result : results) {
                writer.writeNext(new String[]{
                        result.getEnrolledState(), result.getMatchedState(),
                        result.getEnrolledLga(), result.getMatchedLga(),
                        result.getEnrolledFacilityName(), result.getMatchedFacilityName(),
                        result.getEnrolledDatimId(), result.getMatchedDatimId(),
                        result.getEnrolledPatientId(), result.getMatchedPatientId(),
                        result.getEnrolledHospitalNumber(), result.getMatchedHospitalNumber(),
                        result.getEnrolledUniqueId(), result.getMatchedUniqueId(),
                        result.getEnrolledSex(), result.getMatchedSex(),
                        String.valueOf(result.getEnrolledDateOfBirth()), String.valueOf(result.getMatchedDateOfBirth()),
                        result.getEnrolledPatientFingerType(), result.getMatchedPatientFingerType(),
                        result.getEnrolledTargetGroup(), result.getMatchedTargetGroup(),
                        String.valueOf(result.getEnrolledArtStartDate()), String.valueOf(result.getMatchedArtStartDate()),
                        result.getEnrolledSurname(), result.getMatchedSurname(),
                        result.getEnrolledFirstName(), result.getMatchedFirstName(),
                        result.getEnrolledLgaOfResidence(), result.getMatchedLgaOfResidence(),
                        String.valueOf(result.getScore()), String.valueOf(result.getDateOfDeduplication())
                });
            }
            writer.flush();
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            log.error("Error ***** {}", e.getMessage());
            return null;
        }
    }
}
