package com.clinic.medicalmanagement.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "patients", uniqueConstraints = @UniqueConstraint(name = "uk_patient_document", columnNames = "document_number"))
@Getter @Setter @NoArgsConstructor
public class Patient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "document_number", nullable = false, length = 40)
    private String documentNumber;
    @Column(nullable = false, length = 120)
    private String fullName;
    @Column(nullable = false, length = 160)
    private String email;
    @Column(nullable = false, length = 30)
    private String phone;
    private LocalDate birthDate;
    @Column(nullable = false, length = 40)
    private String gender;
}
