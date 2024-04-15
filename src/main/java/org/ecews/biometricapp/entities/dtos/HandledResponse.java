package org.ecews.biometricapp.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandledResponse {
    Integer matchCount = 0;
    Integer noMatchCount = 0;
    Integer subjectCount = 0;
    Integer identifierCount = 0;
    List<MatchedPair> matchedPairs;

}
