package org.ecews.biometricapp.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandledResponse {
    List<MatchedPair> matchedPairs;
}
