package com.blooddonation.dto.response;

import com.blooddonation.enums.BloodGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorSearchResponse {
    private Long id;
    private String name;
    private BloodGroup bloodGroup;
    private String city;
    private String state;
    private String phone;
    private boolean isAvailable;
    private LocalDate lastDonationDate;
}
