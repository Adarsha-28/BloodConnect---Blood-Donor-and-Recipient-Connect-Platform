package com.blooddonation.controller;

import com.blooddonation.dto.request.BloodRequestDTO;
import com.blooddonation.dto.response.BloodRequestResponse;
import com.blooddonation.dto.response.UserResponse;
import com.blooddonation.enums.BloodGroup;
import com.blooddonation.enums.RequestStatus;
import com.blooddonation.enums.UrgencyLevel;
import com.blooddonation.security.JwtUtil;
import com.blooddonation.service.BloodRequestService;
import com.blooddonation.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.blooddonation.config.SecurityConfig;
import com.blooddonation.security.JwtAuthFilter;
import org.springframework.context.annotation.Import;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BloodRequestController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@AutoConfigureMockMvc
public class BloodRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BloodRequestService bloodRequestService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsService userDetailsService;

    private BloodRequestResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = BloodRequestResponse.builder()
                .id(1L)
                .requiredBlood(BloodGroup.O_POSITIVE)
                .hospitalName("Hope Hospital")
                .city("Dallas")
                .contactNumber("1234567890")
                .urgencyLevel(UrgencyLevel.CRITICAL)
                .status(RequestStatus.OPEN)
                .build();
    }

    @Test
    @WithMockUser(username = "recipient@example.com", roles = {"RECIPIENT"})
    public void testCreateRequest_Returns201() throws Exception {
        BloodRequestDTO dto = new BloodRequestDTO(
                BloodGroup.O_POSITIVE, "Hope Hospital", "Dallas", "1234567890", UrgencyLevel.CRITICAL
        );

        UserResponse user = UserResponse.builder().id(1L).email("recipient@example.com").build();
        when(userService.getProfileByEmail("recipient@example.com")).thenReturn(user);
        when(bloodRequestService.createRequest(any(BloodRequestDTO.class), any(Long.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hospitalName").value("Hope Hospital"));
    }

    @Test
    public void testGetOpenRequests_Returns200() throws Exception {
        Page<BloodRequestResponse> page = new PageImpl<>(Collections.singletonList(mockResponse), PageRequest.of(0, 10), 1);
        when(bloodRequestService.getOpenRequests(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/requests")
                        .param("city", "Dallas")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].hospitalName").value("Hope Hospital"));
    }

    @Test
    @WithMockUser(username = "donor@example.com", roles = {"DONOR"})
    public void testFulfillRequest_AsDonor_Returns200() throws Exception {
        mockResponse.setStatus(RequestStatus.FULFILLED);
        when(bloodRequestService.fulfillRequest(1L, 2L)).thenReturn(mockResponse);

        mockMvc.perform(patch("/api/requests/1/fulfill")
                        .with(csrf())
                        .param("donorId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("FULFILLED"));
    }

    @Test
    public void testCreateRequest_WithoutAuth_Returns401() throws Exception {
        BloodRequestDTO dto = new BloodRequestDTO(
                BloodGroup.O_POSITIVE, "Hope Hospital", "Dallas", "1234567890", UrgencyLevel.CRITICAL
        );

        mockMvc.perform(post("/api/requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
