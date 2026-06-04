package com.blooddonation.controller;

import com.blooddonation.dto.response.ApiResponse;
import com.blooddonation.enums.BloodGroup;
import com.blooddonation.model.BloodBank;
import com.blooddonation.model.BloodInventory;
import com.blooddonation.service.BloodBankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banks")
@Tag(name = "Blood Bank Management", description = "Endpoints for viewing blood banks and updating blood inventory")
public class BloodBankController {

    private final BloodBankService bloodBankService;

    public BloodBankController(BloodBankService bloodBankService) {
        this.bloodBankService = bloodBankService;
    }

    @GetMapping
    @Operation(summary = "Get blood banks", description = "Fetch all registered blood banks. Can filter by city. Public endpoint.")
    public ResponseEntity<ApiResponse<List<BloodBank>>> getAllBanks(
            @RequestParam(required = false) String city) {
        List<BloodBank> banks = bloodBankService.getAllBanks(city);
        return ResponseEntity.ok(ApiResponse.success("Blood banks fetched successfully", banks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get blood bank details", description = "Retrieve a blood bank's details by its ID. Public endpoint.")
    public ResponseEntity<ApiResponse<BloodBank>> getBankById(@PathVariable Long id) {
        BloodBank bank = bloodBankService.getBankById(id);
        return ResponseEntity.ok(ApiResponse.success("Blood bank details fetched successfully", bank));
    }

    @GetMapping("/{id}/inventory")
    @Operation(summary = "Get blood bank inventory", description = "Retrieve current blood inventory stock levels for a specific blood bank. Public endpoint.")
    public ResponseEntity<ApiResponse<List<BloodInventory>>> getBankInventory(@PathVariable Long id) {
        List<BloodInventory> inventory = bloodBankService.getInventoryByBank(id);
        return ResponseEntity.ok(ApiResponse.success("Blood inventory fetched successfully", inventory));
    }

    @PutMapping("/{id}/inventory")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update blood inventory", description = "Update stock level for a specific blood group in a blood bank. Restricted to ADMIN role.")
    public ResponseEntity<ApiResponse<BloodInventory>> updateInventory(
            @PathVariable Long id,
            @RequestParam BloodGroup bloodGroup,
            @RequestParam int units) {
        BloodInventory updated = bloodBankService.updateInventory(id, bloodGroup, units);
        return ResponseEntity.ok(ApiResponse.success("Blood inventory updated successfully", updated));
    }
}
