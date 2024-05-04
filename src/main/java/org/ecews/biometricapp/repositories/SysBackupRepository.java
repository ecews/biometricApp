package org.ecews.biometricapp.repositories;

import org.ecews.biometricapp.entities.IdentificationResponse;
import org.ecews.biometricapp.entities.SysBackup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysBackupRepository extends JpaRepository<SysBackup, String> {
}
