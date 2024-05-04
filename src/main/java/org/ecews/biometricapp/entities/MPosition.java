package org.ecews.biometricapp.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "m_positions", schema = "public")
@Data
public class MPosition {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "person_uuid")
    private  String personUuid;

    @Column(name = "m_index", nullable = false)
    private String mIndex;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @PrePersist
    public void prePersist (){
        createdDate = LocalDateTime.now();
        lastModifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}
