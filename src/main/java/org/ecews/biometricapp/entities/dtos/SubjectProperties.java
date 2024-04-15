package org.ecews.biometricapp.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectProperties {
    String id;
    String templateType;
    String personUUid;
    Integer recapture;
}
