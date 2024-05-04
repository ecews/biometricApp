package org.ecews.biometricapp.repositories.mappers;

import org.ecews.biometricapp.entities.PatientInfo;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PatientInfoRowMapper implements RowMapper<PatientInfo> {
    @Override
    public PatientInfo mapRow(ResultSet rs, int rowNum) throws SQLException {

        PatientInfo patientInfo = new PatientInfo();
        patientInfo.setUuid(rs.getString("uuid"));
        patientInfo.setSurname(rs.getString("surname"));
        patientInfo.setFirstName(rs.getString("first_name"));
        patientInfo.setSex(rs.getString("sex"));
        patientInfo.setDateStarted(rs.getDate("date_started").toLocalDate());
        patientInfo.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        return patientInfo;
    }
}
