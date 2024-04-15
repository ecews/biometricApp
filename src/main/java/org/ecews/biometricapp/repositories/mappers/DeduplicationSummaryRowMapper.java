package org.ecews.biometricapp.repositories.mappers;

import org.ecews.biometricapp.entities.dtos.DeduplicationSummary;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DeduplicationSummaryRowMapper implements RowMapper<DeduplicationSummary> {
    @Override
    public DeduplicationSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
        DeduplicationSummary obj = new DeduplicationSummary();
        obj.setPersonUuid(rs.getString("person_uuid"));
        obj.setDateOfDeduplication(rs.getDate("date_of_deduplication").toLocalDate());
        obj.setIdentifierCount(rs.getInt("identifier_count"));
        obj.setMatchCount(rs.getInt("match_count"));
        obj.setNoMatchCount(rs.getInt("no_match_count"));
        obj.setSubjectCount(rs.getInt("subject_count"));
        obj.setLga(rs.getString("lga"));
        obj.setState(rs.getString("state"));
        obj.setPatientId(rs.getString("patientId"));
        obj.setHospitalNumber(rs.getString("hospital_number"));
        obj.setUniqueId(rs.getString("uniqueId"));
        obj.setAge(rs.getInt("age"));
        obj.setSex(rs.getString("sex"));
        obj.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        obj.setFacilityName(rs.getString("facility_name"));
        obj.setDatimId(rs.getString("datimId"));
        obj.setTargetGroup(rs.getString("targetGroup"));
        obj.setEnrollmentSetting(rs.getString("enrollment_setting"));
        obj.setArtStartDate(rs.getDate("art_start_date").toLocalDate());
        obj.setRegimenAtArtStart(rs.getString("regimen_at_art_start"));
        obj.setDateOfRegistration(rs.getDate("date_of_registration").toLocalDate());
        obj.setSurname(rs.getString("surname"));
        obj.setFirstName(rs.getString("first_name"));
        obj.setLgaOfResidence(rs.getString("lgaOfResidence"));
        return obj;
    }
}
