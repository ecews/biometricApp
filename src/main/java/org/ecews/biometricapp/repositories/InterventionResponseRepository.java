package org.ecews.biometricapp.repositories;

import org.ecews.biometricapp.entities.IdentificationResponse;
import org.ecews.biometricapp.entities.InterventionResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterventionResponseRepository extends JpaRepository<InterventionResponse, String> {
}
