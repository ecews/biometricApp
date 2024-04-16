package org.ecews.biometricapp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ndr_message_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NdrMessageLog {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "identifier", nullable = false, length = 64)
    private String identifier;

    @Column(name = "file", nullable = false, length = 128)
    private String file;
    
    @Column(name = "file_type", nullable = false, length = 128)
    private String fileType;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public NdrMessageLog(String identifier, String file, LocalDateTime lastUpdated)
    {
        this.identifier = identifier;
        this.file = file;
        this.lastUpdated = lastUpdated;

    }
}
