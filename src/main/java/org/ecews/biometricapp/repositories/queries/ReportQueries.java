package org.ecews.biometricapp.repositories.queries;

public class ReportQueries {
    
    public static final String REPORT_QUERY_SUMMARY = """
            with deduplication as (
                    with identification as (
                            select * from (select person_uuid, date_of_deduplication, identifier_count, jsonb_array_length(matched_pairs) as match_count,
                                    (identifier_count - jsonb_array_length(matched_pairs)) AS no_match_count, subject_count,
                                           ROW_NUMBER() OVER (PARTITION BY person_uuid ORDER BY date_of_deduplication DESC) AS rank
                                    from identification_response where deduplication_type = :deduplicationType) t where t.rank = 1
                        ), intervention as (
                            select * from (select person_uuid, date_of_deduplication, identifier_count, jsonb_array_length(matched_pairs) as match_count,
                                    (identifier_count - jsonb_array_length(matched_pairs)) AS no_match_count, subject_count,
                                           ROW_NUMBER() OVER (PARTITION BY person_uuid ORDER BY date_of_deduplication DESC) AS rank
                                    from intervention_response where deduplication_type = :deduplicationType) t where t.rank = 1
                    )
                    select i.person_uuid, i.date_of_deduplication, coalesce(iv.match_count, i.match_count) as match_count,
                           coalesce(iv.no_match_count, i.no_match_count) as no_match_count, coalesce(iv.subject_count, i.subject_count) as subject_count,
                           coalesce(iv.identifier_count, i.identifier_count) as identifier_count
                    from identification i left join intervention iv on i.person_uuid = iv.person_uuid
                ),
                match_sets as (
                    with identification as (
                            select person_uuid, baseline, recapture_1, recapture_2 from
                             (select *, ROW_NUMBER() OVER (PARTITION BY person_uuid ORDER BY date_of_deduplication DESC) AS rank
                               from (with match as (
                            select de.person_uuid, de.matchedFingerId, de.date_of_deduplication, b.recapture from(select ir.person_uuid, date_of_deduplication,
                                   mp->>'matchedFingerId' as matchedFingerId
                            from identification_response ir, jsonb_array_elements(ir.matched_pairs) WITH ORDINALITY p(mp)
                                         where ir.deduplication_type = :deduplicationType) as de
                            join biometric b on b.id = de.matchedFingerId
                            )
                            SELECT person_uuid, date_of_deduplication, COUNT(CASE WHEN recapture = 0 THEN 1 END) AS baseline,
                                COUNT(CASE WHEN recapture = 1 THEN 1 END) AS recapture_1, COUNT(CASE WHEN recapture = 2 THEN 1 END) AS recapture_2
                            FROM
                                match
                            GROUP BY
                                person_uuid, date_of_deduplication) t ) tb where tb.rank = 1
                        ), intervention as (
                            select person_uuid, baseline, recapture_1, recapture_2 from
                             (select *, ROW_NUMBER() OVER (PARTITION BY person_uuid ORDER BY date_of_deduplication DESC) AS rank
                               from (with match as (
                            select de.person_uuid, de.matchedFingerId, de.date_of_deduplication, b.recapture from(select ir.person_uuid, date_of_deduplication,
                                   mp->>'matchedFingerId' as matchedFingerId
                            from intervention_response ir, jsonb_array_elements(ir.matched_pairs) WITH ORDINALITY p(mp)
                                         where ir.deduplication_type = :deduplicationType) as de
                            join biometric b on b.id = de.matchedFingerId
                            )
                            SELECT person_uuid, date_of_deduplication, COUNT(CASE WHEN recapture = 0 THEN 1 END) AS baseline,
                                COUNT(CASE WHEN recapture = 1 THEN 1 END) AS recapture_1, COUNT(CASE WHEN recapture = 2 THEN 1 END) AS recapture_2
                            FROM
                                match
                            GROUP BY
                                person_uuid, date_of_deduplication) t ) tb where tb.rank = 1
                        )
                    select coalesce(it.person_uuid, id.person_uuid) as person_uuid, coalesce(it.baseline, id.baseline) as baseline,
                           coalesce(it.recapture_1, id.recapture_1) as recapture_1, coalesce(it.recapture_2, id.recapture_2) as recapture_2
                    from identification id
                    full outer join intervention it on id.person_uuid = it.person_uuid
                ),
                bio_data AS (
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
                                    END
                            WHERE
                                p.archived = 0
                            )
            select * from deduplication d
            join bio_data b on  b.patientId = d.person_uuid
            left join match_sets m on d.person_uuid = m.person_uuid
            """;

    public static final String SUMMARY_REPORT_QUERY = """
            with deduplication as (
                select person_uuid, date_of_deduplication, identifier_count, jsonb_array_length(matched_pairs) as match_count, 
                (identifier_count - jsonb_array_length(matched_pairs)) AS no_match_count, subject_count,
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
