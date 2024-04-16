package org.ecews.biometricapp.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "ndr_messages", schema = "public")
@Data
@NoArgsConstructor
public class NDRMessages implements Serializable, Persistable<Long> {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate messageDate;
    private String deMessage;
    private Long facilityId;
    private Boolean isPushed;
    private String identifier;
    private String batchNumber;

    @Override
    public boolean isNew() {
        return false;
    }
}