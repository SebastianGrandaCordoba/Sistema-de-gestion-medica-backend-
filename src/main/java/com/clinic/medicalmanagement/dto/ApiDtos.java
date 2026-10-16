package com.clinic.medicalmanagement.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}

    public record PatientRequest(
            @NotBlank @Size(max = 40) String documentNumber,
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email @Size(max = 160) String email,
            @NotBlank @Size(max = 30) String phone,
            @Past LocalDate birthDate,
            @NotBlank @Size(max = 40) String gender) {}

    public record PatientResponse(Long id, String documentNumber, String fullName, String email,
                                  String phone, LocalDate birthDate, String gender) {}

    public record AppointmentRequest(@NotNull @Positive Long patientId,
                                     @NotBlank @Size(max = 80) String specialty,
                                     @NotNull @FutureOrPresent LocalDate date,
                                     @Positive Long doctorId,
                                     LocalTime time) {}

    public record AppointmentResponse(Long id, Long patientId, String doctorName, String specialty,
                                      LocalDateTime scheduledAt, String status, String reminder) {}

    public record DoctorResponse(Long id, String fullName, String specialty) {}

    public record AvailabilityResponse(Long doctorId, String doctorName, String specialty,
                                       LocalDate date, List<LocalTime> availableSlots) {}

    public record MedicalRecordRequest(@NotNull @Positive Long patientId,
                                       @NotBlank @Size(max = 500) String consultation,
                                       @Size(max = 500) String diagnosis,
                                       @Size(max = 500) String allergies) {}

    public record MedicalRecordResponse(Long id, String consultation, String diagnosis,
                                        String allergies, LocalDateTime recordedAt) {}

    public record PrescriptionRequest(@NotNull @Positive Long patientId,
                                      @NotEmpty List<@NotBlank @Size(max = 300) String> medications) {}

    public record MedicationResponse(String name, String dose, String duration) {}
    public record PrescriptionResponse(Long id, Long patientId, LocalDateTime createdAt,
                                       List<MedicationResponse> medications) {}

    public record LabRequest(@NotNull @Positive Long patientId,
                             @NotEmpty List<@NotBlank @Size(max = 100) String> exams) {}

    public record LabResultResponse(String examName, String value, String referenceRange) {}
    public record LabOrderResponse(Long id, Long patientId, LocalDateTime requestedAt,
                                   List<LabResultResponse> results) {}

    public record CompleteHistoryResponse(PatientResponse patient,
                                          List<AppointmentResponse> upcomingAppointments,
                                          List<AppointmentResponse> pastAppointments,
                                          List<MedicalRecordResponse> medicalRecords,
                                          List<PrescriptionResponse> prescriptions,
                                          List<LabOrderResponse> laboratoryOrders) {}

    public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message,
                                String path, java.util.Map<String, String> validationErrors) {}
}
