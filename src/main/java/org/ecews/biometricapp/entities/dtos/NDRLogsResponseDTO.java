package org.ecews.biometricapp.entities.dtos;

import lombok.Data;

import java.util.List;

@Data
public class NDRLogsResponseDTO {
    Integer code;
    String message;
    List<NDRMessageLogDTO> messageLogs;
}
