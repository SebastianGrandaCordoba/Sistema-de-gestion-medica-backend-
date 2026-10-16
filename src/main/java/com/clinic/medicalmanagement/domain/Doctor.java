package com.clinic.medicalmanagement.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "doctors")
@Getter @Setter @NoArgsConstructor
public class Doctor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String fullName;
    @Column(nullable = false, length = 80)
    private String specialty;

    public Doctor(String fullName, String specialty) {
        this.fullName = fullName;
        this.specialty = specialty;
    }
}
