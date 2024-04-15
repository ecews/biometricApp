package org.ecews.biometricapp.repositories;

import org.ecews.biometricapp.entities.IdentificationResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentificationResponseRepository extends JpaRepository<IdentificationResponse, String> {
}
