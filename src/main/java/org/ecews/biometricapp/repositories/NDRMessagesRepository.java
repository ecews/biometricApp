package org.ecews.biometricapp.repositories;

import org.ecews.biometricapp.entities.NDRMessages;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NDRMessagesRepository extends JpaRepository<NDRMessages, Long> {
    @Query(value = "SELECT * FROM ndr_messages where facility_id = ?1 ORDER BY id DESC", nativeQuery = true)
    Page<NDRMessages> findAllNDRMessagesByFacId(String queryParam, Integer archived, Long facilityId, Pageable pageable);

   List<NDRMessages> findNDRMessagesByIsPushedAndFacilityIdAndIdentifier(boolean isPushed, Long facilityId, String identifier);
   

    @Query(value = "SELECT count(*) FROM ndr_messages where facility_id = ?1 and identifier = ?2", nativeQuery = true)
    int getTotalRecordByFacilityAndIdentifier(Long facilityId, String identifier);
    
    
    
    
}
