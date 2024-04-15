package org.ecews.biometricapp.repositories.queries;

public class ReportQueries {

    public static final String SUMMARY_REPORT_QUERY = """
            with deduplication as (
                select person_uuid, date_of_deduplication, identifier_count, match_count, no_match_count, subject_count,
                       ROW_NUMBER() OVER (PARTITION BY person_uuid ORDER BY date_of_deduplication DESC) AS rank
                from identification_response where deduplication_type = ?
            ),bio_data AS (
                SELECT
                    facility_lga.name AS lga, facility_state.name AS state,
                    p.uuid as patientId, p.hospital_number, h.unique_id as uniqueId,
                    EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS age,
                    INITCAP(p.sex) AS sex, p.date_of_birth,
                    facility.name AS facility_name, boui.code AS datimId,
                    tgroup.display AS targetGroup, eSetting.display AS enrollment_setting,
                    hac.visit_date AS art_start_date, hr.description AS regimen_at_art_start,
                    p.date_of_registration, p.surname, p.first_name,
                    boo.name as lgaOfResidence
                FROM
                    patient_person p
                INNER JOIN
                    base_organisation_unit facility ON facility.id = p.facility_id
                INNER JOIN
                    base_organisation_unit facility_lga ON facility_lga.id = facility.parent_organisation_unit_id
                INNER JOIN
                    base_organisation_unit facility_state ON facility_state.id = facility_lga.parent_organisation_unit_id
                INNER JOIN
                    base_organisation_unit_identifier boui ON boui.organisation_unit_id = p.facility_id AND boui.name='DATIM_ID'
                INNER JOIN
                    hiv_enrollment h ON h.person_uuid = p.uuid
                LEFT JOIN
                    base_application_codeset tgroup ON tgroup.id = h.target_group_id
                LEFT JOIN
                    base_application_codeset eSetting ON eSetting.id = h.enrollment_setting_id
                LEFT JOIN
                    hiv_art_clinical hac ON hac.hiv_enrollment_uuid = h.uuid
                       AND hac.archived = 0
                       AND hac.is_commencement = TRUE
                LEFT JOIN hiv_regimen hr ON hr.id = hac.regimen_id
                LEFT JOIN base_organisation_unit boo on boo.id =
                    CASE
                        WHEN (string_to_array(p.address->'address'->0->>'district', ','))[1] ~ '^\\d+$'THEN cast(p.address->'address'->0->>'district' as bigint)
                        ELSE NULL
                    END
                WHERE
                    p.archived = 0
                )
            select * from deduplication d
            join bio_data b on  b.patientId = d.person_uuid where d.rank = 1;
            """;

    public static final String DETAIL_REPORT_QUERY = """
            with deduplication as (
                select * from(select ir.date_of_deduplication, ir.person_uuid, mp->>'score' as score, mp->>'enrolledPatientFingerType' as enrolledPatientFingerType,
                       mp->>'matchedPatientId' as matchedPatientId,
                       mp->>'matchedPatientFingerType' as matchedPatientFingerType,
                       ROW_NUMBER() OVER (PARTITION BY person_uuid ORDER BY date_of_deduplication DESC) AS rank
                from identification_response ir, jsonb_array_elements(ir.matched_pairs) WITH ORDINALITY p(mp)
                             where ir.deduplication_type = ?) as de where de.rank = 1
            ),
                bio_data AS (
                SELECT
                    facility_lga.name AS lga, facility_state.name AS state,
                    p.uuid as patientId, p.hospital_number, h.unique_id as uniqueId,
                    INITCAP(p.sex) AS sex, p.date_of_birth,
                    facility.name AS facility_name, boui.code AS datimId,
                    tgroup.display AS targetGroup,
                    hac.visit_date AS art_start_date, p.surname, p.first_name,
                    boo.name as lgaOfResidence
                FROM
                    patient_person p
                INNER JOIN
                    base_organisation_unit facility ON facility.id = p.facility_id
                INNER JOIN
                    base_organisation_unit facility_lga ON facility_lga.id = facility.parent_organisation_unit_id
                INNER JOIN
                    base_organisation_unit facility_state ON facility_state.id = facility_lga.parent_organisation_unit_id
                INNER JOIN
                    base_organisation_unit_identifier boui ON boui.organisation_unit_id = p.facility_id AND boui.name='DATIM_ID'
                INNER JOIN
                    hiv_enrollment h ON h.person_uuid = p.uuid
                LEFT JOIN
                    base_application_codeset tgroup ON tgroup.id = h.target_group_id
                LEFT JOIN
                    hiv_art_clinical hac ON hac.hiv_enrollment_uuid = h.uuid
                       AND hac.archived = 0
                       AND hac.is_commencement = TRUE
                LEFT JOIN base_organisation_unit boo on boo.id =
                    CASE
                        WHEN (string_to_array(p.address->'address'->0->>'district', ','))[1] ~ '^\\d+$'THEN cast(p.address->'address'->0->>'district' as bigint)
                        ELSE NULL
                    END
                WHERE
                    p.archived = 0
                )
            select
                    be.lga as enrolled_lga, be.state as enrolled_state,
                    be.patientId as enrolled_patient_id, be.hospital_number as enrolled_hospital_number, be.uniqueId as enrolled_unique_id,
                    be.sex as enrolled_sex, be.date_of_birth as enrolled_date_of_birth, d.enrolledPatientFingerType as enrolled_patient_finger_type,
                    be.facility_name as enrolled_facility_name, be.datimId as enrolled_datimId,
                    be.targetGroup as enrolled_target_group, be.art_start_date as enrolled_art_start_date,
                    be.surname as enrolled_surname, be.first_name as enrolled_first_name, be.lgaOfResidence as enrolled_lga_of_residence,
                    bm.lga as matched_lga, bm.state as matched_state,
                    bm.patientId as matched_patient_id, bm.hospital_number as matched_hospital_number, bm.uniqueId as matched_unique_id,
                    bm.sex as matched_sex, bm.date_of_birth as matched_date_of_birth, d.matchedPatientFingerType as matched_patient_finger_type,
                    bm.facility_name as matched_facility_name, bm.datimId as matched_datimId,
                    bm.targetGroup as matched_target_group, bm.art_start_date as matched_art_start_date,
                    bm.surname as matched_surname, bm.first_name as matched_first_name, bm.lgaOfResidence as matched_lga_of_residence,
                    d.score, d.date_of_deduplication
                from deduplication d
            left join bio_data be on be.patientId = d.person_uuid
            left join bio_data bm on bm.patientId = d.matchedPatientId
            """;
}
