package com.blooddonation.service;

import com.blooddonation.dto.request.RegisterRequest;
import com.blooddonation.dto.response.AuthResponse;
import com.blooddonation.dto.response.DonorSearchResponse;
import com.blooddonation.dto.response.UserResponse;
import com.blooddonation.enums.BloodGroup;
import com.blooddonation.enums.Role;
import com.blooddonation.exception.UserAlreadyExistsException;
import com.blooddonation.model.User;
import com.blooddonation.repository.DonationRepository;
import com.blooddonation.repository.UserRepository;
import com.blooddonation.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private UserService userService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil, null, userDetailsService);
    }

    @Test
    public void testRegisterDonor_Success() {
        RegisterRequest request = new RegisterRequest(
                "John Doe", "john@example.com", "password123", "1234567890",
                Role.DONOR, BloodGroup.O_POSITIVE, "Dallas", "Texas"
        );

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed_password");

        User savedUser = User.builder()
                .id(1L)
                .name(request.getName())
                .email(request.getEmail())
                .password("hashed_password")
                .phone(request.getPhone())
                .role(request.getRole())
                .bloodGroup(request.getBloodGroup())
                .city(request.getCity())
                .state(request.getState())
                .isAvailable(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(request.getEmail())).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt_token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
        assertEquals("John Doe", response.getUser().getName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testRegisterDonor_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "John Doe", "john@example.com", "password123", "1234567890",
                Role.DONOR, BloodGroup.O_POSITIVE, "Dallas", "Texas"
        );

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(request);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegisterAdmin_ThrowsIllegalArgumentException() {
        RegisterRequest request = new RegisterRequest(
                "Admin User", "admin@example.com", "password123", "1234567890",
                Role.ADMIN, null, "Dallas", "Texas"
        );

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            authService.register(request);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testFindNearbyDonors_ReturnsMatchingDonors() {
        User donor1 = User.builder()
                .id(1L)
                .name("Alice")
                .bloodGroup(BloodGroup.O_POSITIVE)
                .city("Dallas")
                .state("Texas")
                .phone("1112223333")
                .isAvailable(true)
                .role(Role.DONOR)
                .build();

        when(userRepository.findByBloodGroupAndCityIgnoreCaseAndIsAvailable(BloodGroup.O_POSITIVE, "Dallas", true))
                .thenReturn(Arrays.asList(donor1));

        List<DonorSearchResponse> results = userService.findNearbyDonors("Dallas", BloodGroup.O_POSITIVE);

        assertEquals(1, results.size());
        assertEquals("Alice", results.get(0).getName());
    }

    @Test
    public void testToggleAvailability_Success() {
        User donor = User.builder()
                .id(1L)
                .name("Bob")
                .isAvailable(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(donor));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.toggleAvailability(1L);

        assertFalse(response.isAvailable());
        verify(userRepository, times(1)).save(donor);
    }
}
