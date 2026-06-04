package com.blooddonation.repository;

import com.blooddonation.enums.BloodGroup;
import com.blooddonation.model.BloodInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BloodInventoryRepository extends JpaRepository<BloodInventory, Long> {

    List<BloodInventory> findByBloodBankId(Long bloodBankId);

    Optional<BloodInventory> findByBloodBankIdAndBloodGroup(Long bloodBankId, BloodGroup bloodGroup);
}
