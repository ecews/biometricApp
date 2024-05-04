package org.ecews.biometricapp.repositories;

import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.PatientInfo;
import org.ecews.biometricapp.repositories.mappers.DeduplicationSummaryRowMapper;
import org.ecews.biometricapp.repositories.mappers.PatientInfoRowMapper;
import org.ecews.biometricapp.repositories.queries.ReportQueries;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Repository
public class AllRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AllRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PatientInfo> getPatient (String search) {
        String query = """
                select p.uuid, p.surname, p.first_name, p.date_of_birth, p.gender->>'display' as sex, h.date_started
                from patient_person p
                join hiv_enrollment h on h.person_uuid = p.uuid
                where (p.hospital_number ilike :search and p.archived = 0) OR
                   (h.archived = 0 and h.unique_id ilike :search);
                """;
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("search", "%" + search + "%");
        return jdbcTemplate.query(query, parameters, (rs, rowNum) -> {
            String uuid = rs.getString("uuid");
            String surname = rs.getString("surname");
            String firstName = rs.getString("first_name");
            LocalDate dateOfBirth = rs.getDate("date_of_birth").toLocalDate();
            String sex = rs.getString("sex");
            LocalDate dateStarted = rs.getDate("date_started").toLocalDate();

            return new PatientInfo(uuid, surname, firstName, dateOfBirth, sex, dateStarted);
        });
    }
}
