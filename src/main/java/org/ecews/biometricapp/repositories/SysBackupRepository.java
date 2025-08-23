package org.ecews.biometricapp.repositories;

import org.ecews.biometricapp.entities.IdentificationResponse;
import org.ecews.biometricapp.entities.SysBackup;
import org.ecews.biometricapp.repositories.projections.FingerCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SysBackupRepository extends JpaRepository<SysBackup, String> {

    @Query(value = "SELECT sb.person_uuid, COUNT(sb) AS finger_count FROM sys_backup sb WHERE sb.backup_date = :backupDate GROUP BY sb.person_uuid", nativeQuery = true)
    List<FingerCountProjection> getFingerCountForDate(@Param("backupDate") LocalDate backupDate);
}
