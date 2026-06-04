package com.blooddonation.service;

import com.blooddonation.dto.request.BloodRequestDTO;
import com.blooddonation.dto.response.BloodRequestResponse;
import com.blooddonation.dto.response.UserResponse;
import com.blooddonation.enums.BloodGroup;
import com.blooddonation.enums.DonationStatus;
import com.blooddonation.enums.RequestStatus;
import com.blooddonation.enums.Role;
import com.blooddonation.exception.ResourceNotFoundException;
import com.blooddonation.exception.UnauthorizedException;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Donation;
import com.blooddonation.model.User;
import com.blooddonation.repository.BloodRequestRepository;
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
public class BloodRequestService {

    private final BloodRequestRepository bloodRequestRepository;
    private final UserRepository userRepository;
    private final DonationRepository donationRepository;

    public BloodRequestService(BloodRequestRepository bloodRequestRepository,
                               UserRepository userRepository,
                               DonationRepository donationRepository) {
        this.bloodRequestRepository = bloodRequestRepository;
        this.userRepository = userRepository;
        this.donationRepository = donationRepository;
    }

    @Transactional
    public BloodRequestResponse createRequest(BloodRequestDTO dto, Long userId) {
        log.info("Creating blood request by user ID: {}", userId);
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        BloodRequest request = BloodRequest.builder()
                .requiredBlood(dto.getRequiredBlood())
                .hospitalName(dto.getHospitalName())
                .city(dto.getCity())
                .contactNumber(dto.getContactNumber())
                .urgencyLevel(dto.getUrgencyLevel())
                .status(RequestStatus.OPEN)
                .requestedBy(requester)
                .build();

        BloodRequest savedRequest = bloodRequestRepository.save(request);
        log.info("Blood request created successfully with ID: {}", savedRequest.getId());

        // Find matching donors in same city
        List<User> matchingDonors = userRepository.findByBloodGroupAndCityIgnoreCaseAndIsAvailable(
                dto.getRequiredBlood(), dto.getCity(), true);

        log.info("Found {} matching donors in {} to notify", matchingDonors.size(), dto.getCity());


        return mapToBloodRequestResponse(savedRequest);
    }

    @Transactional
    public BloodRequestResponse fulfillRequest(Long requestId, Long donorId) {
        log.info("Fulfilling request ID: {} with donor ID: {}", requestId, donorId);
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.OPEN) {
            log.warn("Fulfillment failed: request ID {} is not OPEN (status: {})", requestId, request.getStatus());
            throw new IllegalStateException("Blood request is not open for fulfillment");
        }

        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with id: " + donorId));

        // Create Donation record
        Donation donation = Donation.builder()
                .donor(donor)
                .request(request)
                .status(DonationStatus.COMPLETED)
                .build();
        donationRepository.save(donation);

        // Update request status
        request.setStatus(RequestStatus.FULFILLED);
        bloodRequestRepository.save(request);

        // Update donor's last donation date
        donor.setLastDonationDate(LocalDate.now());
        userRepository.save(donor);

        log.info("Request ID: {} successfully fulfilled. Donation recorded.", requestId);


        return mapToBloodRequestResponse(request);
    }

    @Transactional
    public BloodRequestResponse cancelRequest(Long requestId, Long userId) {
        log.info("User ID: {} attempting to cancel request ID: {}", userId, requestId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with id: " + requestId));

        // Allow cancellation if user is the requester OR is an admin
        if (!request.getRequestedBy().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            log.warn("Cancellation unauthorized: user ID {} is neither owner nor admin", userId);
            throw new UnauthorizedException("You are not authorized to cancel this request");
        }

        request.setStatus(RequestStatus.CANCELLED);
        BloodRequest updatedRequest = bloodRequestRepository.save(request);
        log.info("Request ID: {} cancelled successfully", requestId);

        return mapToBloodRequestResponse(updatedRequest);
    }

    public Page<BloodRequestResponse> getOpenRequests(String city, Pageable pageable) {
        log.info("Fetching open blood requests (city filter: {})", city);
        Page<BloodRequest> requests;
        if (city != null && !city.trim().isEmpty()) {
            requests = bloodRequestRepository.findByStatusAndCity(RequestStatus.OPEN, city, pageable);
        } else {
            requests = bloodRequestRepository.findByStatus(RequestStatus.OPEN, pageable);
        }
        return requests.map(this::mapToBloodRequestResponse);
    }

    public List<BloodRequestResponse> getMyRequests(Long userId) {
        log.info("Fetching all blood requests created by user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return bloodRequestRepository.findByRequestedBy(user).stream()
                .map(this::mapToBloodRequestResponse)
                .collect(Collectors.toList());
    }

    public BloodRequestResponse getRequestById(Long id) {
        log.info("Fetching blood request by ID: {}", id);
        BloodRequest request = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with id: " + id));
        return mapToBloodRequestResponse(request);
    }

    public BloodRequestResponse mapToBloodRequestResponse(BloodRequest request) {
        return BloodRequestResponse.builder()
                .id(request.getId())
                .requiredBlood(request.getRequiredBlood())
                .hospitalName(request.getHospitalName())
                .city(request.getCity())
                .contactNumber(request.getContactNumber())
                .urgencyLevel(request.getUrgencyLevel())
                .status(request.getStatus())
                .requestedBy(mapToUserResponse(request.getRequestedBy()))
                .createdAt(request.getCreatedAt())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
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
}
