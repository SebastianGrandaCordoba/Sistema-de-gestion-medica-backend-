package com.clinic.medicalmanagement.service;

import com.clinic.medicalmanagement.domain.*;
import com.clinic.medicalmanagement.dto.ApiDtos.AppointmentResponse;
import com.clinic.medicalmanagement.dto.ApiDtos.DoctorResponse;
import com.clinic.medicalmanagement.exception.BadRequestException;
import com.clinic.medicalmanagement.exception.ConflictException;
import com.clinic.medicalmanagement.exception.ResourceNotFoundException;
import com.clinic.medicalmanagement.repository.AppointmentRepository;
import com.clinic.medicalmanagement.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private static final List<LocalTime> DAILY_SLOTS = List.of(
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0));

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientService patientService;

    @Transactional
    public AppointmentResponse book(Long patientId, String specialty, LocalDate date) {
        patientService.getPatient(patientId);
        if (date.isBefore(LocalDate.now())) throw new BadRequestException("Appointment date cannot be in the past");
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCaseOrderByFullNameAsc(specialty.trim());
        if (doctors.isEmpty()) throw new ResourceNotFoundException("No doctors found for specialty " + specialty);

        for (Doctor doctor : doctors) {
            for (LocalTime slot : DAILY_SLOTS) {
                LocalDateTime scheduledAt = LocalDateTime.of(date, slot);
                if (scheduledAt.isAfter(LocalDateTime.now()) && !appointmentRepository
                        .existsByDoctorIdAndScheduledAtAndStatus(doctor.getId(), scheduledAt, AppointmentStatus.SCHEDULED)) {
                    Appointment appointment = new Appointment();
                    appointment.setPatient(patientService.getPatient(patientId));
                    appointment.setDoctor(doctor);
                    appointment.setScheduledAt(scheduledAt);
                    appointment.setStatus(AppointmentStatus.SCHEDULED);
                    return toResponse(appointmentRepository.save(appointment));
                }
            }
        }
        throw new ConflictException("No appointment slots are available for the selected specialty and date");
    }

    @Transactional
    public AppointmentResponse cancel(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment " + appointmentId + " was not found"));
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ConflictException("Only scheduled appointments can be cancelled");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPastAppointments(Long patientId) {
        return appointmentRepository.findByPatientIdAndScheduledAtBeforeOrderByScheduledAtDesc(patientId, LocalDateTime.now())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getDoctors(String specialty) {
        List<Doctor> doctors = specialty == null || specialty.isBlank()
                ? doctorRepository.findAll() : doctorRepository.findBySpecialtyIgnoreCaseOrderByFullNameAsc(specialty.trim());
        return doctors.stream().map(d -> new DoctorResponse(d.getId(), d.getFullName(), d.getSpecialty())).toList();
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(appointment.getId(), appointment.getPatient().getId(),
                appointment.getDoctor().getFullName(), appointment.getDoctor().getSpecialty(),
                appointment.getScheduledAt(), appointment.getStatus().name(),
                "Please arrive 15 minutes before " + appointment.getScheduledAt());
    }
}
