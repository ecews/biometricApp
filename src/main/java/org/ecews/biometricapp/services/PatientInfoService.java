package org.ecews.biometricapp.services;

import org.ecews.biometricapp.entities.PatientInfo;
import org.ecews.biometricapp.repositories.AllRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientInfoService {

    @Autowired
    AllRepository allRepository;

    public List<PatientInfo> getPatientInfo (String search) {
        return allRepository.getPatient(search);
    }
}
