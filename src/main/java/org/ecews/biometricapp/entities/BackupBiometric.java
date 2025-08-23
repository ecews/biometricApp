package org.ecews.biometricapp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity(name = "backup_biometric")
public class BackupBiometric {
    @Id
    private String id;
    private String personUuid;
    private byte[] template;
    private String templateType;
    private String hashed;
    private Integer imageQuality;
    private String device;
    @Column(length = 1000)
    private String base64Image;
    private Integer level;
    private Boolean used;
    private LocalDate dateCreated;
}
