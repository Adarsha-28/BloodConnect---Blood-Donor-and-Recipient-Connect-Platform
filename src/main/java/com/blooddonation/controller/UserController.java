package com.blooddonation.controller;

import com.blooddonation.dto.request.UpdateProfileRequest;
import com.blooddonation.dto.response.ApiResponse;
import com.blooddonation.dto.response.PaginatedResponse;
import com.blooddonation.dto.response.UserResponse;
import com.blooddonation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for user profiles and admin operations")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get own profile", description = "Retrieve profile details of the authenticated user.")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(Principal principal) {
        UserResponse response = userService.getProfileByEmail(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", response));
    }

    @PutMapping("/me")
    @Operation(summary = "Update own profile", description = "Update profile details of the authenticated user.")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            Principal principal, @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateProfileByEmail(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @PatchMapping("/me/availability")
    @PreAuthorize("hasRole('DONOR')")
    @Operation(summary = "Toggle availability", description = "Toggles donor's availability status. Restricted to DONOR role.")
    public ResponseEntity<ApiResponse<UserResponse>> toggleAvailability(Principal principal) {
        UserResponse response = userService.toggleAvailabilityByEmail(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Availability status toggled successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Retrieve any user's profile by their ID. Restricted to ADMIN role.")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getProfile(id);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Fetch all registered users in a paginated list. Restricted to ADMIN role.")
    public ResponseEntity<PaginatedResponse<UserResponse>> getAllUsers(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<UserResponse> page = userService.getAllUsers(pageable);
        return ResponseEntity.ok(PaginatedResponse.of(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements()
        ));
    }
}
