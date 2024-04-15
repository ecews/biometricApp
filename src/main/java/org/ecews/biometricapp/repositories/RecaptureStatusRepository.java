package org.ecews.biometricapp.repositories;

import org.ecews.biometricapp.entities.RecaptureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecaptureStatusRepository extends JpaRepository<RecaptureStatus, String> {
}
