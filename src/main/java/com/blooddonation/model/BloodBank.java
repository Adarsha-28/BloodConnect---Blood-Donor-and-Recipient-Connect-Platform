package com.blooddonation.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "blood_banks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    private String phone;

    private String operatingHours;
}
