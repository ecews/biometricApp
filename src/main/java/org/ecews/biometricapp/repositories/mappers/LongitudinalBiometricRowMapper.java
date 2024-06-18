package org.ecews.biometricapp.repositories.mappers;

import org.ecews.biometricapp.entities.dtos.BiometricLongitudinalDTO;
import org.ecews.biometricapp.entities.dtos.CurrentRecaptureDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LongitudinalBiometricRowMapper implements RowMapper<BiometricLongitudinalDTO> {
    @Override
    public BiometricLongitudinalDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

        BiometricLongitudinalDTO cr = new BiometricLongitudinalDTO();

        try {
            cr.setLga(rs.getString("lga"));
            cr.setState(rs.getString("state"));
            cr.setPatientId(rs.getString("patientId"));
            cr.setHospitalNumber(rs.getString("hospital_number"));
            cr.setUniqueId(rs.getString("uniqueId"));
            cr.setAge(rs.getInt("age"));
            cr.setSex(rs.getString("sex"));
            cr.setDateOfBirth((rs.getDate("date_of_birth") != null) ? rs.getDate("date_of_birth").toLocalDate() : null);
            cr.setFacilityName(rs.getString("facility_name"));
            cr.setDatimId(rs.getString("datimId"));
            cr.setTargetGroup(rs.getString("targetGroup"));
            cr.setEnrollmentSetting(rs.getString("enrollment_setting"));
            cr.setDateOfRegistration((rs.getDate("date_of_registration") != null) ? rs.getDate("date_of_registration").toLocalDate() : null);
            cr.setSurname(rs.getString("surname"));
            cr.setFirstName(rs.getString("first_name"));
            cr.setLgaOfResidence(rs.getString("lgaOfResidence"));

            cr.setBaselineCount(rs.getInt("baseline_count"));
            cr.setBaselineDate((rs.getDate("baseline_date") != null) ? rs.getDate("baseline_date").toLocalDate() : null);

            cr.setRecaptureOneCount(rs.getInt("recapture_1_count"));
            cr.setRecaptureOneDate((rs.getDate("recapture_1_date") != null) ? rs.getDate("recapture_1_date").toLocalDate() : null);

            cr.setRecaptureTwoCount(rs.getInt("recapture_2_count"));
            cr.setRecaptureTwoDate((rs.getDate("recapture_2_date") != null) ? rs.getDate("recapture_2_date").toLocalDate() : null);

            cr.setRecaptureThreeCount(rs.getInt("recapture_3_count"));
            cr.setRecaptureThreeDate((rs.getDate("recapture_3_date") != null) ? rs.getDate("recapture_3_date").toLocalDate() : null);

            cr.setRecaptureFourCount(rs.getInt("recapture_4_count"));
            cr.setRecaptureFourDate((rs.getDate("recapture_4_date") != null) ? rs.getDate("recapture_4_date").toLocalDate() : null);

            cr.setRecaptureFiveCount(rs.getInt("recapture_5_count"));
            cr.setRecaptureFiveDate((rs.getDate("recapture_5_date") != null) ? rs.getDate("recapture_5_date").toLocalDate() : null);

            cr.setRecaptureSixCount(rs.getInt("recapture_6_count"));
            cr.setRecaptureSixDate((rs.getDate("recapture_6_date") != null) ? rs.getDate("recapture_6_date").toLocalDate() : null);

            cr.setRecaptureSevenCount(rs.getInt("recapture_7_count"));
            cr.setRecaptureSevenDate((rs.getDate("recapture_7_date") != null) ? rs.getDate("recapture_7_date").toLocalDate() : null);

            cr.setRecaptureEightCount(rs.getInt("recapture_8_count"));
            cr.setRecaptureEightDate((rs.getDate("recapture_8_date") != null) ? rs.getDate("recapture_8_date").toLocalDate() : null);

            cr.setRecaptureNineCount(rs.getInt("recapture_9_count"));
            cr.setRecaptureNineDate((rs.getDate("recapture_9_date") != null) ? rs.getDate("recapture_9_date").toLocalDate() : null);

            cr.setRecaptureTenCount(rs.getInt("recapture_10_count"));
            cr.setRecaptureTenDate((rs.getDate("recapture_10_date") != null) ? rs.getDate("recapture_10_date").toLocalDate() : null);
        } catch (Exception e){
            e.printStackTrace();
        }

        return cr;
    }
}
