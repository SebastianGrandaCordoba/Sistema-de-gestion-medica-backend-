package com.clinic.medicalmanagement.repository;

import com.clinic.medicalmanagement.domain.Appointment;
import com.clinic.medicalmanagement.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    boolean existsByDoctorIdAndScheduledAtAndStatus(Long doctorId, LocalDateTime scheduledAt, AppointmentStatus status);
    List<Appointment> findByPatientIdAndScheduledAtBeforeOrderByScheduledAtDesc(Long patientId, LocalDateTime before);
}
