package org.ecews.biometricapp.services;

import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.dtos.DeduplicationDetail;
import org.ecews.biometricapp.entities.dtos.DeduplicationSummary;
import org.ecews.biometricapp.repositories.ReportRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public ByteArrayInputStream  generateSummaryReport (String deduplicationType) {
        var data = reportRepository.getSummaryReport(deduplicationType);
        return writeDeduplicationResultsToCsvInMemory(data);
    }

    public ByteArrayInputStream  generateDetailReport (String deduplicationType) {
        var data = reportRepository.getDetailReport(deduplicationType);
        var filtered = data.stream().filter(f -> f.getMatchedPatientId() != null).collect(Collectors.toList());
        return writeDeduplicationDetailReport(filtered);
    }

    public ByteArrayInputStream writeDeduplicationResultsToCsvInMemory(List<DeduplicationSummary> results) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
            // Writing header
            String [] headers = new String[]{"State", "LGA", "Facility", "Datim Code", "Patient Id", "Surname", "First Name", "Sex", "LGA of Residence",
                    "Hospital Number", "Unique Id", "Date of Birth", "ART Start Date", "Target Group", "Date of Deduplication", "Match Count", "No Match Count", "Subject Count", "Identifier Count"
            };
            writer.writeNext(headers);

            // Writing data
            for (DeduplicationSummary result : results) {
                writer.writeNext(new String[]{result.getState(), result.getLga(), result.getFacilityName(),
                        result.getDatimId(), result.getPatientId(), result.getSurname(), result.getFirstName(), result.getSex(),
                        result.getLgaOfResidence(), result.getHospitalNumber(), result.getUniqueId(), String.valueOf(result.getDateOfBirth()),
                        String.valueOf(result.getArtStartDate()), result.getTargetGroup(), String.valueOf(result.getDateOfDeduplication()), String.valueOf(result.getMatchCount()), String.valueOf(result.getNoMatchCount()),
                        String.valueOf(result.getSubjectCount()), String.valueOf(result.getIdentifierCount())
                });
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
