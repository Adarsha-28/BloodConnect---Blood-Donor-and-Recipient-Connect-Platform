package com.blooddonation.repository;

import com.blooddonation.model.BloodBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodBankRepository extends JpaRepository<BloodBank, Long> {

    List<BloodBank> findByCity(String city);

    List<BloodBank> findByCityIgnoreCase(String city);
}
