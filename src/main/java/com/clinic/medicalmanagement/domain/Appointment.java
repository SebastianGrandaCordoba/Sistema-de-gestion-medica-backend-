package com.clinic.medicalmanagement.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointment_doctor_time_status", columnList = "doctor_id,scheduled_at,status"),
        @Index(name = "idx_appointment_patient_time", columnList = "patient_id,scheduled_at")
})
@Getter @Setter @NoArgsConstructor
public class Appointment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Patient patient;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Doctor doctor;
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;
}
