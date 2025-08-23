package org.ecews.biometricapp.repositories.mappers;

import org.ecews.biometricapp.entities.PatientInfo;
import org.ecews.biometricapp.entities.dtos.CurrentRecaptureDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrentRecaptureRowMapper implements RowMapper<CurrentRecaptureDTO> {
    @Override
    public CurrentRecaptureDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

        CurrentRecaptureDTO cr = new CurrentRecaptureDTO();
        cr.setPatientId(rs.getString("uuid"));
        cr.setSurname(rs.getString("surname"));
        cr.setFirstName(rs.getString("first_name"));
        cr.setSex(rs.getString("sex"));
        cr.setArtStartDate((rs.getDate("date_started") != null) ? rs.getDate("date_started").toLocalDate() : null);
        cr.setDateOfBirth((rs.getDate("date_of_birth") != null) ? rs.getDate("date_of_birth").toLocalDate() : null);
        cr.setAge(rs.getInt("age"));
        cr.setDatimId(rs.getString("datimId"));
        cr.setCurrentRecapture(rs.getInt("current_recapture"));
        cr.setDateOfCurrentRecapture((rs.getDate("date_of_birth") != null) ? rs.getDate("date_of_birth").toLocalDate() : null);
        return cr;
    }
}
