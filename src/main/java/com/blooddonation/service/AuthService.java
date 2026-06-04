package com.blooddonation.service;

import com.blooddonation.dto.request.LoginRequest;
import com.blooddonation.dto.request.RegisterRequest;
import com.blooddonation.dto.response.AuthResponse;
import com.blooddonation.dto.response.UserResponse;
import com.blooddonation.enums.Role;
import com.blooddonation.exception.UserAlreadyExistsException;
import com.blooddonation.model.User;
import com.blooddonation.repository.UserRepository;
import com.blooddonation.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager, UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        if (request.getRole() == Role.ADMIN) {
            log.warn("Registration failed: registration of ADMIN role is not allowed");
            throw new IllegalArgumentException("Registration of ADMIN role is not allowed through the public portal");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .bloodGroup(request.getBloodGroup())
                .city(request.getCity())
                .state(request.getState())
                .isAvailable(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());


        // Generate token
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(savedUser))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for email: {}", request.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("User not found with email: " + request.getEmail()));

        if (user.getRole() == Role.ADMIN) {
            log.warn("Login failed: Admin attempts standard login for email {}", request.getEmail());
            throw new BadCredentialsException("Admin accounts must login through the admin portal.");
        }

        log.info("User login successful: {}", request.getEmail());
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(user))
                .build();
    }

    public AuthResponse adminLogin(LoginRequest request) {
        log.info("Attempting admin login for email: {}", request.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("User not found with email: " + request.getEmail()));

        if (user.getRole() != Role.ADMIN) {
            log.warn("Admin login failed: Non-admin role {} attempts admin login for email {}", user.getRole(), request.getEmail());
            throw new BadCredentialsException("Access denied: Only administrators can log in here.");
        }

        log.info("Admin login successful: {}", request.getEmail());
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(user))
                .build();
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
}
