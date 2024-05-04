package org.ecews.biometricapp.entities.dtos;

import lombok.Data;

@Data
public class CapturedBiometricDto {
    private String id;
    private byte[] template;
    private String templateType;
    private String hashed;
    private Integer imageQuality;
    private String device;
    private String base64Image;
}