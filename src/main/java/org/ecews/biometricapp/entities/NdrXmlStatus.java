package org.ecews.biometricapp.entities;


import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Entity
@Table(name = "ndr_xml_status")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NdrXmlStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Column(name = "files", nullable = false)
    private Integer files;

    @Column(name = "file_name", nullable = false)
    private String  fileName;

    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModified;

    private String pushIdentifier;

    private Long percentagePushed;

//    private Boolean completelyPushed;
//    @Type(type = "jsonb")
//    @Column(name = "error", columnDefinition = "jsonb")
//    private JsonNode error;

    @Column(name = "type", nullable = false, length = 128)
    private String type;

}
