package com.clinic.medicalmanagement.service;

import com.clinic.medicalmanagement.domain.*;
import com.clinic.medicalmanagement.dto.ApiDtos.AppointmentResponse;
import com.clinic.medicalmanagement.dto.ApiDtos.AvailabilityResponse;
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
        return book(patientId, specialty, date, null, null);
    }

    @Transactional
    public AppointmentResponse book(Long patientId, String specialty, LocalDate date, Long doctorId, LocalTime time) {
        Patient patient = patientService.getPatient(patientId);
        if (date.isBefore(LocalDate.now())) throw new BadRequestException("Appointment date cannot be in the past");
        if (time != null && !DAILY_SLOTS.contains(time)) {
            throw new BadRequestException("Appointment time must be one of the clinic slots: " + DAILY_SLOTS);
        }

        List<Doctor> candidates = resolveCandidates(specialty, doctorId);
        List<LocalTime> slots = time == null ? DAILY_SLOTS : List.of(time);

        for (Doctor doctor : candidates) {
            for (LocalTime slot : slots) {
                LocalDateTime scheduledAt = LocalDateTime.of(date, slot);
                if (scheduledAt.isAfter(LocalDateTime.now()) && isSlotFree(doctor.getId(), scheduledAt)) {
                    Appointment appointment = new Appointment();
                    appointment.setPatient(patient);
                    appointment.setDoctor(doctor);
                    appointment.setScheduledAt(scheduledAt);
                    appointment.setStatus(AppointmentStatus.SCHEDULED);
                    return toResponse(appointmentRepository.save(appointment));
                }
            }
        }
        throw new ConflictException(doctorId != null || time != null
                ? "The selected doctor is not available at the selected time"
                : "No appointment slots are available for the selected specialty and date");
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
    public List<AppointmentResponse> getUpcomingAppointments(Long patientId) {
        return appointmentRepository.findByPatientIdAndScheduledAtGreaterThanEqualAndStatusOrderByScheduledAtAsc(
                        patientId, LocalDateTime.now(), AppointmentStatus.SCHEDULED)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor " + doctorId + " was not found"));
        LocalDateTime now = LocalDateTime.now();
        List<LocalTime> availableSlots = DAILY_SLOTS.stream()
                .filter(slot -> LocalDateTime.of(date, slot).isAfter(now))
                .filter(slot -> isSlotFree(doctorId, LocalDateTime.of(date, slot)))
                .toList();
        return new AvailabilityResponse(doctor.getId(), doctor.getFullName(), doctor.getSpecialty(), date, availableSlots);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getDoctors(String specialty) {
        List<Doctor> doctors = specialty == null || specialty.isBlank()
                ? doctorRepository.findAll() : doctorRepository.findBySpecialtyIgnoreCaseOrderByFullNameAsc(specialty.trim());
        return doctors.stream().map(d -> new DoctorResponse(d.getId(), d.getFullName(), d.getSpecialty())).toList();
    }

    private List<Doctor> resolveCandidates(String specialty, Long doctorId) {
        if (doctorId != null) {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor " + doctorId + " was not found"));
            if (specialty != null && !specialty.isBlank() && !doctor.getSpecialty().equalsIgnoreCase(specialty.trim())) {
                throw new BadRequestException("Doctor " + doctor.getFullName() + " does not belong to specialty " + specialty);
            }
            return List.of(doctor);
        }
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCaseOrderByFullNameAsc(specialty.trim());
        if (doctors.isEmpty()) throw new ResourceNotFoundException("No doctors found for specialty " + specialty);
        return doctors;
    }

    private boolean isSlotFree(Long doctorId, LocalDateTime scheduledAt) {
        return !appointmentRepository.existsByDoctorIdAndScheduledAtAndStatus(
                doctorId, scheduledAt, AppointmentStatus.SCHEDULED);
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(appointment.getId(), appointment.getPatient().getId(),
                appointment.getDoctor().getFullName(), appointment.getDoctor().getSpecialty(),
                appointment.getScheduledAt(), appointment.getStatus().name(),
                "Please arrive 15 minutes before " + appointment.getScheduledAt());
    }
}
