package org.ecews.biometricapp.services;

import org.ecews.biometricapp.entities.IdentificationResponse;
import org.ecews.biometricapp.entities.InterventionResponse;
import org.ecews.biometricapp.repositories.IdentificationResponseRepository;
import org.ecews.biometricapp.repositories.InterventionResponseRepository;
import org.springframework.stereotype.Service;

@Service
public class InterventionResponseService {

    private final InterventionResponseRepository interventionResponseRepository;

    public InterventionResponseService(
            InterventionResponseRepository interventionResponseRepository) {
        this.interventionResponseRepository = interventionResponseRepository;
    }

    public void saveInterventionResponses (InterventionResponse interventionResponse) {
        interventionResponseRepository.save(interventionResponse);
    }
}
