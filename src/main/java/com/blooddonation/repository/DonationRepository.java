package com.blooddonation.repository;

import com.blooddonation.enums.DonationStatus;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Donation;
import com.blooddonation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByDonor(User donor);

    List<Donation> findByRequest(BloodRequest request);

    long countByDonorAndStatus(User donor, DonationStatus status);
}
