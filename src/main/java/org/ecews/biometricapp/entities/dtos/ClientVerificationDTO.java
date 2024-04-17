package org.ecews.biometricapp.entities.dtos;

import java.time.LocalDate;

public interface ClientVerificationDTO {
    String getClientVerification();
    String getPickupByProxy();
    String getDuplicatedDemographic();
    String getNoRecapture();
    String getBatchPickupDates();
    String getLastVisitIsOver18M();
    String getArtStartPickupDate();
    String getNoInitBiometric();
    String getIncompleteVisitData();
    String getRepeatEncounterNoPrint();
    String getLongIntervalsARVPickup();
    String getSameSexDOBARTStartDate();
    String getOtherSpecifyForCV();
    LocalDate getCt1STDate();
    String getFirstStatus();
    String getFirstOutcome();
    LocalDate getCt2NdDate();
    String getSecondStatus();
    String getSecondOutcome();
    LocalDate getCtLastDate();
    String getLastStatus();
    String getLastOutcome();

}
