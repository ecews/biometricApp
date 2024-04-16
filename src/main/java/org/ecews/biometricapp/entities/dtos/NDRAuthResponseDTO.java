package org.ecews.biometricapp.entities.dtos;

import lombok.Data;

@Data
public class NDRAuthResponseDTO {
    String token;
    Boolean isAuthenticated;
    Integer code;
}
