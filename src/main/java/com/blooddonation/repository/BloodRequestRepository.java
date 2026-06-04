package com.blooddonation.repository;

import com.blooddonation.enums.BloodGroup;
import com.blooddonation.enums.RequestStatus;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    List<BloodRequest> findByStatus(RequestStatus status);

    Page<BloodRequest> findByStatus(RequestStatus status, Pageable pageable);

    Page<BloodRequest> findByStatusAndCity(RequestStatus status, String city, Pageable pageable);

    List<BloodRequest> findByCity(String city);

    List<BloodRequest> findByRequiredBloodAndStatusAndCity(BloodGroup requiredBlood, RequestStatus status, String city);

    List<BloodRequest> findByRequestedBy(User requestedBy);

    @Query("SELECT r.requiredBlood, COUNT(r) FROM BloodRequest r WHERE r.status = com.blooddonation.enums.RequestStatus.OPEN GROUP BY r.requiredBlood")
    List<Object[]> countOpenRequestsByBloodGroup();
}
