package com.blooddonation.dto.response;

import com.blooddonation.enums.BloodGroup;
import com.blooddonation.enums.RequestStatus;
import com.blooddonation.enums.UrgencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodRequestResponse {
    private Long id;
    private BloodGroup requiredBlood;
    private String hospitalName;
    private String city;
    private String contactNumber;
    private UrgencyLevel urgencyLevel;
    private RequestStatus status;
    private UserResponse requestedBy;
    private LocalDateTime createdAt;
}
