package org.ecews.biometricapp.services;

import org.ecews.biometricapp.entities.RecaptureStatus;
import org.ecews.biometricapp.repositories.RecaptureStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecaptureStatusService {

    @Autowired
    RecaptureStatusRepository recaptureStatusRepository;

    public void saveStatus (List<RecaptureStatus> recaptureStatuses) {
        recaptureStatusRepository.saveAll(recaptureStatuses);
    }
}
