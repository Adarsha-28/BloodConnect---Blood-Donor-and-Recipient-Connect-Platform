package com.blooddonation.model;

import com.blooddonation.enums.DonationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_request_id", nullable = false)
    private BloodRequest request;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime donatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationStatus status;
}
