package org.ecews.biometricapp.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ecews.biometricapp.entities.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportFileDTO {
    List<MPosition> mPositionList;
    List<BiometricFullDTO> sysBackups;
    List<IdentificationResponse> identificationResponses;
    List<InterventionResponse> interventionResponses;
    List<RecaptureStatus> recaptureStatuses;
}
