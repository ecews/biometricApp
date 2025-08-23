package org.ecews.biometricapp.repositories.mappers;

import org.ecews.biometricapp.entities.dtos.DeduplicationDetail;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DeduplicationDetailRowMapper implements RowMapper<DeduplicationDetail> {
    @Override
    public DeduplicationDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        DeduplicationDetail result = new DeduplicationDetail();
        result.setEnrolledLga(rs.getString("enrolled_lga"));
        result.setEnrolledState(rs.getString("enrolled_state"));
        result.setEnrolledPatientId(rs.getString("enrolled_patient_id"));
        result.setEnrolledHospitalNumber(rs.getString("enrolled_hospital_number"));
        result.setEnrolledUniqueId(rs.getString("enrolled_unique_id"));
        result.setEnrolledSex(rs.getString("enrolled_sex"));
        result.setEnrolledDateOfBirth((rs.getDate("enrolled_date_of_birth") != null) ? rs.getDate("enrolled_date_of_birth").toLocalDate() : null);
        result.setEnrolledPatientFingerType(rs.getString("enrolled_patient_finger_type"));
        result.setEnrolledFacilityName(rs.getString("enrolled_facility_name"));
        result.setEnrolledDatimId(rs.getString("enrolled_datimId"));
        result.setEnrolledTargetGroup(rs.getString("enrolled_target_group"));
        result.setEnrolledArtStartDate((rs.getDate("enrolled_art_start_date") != null) ? rs.getDate("enrolled_art_start_date").toLocalDate() : null);
        result.setEnrolledSurname(rs.getString("enrolled_surname"));
        result.setEnrolledFirstName(rs.getString("enrolled_first_name"));
        result.setEnrolledLgaOfResidence(rs.getString("enrolled_lga_of_residence"));

        result.setMatchedLga(rs.getString("matched_lga"));
        result.setMatchedState(rs.getString("matched_state"));
        result.setMatchedPatientId(rs.getString("matched_patient_id"));
        result.setMatchedHospitalNumber(rs.getString("matched_hospital_number"));
        result.setMatchedUniqueId(rs.getString("matched_unique_id"));
        result.setMatchedSex(rs.getString("matched_sex"));
        result.setMatchedDateOfBirth((rs.getDate("matched_date_of_birth") != null) ? rs.getDate("matched_date_of_birth").toLocalDate() : null);
        result.setMatchedPatientFingerType(rs.getString("matched_patient_finger_type"));
        result.setMatchedFacilityName(rs.getString("matched_facility_name"));
        result.setMatchedDatimId(rs.getString("matched_datimId"));
        result.setMatchedTargetGroup(rs.getString("matched_target_group"));
        result.setMatchedArtStartDate((rs.getDate("matched_art_start_date") != null) ? rs.getDate("matched_art_start_date").toLocalDate() : null);
        result.setMatchedSurname(rs.getString("matched_surname"));
        result.setMatchedFirstName(rs.getString("matched_first_name"));
        result.setMatchedLgaOfResidence(rs.getString("matched_lga_of_residence"));

        result.setScore(rs.getInt("score"));
        result.setDateOfDeduplication((rs.getDate("date_of_deduplication") != null) ? rs.getDate("date_of_deduplication").toLocalDate() : null);

        return result;
    }
}
