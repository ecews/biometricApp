package org.ecews.biometricapp.entities.dtos;

import lombok.Data;

@Data
public class NDRAuthRequestDTO {
    String email;
    String password;
    String baseUrl;
}
