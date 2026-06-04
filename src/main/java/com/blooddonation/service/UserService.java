package com.blooddonation.service;

import com.blooddonation.dto.request.UpdateProfileRequest;
import com.blooddonation.dto.response.DonorSearchResponse;
import com.blooddonation.dto.response.UserResponse;
import com.blooddonation.enums.BloodGroup;
import com.blooddonation.enums.Role;
import com.blooddonation.exception.ResourceNotFoundException;
import com.blooddonation.model.Donation;
import com.blooddonation.model.User;
import com.blooddonation.repository.DonationRepository;
import com.blooddonation.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final DonationRepository donationRepository;

    public UserService(UserRepository userRepository, DonationRepository donationRepository) {
        this.userRepository = userRepository;
        this.donationRepository = donationRepository;
    }

    public UserResponse getProfile(Long userId) {
        log.info("Fetching profile for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating profile for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }
        if (request.getState() != null) {
            user.setState(request.getState());
        }
        if (request.getBloodGroup() != null) {
            user.setBloodGroup(request.getBloodGroup());
        }
        if (request.getIsAvailable() != null) {
            user.setAvailable(request.getIsAvailable());
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user ID: {}", userId);
        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public UserResponse toggleAvailability(Long userId) {
        log.info("Toggling availability for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setAvailable(!user.isAvailable());
        User updatedUser = userRepository.save(user);
        log.info("Availability status of user ID: {} changed to {}", userId, updatedUser.isAvailable());
        return mapToUserResponse(updatedUser);
    }

    public List<DonorSearchResponse> findNearbyDonors(String city, BloodGroup bloodGroup) {
        log.info("Searching donors in city: {} with blood group: {}", city, bloodGroup);
        List<User> donors;
        if (bloodGroup != null) {
            donors = userRepository.findByBloodGroupAndCityIgnoreCaseAndIsAvailable(bloodGroup, city, true).stream()
                    .filter(u -> u.getRole() == Role.DONOR)
                    .collect(Collectors.toList());
        } else {
            donors = userRepository.findByCityIgnoreCase(city).stream()
                    .filter(u -> u.getRole() == Role.DONOR && u.isAvailable())
                    .collect(Collectors.toList());
        }

        log.info("Found {} matching donors in {}", donors.size(), city);
        return donors.stream()
                .map(this::mapToDonorSearchResponse)
                .collect(Collectors.toList());
    }

    public List<Donation> getDonationHistory(Long userId) {
        log.info("Fetching donation history for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return donationRepository.findByDonor(user);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Fetching all users (paginated)");
        return userRepository.findAll(pageable).map(this::mapToUserResponse);
    }

    public Page<UserResponse> getAllDonors(Pageable pageable) {
        log.info("Fetching all donors (paginated)");
        return userRepository.findByRole(Role.DONOR, pageable).map(this::mapToUserResponse);
    }

    public UserResponse getProfileByEmail(String email) {
        log.info("Fetching profile for user email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfileByEmail(String email, UpdateProfileRequest request) {
        log.info("Updating profile for user email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return updateProfile(user.getId(), request);
    }

    @Transactional
    public UserResponse toggleAvailabilityByEmail(String email) {
        log.info("Toggling availability for user email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return toggleAvailability(user.getId());
    }

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .bloodGroup(user.getBloodGroup())
                .city(user.getCity())
                .state(user.getState())
                .lastDonationDate(user.getLastDonationDate())
                .isAvailable(user.isAvailable())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private DonorSearchResponse mapToDonorSearchResponse(User user) {
        return DonorSearchResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .bloodGroup(user.getBloodGroup())
                .city(user.getCity())
                .state(user.getState())
                .phone(user.getPhone())
                .isAvailable(user.isAvailable())
                .lastDonationDate(user.getLastDonationDate())
                .build();
    }
}
