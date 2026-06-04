package com.blooddonation.dto.request;

import com.blooddonation.enums.BloodGroup;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    private String name;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    private String city;

    private String state;

    private BloodGroup bloodGroup;

    private Boolean isAvailable;
}
