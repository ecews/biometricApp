package org.ecews.biometricapp.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.*;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.LocalDate;


@Entity(name = "biometric")
@SQLDelete(sql = "delete from biometric where id = ?", check = ResultCheckStyle.COUNT)
@Where(clause = "archived = 0")
@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
public class Biometric implements Serializable, Persistable<String> {

    @Id
    private String id;

    @Column(name = "person_uuid")
    private String personUuid;

    @NotNull
    private byte[] template;

    @Column(name = "biometric_type")
    @NotNull
    private String biometricType;

    @Column(name = "template_type")
    @NotNull
    private String templateType;

    @Column(name = "enrollment_date")
    @NotNull
    private LocalDate date;

    private Integer archived = 0;

    private Boolean iso = false;

    /*@Column(columnDefinition = "jsonb")
    @JsonbProperty("extra")
    private Object extra;*/

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "facility_id")
    private Long facilityId;

    private String reason;

    @Column(name = "version_iso_20")
    private Boolean versionIso20;

    @Column(name = "image_quality")
    private Integer imageQuality=0;

    @Column(name = "recapture")
    private Integer recapture;

    @Column(name = "recapture_message")
    private String recaptureMessage;

    @Column(name = "hashed")
    private String hashed;

    @Column(name = "count")
    private Integer count;

    @Override
    public boolean isNew() {
        return id == null;
    }

}
