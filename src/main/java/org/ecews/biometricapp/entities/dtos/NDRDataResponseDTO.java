package org.ecews.biometricapp.entities.dtos;

import lombok.Data;

@Data
public class NDRDataResponseDTO {
    Integer code;
    String batchNumber;
    String message;
    Boolean isAuthenticated;
}
