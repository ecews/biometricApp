package org.ecews.biometricapp.repositories;

import org.ecews.biometricapp.entities.Biometric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
        select * from biometric where archived = 0 and recapture <= ?1
""", nativeQuery = true)
    List<Biometric> getFingerprints(Long recapture);

    @Query(value = """
                with recaptureIds as (
                    select cast(jsonb_array_elements(r.deduplicated_ids) as text) as ids from (
                        select  deduplicated_ids, ROW_NUMBER() OVER (PARTITION BY person_uuid ORDER BY date_of_deduplication DESC) AS rank
                                from identification_response where deduplication_type = ?2 and jsonb_array_length(matched_pairs) > 0) r where r.rank = 1
                )
                select * from biometric where archived = 0 and recapture = ?1 and id not in (select substr(ids, 2, length(ids) - 2) from recaptureIds)
            """, nativeQuery = true)
    List<Biometric> getMatchedFingerprints(Long recapture, String deduplicationType);

    @Query(value = """
        select distinct person_uuid from identification_response ir
            where deduplication_type = :deduplicationType
            and jsonb_array_length(ir.matched_pairs) <= 2 or matched_pairs is null
        limit (select count(distinct person_uuid) * :percentage from identification_response
                where deduplication_type = :deduplicationType and (jsonb_array_length(matched_pairs) <= 2 or matched_pairs is null))
""", nativeQuery = true)
    Set<String> getClientForIntervention(@Param("deduplicationType") String deduplicationType, @Param("percentage") Double percentage);

    @Query(value = """
        select * from biometric where archived = 0 and person_uuid in :clients and recapture in :recaptures and version_iso_20 = true
""", nativeQuery = true)
    List<Biometric> getClientPrintsForIntervention (@Param("clients") Set<String> clients, @Param("recaptures") Set<Integer> recaptures);

    @Query(value = """
        UPDATE biometric SET template = :template, hashed = :hashed where id = :id 
""", nativeQuery = true)
    void updateBiometric(@Param("id") String id, @Param("template") byte[] template, @Param("hashed") String hashed);

    @Query(value = """
         select * from (select b.* from biometric b
                     inner join (select distinct person_uuid from sys_backup where backup_date = :backupDate) bk on bk.person_uuid = b.person_uuid
                     where b.archived = 0 and b.recapture in :recaptures and b.version_iso_20 = true) t
         where t.person_uuid not in (select distinct person_uuid from intervention_response where date_of_deduplication = :backupDate and deduplication_type = :deduplicationType)
""", nativeQuery = true)
    List<Biometric> getInterventionPrintsForDeduplication (@Param("backupDate") LocalDate backupDate,
                                                           @Param("recaptures") Set<Integer> recaptures,
                                                           @Param("deduplicationType") String deduplicationType);
    @Query(value = """
        select * from biometric where person_uuid = :personUuid and recapture in :recaptures and archived = 0 and version_iso_20 = true
""", nativeQuery = true)
    List<Biometric> getPersonBiometrics(@Param("personUuid") String personUuid, @Param("recaptures") Set<Integer> recaptures);

}
