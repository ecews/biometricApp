package org.ecews.biometricapp.entities.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class NdrXmlStatusDto implements Serializable {
    private  Integer id;
    private  Integer files;
    private  String fileName;
    private  LocalDateTime lastModified;
}
