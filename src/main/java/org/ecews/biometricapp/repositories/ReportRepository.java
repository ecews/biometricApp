package org.ecews.biometricapp.repositories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.dtos.BiometricLongitudinalDTO;
import org.ecews.biometricapp.entities.dtos.CurrentRecaptureDTO;
import org.ecews.biometricapp.entities.dtos.DeduplicationDetail;
import org.ecews.biometricapp.entities.dtos.DeduplicationSummary;
import org.ecews.biometricapp.repositories.mappers.DeduplicationDetailRowMapper;
import org.ecews.biometricapp.repositories.mappers.DeduplicationSummaryRowMapper;
import org.ecews.biometricapp.repositories.mappers.LongitudinalBiometricRowMapper;
import org.ecews.biometricapp.repositories.queries.ReportQueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class ReportRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public ReportRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<DeduplicationSummary> getSummaryReport(String deduplicationType) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("deduplicationType", deduplicationType);
        return namedParameterJdbcTemplate.query(ReportQueries.REPORT_QUERY_SUMMARY, parameters, new DeduplicationSummaryRowMapper());
    }


    public List<DeduplicationDetail> getDetailReport(String deduplicationType) {
        return jdbcTemplate.query(ReportQueries.DETAIL_REPORT_QUERY, new Object[]{deduplicationType}, new DeduplicationDetailRowMapper());
    }

    public List<JsonNode> getDynamicQueryResults() {
        String sql = "SELECT result FROM generate_dynamic_recapture_query()";
        return jdbcTemplate.query(sql, new RowMapper<JsonNode>() {
            @Override
            public JsonNode mapRow(ResultSet rs, int rowNum) throws SQLException {
                String json = rs.getString("result");
                try {
                    return objectMapper.readTree(json);
                } catch (Exception e) {
                    throw new SQLException("Error parsing JSON", e);
                }
            }
        });
    }

    public List<CurrentRecaptureDTO> getPatientData() {
        return jdbcTemplate.query(ReportQueries.REPORT_CURRENT_RECAPTURE, new BeanPropertyRowMapper<>(CurrentRecaptureDTO.class));
    }

    public List<BiometricLongitudinalDTO> getLongitudinalRecaptureReport() {
        return jdbcTemplate.query(ReportQueries.BIOMETRIC_LONGITUDINAL_REPORT, new LongitudinalBiometricRowMapper());
    }
}
