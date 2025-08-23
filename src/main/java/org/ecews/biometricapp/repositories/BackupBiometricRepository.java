package org.ecews.biometricapp.repositories;

import org.ecews.biometricapp.entities.BackupBiometric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BackupBiometricRepository extends JpaRepository<BackupBiometric, String> {

    public List<BackupBiometric> findByPersonUuid(String personUUid);
}
