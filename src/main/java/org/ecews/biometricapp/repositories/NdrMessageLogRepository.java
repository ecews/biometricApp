package org.ecews.biometricapp.repositories;


import org.ecews.biometricapp.entities.NdrMessageLog;
import org.ecews.biometricapp.entities.dtos.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NdrMessageLogRepository extends JpaRepository<NdrMessageLog, Integer> {
    List<NdrMessageLog> getNdrMessageLogByIdentifier(String identifier);
    
    Optional<NdrMessageLog> findFirstByIdentifier(String identifier);
    
    Optional<NdrMessageLog> findFirstByIdentifierAndFileType(String identifier, String fileType);
    @Query(value="SELECT\n" +
            "                    DISTINCT (p.uuid) AS personUuid, p.date_of_registration AS diagnosisDate,\n" +
            "                             p.date_of_birth AS dateOfBirth,\n" +
            "                             p.id AS personId,\n" +
            "                             p.hospital_number AS hospitalNumber,\n" +
            "             concat( boui.code,'_', p.uuid) as patientIdentifier,\n" +
            "                            EXTRACT(YEAR FROM AGE(NOW(), date_of_birth)) AS age,\n" +
            "                             (CASE WHEN INITCAP(p.sex)='Female' THEN 'F' ELSE 'M' END) AS patientSexCode,\n" +
            "                             p.date_of_birth AS patientDateOfBirth, 'FAC' AS facilityTypeCode,\n" +
            "                             facility.name AS facilityName,\n" +
            "                            facility_lga.name AS lga,\n" +
            "                             facility_state.name AS state,\n" +
            "                             boui.code AS facilityId,\n" +
            "                             hac.visit_date AS artStartDate,\n" +
            "                            hrr.regimen AS firstARTRegimenCodeDescTxt,\n" +
            "            ncs.code AS firstARTRegimenCode,\n" +
            "            lgaCode.code AS lgaCode,\n" +
            "            enrollStatus.display AS statusAtRegistration,\n" +
            "            stateCode.code AS stateCode,\n" +
            "            'NGA' AS countryCode,\n" +
            "             emplCode.code AS patientOccupationCode,\n" +
            "             mariCode.code AS PatientMaritalStatusCode,\n" +
            "             stateCode.code AS stateOfNigeriaOriginCode,\n" +
            "            eduCode.code AS patientEducationLevelCode,\n" +
            "            ndrTbstatus.code AS tbStatus,\n" +
            "            COALESCE(ndrFuncStatCodestatus.code, ndrClinicStage.code) AS functionalStatusStartART,\n" +
            "            CASE WHEN hpt.reason_for_discountinuation = 'Death' THEN hpt.cause_of_death ELSE NULL END AS causeOfDeath\n" +
            "               FROM\n" +
            "                    patient_person p\n" +
            "                       INNER JOIN base_organisation_unit facility ON facility.id = facility_id\n" +
            "                        INNER JOIN base_organisation_unit facility_lga ON facility_lga.id = facility.parent_organisation_unit_id\n" +
            "                       INNER JOIN base_organisation_unit facility_state ON facility_state.id = facility_lga.parent_organisation_unit_id\n" +
            "                       INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id = facility_id AND boui.name ='DATIM_ID'\n" +
            "                        INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
            "                        INNER JOIN hiv_art_clinical hac ON hac.hiv_enrollment_uuid = h.uuid AND hac.archived = 0\n" +
            "                      INNER JOIN hiv_regimen hr ON hr.id = hac.regimen_id\n" +
            "                        INNER JOIN hiv_regimen_type hrt ON hrt.id = hac.regimen_type_id\n" +
            "            INNER JOIN hiv_regimen_resolver hrr ON hrr.regimensys=hr.description\n" +
            "            INNER JOIN ndr_code_set ncs ON ncs.code_description=hrr.regimen\n" +
            "           LEFT JOIN ndr_code_set lgaCode ON trim(lgaCode.code_description)=trim(facility_lga.name) and lgaCode.code_set_nm = 'LGA'\n" +
            "            LEFT JOIN base_application_codeset enrollStatus ON enrollStatus.id= h.status_at_registration_id\n" +
            "            LEFT JOIN ndr_code_set stateCode ON trim(stateCode.code_description)=trim(facility_state.name) and  stateCode.code_set_nm = 'STATES'\n" +
            "            LEFT JOIN ndr_code_set emplCode ON emplCode.code_description=p.employment_status->>'display' and emplCode.code_set_nm = 'OCCUPATION_STATUS'\n" +
            "            LEFT JOIN ndr_code_set mariCode ON mariCode.code_description=p.marital_status->>'display' and mariCode.code_set_nm = 'MARITAL_STATUS'\n" +
            "            LEFT JOIN ndr_code_set eduCode ON  eduCode.code_description=p.education->>'display' and eduCode.code_set_nm = 'EDUCATIONAL_LEVEL'\n" +
            "           LEFT JOIN base_application_codeset fsCodeset ON fsCodeset.id=hac.functional_status_id\n" +
            "           LEFT JOIN base_application_codeset tbCodeset ON tbCodeset.id=h.tb_status_id\n" +
            "            LEFT JOIN base_application_codeset csCodeset ON csCodeset.id=hac.clinical_stage_id\n" +
            "            LEFT JOIN ndr_code_set ndrFuncStatCodestatus ON ndrFuncStatCodestatus.code_description=fsCodeset.display\n" +
            "            LEFT JOIN ndr_code_set ndrTbstatus ON trim(ndrTbstatus.code_description)=trim(tbCodeset.display)\n" +
            "            LEFT JOIN ndr_code_set ndrClinicStage ON ndrClinicStage.code_description=csCodeset.display\n" +
            "           LEFT JOIN hiv_patient_tracker hpt ON hpt.person_uuid = p.uuid\n" +
            "               WHERE h.archived = 0\n" +
            "             AND p.uuid = ?1\n" +
            "               AND h.facility_id = ?2\n" +
            "               AND hac.is_commencement = TRUE LIMIT 1\n" ,
            nativeQuery = true)
    Optional<PatientDemographicDTO> getPatientDemographics(String identifier, Long facilityId);
    
    @Query(value = "SELECT hac.person_uuid as patientUuid, cast( json_agg(distinct  jsonb_build_object('visitID', hac.uuid,\n" +
            "\t\t\t\t\t\t\t\t\t  'visitDate', CAST(hac.visit_date AS DATE),\n" +
            "\t\t\t\t\t\t\t\t\t  'weight',  CASE WHEN tvs.body_weight IS NULL THEN 0 ELSE tvs.body_weight END,\n" +
            "\t\t\t\t\t\t\t\t\t  'childHeight', CASE WHEN tvs.height IS NULL THEN 0 ELSE tvs.height END,\n" +
            "\t\t\t\t\t\t\t\t\t   'tbStatus', ncs.code,\n" +
            "\t\t\t\t\t\t\t\t\t\t'bloodPressure', \n" +
            "\t\t\t\t\t\t\t\t\t\t(CASE WHEN tvs.systolic IS NOT NULL AND tvs.diastolic IS NOT NULL \n" +
            "\t\t\t\t\t\t\t\t\t\tTHEN CONCAT(CAST(tvs.systolic AS INTEGER), '/', CAST(tvs.diastolic AS INTEGER))\n" +
            "\t\t\t\t\t\t\t\t\t\tELSE '' END),\n" +
            "\t\t\t\t\t\t\t\t\t  'nextAppointmentDate', hac.next_appointment)) as varchar) AS encounters\n" +
            "\tFROM hiv_art_clinical hac \n" +
            "\tLEFT JOIN triage_vital_sign tvs ON hac.vital_sign_uuid=tvs.uuid AND hac.archived=0\n" +
            "\tLEFT JOIN base_application_codeset bac_tb ON bac_tb.id=CAST(hac.tb_status AS BIGINT) AND bac_tb.archived=0\n" +
            "\tLEFT JOIN ndr_code_set ncs ON ncs.code_description=bac_tb.display\n" +
            "\tWHERE hac.archived = 0\n" +
            "\t  And hac.person_uuid = ?1\n" +
            "      AND hac.facility_id = ?2\n" +
            "      AND hac.visit_date >= ?3\n" +
            "      AND hac.visit_date <= ?4\n" +
            "\tGROUP BY hac.person_uuid", nativeQuery = true)
    Optional<PatientEncounterDTO> getPatientEncounter(String identifier, Long facilityId, LocalDate start, LocalDate end);

    @Query(value = "SELECT hac.person_uuid as patientUuid, cast( json_agg(distinct  jsonb_build_object('visitID', hac.uuid,\n" +
            "\t\t\t\t\t\t\t\t\t  'visitDate', CAST(hac.visit_date AS DATE),\n" +
            "\t\t\t\t\t\t\t\t\t  'weight',  CASE WHEN tvs.body_weight IS NULL THEN 0 ELSE tvs.body_weight END,\n" +
            "\t\t\t\t\t\t\t\t\t  'childHeight', CASE WHEN tvs.height IS NULL THEN 0 ELSE tvs.height END,\n" +
            "\t\t\t\t\t\t\t\t\t   'tbStatus', ncs.code,\n" +
            "\t\t\t\t\t\t\t\t\t\t'bloodPressure', \n" +
            "\t\t\t\t\t\t\t\t\t\t(CASE WHEN tvs.systolic IS NOT NULL AND tvs.diastolic IS NOT NULL \n" +
            "\t\t\t\t\t\t\t\t\t\tTHEN CONCAT(CAST(tvs.systolic AS INTEGER), '/', CAST(tvs.diastolic AS INTEGER))\n" +
            "\t\t\t\t\t\t\t\t\t\tELSE '' END),\n" +
            "\t\t\t\t\t\t\t\t\t  'nextAppointmentDate', hac.next_appointment)) as varchar) AS encounters\n" +
            "\tFROM hiv_art_clinical hac \n" +
            "\tLEFT JOIN triage_vital_sign tvs ON hac.vital_sign_uuid=tvs.uuid AND hac.archived=0\n" +
            "\tLEFT JOIN base_application_codeset bac_tb ON bac_tb.id=CAST(hac.tb_status AS BIGINT) AND bac_tb.archived=0\n" +
            "\tLEFT JOIN ndr_code_set ncs ON ncs.code_description=bac_tb.display\n" +
            "\tWHERE hac.archived = 0\n" +
            "\t  And hac.person_uuid = ?1\n" +
            "      AND hac.facility_id = ?2\n" +
            "\tGROUP BY hac.person_uuid, hac.visit_date order by hac.visit_date desc limit 1", nativeQuery = true)
    Optional<PatientEncounterDTO> getPatientLastEncounter(String identifier, Long facilityId);

   @Query(value = "SELECT person_uuid, cast(json_agg(DISTINCT  jsonb_build_object('visitID', phar.uuid,\n" +
           "\t\t\t\t\t\t\t\t\t  'visitDate', phar.visitDate,\n" +
           "\t\t\t\t\t\t\t\t\t  'prescribedRegimenCode',  phar.prescribedRegimenCode,\n" +
           "\t\t\t\t\t\t\t\t\t  'prescribedRegimenCodeDescTxt', phar.prescribedRegimenCodeDescTxt,\n" +
           "\t\t\t\t\t\t\t\t\t   'prescribedRegimenTypeCode', (CASE WHEN regimen_type_id=8 THEN 'OI' ELSE 'ART' END),\n" +
           "\t\t\t\t\t\t\t \t\t\t'prescribedRegimenDuration', phar.duration,\n" +
           "  'dateRegimenStarted', phar.visitDate))as varchar) AS regimens\n" +
           "FROM (SELECT DISTINCT pharmacy.person_uuid, pharmacy.uuid, pharmacy.visit_date AS visitDate,\n" +
           "pharmacy_object ->> 'name' as name, cast(pharmacy_object ->> 'duration' as VARCHAR) as duration,\n" +
           "hr.regimen_type_id, (CASE WHEN hrr.regimen IS NULL THEN hr.description ELSE hrr.regimen END) AS prescribedRegimenCodeDescTxt, \n" +
           "(CASE WHEN ncs_reg.code IS NULL THEN ncs_others.code ELSE ncs_reg.code END)AS prescribedRegimenCode\n" +
           "FROM hiv_art_pharmacy pharmacy,\n" +
           "jsonb_array_elements(extra->'regimens') with ordinality p(pharmacy_object)\n" +
           "INNER JOIN hiv_regimen hr ON hr.description=CAST(pharmacy_object ->> 'name' AS VARCHAR) \n" +
           "AND hr.regimen_type_id IN (1,2,3,4,14,8)\n" +
           "LEFT JOIN hiv_regimen_resolver hrr ON hrr.regimensys=hr.description\n" +
           "LEFT JOIN ndr_code_set ncs_reg ON ncs_reg.code_description=hrr.regimen\n" +
           "LEFT JOIN ndr_code_set ncs_others ON ncs_others.code_description=hr.description --12662\n" +
           "AND hr.regimen_type_id NOT IN (1,2,3,4,14)\n" +
           "\t  WHERE pharmacy.archived = 0\n" +
           "\t  AND  pharmacy.person_uuid = ?1\n" +
           "      AND pharmacy.facility_id = ?2\n" +
           "      AND pharmacy.visit_date >= ?3\n" +
           "      AND pharmacy.visit_date <= ?4\n" +
           "\t \n" +
           "\t ) phar\n" +
           "\tGROUP BY person_uuid", nativeQuery = true)
   Optional<PatientPharmacyEncounterDTO> getPatientPharmacyEncounter(String identifier, Long facilityId, LocalDate start, LocalDate end);

    @Query(value = "SELECT phar.person_uuid, \n" +
            "       CAST(json_agg(DISTINCT jsonb_build_object('visitID', phar.uuid,\n" +
            " 'visitDate', phar.visitDate,\n" +
            " 'prescribedRegimenCode', phar.prescribedRegimenCode,\n" +
            " 'prescribedRegimenCodeDescTxt', phar.prescribedRegimenCodeDescTxt,\n" +
            " 'prescribedRegimenTypeCode', phar.prescribedRegimenTypeCode,\n" +
            " 'prescribedRegimenDuration', phar.duration,\n" +
            " 'dateRegimenStarted', phar.visitDate)) AS varchar) AS regimens\n" +
            "FROM (\n" +
            "    SELECT pharmacy.person_uuid, \n" +
            "           pharmacy.uuid, \n" +
            "           pharmacy.visit_date AS visitDate,\n" +
            "           pharmacy_object ->> 'name' AS name, \n" +
            "           CAST(pharmacy_object ->> 'duration' AS VARCHAR) AS duration,\n" +
            "           hr.regimen_type_id, \n" +
            "           (CASE WHEN hrr.regimen IS NULL THEN hr.description ELSE hrr.regimen END) AS prescribedRegimenCodeDescTxt, \n" +
            "           (CASE WHEN ncs_reg.code IS NULL THEN ncs_others.code ELSE ncs_reg.code END) AS prescribedRegimenCode,\n" +
            "           (CASE WHEN regimen_type_id=8 THEN 'OI' ELSE 'ART' END) AS prescribedRegimenTypeCode\n" +
            "    FROM hiv_art_pharmacy pharmacy\n" +
            "    JOIN (\n" +
            "        SELECT person_uuid, MAX(visit_date) AS max_visit_date\n" +
            "        FROM hiv_art_pharmacy\n" +
            "        WHERE archived = 0\n" +
            "         AND  person_uuid = ?1\n" +
            "          AND facility_id = ?2\n" +
            "        GROUP BY person_uuid\n" +
            "    ) max_dates ON pharmacy.person_uuid = max_dates.person_uuid AND pharmacy.visit_date = max_dates.max_visit_date\n" +
            "    CROSS JOIN LATERAL jsonb_array_elements(extra->'regimens') WITH ORDINALITY p(pharmacy_object)\n" +
            "    JOIN hiv_regimen hr ON hr.description = CAST(pharmacy_object ->> 'name' AS VARCHAR) \n" +
            "        AND hr.regimen_type_id IN (1,2,3,4,14,8)\n" +
            "    LEFT JOIN hiv_regimen_resolver hrr ON hrr.regimensys = hr.description\n" +
            "    LEFT JOIN ndr_code_set ncs_reg ON ncs_reg.code_description = hrr.regimen\n" +
            "    LEFT JOIN ndr_code_set ncs_others ON ncs_others.code_description = hr.description\n" +
            "    WHERE pharmacy.archived = 0\n" +
            ") phar\n" +
            "GROUP BY phar.person_uuid;", nativeQuery = true)
    Optional<PatientPharmacyEncounterDTO> getPatientLastPharmacyEncounter(String identifier, Long facilityId);

   @Query(value = "SELECT DISTINCT person_uuid FROM hiv_art_pharmacy ph\n" +
           "           INNER JOIN patient_person p ON p.uuid = ph.person_uuid\n" +
           "           LEFT JOIN laboratory_result lr ON lr.patient_uuid = ph.person_uuid\n" +
           "           LEFT JOIN hiv_status_tracker hst ON hst.person_id = ph.person_uuid\n" +
           "           WHERE ph.archived = 0 AND p.archived = 0 AND hst.archived = 0\n" +
           "           AND (ph.last_modified_date between ?1 and ?2) \n" +
           "           OR (lr.date_result_reported between ?1 and ?2)\n" +
           "           OR (lr.date_modified  between ?1 and ?2)\n" +
           "           OR (hst.last_modified_date  between ?1 and ?2) \n" +
           "           AND ph.facility_id = ?3", nativeQuery = true)
   List<String>  getPatientIdsEligibleForNDR(LocalDateTime start, LocalDateTime endDate,  long facilityId);
   
  @Query(value = "SELECT\n" +
          "    lo.patient_uuid,\n" +
          "    CAST(\n" +
          "        json_agg(\n" +
          "            DISTINCT jsonb_build_object(\n" +
          "                'visitId', lo.visitid,\n" +
          "                'visitDate', lo.visitdate,\n" +
          "                'collectionDate', ls.collectiondate,\n" +
          "                'laboratoryTestIdentifier', lt.laboratorytestidentifier,\n" +
          "                'laboratoryTestTypeCode', lt.laboratorytesttypecode,\n" +
          "                'orderedTestDate', lo.orderedtestdate,\n" +
          "                'laboratoryResultedTestCode', lt.laboratoryresultedtestcode,\n" +
          "                'laboratoryResultedTestCodeDescTxt', lt.laboratoryresultedtestcodedesctxt,\n" +
          "                'laboratoryResultAnswerNumeric', lr.laboratoryresultanswernumeric,\n" +
          "                'resultedTestDate', lr.resultedtestdate\n" +
          "            )\n" +
          "        ) AS VARCHAR\n" +
          "    ) AS labs\n" +
          "FROM (\n" +
          "    SELECT\n" +
          "        uuid AS VisitID,\n" +
          "        id,\n" +
          "        CAST(lo.order_date AS DATE) AS OrderedTestDate,\n" +
          "        CAST(lo.order_date AS DATE) AS VisitDate,\n" +
          "        lo.patient_uuid\n" +
          "    FROM\n" +
          "        laboratory_order lo\n" +
          "    WHERE\n" +
          "        lo.order_date IS NOT NULL\n" +
          "        AND lo.archived = 0\n" +
          "        AND lo.facility_id = ?2\n" +
          "        AND lo.order_date >= ?3\n" +
          "        AND lo.order_date <= ?4\n" +
          "        AND lo.patient_uuid = ?1\n" +
          ") lo\n" +
          "INNER JOIN (\n" +
          "\t\n" +
          "\t SELECT\n" +
          "    lt.id,\n" +
          "    lt.lab_order_id,\n" +
          "    lt.lab_test_id,\n" +
          "    lt.lab_test_group_id,\n" +
          "    lt.patient_uuid,\n" +
          "    llt.modified_lab_test_name,  -- Using a modified name for comparison\n" +
          "    lt.lab_order_id AS LaboratoryTestIdentifier,\n" +
          "    testncs.code AS LaboratoryTestTypeCode,\n" +
          "    testncs.code AS LaboratoryResultedTestCode,\n" +
          "    testncs.code_description AS LaboratoryResultedTestCodeDescTxt\n" +
          "FROM\n" +
          "    laboratory_test lt\n" +
          "INNER JOIN (\n" +
          "    SELECT\n" +
          "        id,\n" +
          "        labtestgroup_id,\n" +
          "        CASE\n" +
          "           WHEN lab_test_name ilike 'Gene Xpert' THEN 'Other Test (TB-LAM, LF-LAM, etc)'\n" +
          "\t       WHEN lab_test_name = 'TB-LAM' THEN 'Other Test (TB-LAM, LF-LAM, etc)'\n" +
          "\t       WHEN lab_test_name = 'LF-LAM' THEN 'Other Test (TB-LAM, LF-LAM, etc)'\n" +
          "\t       WHEN lab_test_name = 'Visitect CD4' THEN 'CD4 LFA RESULT'\n" +
          "            ELSE lab_test_name\n" +
          "        END AS modified_lab_test_name\n" +
          "    FROM\n" +
          "        laboratory_labtest\n" +
          ") llt ON llt.id = lt.lab_test_id\n" +
          "LEFT JOIN\n" +
          "    ndr_code_set testncs ON TRIM(LOWER(llt.modified_lab_test_name)) = TRIM(LOWER(testncs.code_description))\n" +
          "WHERE\n" +
          "    lt.archived = 0\n" +
          "    AND testncs.code_set_nm = 'LAB_RESULTED_TEST'\n" +
          "    AND lt.facility_id = ?2\n" +
          ") lt ON lt.lab_order_id = lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
          "INNER JOIN (\n" +
          "    SELECT\n" +
          "        DISTINCT CAST(ls.date_sample_collected AS DATE) AS CollectionDate,\n" +
          "        ls.patient_uuid,\n" +
          "        test_id\n" +
          "    FROM\n" +
          "        laboratory_sample ls\n" +
          "    WHERE\n" +
          "        ls.archived = 0\n" +
          "        AND ls.facility_id = ?2\n" +
          "        AND ls.date_sample_collected IS NOT NULL\n" +
          "        AND ls.date_sample_collected >= ?3\n" +
          "        AND ls.date_sample_collected <= ?4\n" +
          "        AND ls.patient_uuid = ?1\n" +
          ") ls ON ls.test_id = lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
          "INNER JOIN (\n" +
          "    SELECT\n" +
          "        DISTINCT CAST(lr.date_result_reported AS DATE) AS resultedTestDate,\n" +
          "        lr.patient_uuid,\n" +
          "        lr.result_reported AS LaboratoryResultAnswerNumeric,\n" +
          "        lr.test_id\n" +
          "    FROM\n" +
          "        laboratory_result lr\n" +
          "    WHERE\n" +
          "        lr.archived = 0\n" +
          "        AND lr.facility_id = ?2\n" +
          "        AND lr.date_result_reported IS NOT NULL\n" +
          "        AND lr.result_reported IS NOT NULL\n" +
          "        AND lr.date_result_reported >= ?3\n" +
          "        AND lr.date_result_reported <= ?4\n" +
          "        AND lr.patient_uuid = ?1\n" +
          ") lr ON lr.test_id = lt.id AND lr.patient_uuid = lt.patient_uuid\n" +
          "GROUP BY lo.patient_uuid", nativeQuery = true)
  Optional<PatientLabEncounterDTO> getPatientLabEncounter(String identifier, Long facilityId, LocalDate start, LocalDate end);


    @Query(value = "SELECT\n" +
            "    lo.patient_uuid,\n" +
            "    lr.resultedtestdate,\n" +
            "    CAST(\n" +
            "        json_agg(\n" +
            "            DISTINCT jsonb_build_object(\n" +
            "                'visitId', lo.visitid,\n" +
            "                'visitDate', lo.visitdate,\n" +
            "                'collectionDate', ls.collectiondate,\n" +
            "                'laboratoryTestIdentifier', lt.laboratorytestidentifier,\n" +
            "                'laboratoryTestTypeCode', lt.laboratorytesttypecode,\n" +
            "                'orderedTestDate', lo.orderedtestdate,\n" +
            "                'laboratoryResultedTestCode', lt.laboratoryresultedtestcode,\n" +
            "                'laboratoryResultedTestCodeDescTxt', lt.laboratoryresultedtestcodedesctxt,\n" +
            "                'laboratoryResultAnswerNumeric', lr.laboratoryresultanswernumeric,\n" +
            "                'resultedTestDate', lr.resultedtestdate\n" +
            "            )\n" +
            "        ) AS VARCHAR\n" +
            "    ) as labs\n" +
            "FROM (\n" +
            "    SELECT\n" +
            "        uuid AS VisitID,\n" +
            "        id,\n" +
            "        CAST(lo.order_date AS DATE) AS OrderedTestDate,\n" +
            "        CAST(lo.order_date AS DATE) AS VisitDate,\n" +
            "        lo.patient_uuid\n" +
            "    FROM laboratory_order lo\n" +
            "    WHERE\n" +
            "        lo.order_date IS NOT NULL\n" +
            "        AND lo.archived = 0\n" +
            "        AND lo.facility_id = ?2\n" +
            "        AND lo.patient_uuid = ?1\n" +
            ") lo\n" +
            "INNER JOIN (\n" +
            "    SELECT\n" +
            "    lt.id,\n" +
            "    lt.lab_order_id,\n" +
            "    lt.lab_test_id,\n" +
            "    lt.lab_test_group_id,\n" +
            "    lt.patient_uuid,\n" +
            "    llt.modified_lab_test_name,  -- Using a modified name for comparison\n" +
            "    lt.lab_order_id AS LaboratoryTestIdentifier,\n" +
            "    testncs.code AS LaboratoryTestTypeCode,\n" +
            "    testncs.code AS LaboratoryResultedTestCode,\n" +
            "    testncs.code_description AS LaboratoryResultedTestCodeDescTxt\n" +
            "FROM\n" +
            "    laboratory_test lt\n" +
            "INNER JOIN (\n" +
            "    SELECT\n" +
            "        id,\n" +
            "        labtestgroup_id,\n" +
            "        CASE\n" +
            "           WHEN lab_test_name ilike 'Gene Xpert' THEN 'Other Test (TB-LAM, LF-LAM, etc)'\n" +
            "\t       WHEN lab_test_name = 'TB-LAM' THEN 'Other Test (TB-LAM, LF-LAM, etc)'\n" +
            "\t       WHEN lab_test_name = 'LF-LAM' THEN 'Other Test (TB-LAM, LF-LAM, etc)'\n" +
            "\t       WHEN lab_test_name = 'Visitect CD4' THEN 'CD4 LFA RESULT'\n" +
            "            ELSE lab_test_name\n" +
            "        END AS modified_lab_test_name\n" +
            "    FROM\n" +
            "        laboratory_labtest\n" +
            ") llt ON llt.id = lt.lab_test_id\n" +
            "INNER JOIN\n" +
            "    ndr_code_set testncs ON TRIM(LOWER(llt.modified_lab_test_name)) = TRIM(LOWER(testncs.code_description))\n" +
            "WHERE\n" +
            "    lt.archived = 0\n" +
            "    AND testncs.code_set_nm = 'LAB_RESULTED_TEST'\n" +
            "    AND lt.facility_id = ?2\n" +
            ") lt ON lt.lab_order_id = lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
            "INNER JOIN (\n" +
            "    SELECT\n" +
            "        DISTINCT CAST(ls.date_sample_collected AS DATE) AS CollectionDate,\n" +
            "        ls.patient_uuid,\n" +
            "        test_id\n" +
            "    FROM laboratory_sample ls\n" +
            "    WHERE\n" +
            "        ls.archived = 0\n" +
            "       AND ls.facility_id = ?2\n" +
            "        AND ls.date_sample_collected IS NOT NULL\n" +
            "        AND ls.patient_uuid = ?1\n" +
            ") ls ON ls.test_id = lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
            "INNER JOIN (\n" +
            "    SELECT\n" +
            "        DISTINCT CAST(lr.date_result_reported AS DATE) AS resultedTestDate,\n" +
            "        lr.patient_uuid,\n" +
            "        lr.result_reported AS LaboratoryResultAnswerNumeric,\n" +
            "        lr.test_id\n" +
            "    FROM laboratory_result lr\n" +
            "    WHERE\n" +
            "        lr.archived = 0\n" +
            "        AND lr.facility_id = ?2\n" +
            "        AND lr.date_result_reported IS NOT NULL\n" +
            "        AND lr.result_reported IS NOT NULL\n" +
            "        AND lr.patient_uuid = ?1\n" +
            ") lr ON lr.test_id = lt.id AND lr.patient_uuid = lt.patient_uuid\n" +
            "GROUP BY lo.patient_uuid, lr.resultedtestdate\n" +
            "ORDER BY lr.resultedtestdate DESC\n" +
            "LIMIT 1", nativeQuery = true)
    Optional<PatientLabEncounterDTO> getPatientLastLabEncounter(String identifier, Long facilityId);


  //mortality query
  @Query(value = "SELECT\n" +
          "\t\tDISTINCT ON (p.uuid)\n" +
          "          p.uuid,\n" +
          "          COALESCE(hpt.uuid, ho.uuid, last_status.uuid, e.uuid ) AS visitId,\n" +
          "          COALESCE(CAST(hpt.attempts->0->> 'attemptDate' AS DATE), ho.date_of_observation, last_status.status_date, e.date_of_registration ) AS visitDate,\n" +
          "          hpt.reason_for_tracking,\n" +
          "          hpt.reason_for_tracking_others AS otherTrackingReason,\n" +
          "          CONCAT(p.contact->'contact'->0->>'surname', '', p.contact->'contact'->0->>'otherName') AS partnerFullName,\n" +
          "          TRANSLATE(CAST(p.contact->'contact'->0->'address'->>'line' AS VARCHAR), '\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\",[\\\\\\\\\\\\\\\\]', ' ') AS addressofTreatmentSupporter,\n" +
          "          (p.contact->'contact'->0->'contactPoint'->>'value') AS contactPhoneNumber,\n" +
          "          hpt.date_last_appointment AS dateofLastActualContact,\n" +
          "          hpt.date_missed_appointment AS dateofMissedScheduledAppointment,\n" +
          "          CAST(hpt.attempts->0->> 'attemptDate' AS DATE) AS datePatientContacted,\n" +
          "          hpt.attempts->0->> 'whoAttemptedContact' AS nameofPersonWhoAttemptedContact,\n" +
          "          hpt.attempts->0->> 'modeOfConatct' AS modeofCommunication,\n" +
          "          hpt.attempts->0->> 'personContacted' AS personContacted,\n" +
          "          hpt.attempts->0->> 'reasonForDefaulting' AS reasonforDefaulting,\n" +
          "          hpt.attempts->0->> 'reasonForDefaultingOthers' AS otherReasonforDefaulting,\n" +
          "          CASE WHEN last_status.HIV_STATUS = 'LOST_TO_FOLLOWUP' THEN TRUE ELSE FALSE END AS losttoFollowup,\n" +
          "          hpt.reason_for_loss_to_follow_up AS reasonforLosttoFollowup,\n" +
          "          CASE WHEN last_status.HIV_STATUS = 'LOST_TO_FOLLOWUP' THEN last_status.status_date ELSE NULL END AS dateLosttoFollowup,\n" +
          "          NULL AS previousARVExposure,\n" +
          "          hpt.date_of_discontinuation AS dateofTermination,\n" +
          "          hpt.reason_for_discountinuation AS reasonforTermination,\n" +
          "          NULL AS transferredOutTo,\n" +
          "          CASE WHEN last_status.HIV_STATUS IN ('Died (Confirmed)', 'KNOWN_DEATH') THEN last_status.HIV_STATUS ELSE NULL END AS death,\n" +
          "          last_status.va_cause_of_death_type AS vaCauseofDeath,\n" +
          "          hpt.cause_of_death_others AS otherCauseofDeath,\n" +
          "          last_status.va_cause_of_death AS causeOfDeath,\n" +
          "          hpt.reason_for_discountinuation AS discontinuedCare,\n" +
          "          NULL AS discontinueCareOtherSpecify,\n" +
          "          hpt.date_return_to_care AS dateReturnedtoCare,\n" +
          "          hpt.referred_for AS reffferedFor,\n" +
          "          hpt.referred_for_others AS reffferedForOther,\n" +
          "          NULL AS nameofContactTracer,\n" +
          "          CAST(NULL AS DATE) AS contactTrackerSignatureDate\n" +
          "          FROM\n" +
          "          patient_person p\n" +
          "          INNER JOIN\n" +
          "          hiv_enrollment e ON p.uuid = e.person_uuid\n" +
          "          LEFT JOIN (\n" +
          "          SELECT DISTINCT ON (person_uuid) *\n" +
          "          FROM hiv_patient_tracker\n" +
          "          ) hpt ON hpt.person_uuid = e.person_uuid\n" +
          "          left JOIN (\n" +
          "          SELECT\n" +
          "          person_uuid,\n" +
          "          visit_id,\n" +
          "\t\t  date_of_observation,\n" +
          "          uuid,\n" +
          "          data->'attempt'->0->>'outcome' AS clientVerificationStatus,\n" +
          "          CAST(data->'attempt'->0->>'dateOfAttempt' AS DATE) AS dateOfOutcome,\n" +
          "          (data->>'ClientVerificationOther') AS ClientVerificationOther,\n" +
          "          CAST(TRANSLATE((data->>'anyOfTheFollowing'), '\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"[]', ' ') AS VARCHAR) AS indicationforClientVerification,\n" +
          "          ROW_NUMBER() OVER (PARTITION BY person_uuid ORDER BY CAST(data->'attempt'->0->>'dateOfAttempt' AS DATE) DESC) AS rn\n" +
          "          FROM\n" +
          "          public.hiv_observation\n" +
          "          WHERE\n" +
          "          type = 'Client Verification'\n" +
          "          AND archived = 0\n" +
          "          ) ho ON ho.person_uuid = e.person_uuid AND ho.rn = 1\n" +
          "          left JOIN (\n" +
          "          SELECT\n" +
          "\t\t\t  DISTINCT ON (person_id)\n" +
          "          person_id,\n" +
          "          hiv_status,\n" +
          "          status_date,\n" +
          "          visit_id,\n" +
          "          uuid,\n" +
          "          va_cause_of_death,\n" +
          "          va_cause_of_death_type,\n" +
          "          ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC) AS rn\n" +
          "          FROM\n" +
          "          hiv_status_tracker\n" +
          "          WHERE\n" +
          "          archived = 0\n" +
          "          ) last_status ON last_status.person_id = e.person_uuid AND last_status.rn = 1\n" +
          "          WHERE\n" +
          "          p.archived = 0\n" +
          "          AND last_status.status_date <= ?4\n" +
          "          AND last_status.status_date >= ?3\n" +
          "          AND p.facility_id = ?2\n" +
          "          AND p.uuid = ?1", nativeQuery = true)
  List<MortalityDTO> getPatientMortalities(String identifier, Long facilityId, LocalDate start, LocalDate end);
  @Query(value = "SELECT person_uuid,\n" +
          "       MAX(CASE WHEN rn = 1 THEN data->>'serialEnrollmentNo' END) AS clientVerification,\n" +
          "       MAX(CASE WHEN rn = 1 THEN data->'attempt'->0->>'outcome' END) AS firstOutcome,\n" +
          "       MAX(CASE WHEN rn = 1 THEN data->'attempt'->0->>'verificationStatus' END) AS firstStatus,\n" +
          "       MAX(CASE WHEN rn = 2 THEN data->'attempt'->0->>'outcome' END) AS secondOutcome,\n" +
          "       MAX(CASE WHEN rn = 2 THEN data->'attempt'->0->>'verificationStatus' END) AS secondVerificationStatus,\n" +
          "       MAX(CASE WHEN rn = 3 THEN data->'attempt'->0->>'outcome' END) AS lastOutcome,\n" +
          "       MAX(CASE WHEN rn = 3 THEN data->'attempt'->0->>'verificationStatus' END) AS lastVerificationStatus,\n" +
          "       MAX(CASE WHEN rn = 1 THEN CAST(data->'attempt'->0->>'dateOfAttempt' AS DATE) END) AS ct1STDate,\n" +
          "\t   MAX(CASE WHEN rn = 2 THEN CAST(data->'attempt'->0->>'dateOfAttempt' AS DATE) END) AS ct2NdDate,\n" +
          "\t   MAX(CASE WHEN rn = 3 THEN CAST(data->'attempt'->0->>'dateOfAttempt' AS DATE) END) AS ctLastDate,\n" +
          "       MAX(CASE WHEN rn = 1 AND 'No initial fingerprint was captured' IN (SELECT jsonb_array_elements_text(anyThing)) THEN 'No initial biometric capture' END) AS noInitBiometric,\n" +
          "       MAX(CASE WHEN rn = 1 AND 'Duplicated demographic and clinical variables' IN (SELECT jsonb_array_elements_text(anyThing)) THEN 'Duplicated demographic and clinical variables' END) AS duplicatedDemographic,\n" +
          "       MAX(CASE WHEN rn = 1 AND 'Records of repeated clinical encounters, with no fingerprint recapture.' IN (SELECT jsonb_array_elements_text(anyThing)) THEN 'No biometrics recapture' END) AS noRecapture,\n" +
          "       MAX(CASE WHEN rn = 1 AND 'Last clinical visit is over 15 months prior' IN (SELECT jsonb_array_elements_text(anyThing)) THEN 'Last clinical visit is over 15 months prior' END) AS lastVisitIsOver18M,\n" +
          "       MAX(CASE WHEN rn = 1 AND 'Incomplete visit data on the care card or pharmacy forms or EMR ' IN (SELECT jsonb_array_elements_text(anyThing)) THEN 'Incomplete visit data on the care card or pharmacy forms or EMz' END) AS incompleteVisitData,\n" +
          "       MAX(CASE WHEN rn = 1 AND 'Records of repeated clinical encounters, with no fingerprint recapture.' IN (SELECT jsonb_array_elements_text(anyThing)) THEN 'Records of repeated clinical encounters, with no fingerprint recapture.' END) AS repeatEncounterNoPrint,\n" +
          "       MAX(CASE WHEN rn = 1 AND 'Long intervals between ARV pick-ups (pick-ups more than one year apart in the same facility)' IN (SELECT jsonb_array_elements_text(anyThing)) THEN 'Long intervals between ARV pick-ups (pick-ups more than one year apart in the same facility' END) AS LongIntervalsARVPickup,\n" +
          "       MAX(CASE WHEN rn = 1 AND 'Long intervals between ARV pick-ups (pick-ups more than one year apart in the same facility)' IN (SELECT jsonb_array_elements_text(anyThing)) THEN 'Long intervals between ARV pick-ups (pick-ups more than one year apart in the same facility' END) AS batchPickupDates,\n" +
          "       MAX(CASE WHEN rn = 1 AND 'Same sex, DOB and ART start date' IN (SELECT jsonb_array_elements_text(anyThing)) THEN 'Same sex, DOB and ART start date' END) AS sameSexDOBARTStartDate,\n" +
          "       MAX(CASE WHEN rn = 1 AND 'Consistently had drug pickup by proxy without viral load sample collection for two quarters' IN (SELECT jsonb_array_elements_text(anyThing)) THEN 'Consistently had drug pickup by proxy without viral load sample collection for two quarters' END) AS pickupByProxy,\n" +
          "       MAX(CASE WHEN rn = 1 AND 'Others' IN (SELECT jsonb_array_elements_text(anyThing)) THEN 'Others' END) AS otherSpecifyForCV\n" +
          "FROM (\n" +
          "    SELECT person_uuid,\n" +
          "           data->'anyOfTheFollowing' AS anyThing,\n" +
          "           date_of_observation,\n" +
          "           data,\n" +
          "           ROW_NUMBER() OVER (PARTITION BY person_uuid ORDER BY CAST(data->'attempt'->0->>'dateOfAttempt' AS DATE) DESC) AS rn\n" +
          "    FROM hiv_observation ho\n" +
          "    LEFT JOIN patient_person pp ON pp.uuid = ho.person_uuid\n" +
          "    WHERE type = 'Client Verification'\n" +
          "       AND pp.uuid = ?1\n" +
          "       AND pp.archived = 0\n" +
          "       AND ho.archived = 0\n" +
          "       AND ho.date_of_observation <= ?4\n" +
          "       AND ho.date_of_observation >= ?3\n" +
          "       AND pp.facility_id = ?2\n" +
          ") cc\n" +
          "GROUP BY person_uuid", nativeQuery = true)
  ClientVerificationDTO getClientVerification(String identifier, Long facilityId, LocalDate start, LocalDate end);
  @Query(value = "SELECT client_code from hts_client where facility_id=?1 AND date_modified > ?2 AND archived = 0 ", nativeQuery = true)
  List<String>getHtsClientCode(Long facilityId, LocalDateTime lastModified);

  @Query(value = "SELECT DISTINCT ON (hc.uuid) hc.uuid as uuid, hc.client_code AS clientCode,\n" +
          "             (CASE WHEN hc.person_uuid IS NULL THEN INITCAP(hc.extra->>'gender') ELSE INITCAP(pp.sex) END) AS sex, \n" +
          "        (CASE WHEN sex IS NULL THEN sexMigrated.code ELSE sex.code END) AS patientSexCode, \n" +
          "             (CASE WHEN hc.person_uuid IS NOT NULL THEN pp.date_of_birth       \n" +
          "             WHEN hc.person_uuid IS NULL AND LENGTH(hc.extra->>'date_of_birth') > 0    \n" +
          "                         AND hc.extra->>'date_of_birth' != '' THEN CAST(NULLIF(hc.extra->>'date_of_birth', '') AS DATE)        \n" +
          "             ELSE NULL END) AS dateOfBirth,             \n" +
          "              (CASE WHEN hc.person_uuid IS NULL THEN hc.extra->>'marital_status'       \n" +
          "             ELSE pp.marital_status->>'display' END) AS maritalStatus,  \n" +
          "              (CASE WHEN hc.person_uuid IS NULL THEN maritalMigrated.code       \n" +
          "             ELSE marital.code END) AS patientMaritalStatusCode, \n" +
          "             (CASE WHEN hc.person_uuid IS NULL       \n" +
          "             THEN hc.extra->>'lga_of_residence' ELSE null END) AS LGAOfResidence,       \n" +
          "             (CASE WHEN hc.person_uuid IS NULL        \n" +
          "              THEN hc.extra->>'state_of_residence' ELSE NULL END) AS StateOfResidence, \n" +
          "\n" +
          "              (CASE WHEN hc.person_uuid IS NULL        \n" +
          "              THEN stateOriginMigrated.code ELSE null END) AS stateOfNigeriaOriginCode, \n" +
          "\n" +
          "              'NGN' AS countryCode,\n" +
          "              'FAC' AS facilityCode,\n" +
          "              facility.name AS facilityName,       \n" +
          "              state.name AS state,   \n" +
          "              facilityState.code AS stateCode,\n" +
          "              lga.name AS lga,    \n" +
          "              facilityLga.code AS lgaCode,\n" +
          "              pp.uuid AS personUuid,\n" +
          "              (CASE WHEN pp.uuid IS NOT NULL OR pp.uuid !='' THEN CONCAT(pp.uuid, '_', boui.code)\n" +
          "              ELSE CONCAT(hc.uuid, '_', boui.code) END) AS PatientIdentifier,\n" +
          "              (CASE WHEN pp.hospital_number IS NOT NULL OR pp.hospital_number !='' THEN pp.hospital_number\n" +
          "              ELSE hc.client_code END) AS hospitalNumber,\n" +
          "             edu.code AS patientEducationLevelCode,        \n" +
          "             pp.employment_status->>'display' as occup, \n" +
          "             occupation.code AS patientOccupationCode,\n" +
          "             boui.code as facilityId,       \n" +
          "             hc.others->>'latitude' AS HTSLatitude,       \n" +
          "             hc.others->>'longitude' AS HTSLongitude,         \n" +
          "             (CASE WHEN hc.person_uuid IS NULL THEN hc.extra->>'client_address' ELSE null END) AS clientAddress,       \n" +
          "             hc.date_visit AS dateVisit,       \n" +
          "             (CASE WHEN hc.first_time_visit IS true THEN 'Yes' ELSE 'No' END) firstTimeVisit,       \n" +
          "             hc.num_children AS numberOfChildren,       \n" +
          "             hc.num_wives AS numberOfWives\n" +
          "                     FROM hts_client hc      \n" +
          "             LEFT JOIN patient_person pp ON pp.uuid=hc.person_uuid \n" +
          "             LEFT JOIN ndr_code_set sex ON TRIM(sex.code_description)=TRIM(pp.sex)\n" +
          "             LEFT JOIN ndr_code_set sexMigrated ON TRIM(sexMigrated.code_description)=TRIM(INITCAP(hc.extra->>'gender'))  \n" +
          "             LEFT JOIN ndr_code_set edu ON TRIM(edu.code_description) = TRIM(CAST(pp.education->>'display' AS VARCHAR))\n" +
          "             LEFT JOIN ndr_code_set marital ON TRIM(marital.code_description) = TRIM(CAST(pp.marital_status->>'display' AS VARCHAR)) \n" +
          "             LEFT JOIN ndr_code_set maritalMigrated ON TRIM(maritalMigrated.code_description) = TRIM(CAST(hc.extra->>'marital_status' AS VARCHAR))\n" +
          "             LEFT JOIN ndr_code_set occupation ON TRIM(occupation.code_description) = TRIM(CAST(pp.employment_status->>'display' AS VARCHAR))\n" +
          "             LEFT JOIN ndr_code_set stateOriginMigrated ON stateOriginMigrated.code_description = TRIM(CAST(hc.extra->>'state_of_residence' AS VARCHAR))      \n" +
          "             LEFT JOIN base_organisation_unit facility ON facility.id=hc.facility_id   \n" +
          "             LEFT JOIN base_organisation_unit lga ON lga.id=facility.parent_organisation_unit_id   \n" +
          "             LEFT JOIN base_organisation_unit state ON state.id=lga.parent_organisation_unit_id     \n" +
          "             LEFT JOIN ndr_code_set facilityState ON TRIM(facilityState.code_description) = TRIM(state.name) AND facilityState.code_set_nm = 'STATES'\n" +
          "             LEFT JOIN ndr_code_set facilityLga ON TRIM(facilityLga.code_description) = TRIM(lga.name) AND facilityLga.code_set_nm = 'LGA'\n" +
          "             LEFT JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=hc.facility_id AND boui.name='DATIM_ID'    \n" +
          "             WHERE hc.archived=0 AND hc.facility_id =?1 and hc.client_code =?2\n" +
          "\t\t\t AND hc.date_modified > ?3", nativeQuery = true)
  Optional<PatientDemographicDTO> getHtsPatientDemographics(long facilityId,  String clientCode, LocalDateTime lastModified);
//
//  @Query(value = "SELECT hc.person_uuid AS personUuid, hc.uuid as visitId, hc.testing_setting as setting, 'YES' as firstTimeVisit, hc.client_code AS clientCode, hc.date_visit  as visitDate, \n" +
//          "tr.screeningTestResult,\n" +
//          "tr.screeningTestResultDate, tr.confirmatoryTestResult, \n" +
//          "tr.confirmatoryTestResultDate, \n" +
//          "(CASE WHEN tr.tieBreakerTestResult IS NULL OR tr.tieBreakerTestResult='' THEN tr.confirmatoryTestResult\n" +
//          "ELSE tr.tieBreakerTestResult END) AS tieBreakerTestResult,\n" +
//          "(CASE WHEN tr.tieBreakerTestResult IS NULL OR tr.tieBreakerTestResult='' THEN tr.confirmatoryTestResultDate\n" +
//          "ELSE tr.tieBreakerTestResultDate END) AS tieBreakerTestResultDate,\n" +
//          "CAST(risk_assessment ->> 'everHadSexualIntercourse' AS BOOLEAN) AS \teverHadSexualIntercourse,\n" +
//          "CAST(risk_assessment ->> 'bloodTransfusionInLast3Months' AS BOOLEAN) AS bloodTransfussionInLast3Months,\n" +
//          "CAST(risk_assessment ->> 'unprotectedSexWithCasualPartnerInLast3Months' AS BOOLEAN) AS unprotectedSexWithCasualPartnerinLast3Months,\n" +
//          "CAST(risk_assessment ->> 'moreThan1SexPartnerDuringLast3Months' AS BOOLEAN) AS moreThan1SexPartnerDuringLast3Months,\n" +
//          "CAST(risk_assessment ->> 'stiInLast3Months' AS BOOLEAN) AS stiInLast3Months,\n" +
//          "\n" +
//          "--ClinicalTBScreeningType\n" +
//          "CAST(tb_screening ->> 'currentlyCough' AS BOOLEAN) AS currentlyCough,\n" +
//          "CAST(tb_screening ->> 'weightLoss' AS BOOLEAN) AS weightLoss,\n" +
//          "CAST(tb_screening ->> 'fever' AS BOOLEAN) AS fever,\n" +
//          "CAST(tb_screening ->> 'nightSweats' AS BOOLEAN) AS nightSweats,\n" +
//          "--SyndromicSTIScreeningType\n" +
////          "CAST(tb_screening ->> 'currentlyCough' AS BOOLEAN) AS currentlyCough,\n" +
////          "CAST(tb_screening ->> 'weightLoss' AS BOOLEAN) AS weightLoss,\n" +
////          "CAST(tb_screening ->> 'fever' AS BOOLEAN) AS fever,\n" +
////          "CAST(tb_screening ->> 'nightSweats' AS BOOLEAN) AS nightSweats, \n" +
//          "--PostTesting\n" +
//          "CASE WHEN CAST(post_test_counseling ->> 'hivTestBefore' AS VARCHAR) ILIKE '%Not%' THEN false\n" +
//          "\t ELSE true END AS testedForHIVBeforeWithinThisYear,\n" +
//          "CASE WHEN post_test_counseling ->> 'hivRequestResult'='' \n" +
//          "\t  OR post_test_counseling ->> 'hivRequestResult' ILIKE 'false' THEN FALSE ELSE true END AS hivRequestAndResultFormSignedByTester,\n" +
//          "CASE WHEN post_test_counseling ->> 'hivRequestResultCt'='' \n" +
//          "\t\tOR post_test_counseling ->> 'hivRequestResultCt' ILIKE 'false' THEN FALSE ELSE TRUE END AS hivRequestAndResultFormFilledWithCTIForm,\n" +
//          "CASE WHEN post_test_counseling ->> 'clientReceivedHivTestResult'=''\n" +
//          "\tOR post_test_counseling ->> 'clientReceivedHivTestResult' ILIKE 'false' THEN FALSE ELSE TRUE END AS clientRecievedHIVTestResult,\n" +
//          "CASE WHEN post_test_counseling ->> 'postTestCounseling' = ''\n" +
//          "OR post_test_counseling ->> 'postTestCounseling' ILIKE 'false' THEN FALSE ELSE TRUE END  AS postTestCounsellingDone,\n" +
//          "CASE WHEN post_test_counseling ->> 'riskReduction'=''\n" +
//          "OR post_test_counseling ->> 'riskReduction' ILIKE 'false' THEN  FALSE ELSE TRUE  END AS riskReductionPlanDeveloped,\n" +
//          "CASE WHEN post_test_counseling ->> 'postTestDisclosure' = ''\n" +
//          "OR post_test_counseling ->> 'postTestDisclosure' ='false' THEN FALSE ELSE TRUE  END AS postTestDisclosurePlanDeveloped,\n" +
//          "CASE WHEN post_test_counseling ->> 'bringPartnerHivtesting' =''\n" +
//          "OR post_test_counseling ->> 'bringPartnerHivtesting' ILIKE 'false'THEN FALSE ELSE TRUE  END AS willBringPartnerForHIVTesting,\n" +
//          "CASE WHEN post_test_counseling ->> 'childrenHivtesting' =''\n" +
//          "OR post_test_counseling ->> 'childrenHivtesting' ILIKE 'false' THEN FALSE ELSE TRUE  END AS willBringOwnChildrenForHIVTesting,\n" +
//          "CASE WHEN post_test_counseling ->> 'informationFp' = ''\n" +
//          "OR post_test_counseling ->> 'informationFp' ILIKE 'false' THEN FALSE ELSE TRUE  END AS providedWithInformationOnFPandDualContraception,\n" +
//          "CASE WHEN post_test_counseling ->> 'partnerFpThanCondom' =''\n" +
//          "OR post_test_counseling ->> 'partnerFpThanCondom' ILIKE 'false' THEN FALSE ELSE TRUE END AS clientOrPartnerUseFPMethodsOtherThanCondoms,\n" +
//          "CASE WHEN post_test_counseling ->> 'partnerFpUseCondom' =''\n" +
//          "OR post_test_counseling ->> 'partnerFpUseCondom'ILIKE 'false' THEN FALSE ELSE TRUE END  AS clientOrPartnerUseCondomsAsOneFPMethods,\n" +
//          "CASE WHEN post_test_counseling ->> 'correctCondomUse' =''\n" +
//          "OR post_test_counseling ->> 'correctCondomUse' ILIKE 'false' THEN FALSE ELSE TRUE END AS correctCondomUseDemonstrated,\n" +
//          "CASE WHEN post_test_counseling ->> 'condomProvidedToClient' =''\n" +
//          "OR  post_test_counseling ->> 'condomProvidedToClient' ILIKE 'false' THEN FALSE ELSE TRUE END  AS condomsProvidedToClient,\n" +
//          "CASE WHEN post_test_counseling ->> 'referredToServices'=''\n" +
//          "OR post_test_counseling ->> 'referredToServices' ILIKE 'false' THEN FALSE ELSE TRUE END AS clientReferredToOtherServices\n" +
//          "from hts_client hc\n" +
//          "INNER JOIN \n" +
//          "(SELECT person_uuid AS personUuid, uuid, client_code AS clientCode, \n" +
//          "CASE WHEN test1 ->> 'result' ILIKE 'Yes' THEN 'Positive' ELSE 'Negative' END AS screeningTestResult,\n" +
//          "(CASE WHEN (test1 ->> 'date' ~* '[0-9]') is false THEN NULL\n" +
//          "\tELSE CAST(test1 ->> 'date' AS DATE) END) AS screeningTestResultDate,\n" +
//          "CASE WHEN confirmatory_test ->> 'result' ILIKE 'Yes' THEN 'Positive' ELSE 'Negative' END AS confirmatoryTestResult,\n" +
//          "(CASE WHEN (confirmatory_test ->> 'date' ~* '[0-9]') is false THEN NULL\n" +
//          "\tELSE CAST(confirmatory_test ->> 'date' AS DATE) END) AS confirmatoryTestResultDate,\n" +
//          "CASE WHEN tie_breaker_test ->> 'result' ILIKE 'Yes' THEN 'Positive' ELSE 'Negative' END AS tieBreakerTestResult,\n" +
//          "(CASE WHEN (tie_breaker_test ->> 'date' ~* '[0-9]') is false THEN NULL\n" +
//          "\tELSE CAST(tie_breaker_test ->> 'date' AS DATE) END)AS tieBreakerTestResultDate\n" +
//          "from hts_client) tr ON tr.clientcode=hc.client_code \n" +
//          "WHERE hc.facility_id= ?1 AND hc.client_code = ?2\n" +
//          "AND hc.date_modified > ?3 AND hc.archived = 0",nativeQuery = true)


  @Query(value = "SELECT DISTINCT ON (hc.person_uuid) hc.person_uuid AS personUuid,\n" +
          "rc.consent,\n" +
          "rc.recencyNumber, \n" +
          "rc.sampleType, \n" +
          "rc.controlLine, \n" +
          "rc.viralLoadRequest, \n" +
          "rc.longTermLine,   \n" +
          "rc.pcrLab, \n" +
          "rc.verificationLine,  \n" +
          "rc.finalRecencyTestResult,  \n" +
          "rc.testDate, \n" +
          "rc.testName,\n" +
          "rc.dateSampleCollected, \n" +
          "rc.sampleReferenceNumber,\n" +
          "rc.dateSampleSent, \n" +
          "rc.recencyInterpretation, \n" +
          "rc.viralLoadConfirmationResult, \n" +
          "rc.viralLoadClassification,\n" +
          "hc.uuid as visitId, hc.testing_setting as setting, \n" +
          "'YES' as firstTimeVisit, hc.client_code AS clientCode,\n" +
          " hc.date_visit  as visitDate, \n" +
          "--  hc.referred_from AS referred_from,\n" +
          "--  hc.extra->>'marital_status' AS maritalStatus,\n" +
          "\tkc.previouslyTestedHIVNegative,\n" +
          "\tkc.clientInformedAboutHIVTransmissionRoutes,\n" +
          "\tkc.clientPregnant,\n" +
          "\tkc.clientInformedOfHIVTransmissionRiskFactors,\n" +
          "\tkc.clientInformedAboutPreventingHIV,\n" +
          "\tkc.clientInformedAboutPossibleTestResults,\n" +
          "\tkc.informedConsentForHIVTestingGiven,\n" +
          " tr.screeningTestResult,\n" +
          " tr.screeningTestResultDate, tr.confirmatoryTestResult, \n" +
          " tr.confirmatoryTestResultDate, \n" +
          "(CASE WHEN tr.tieBreakerTestResult IS NULL OR tr.tieBreakerTestResult='' THEN tr.confirmatoryTestResult\n" +
          "ELSE tr.tieBreakerTestResult END) AS tieBreakerTestResult,\n" +
          "(CASE WHEN tr.tieBreakerTestResult IS NULL OR tr.tieBreakerTestResult='' THEN tr.confirmatoryTestResultDate\n" +
          "ELSE tr.tieBreakerTestResultDate END) AS tieBreakerTestResultDate,\n" +
          "CASE WHEN risk_assessment ->> 'everHadSexualIntercourse' = '' then false else\n" +
          "CAST(risk_assessment ->> 'everHadSexualIntercourse' AS BOOLEAN) end AS teverHadSexualIntercourse,\n" +
          "CASE WHEN risk_assessment ->> 'bloodTransfusionInLast3Months' = '' then false else\n" +
          "  CAST(risk_assessment ->> 'bloodTransfusionInLast3Months' AS BOOLEAN) end AS bloodTransfussionInLast3Months,\n" +
          "CASE WHEN risk_assessment ->> 'unprotectedSexWithCasualPartnerInLast3Months'= '' then false else\n" +
          "  CAST(risk_assessment ->> 'unprotectedSexWithCasualPartnerInLast3Months' AS BOOLEAN)end AS unprotectedSexWithCasualPartnerinLast3Months,\n" +
          "CASE WHEN risk_assessment ->> 'moreThan1SexPartnerDuringLast3Months'= '' then false else\n" +
          "  CAST(risk_assessment ->> 'moreThan1SexPartnerDuringLast3Months' AS BOOLEAN)end AS moreThan1SexPartnerDuringLast3Months,\n" +
          "CASE WHEN risk_assessment ->> 'stiInLast3Months'= '' then false else\n" +
          " CAST(risk_assessment ->> 'stiInLast3Months' AS BOOLEAN) end AS stiInLast3Months,\n" +
          "\n" +
          "--ClinicalTBScreeningType\n" +
          "CASE WHEN tb_screening ->> 'currentlyCough' = '' then false else \n" +
          "CAST( tb_screening ->> 'currentlyCough' AS BOOLEAN) end  AS currentlyCough,\n" +
          "CASE WHEN tb_screening ->> 'weightLoss' = '' then false else\n" +
          "CAST(tb_screening ->> 'weightLoss' AS BOOLEAN) end AS weightLoss,\n" +
          "CASE WHEN tb_screening ->> 'fever' = '' then false else\n" +
          " CAST(tb_screening ->> 'fever' AS BOOLEAN) end AS fever,\n" +
          "CASE WHEN tb_screening ->> 'nightSweats' = '' then false else\n" +
          "CAST(tb_screening ->> 'nightSweats' AS BOOLEAN) end AS nightSweats,\n" +
          "CASE WHEN CAST(post_test_counseling ->> 'hivTestBefore' AS VARCHAR) ILIKE '%Not%' THEN false\n" +
          " ELSE true END AS testedForHIVBeforeWithinThisYear,\n" +
          "CASE WHEN post_test_counseling ->> 'hivRequestResult'='' \n" +
          "  OR post_test_counseling ->> 'hivRequestResult' ILIKE 'false' THEN FALSE ELSE true END AS hivRequestAndResultFormSignedByTester,\n" +
          "CASE WHEN post_test_counseling ->> 'hivRequestResultCt'='' \n" +
          "OR post_test_counseling ->> 'hivRequestResultCt' ILIKE 'false' THEN FALSE ELSE TRUE END AS hivRequestAndResultFormFilledWithCTIForm,\n" +
          "CASE WHEN post_test_counseling ->> 'clientReceivedHivTestResult'=''\n" +
          "OR post_test_counseling ->> 'clientReceivedHivTestResult' ILIKE 'false' THEN FALSE ELSE TRUE END AS clientRecievedHIVTestResult,\n" +
          "CASE WHEN post_test_counseling ->> 'postTestCounseling' = ''\n" +
          "OR post_test_counseling ->> 'postTestCounseling' ILIKE 'false' THEN FALSE ELSE TRUE END  AS postTestCounsellingDone,\n" +
          "CASE WHEN post_test_counseling ->> 'riskReduction'=''\n" +
          "OR post_test_counseling ->> 'riskReduction' ILIKE 'false' THEN  FALSE ELSE TRUE  END AS riskReductionPlanDeveloped,\n" +
          "CASE WHEN post_test_counseling ->> 'postTestDisclosure' = ''\n" +
          "OR post_test_counseling ->> 'postTestDisclosure' ='false' THEN FALSE ELSE TRUE  END AS postTestDisclosurePlanDeveloped,\n" +
          "CASE WHEN post_test_counseling ->> 'bringPartnerHivtesting' =''\n" +
          "OR post_test_counseling ->> 'bringPartnerHivtesting' ILIKE 'false'THEN FALSE ELSE TRUE  END AS willBringPartnerForHIVTesting,\n" +
          "CASE WHEN post_test_counseling ->> 'childrenHivtesting' =''\n" +
          "OR post_test_counseling ->> 'childrenHivtesting' ILIKE 'false' THEN FALSE ELSE TRUE  END AS willBringOwnChildrenForHIVTesting,\n" +
          "CASE WHEN post_test_counseling ->> 'informationFp' = ''\n" +
          "OR post_test_counseling ->> 'informationFp' ILIKE 'false' THEN FALSE ELSE TRUE  END AS providedWithInformationOnFPandDualContraception,\n" +
          "CASE WHEN post_test_counseling ->> 'partnerFpThanCondom' =''\n" +
          "OR post_test_counseling ->> 'partnerFpThanCondom' ILIKE 'false' THEN FALSE ELSE TRUE END AS clientOrPartnerUseFPMethodsOtherThanCondoms,\n" +
          "CASE WHEN post_test_counseling ->> 'partnerFpUseCondom' =''\n" +
          "OR post_test_counseling ->> 'partnerFpUseCondom'ILIKE 'false' THEN FALSE ELSE TRUE END  AS clientOrPartnerUseCondomsAsOneFPMethods,\n" +
          "CASE WHEN post_test_counseling ->> 'correctCondomUse' =''\n" +
          "OR post_test_counseling ->> 'correctCondomUse' ILIKE 'false' THEN FALSE ELSE TRUE END AS correctCondomUseDemonstrated,\n" +
          "CASE WHEN post_test_counseling ->> 'condomProvidedToClient' =''\n" +
          "OR  post_test_counseling ->> 'condomProvidedToClient' ILIKE 'false' THEN FALSE ELSE TRUE END  AS condomsProvidedToClient,\n" +
          "CASE WHEN post_test_counseling ->> 'referredToServices'=''\n" +
          "OR post_test_counseling ->> 'referredToServices' ILIKE 'false' THEN FALSE ELSE TRUE END AS clientReferredToOtherServices\n" +
          "from hts_client hc\n" +
          "INNER JOIN \n" +
          "(SELECT person_uuid AS personUuid, uuid, client_code AS clientCode, \n" +
          "CASE WHEN test1 ->> 'result' ILIKE 'Yes' THEN 'Positive' ELSE 'Negative' END AS screeningTestResult,\n" +
          "(CASE WHEN (test1 ->> 'date' ~* '[0-9]') is false THEN NULL\n" +
          "ELSE CAST(test1 ->> 'date' AS DATE) END) AS screeningTestResultDate,\n" +
          "CASE WHEN confirmatory_test ->> 'result' ILIKE 'Yes' THEN 'Positive' ELSE 'Negative' END AS confirmatoryTestResult,\n" +
          "(CASE WHEN (confirmatory_test ->> 'date' ~* '[0-9]') is false THEN NULL\n" +
          "ELSE CAST(confirmatory_test ->> 'date' AS DATE) END) AS confirmatoryTestResultDate,\n" +
          "CASE WHEN tie_breaker_test ->> 'result' ILIKE 'Yes' THEN 'Positive' ELSE 'Negative' END AS tieBreakerTestResult,\n" +
          "(CASE WHEN (tie_breaker_test ->> 'date' ~* '[0-9]') is false THEN NULL\n" +
          "ELSE CAST(tie_breaker_test ->> 'date' AS DATE) END)AS tieBreakerTestResultDate\n" +
          "from hts_client) tr ON tr.clientcode=hc.client_code\n" +
          "LEFT JOIN(\n" +
          "SELECT DISTINCT ON (person_uuid) client_code as clientCode , person_uuid as personUuid, uuid,\n" +
          "recency->>'optOutRTRI' As consent,\n" +
          "recency->> 'rencencyId' AS recencyNumber, \n" +
          "recency->>'sampleType' AS  sampleType, \n" +
          "recency->>'controlLine' AS controlLine, \n" +
          "recency->>'hasViralLoad' AS viralLoadRequest, \n" +
          "recency->> 'longTermLine' AS longTermLine,   \n" +
          "(CASE WHEN (recency->> 'sampleTestDate' ~* '[0-9]') is false THEN NULL\n" +
          "ELSE CAST(recency->> 'sampleTestDate' AS DATE) END)\n" +
          "AS  sampleTestDate,  \n" +
          "recency->>'receivingPcrLab' AS pcrLab, \n" +
          "recency->>'verififcationLine' AS verificationLine,  \n" +
          "recency->>'finalRecencyResult' AS finalRecencyTestResult, \n" +
          "(CASE WHEN (recency->>'optOutRTRITestDate'  ~* '[0-9]') is false THEN NULL\n" +
          "ELSE CAST(recency->>'optOutRTRITestDate' AS DATE) END)\n" +
          "AS testDate, \n" +
          "recency->>'optOutRTRITestName' AS testName,\n" +
          "(CASE WHEN (recency->>'sampleCollectedDate' ~* '[0-9]') is false THEN NULL\n" +
          "ELSE CAST(recency->>'sampleCollectedDate' AS DATE) END) AS dateSampleCollected, \n" +
          "recency->>'sampleReferanceNumber' AS sampleReferenceNumber,\n" +
          "(CASE WHEN (recency->>'dateSampleSentToPCRLab'   ~* '[0-9]') is false THEN NULL\n" +
          "ELSE CAST(recency->>'dateSampleSentToPCRLab'  AS DATE) END)\n" +
          "AS dateSampleSent, \n" +
          "recency->>'rencencyInterpretation' AS recencyInterpretation, \n" +
          "(CASE WHEN (recency->>'viralLoadConfirmationResult'   ~* '[0-9]') is false THEN NULL\n" +
          "ELSE CAST(recency->>'viralLoadConfirmationResult'  AS float) END)\n" +
          "AS viralLoadConfirmationResult, \n" +
          "recency->>'viralLoadResultClassification' AS viralLoadClassification\n" +
          "from hts_client ) rc ON rc.clientCode = hc.client_code  \n" +
          "\n" +
          "--KnowledgeAssesment\n" +
          "LEFT JOIN (\n" +
          "SELECT DISTINCT ON (person_uuid) client_code as clientCode , person_uuid as personUuid, uuid,\n" +
          "\tCASE WHEN knowledge_assessment ->> 'previousTestedHIVNegative'='' \n" +
          " OR knowledge_assessment ->> 'previousTestedHIVNegative' ILIKE 'false' THEN FALSE ELSE true END AS previouslyTestedHIVNegative,\n" +
          "CASE WHEN knowledge_assessment ->> 'clientInformHivTransRoutes'='' \n" +
          " OR knowledge_assessment ->> 'clientInformHivTransRoutes' ILIKE 'false' THEN FALSE ELSE true END AS clientInformedAboutHIVTransmissionRoutes,\n" +
          "CASE WHEN knowledge_assessment ->> 'clientPregnant'='' \n" +
          " OR knowledge_assessment ->> 'clientPregnant' ILIKE 'false' THEN FALSE ELSE true END AS clientPregnant,\n" +
          "CASE WHEN knowledge_assessment ->> 'clientInformRiskkHivTrans'='' \n" +
          " OR knowledge_assessment ->> 'clientInformRiskkHivTrans' ILIKE 'false' THEN FALSE ELSE true END AS clientInformedOfHIVTransmissionRiskFactors,\n" +
          "CASE WHEN knowledge_assessment ->> 'clientInformPreventingsHivTrans'='' \n" +
          " OR knowledge_assessment ->> 'clientInformPreventingsHivTrans' ILIKE 'false' THEN FALSE ELSE true END AS clientInformedAboutPreventingHIV,\n" +
          "CASE WHEN knowledge_assessment ->> 'clientInformPossibleTestResult'='' \n" +
          " OR knowledge_assessment ->> 'clientInformPossibleTestResult' ILIKE 'false' THEN FALSE ELSE true END AS clientInformedAboutPossibleTestResults,\t\n" +
          "CASE WHEN knowledge_assessment ->> 'informConsentHivTest'='' \n" +
          " OR knowledge_assessment ->> 'informConsentHivTest' ILIKE 'false' THEN FALSE ELSE true END AS informedConsentForHIVTestingGiven\n" +
          "\tFROM hts_client\n" +
          ") kc ON kc.clientCode = hc.client_code  \n" +
          "\n" +
          "WHERE hc.facility_id= ?1\n" +
          "AND hc.client_code = ?2\n" +
          "AND hc.date_modified > ?3 \n" +
          "AND hc.archived = 0 ", nativeQuery = true) List<HtsReportDto> getHstReportByClientCodeAndLastModified(Long facilityId, String clientCode, LocalDateTime lastModified);

  // client verification query


    @Query(value = "SELECT DISTINCT p.uuid FROM patient_person AS p\n" +
            "\tJOIN hiv_enrollment AS e on e.person_uuid = p.uuid\n" +
            "\tINNER JOIN hiv_art_clinical hac ON hac.hiv_enrollment_uuid = e.uuid\n" +
            "\tWHERE p.archived = 1 OR e.archived = 1 AND e.last_modified_date >= ?1 AND e.facility_id = ?2\n" +
            "\tAND hac.is_commencement = TRUE", nativeQuery = true)
    List<String> getPatientIdsEligibleForRedaction(LocalDateTime start, long facilityId);

    @Query(value="SELECT  \n" +
            "            DISTINCT (p.uuid) AS personUuid,\n" +
            "            p.hospital_number AS hospitalNumber, \n" +
            "            p.date_of_registration AS diagnosisDate, \n" +
            "            p.id AS personId,\n" +
            "            boui.code AS facilityId,\n" +
            "            facility.name AS facilityName,  \n" +
            "            concat(boui.code, '_', p.uuid) as patientIdentifier, \n" +
            "            lgaCode.code AS lgaCode, \n" +
            "            stateCode.code AS stateCode,\n" +
            "            enrollStatus.display AS statusAtRegistration,\n" +
            "            h.created_date,\n" +
            "            h.last_modified_date,\n" +
            "            h.archived,\n" +
            "            p.reason\n" +
            "            FROM patient_person p \n" +
            "            JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
            "            INNER JOIN base_organisation_unit facility ON facility.id = h.facility_id  \n" +
            "            INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id = h.facility_id \n" +
            "            AND boui.name = 'DATIM_ID'\n" +
            "            INNER JOIN base_organisation_unit facility_lga ON facility_lga.id = facility.parent_organisation_unit_id\n" +
            "            INNER JOIN base_organisation_unit facility_state ON facility_state.id = facility_lga.parent_organisation_unit_id\n" +
            "            LEFT JOIN ndr_code_set lgaCode ON trim(lgaCode.code_description)= trim(facility_lga.name) \n" +
            "            AND lgaCode.code_set_nm = 'LGA' \n" +
            "            LEFT JOIN ndr_code_set stateCode ON trim(stateCode.code_description)= trim(facility_state.name) \n" +
            "            AND stateCode.code_set_nm = 'STATES' \n" +
            "            LEFT JOIN base_application_codeset enrollStatus ON enrollStatus.id = h.status_at_registration_id \n" +
            "            INNER JOIN hiv_art_clinical hac ON hac.hiv_enrollment_uuid = h.uuid\n" +
            "            WHERE p.archived = 1 \n" +
            "            AND h.person_uuid = ?1\n" +
            "            AND h.facility_id = ?2\n" +
            "            --AND hac.is_commencement = TRUE",
            nativeQuery = true)
    Optional<PatientRedactedDemographicDTO> getRedactedPatientDemographics(String identifier, Long facilityId);

    @Query(value=" SELECT ph.visit_id AS visitId, hac.reason AS reason FROM hiv_art_pharmacy ph\n" +
            "\t\t\t\tINNER JOIN hiv_enrollment h ON h.person_uuid =ph.person_uuid\n" +
            "\t\t\t\tINNER JOIN patient_person p ON p.uuid = h.person_uuid\n" +
            "\t\t\t\tJOIN hiv_art_clinical hac ON hac.person_uuid = h.person_uuid\n" +
            "\t\t\t\tWHERE p.archived = 1 \n" +
            "                AND h.person_uuid = ?1", nativeQuery = true)

    List<RedactedVisitTypeDTO> getRedactedPatientVisits(String identifier);
}
