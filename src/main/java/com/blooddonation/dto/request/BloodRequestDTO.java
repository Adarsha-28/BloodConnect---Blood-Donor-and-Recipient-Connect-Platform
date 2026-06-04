package com.blooddonation.dto.request;

import com.blooddonation.enums.BloodGroup;
import com.blooddonation.enums.UrgencyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BloodRequestDTO {

    @NotNull(message = "Required blood group is required")
    private BloodGroup requiredBlood;

    @NotBlank(message = "Hospital name is required")
    private String hospitalName;

    @NotBlank(message = "City is required")
    private String city;

    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be exactly 10 digits")
    private String contactNumber;

    @NotNull(message = "Urgency level is required")
    private UrgencyLevel urgencyLevel;
}
