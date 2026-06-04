package com.blooddonation.controller;

import com.blooddonation.dto.response.ApiResponse;
import com.blooddonation.dto.response.DonorSearchResponse;
import com.blooddonation.dto.response.UserResponse;
import com.blooddonation.enums.BloodGroup;
import com.blooddonation.model.Donation;
import com.blooddonation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/donors")
@Tag(name = "Donor Directory", description = "Endpoints for searching donors and tracking donation history")
public class DonorController {

    private final UserService userService;

    public DonorController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search local donors", description = "Search for available donors in a specific city and optional blood group. Publicly accessible.")
    public ResponseEntity<ApiResponse<List<DonorSearchResponse>>> searchDonors(
            @RequestParam String city,
            @RequestParam(required = false) BloodGroup bloodGroup) {
        List<DonorSearchResponse> response = userService.findNearbyDonors(city, bloodGroup);
        return ResponseEntity.ok(ApiResponse.success("Donors fetched successfully", response));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('DONOR')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get donation history", description = "Fetch own donation history record. Restricted to DONOR role.")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMyDonationHistory(Principal principal) {
        UserResponse user = userService.getProfileByEmail(principal.getName());
        List<Donation> history = userService.getDonationHistory(user.getId());

        List<Map<String, Object>> response = history.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("donatedAt", d.getDonatedAt());
            map.put("status", d.getStatus());

            Map<String, Object> requestDetails = new HashMap<>();
            requestDetails.put("requestId", d.getRequest().getId());
            requestDetails.put("hospitalName", d.getRequest().getHospitalName());
            requestDetails.put("city", d.getRequest().getCity());
            requestDetails.put("requiredBlood", d.getRequest().getRequiredBlood());
            requestDetails.put("urgencyLevel", d.getRequest().getUrgencyLevel());
            map.put("request", requestDetails);

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Donation history fetched successfully", response));
    }
}
