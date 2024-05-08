package org.ecews.biometricapp.entities.dtos;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BiometricFullDTO {

    private String id;
    private String personUuid;
    private String template;
    private String biometricType;
    private String templateType;
    private LocalDate date;
    private Integer archived;
    private Boolean iso;
    private String deviceName;
    private Long facilityId;
    private String reason;
    private Boolean versionIso20;
    private Integer imageQuality;
    private Integer recapture;
    private String recaptureMessage;
    private String hashed;
    private Integer count;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;

}
