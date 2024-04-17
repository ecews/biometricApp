package org.ecews.biometricapp.entities.dtos;

import lombok.Data;

@Data
public class NDRPingResponseDTO {
    String serverDate;
    String serverTime;
    String serverTimeZone;
}
