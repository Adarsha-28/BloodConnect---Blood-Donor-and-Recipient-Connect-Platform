package com.blooddonation.model;

import com.blooddonation.enums.BloodGroup;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_inventories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_bank_id", nullable = false)
    private BloodBank bloodBank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodGroup bloodGroup;

    @Column(nullable = false)
    private Integer unitsAvailable;

    @UpdateTimestamp
    private LocalDateTime lastUpdated;
}
