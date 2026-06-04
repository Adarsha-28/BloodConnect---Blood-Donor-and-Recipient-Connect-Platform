package com.blooddonation.service;

import com.blooddonation.dto.request.BloodRequestDTO;
import com.blooddonation.dto.response.BloodRequestResponse;
import com.blooddonation.enums.BloodGroup;
import com.blooddonation.enums.RequestStatus;
import com.blooddonation.enums.Role;
import com.blooddonation.enums.UrgencyLevel;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Donation;
import com.blooddonation.model.User;
import com.blooddonation.repository.BloodRequestRepository;
import com.blooddonation.repository.DonationRepository;
import com.blooddonation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BloodRequestServiceTest {

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DonationRepository donationRepository;

    @InjectMocks
    private BloodRequestService bloodRequestService;

    @Test
    public void testCreateRequest_Success() {
        BloodRequestDTO dto = new BloodRequestDTO(
                BloodGroup.AB_NEGATIVE, "City Hospital", "Austin", "9998887776", UrgencyLevel.HIGH
        );

        User requester = User.builder().id(1L).name("Recipient").email("rec@example.com").role(Role.RECIPIENT).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));

        BloodRequest request = BloodRequest.builder()
                .id(10L)
                .requiredBlood(dto.getRequiredBlood())
                .hospitalName(dto.getHospitalName())
                .city(dto.getCity())
                .contactNumber(dto.getContactNumber())
                .urgencyLevel(dto.getUrgencyLevel())
                .status(RequestStatus.OPEN)
                .requestedBy(requester)
                .build();

        when(bloodRequestRepository.save(any(BloodRequest.class))).thenReturn(request);

        BloodRequestResponse response = bloodRequestService.createRequest(dto, 1L);

        assertNotNull(response);
        assertEquals(RequestStatus.OPEN, response.getStatus());
        assertEquals("City Hospital", response.getHospitalName());
        verify(bloodRequestRepository, times(1)).save(any(BloodRequest.class));
    }

    @Test
    public void testCreateRequest_NotifiesDonors() {
        BloodRequestDTO dto = new BloodRequestDTO(
                BloodGroup.O_NEGATIVE, "City Hospital", "Austin", "9998887776", UrgencyLevel.CRITICAL
        );

        User requester = User.builder().id(1L).name("Recipient").email("rec@example.com").role(Role.RECIPIENT).build();
        User donor1 = User.builder().id(2L).name("Donor1").email("d1@example.com").role(Role.DONOR).build();
        User donor2 = User.builder().id(3L).name("Donor2").email("d2@example.com").role(Role.DONOR).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepository.findByBloodGroupAndCityIgnoreCaseAndIsAvailable(BloodGroup.O_NEGATIVE, "Austin", true))
                .thenReturn(Arrays.asList(donor1, donor2));

        BloodRequest request = BloodRequest.builder()
                .id(10L)
                .requiredBlood(dto.getRequiredBlood())
                .hospitalName(dto.getHospitalName())
                .city(dto.getCity())
                .contactNumber(dto.getContactNumber())
                .urgencyLevel(dto.getUrgencyLevel())
                .status(RequestStatus.OPEN)
                .requestedBy(requester)
                .build();

        when(bloodRequestRepository.save(any(BloodRequest.class))).thenReturn(request);

        bloodRequestService.createRequest(dto, 1L);


    }

    @Test
    public void testFulfillRequest_UpdatesStatus() {
        User requester = User.builder().id(1L).name("Recipient").email("rec@example.com").role(Role.RECIPIENT).build();
        User donor = User.builder().id(2L).name("Donor").email("donor@example.com").role(Role.DONOR).build();

        BloodRequest request = BloodRequest.builder()
                .id(10L)
                .status(RequestStatus.OPEN)
                .requestedBy(requester)
                .build();

        when(bloodRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(userRepository.findById(2L)).thenReturn(Optional.of(donor));
        when(donationRepository.save(any(Donation.class))).thenReturn(new Donation());

        BloodRequestResponse response = bloodRequestService.fulfillRequest(10L, 2L);

        assertNotNull(response);
        assertEquals(RequestStatus.FULFILLED, response.getStatus());
        assertNotNull(donor.getLastDonationDate());
        verify(bloodRequestRepository, times(1)).save(request);
        verify(donationRepository, times(1)).save(any(Donation.class));
    }

    @Test
    public void testCancelRequest_Success() {
        User requester = User.builder().id(1L).name("Recipient").email("rec@example.com").role(Role.RECIPIENT).build();
        BloodRequest request = BloodRequest.builder()
                .id(10L)
                .status(RequestStatus.OPEN)
                .requestedBy(requester)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(bloodRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(bloodRequestRepository.save(request)).thenReturn(request);

        BloodRequestResponse response = bloodRequestService.cancelRequest(10L, 1L);

        assertNotNull(response);
        assertEquals(RequestStatus.CANCELLED, response.getStatus());
        verify(bloodRequestRepository, times(1)).save(request);
    }
}
