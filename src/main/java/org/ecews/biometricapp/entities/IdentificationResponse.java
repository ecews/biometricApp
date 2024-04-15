package org.ecews.biometricapp.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.ecews.biometricapp.entities.dtos.MatchedPair;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity(name = "identification_response")
@Data
public class IdentificationResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String personUuid;
    LocalDate dateOfDeduplication;
    Integer matchCount;
    Integer noMatchCount;
    String deduplicationType;
    Integer subjectCount;
    Integer identifierCount;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    List<MatchedPair> matchedPairs;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    Set<String> deduplicatedIds;
}
