package com.blooddonation.controller;

import com.blooddonation.dto.request.BloodRequestDTO;
import com.blooddonation.dto.response.ApiResponse;
import com.blooddonation.dto.response.BloodRequestResponse;
import com.blooddonation.dto.response.PaginatedResponse;
import com.blooddonation.dto.response.UserResponse;
import com.blooddonation.service.BloodRequestService;
import com.blooddonation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
@Tag(name = "Blood Requests", description = "Endpoints for creating and managing blood requests")
public class BloodRequestController {

    private final BloodRequestService bloodRequestService;
    private final UserService userService;

    public BloodRequestController(BloodRequestService bloodRequestService, UserService userService) {
        this.bloodRequestService = bloodRequestService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('RECIPIENT')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Create blood request", description = "Submit a new blood request. Restricted to RECIPIENT role.")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> createRequest(
            Principal principal, @Valid @RequestBody BloodRequestDTO dto) {
        UserResponse user = userService.getProfileByEmail(principal.getName());
        BloodRequestResponse response = bloodRequestService.createRequest(dto, user.getId());
        return new ResponseEntity<>(ApiResponse.success("Blood request created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get open requests", description = "Fetch a paginated list of all open blood requests. Can filter by city. Public endpoint.")
    public ResponseEntity<PaginatedResponse<BloodRequestResponse>> getOpenRequests(
            @RequestParam(required = false) String city,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<BloodRequestResponse> page = bloodRequestService.getOpenRequests(city, pageable);
        return ResponseEntity.ok(PaginatedResponse.of(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements()
        ));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('RECIPIENT')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get my blood requests", description = "Retrieve requests submitted by the authenticated recipient. Restricted to RECIPIENT role.")
    public ResponseEntity<ApiResponse<List<BloodRequestResponse>>> getMyRequests(Principal principal) {
        UserResponse user = userService.getProfileByEmail(principal.getName());
        List<BloodRequestResponse> response = bloodRequestService.getMyRequests(user.getId());
        return ResponseEntity.ok(ApiResponse.success("My requests fetched successfully", response));
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get request details", description = "Retrieve a blood request by its ID. Requires authentication.")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> getRequestById(@PathVariable Long id) {
        BloodRequestResponse response = bloodRequestService.getRequestById(id);
        return ResponseEntity.ok(ApiResponse.success("Request details fetched successfully", response));
    }

    @PatchMapping("/{id}/fulfill")
    @PreAuthorize("hasRole('DONOR')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Fulfill request", description = "Fulfill an open blood request. Restricted to DONOR role.")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> fulfillRequest(
            @PathVariable Long id, @RequestParam Long donorId) {
        BloodRequestResponse response = bloodRequestService.fulfillRequest(id, donorId);
        return ResponseEntity.ok(ApiResponse.success("Blood request fulfilled successfully", response));
    }

    @PatchMapping("/{id}/cancel")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Cancel request", description = "Cancel a blood request. Requesters can cancel their own, and ADMINs can cancel any request.")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> cancelRequest(
            Principal principal, @PathVariable Long id) {
        UserResponse user = userService.getProfileByEmail(principal.getName());
        BloodRequestResponse response = bloodRequestService.cancelRequest(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Blood request cancelled successfully", response));
    }
}
