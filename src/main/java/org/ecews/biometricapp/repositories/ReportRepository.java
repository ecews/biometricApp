package org.ecews.biometricapp.repositories;

import org.ecews.biometricapp.entities.dtos.DeduplicationDetail;
import org.ecews.biometricapp.entities.dtos.DeduplicationSummary;
import org.ecews.biometricapp.repositories.mappers.DeduplicationDetailRowMapper;
import org.ecews.biometricapp.repositories.mappers.DeduplicationSummaryRowMapper;
import org.ecews.biometricapp.repositories.queries.ReportQueries;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DeduplicationSummary> getSummaryReport(String deduplicationType) {
        return jdbcTemplate.query(ReportQueries.SUMMARY_REPORT_QUERY, new Object[]{deduplicationType}, new DeduplicationSummaryRowMapper());
    }


    public List<DeduplicationDetail> getDetailReport(String deduplicationType) {
        return jdbcTemplate.query(ReportQueries.DETAIL_REPORT_QUERY, new Object[]{deduplicationType}, new DeduplicationDetailRowMapper());
    }
}
