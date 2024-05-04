package org.ecews.biometricapp.services;

import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.dtos.DeduplicationDetail;
import org.ecews.biometricapp.entities.dtos.DeduplicationSummary;
import org.ecews.biometricapp.repositories.ReportRepository;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final JdbcTemplate jdbcTemplate;

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
