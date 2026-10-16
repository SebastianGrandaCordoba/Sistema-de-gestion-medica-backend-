package com.clinic.medicalmanagement.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_records", indexes = @Index(name = "idx_medical_record_patient", columnList = "patient_id,recorded_at"))
@Getter @Setter @NoArgsConstructor
public class MedicalRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Patient patient;
    @Column(nullable = false, length = 500)
    private String consultation;
    @Column(length = 500)
    private String diagnosis;
    @Column(length = 500)
    private String allergies;
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();
}
