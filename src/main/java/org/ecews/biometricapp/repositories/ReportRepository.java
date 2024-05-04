package org.ecews.biometricapp.repositories;

import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.dtos.DeduplicationDetail;
import org.ecews.biometricapp.entities.dtos.DeduplicationSummary;
import org.ecews.biometricapp.repositories.mappers.DeduplicationDetailRowMapper;
import org.ecews.biometricapp.repositories.mappers.DeduplicationSummaryRowMapper;
import org.ecews.biometricapp.repositories.queries.ReportQueries;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class ReportRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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
}
