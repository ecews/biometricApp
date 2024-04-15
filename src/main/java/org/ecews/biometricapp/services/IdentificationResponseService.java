package org.ecews.biometricapp.services;

import org.ecews.biometricapp.entities.IdentificationResponse;
import org.ecews.biometricapp.repositories.IdentificationResponseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IdentificationResponseService {

    private final IdentificationResponseRepository identificationResponseRepository;

    public IdentificationResponseService(IdentificationResponseRepository identificationResponseRepository) {
        this.identificationResponseRepository = identificationResponseRepository;
    }

    public void saveIdentificationResponses (IdentificationResponse identificationResponse) {
        identificationResponseRepository.save(identificationResponse);
    }
}
