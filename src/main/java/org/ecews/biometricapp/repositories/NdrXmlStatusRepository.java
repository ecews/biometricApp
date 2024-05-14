package org.ecews.biometricapp.repositories;


import org.ecews.biometricapp.entities.NdrXmlStatus;
import org.ecews.biometricapp.entities.dtos.ARTClinicalInfo;
import org.ecews.biometricapp.entities.dtos.LabDTO;
import org.ecews.biometricapp.entities.dtos.PatientDemographicDTO;
import org.ecews.biometricapp.entities.dtos.PatientDemographics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NdrXmlStatusRepository extends JpaRepository<NdrXmlStatus, Integer> {
	
	@Query(value = "SELECT p.id,p.uuid as personUuid, p.facility_id as facilityId, p.archived \\:\\:BOOLEAN as archived, p.uuid,p.hospital_number as hospitalNumber, \n" +
			"\t\t\t\t  p.surname, p.first_name as firstName,\n" +
			"\t\t\t\t  EXTRACT(YEAR from AGE(NOW(),  date_of_birth)) as age,\n" +
			"\t\t\t\t  p.other_name as otherName, p.sex, p.date_of_birth as dateOfBirth, \n" +
			"\t\t\t\t  h.date_of_registration as dateOfRegistration, p.marital_status->>'display' as maritalStatus, \n" +
			"\t\t\t\t  education->>'display' as education, p.employment_status->>'display' as occupation, \n" +
			"\t\t\t\t  facility.name as facilityName, facility_lga.name as lga, facility_state.name as state, \n" +
			"\t\t\t\t  boui.code as datimId, r.city as town,res_state.name as residentialState, res_lga.name as residentialLga,\n" +
			"\t\t\t\t  r.address as address, p.contact_point->'contactPoint'->0->'value'->>0 AS phone\n" +
			"\t\t\t\t  FROM patient_person p\n" +
			"\t\t\t\t  INNER JOIN (\n" +
			"\t\t\t\t  SELECT * FROM (SELECT p.id, REPLACE(REPLACE(REPLACE(address_object->>'line'\\:\\:text, '\"', ''), ']', ''), '[', '') AS address,\n" +
			"\t\t\t\t\t\t\t\t  REPLACE(REPLACE(REPLACE(address_object->>'city'\\:\\:text, '\\\"', ''), ']', ''), '[', '') AS city,\n" +
			"\t\t\t\tCASE WHEN address_object->>'stateId'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'stateId' ELSE null END  AS stateId,\n" +
			"\t\t\t\tCASE WHEN address_object->>'district'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'district' ELSE null END  AS lgaId\n" +
			"      \t\t\tFROM patient_person p,\n" +
			"jsonb_array_elements(p.address-> 'address') with ordinality l(address_object)) as result\n" +
			"\t\t\t\t  ) r ON r.id=p.id\n" +
			"\t\t\t\t INNER JOIN base_organisation_unit facility ON facility.id=facility_id\n" +
			"\t\t\t\t INNER JOIN base_organisation_unit facility_lga ON facility_lga.id=facility.parent_organisation_unit_id\n" +
			"\t\t\t\t INNER JOIN base_organisation_unit facility_state ON facility_state.id=facility_lga.parent_organisation_unit_id\n" +
			"\t\t\t\t LEFT JOIN base_organisation_unit res_state ON res_state.id=r.stateid\\:\\:BIGINT\n" +
			"\t\t\t\t LEFT JOIN base_organisation_unit res_lga ON res_lga.id=r.lgaid\\:\\:BIGINT\n" +
			"\t\t\t\t INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=facility_id AND boui.name ='DATIM_ID' \n" +
			"\t\t\t\t INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"\t\t\t\t WHERE h.archived=0 AND p.uuid=?1",
			nativeQuery = true)
	Optional<PatientDemographics> getPatientDemographicsByUUID(String patientUuid);


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
	
	@Query(value = "SELECT pregnancy_status from hiv_art_clinical\n" +
			"WHERE person_uuid = ?1 \n" +
			"AND archived = 0\n" +
			"ORDER BY visit_date DESC limit 1", nativeQuery = true)
	Optional<String> getPregnancyStatusByPersonUuid(String patientUuid);
	
	@Query(value = "SELECT a.display as tbStatus from hiv_art_clinical c " +
			"INNER JOIN base_application_codeset a on a.id = cast(c.tb_status as INTEGER) " +
			"WHERE person_uuid = ?1\n" +
			"AND c.archived = 0\n" +
			"ORDER BY visit_date DESC limit 1", nativeQuery = true)
	Optional<String> getTbStatusByPersonUuid(String personUuid);
	
	@Query(value = "SELECT c.id as clinicId,\n" +
			"\tc.facility_id as facilityId,\n" +
			"\tvisit_date as visitDate  ,\n" +
			"\tcd_4 as cd4,\n" +
			"\tcd_4_percentage as cd4Percentage,\t\n" +
			"\tis_commencement as isCommencement,\t\n" +
			"\tfunctional_status_id as functionalStatus,\t\n" +
			"\tclinical_stage_id as clinicalStageId ,\t\n" +
			"\tclinical_note as clinicalNote,\t\n" +
			"\tc.uuid as clinicalUuid,\t\n" +
			"\thiv_enrollment_uuid as hivEnrollmentUuid,\n" +
			"\tregimen_id as regimenId,\n" +
			"\tregimen_type_id as regimenTypeId,\n" +
			"\tc.archived as archived,\n" +
			"\tvital_sign_uuid as vitalSignUuid,\n" +
			"\twho_staging_id as whoStagingId,\n" +
			"\tc.person_uuid as personUuid,\n" +
			"    c.visit_id as visitId,\n" +
			"\tpregnancy_status as pregnencyStatus,\n" +
			"\tsti_treated as stiTreated,\n" +
			"\tadr_screened as adrScreened,\n" +
			"\tadherence_level as adherenceLevel,\n" +
			"\tnext_appointment as nextAppointment,\n" +
			"\tlmp_date as lmpDate,\n" +
			"\tis_viral_load_at_start_of_art as isViralLoadAtStartOfArt,\n" +
			"\tviral_load_at_start_of_art as viralLoadAtStartOfArt,\n" +
			"\tdate_of_viral_load_at_start_of_art as dateOfViralLoadAtStartOfArt,\n" +
			"\tcryptococcal_screening_status as cryptococcalScreeningStatus ,\n" +
			"\tcervical_cancer_screening_status as cervicalCancerScreeningStatus ,\n" +
			"\tcervical_cancer_treatment_provided as cervicalCancerTreatmentProvided ,\n" +
			"\thepatitis_screening_result as hepatitisScreeningResult,\n" +
			"\tfamily_planing as familyPlaning ,\n" +
			"\ton_family_planing as onFamilyPlaning,\n" +
			"\tlevel_of_adherence as levelOfAdherence,\n" +
			"\ttb_status as tbStatus,\n" +
			"\ttb_prevention as tbPrevention,\n" +
			"\tcd4_count as cd4Count,\n" +
			"\tbody_weight as bodyWeight,\n" +
			"\tdiastolic ,\n" +
			"\tcapture_date as captureDate,\n" +
			"\theight,\n" +
			"\ttemperature,\n" +
			"\tpulse,\n" +
			"\trespiratory_rate as respiratory,\n" +
			"\tsystolic\n" +
			"from public.hiv_art_clinical c \n" +
			"inner join triage_vital_sign  v on v.uuid = c.vital_sign_uuid\n" +
			"where c.person_uuid = ?1\n" +
			"and is_commencement is false\n" +
			"and c.archived = 0", nativeQuery = true)
	List<ARTClinicalInfo> getClinicalInfoByPersonUuid(String personUuid);
	
	@Query(value = "SELECT c.id as clinicId,\n" +
			"\tc.facility_id as facilityId,\n" +
			"\tvisit_date as visitDate  ,\n" +
			"\tcd_4 as cd4,\n" +
			"\tcd_4_percentage as cd4Percentage,\t\n" +
			"\tis_commencement as isCommencement,\t\n" +
			"\tfunctional_status_id as functionalStatus,\t\n" +
			"\tclinical_stage_id as clinicalStageId ,\t\n" +
			"\tclinical_note as clinicalNote,\t\n" +
			"\tc.uuid as clinicalUuid,\t\n" +
			"\thiv_enrollment_uuid as hivEnrollmentUuid,\n" +
			"\tregimen_id as regimenId,\n" +
			"\tregimen_type_id as regimenTypeId,\n" +
			"\tc.archived as archived,\n" +
			"\tvital_sign_uuid as vitalSignUuid,\n" +
			"\twho_staging_id as whoStagingId,\n" +
			"\tc.person_uuid as personUuid,\n" +
			"    c.visit_id as visitId,\n" +
			"\tpregnancy_status as pregnencyStatus,\n" +
			"\tsti_treated as stiTreated,\n" +
			"\tadr_screened as adrScreened,\n" +
			"\tadherence_level as adherenceLevel,\n" +
			"\tnext_appointment as nextAppointment,\n" +
			"\tlmp_date as lmpDate,\n" +
			"\tis_viral_load_at_start_of_art as isViralLoadAtStartOfArt,\n" +
			"\tviral_load_at_start_of_art as viralLoadAtStartOfArt,\n" +
			"\tdate_of_viral_load_at_start_of_art as dateOfViralLoadAtStartOfArt,\n" +
			"\tcryptococcal_screening_status as cryptococcalScreeningStatus ,\n" +
			"\tcervical_cancer_screening_status as cervicalCancerScreeningStatus ,\n" +
			"\tcervical_cancer_treatment_provided as cervicalCancerTreatmentProvided ,\n" +
			"\thepatitis_screening_result as hepatitisScreeningResult,\n" +
			"\tfamily_planing as familyPlaning ,\n" +
			"\ton_family_planing as onFamilyPlaning,\n" +
			"\tlevel_of_adherence as levelOfAdherence,\n" +
			"\ttb_status as tbStatus,\n" +
			"\ttb_prevention as tbPrevention,\n" +
			"\tcd4_count as cd4Count,\n" +
			"\tbody_weight as bodyWeight,\n" +
			"\tdiastolic ,\n" +
			"\tcapture_date as captureDate,\n" +
			"\theight,\n" +
			"\ttemperature,\n" +
			"\tpulse,\n" +
			"\trespiratory_rate as respiratory,\n" +
			"\tsystolic\n" +
			"from public.hiv_art_clinical c \n" +
			"inner join triage_vital_sign  v on v.uuid = c.vital_sign_uuid\n" +
			"where c.person_uuid = ?1\n" +
			"and c.last_modified_date >= ?2 \n" +
			"and is_commencement is false\n" +
			"and c.archived = 0", nativeQuery = true)
	List<ARTClinicalInfo> getClinicalInfoByPersonUuidByLastModifiedDate(String personUuid, LocalDateTime lastModified);
	
	
	@Query(value = "SELECT\n" +
			"lt.patient_uuid AS patientId, lt.viral_load_indication AS indicationId, \n" +
			"lr.date_result_reported resultDate,\n" +
			"ls.date_sample_collected dateSampleCollected,\n" +
			"lt.uuid AS visitId,\n" +
			"llt.lab_test_name AS labTestName,\n" +
			"ls.date_modified AS dateSampleModified,\n" +
			"(CASE WHEN lr.date_assayed IS NULL THEN lr.date_result_reported\n" +
			"ELSE lr.date_assayed END) AS dateAssayed,\n" +
			"lr.date_modified AS dateResultModified,\n" +
			"REPLACE(lr.result_reported, '<', '') AS resultReported FROM laboratory_test lt \n" +
			"INNER JOIN laboratory_result lr ON lr.patient_uuid=lt.patient_uuid AND lt.id=lr.test_id\n" +
			"INNER JOIN laboratory_sample ls ON ls.test_id = lt.id AND ls.patient_uuid = lr.patient_uuid\n" +
			"INNER JOIN laboratory_labtest llt ON lt.lab_test_id=llt.id\n" +
			"INNER JOIN laboratory_labtestgroup llg ON llt.labtestgroup_id=llg.id\n" +
			"WHERE (llt.lab_test_name = 'Viral Load' OR llt.lab_test_name='CD4') \n" +
			"AND lt.patient_uuid = ?1\n" +
			"AND lr.date_result_reported >= ?2", nativeQuery = true)
	List<LabDTO> getLabInfoByPersonUuid(String personUuid, LocalDateTime lastGenerateDateTime);
	
	@Query (value = "select last_modified from ndr_xml_status where \n" +
			"facility_id = ?1\n" +
			"AND type = ?2\n" +
			"order by last_modified desc limit 1", nativeQuery = true)
	 Optional<Timestamp> getLastGenerateDateTimeByFacilityId(Long facilityId, String Type);

	@Query (value = "SELECT * FROM ndr_xml_status where type not in('treatment','hts') ORDER BY  id DESC", nativeQuery = true)
	Iterable<NdrXmlStatus> getRecaptureFiles();
	
}
