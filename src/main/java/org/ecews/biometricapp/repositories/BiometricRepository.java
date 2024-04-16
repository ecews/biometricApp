package org.ecews.biometricapp.repositories;

import org.ecews.biometricapp.entities.Biometric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BiometricRepository extends JpaRepository<Biometric, String> {

    @Query(value = """
        with patients as (
           select * from recapture_status where status = 'NO_MATCH'
        ),
        recaptureIds as (
            select cast(jsonb_array_elements(r.deduplicated_ids) as text) as ids from (select  deduplicated_ids, ROW_NUMBER() OVER (PARTITION BY person_uuid ORDER BY date_of_deduplication DESC) AS rank
            from identification_response where deduplication_type = ?2) r where r.rank = 1
        )
        select b.* from patients p
            join biometric b on b.person_uuid = p.patient_id where recapture = ?1 and archived = 0
        and b.id not in (select substr(ids, 2, length(ids) - 2) from recaptureIds);
""", nativeQuery = true)
    List<Biometric> getNoMatchFingerprints(Long recapture, String deduplicationType);

    @Query(value = """
        with patients as (
           select * from recapture_status where status = ?2
        )
        select b.* from patients p
            join biometric b on b.person_uuid = p.patient_id where recapture = ?1 and archived = 0
""", nativeQuery = true)
    List<Biometric> getFingerprintsByNDRStatus(Long recapture, String status);

    @Query(value = """
                with recaptureIds as (
                    select cast(jsonb_array_elements(r.deduplicated_ids) as text) as ids from (select  deduplicated_ids, ROW_NUMBER() OVER (PARTITION BY person_uuid ORDER BY date_of_deduplication DESC) AS rank
                    from identification_response where deduplication_type = ?2) r where r.rank = 1
                )
                select * from biometric where archived = 0 and recapture = ?1 and id not in (select substr(ids, 2, length(ids) - 2) from recaptureIds)
            """, nativeQuery = true)
    List<Biometric> getFingerprints(Long recapture, String deduplicationType);

    @Query(value = """
        select * from biometric where archived = 0 and recapture = ?1
""", nativeQuery = true)
    List<Biometric> getFingerprints(Long recapture);


}
