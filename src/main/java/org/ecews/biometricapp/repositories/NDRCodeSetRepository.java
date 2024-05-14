package org.ecews.biometricapp.repositories;


import org.ecews.biometricapp.entities.NDRCodeSet;
import org.ecews.biometricapp.entities.dtos.ArtCommencementDTO;
import org.ecews.biometricapp.entities.dtos.BiometricDto;
import org.ecews.biometricapp.entities.dtos.RecaptureBiometricDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NDRCodeSetRepository extends JpaRepository<NDRCodeSet, String> {
    Optional<NDRCodeSet> getNDRCodeSetByCodeSetNmAndSysDescription(String codeSetNm, String sysDescription);
    
    Optional<NDRCodeSet> getNDRCodeSetBySysDescription(String sysDescription);

    Optional<NDRCodeSet> getNDRCodeSetByCodeDescription(String codeDescription);

    @Query(value = "select r.regimen from hiv_regimen_resolver r where r.regimensys = ? limit 1", nativeQuery = true)
    Optional<String> getNDREquivalentRegimenUsingSystemRegimen(String systemRegimen);
    
 

    @Query(value = "select template_type as templateType, enrollment_date as enrollmentDate, template, image_quality as quality from biometric where person_uuid = :patientUuid" +
            " and biometric_type = 'FINGERPRINT' and archived = 0  and recapture = 0 and version_iso_20 = true  and iso = true", nativeQuery = true)
    List<BiometricDto> getPatientBiometricByPatientUuid(String patientUuid);
    
    
    @Query(value = "SELECT template_type as templateType, \n" +
            "enrollment_date as enrollmentDate,\n" +
            "recapture as count,\n" +
            "template,\n" +
            "image_quality as quality,\n" +
            "hashed as templateTypeHash\n" +
            "FROM biometric where person_uuid = ?1 \n" +
            "AND biometric_type = 'FINGERPRINT' and archived = 0\n" +
            "AND recapture = ?2\n" +
            "AND version_iso_20 = true  and iso = true\n" +
            "AND enrollment_date >= ?3 \n" +
            "ORDER BY enrollment_date DESC LIMIT 10;", nativeQuery = true)
    List<RecaptureBiometricDTO> getPatientRecapturedBiometricByPatientUuid(String patientUuid, Integer recapture, LocalDate previousUploadDate);
    
    @Query(value =
            """
                    select * from (SELECT template_type as templateType,
                                     enrollment_date as enrollmentDate,
                                     recapture as count,
                                     template,
                                     image_quality as quality,
                                     hashed as templateTypeHash,
                                     ROW_NUMBER() OVER (PARTITION BY person_uuid, template_type ORDER BY enrollment_date DESC) AS rank
                                     FROM biometric where person_uuid = ?1
                                     AND biometric_type = 'FINGERPRINT' and archived = 0
                                     AND recapture = ?2
                                     AND version_iso_20 = true  and iso = true) b where b.rank = 1
                            AND templateType not in ('Left Thumb', 'Right Thumb', 'Left Index', 'Right Index', 'Left Little', 'Right Little',
                            						'Left Middle', 'Right Middle', 'Left Ring', 'Right Ring')
                              """, nativeQuery = true)
    List<RecaptureBiometricDTO> getPatientRecapturedBiometricByPatientUuid(String patientUuid, Integer recapture);
    
   
   @Query(value = "SELECT cd_4 as cd4, cd_4_percentage as cd4Percentage, arc.visit_date AS artStartDate,sgn.body_weight AS bodyWeight,\n" +
           "sgn.height, who.display AS whoStage, funstatus.display AS functionStatus, \n" +
           "reg.description AS regimen \n" +
           "FROM  hiv_art_clinical arc \n" +
           "INNER JOIN triage_vital_sign sgn ON arc.visit_id = sgn.visit_id \n" +
           "LEFT JOIN base_application_codeset who ON who.id = arc.who_staging_id \n" +
           "LEFT JOIN base_application_codeset funstatus ON  funstatus.id = arc.functional_status_id \n" +
           "LEFT JOIN hiv_regimen reg ON reg.id = arc.regimen_id\n" +
           "WHERE is_commencement IS TRUE \n" +
           "AND arc.archived = 0 "+
           "AND  arc.person_uuid = ?1 ORDER BY arc.visit_date DESC LIMIT 1", nativeQuery = true)
   Optional<ArtCommencementDTO> getArtCommencementByPatientUuid(String patientUuid);
   // -- and i.deduplication_type= ?3 and i.identifier_count != 0 -- and jsonb_array_length(i.matched_pairs) >= 0
   @Query(value =
           """ 
               select distinct b.person_uuid from biometric b
                         where b.facility_id = ?1
                         and b.recapture = ?2
                         and b.archived = 0 and enrollment_date between '2023-10-31'<= current_date
                   """, nativeQuery = true)
   Iterable<String> getRecapturedPatientIds(Long facilityId, Integer recaptureType, String deduplicationType);
   
   @Query(value = "select person_uuid from hiv_art_pharmacy \n" +
           "where last_modified_date > ?1\n" +
           "and archived = 0\n" +
           "and  facility_id = ?2", nativeQuery = true)
   List<String> getNDREligiblePatientUuidUpdatedListByLastModifyDate(LocalDateTime lastModifyDate, Long facilityId);
   
   
    



}
