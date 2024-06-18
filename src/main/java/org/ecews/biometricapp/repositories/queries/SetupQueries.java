package org.ecews.biometricapp.repositories.queries;

public class SetupQueries {
    public static final String CLEAN_BIOMETRIC_RECORD = """
            
            ALTER TABLE biometric ADD COLUMN temp_id SERIAL;
            WITH duplicates AS (
                SELECT
                    temp_id,
                    ROW_NUMBER() OVER (PARTITION BY id ORDER BY temp_id) AS row_num
                FROM
                    biometric
            )
            DELETE FROM biometric
            WHERE
                temp_id IN (
                    SELECT temp_id
                    FROM duplicates
                    WHERE row_num > 1
                );
            ALTER TABLE biometric DROP COLUMN temp_id;
            
            update biometric set template_type = 'Left Thumb Finger' where template_type = 'Left Thumb';
            update biometric set template_type = 'Right Thumb Finger' where template_type = 'Right Thumb';
            update biometric set template_type = 'Right Little Finger' where template_type = 'Right LIttle Finger';
            update biometric set template_type = 'Right Index Finger' where template_type = 'Right Index Middle';
            update biometric set template_type = 'Left Little Finger' where template_type = 'Left LittleFinger';
            update biometric set template_type = 'Left Little Finger' where template_type = 'Left  Little Finger';
            update biometric set template_type = 'Left Little Finger' where template_type = 'Left little Finger';
            update biometric set template_type = 'Right Index Finger' where template_type = 'RIGHT_INDEX_FINGER';
            update biometric set template_type = 'Right Middle Finger' where template_type = 'RIGHT_MIDDLE_FINGER';
            update biometric set template_type = 'Left Index Finger' where template_type = 'LEFT_INDEX_FINGER';
            update biometric set template_type = 'Left Middle Finger' where template_type = 'LEFT_MIDDLE_FINGER';
            update biometric set template_type = 'Right Thumb Finger' where template_type = 'RIGHT_THUMB';
            update biometric set template_type = 'Left Thumb Finger' where template_type = 'LEFT_THUMB';
            update biometric set template_type = 'Left Middle Finger' where template_type = 'Left  Middle Finger';
            
            DELETE FROM biometric
            WHERE id IN (
                SELECT id
                FROM (
                    SELECT id,
                           ROW_NUMBER() OVER (PARTITION BY person_uuid, recapture, enrollment_date, template_type ORDER BY enrollment_date DESC) AS rnk
                    FROM biometric
                    WHERE archived = 0 AND recapture IN (0, 1, 2) -- AND person_uuid = 'fb2b0ba9-6aac-4dfe-8f96-0c456c20d2ab'
                ) AS subquery
                WHERE rnk > 1
            );
                        
            DELETE FROM biometric
            WHERE id IN (
                SELECT id
                FROM (
                    SELECT id,
                           ROW_NUMBER() OVER (PARTITION BY person_uuid, recapture, template_type, image_quality ORDER BY enrollment_date ASC) AS rnk
                    FROM biometric
                    WHERE archived = 0 AND recapture IN (0, 1, 2) -- AND person_uuid = '0058e4b2-4c0d-4f75-aba1-51a794c7c657'
                ) AS subquery
                WHERE rnk > 1
            );
                        
            DELETE FROM biometric
            WHERE id IN (
                SELECT id
                FROM (
                    SELECT id, enrollment_date, template_type,
                           ROW_NUMBER() OVER (PARTITION BY person_uuid, recapture ORDER BY enrollment_date ASC) AS rnk
                    FROM biometric
                    WHERE archived = 0 AND recapture IN (0, 1, 2) --AND person_uuid = 'd29a16d6-8129-4137-adbf-b7c54b078c55'
                ) AS subquery
                WHERE rnk > 10
            );
            """;

}
