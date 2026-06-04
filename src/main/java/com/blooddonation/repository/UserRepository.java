package com.blooddonation.repository;

import com.blooddonation.enums.BloodGroup;
import com.blooddonation.enums.Role;
import com.blooddonation.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByBloodGroupAndCityIgnoreCaseAndIsAvailable(BloodGroup bloodGroup, String city, boolean isAvailable);

    List<User> findByCityIgnoreCase(String city);

    List<User> findByRole(Role role);

    Page<User> findByRole(Role role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = com.blooddonation.enums.Role.DONOR AND u.isAvailable = true AND (u.lastDonationDate IS NULL OR u.lastDonationDate <= :cutoffDate)")
    List<User> findEligibleDonors(@Param("cutoffDate") LocalDate cutoffDate);
}
