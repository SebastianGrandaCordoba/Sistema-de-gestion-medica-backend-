package com.clinic.medicalmanagement.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prescription_medications")
@Getter @Setter @NoArgsConstructor
public class PrescriptionMedication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Prescription prescription;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 120)
    private String dose;
    @Column(nullable = false, length = 80)
    private String duration;

    public PrescriptionMedication(String name, String dose, String duration) {
        this.name = name;
        this.dose = dose;
        this.duration = duration;
    }
}
