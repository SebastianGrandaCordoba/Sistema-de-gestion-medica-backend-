package com.clinic.medicalmanagement.service;

import com.clinic.medicalmanagement.dto.ApiDtos.*;
import com.clinic.medicalmanagement.exception.BadRequestException;
import com.clinic.medicalmanagement.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ClinicFacade {
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final PrescriptionService prescriptionService;
    private final LaboratoryService laboratoryService;

    @Transactional
    public AppointmentResponse bookAppointment(Long patientId, String specialty, LocalDate date) {
        return bookAppointment(patientId, specialty, date, null, null);
    }

    @Transactional
    public AppointmentResponse bookAppointment(Long patientId, String specialty, LocalDate date,
                                               Long doctorId, LocalTime time) {
        patientService.getPatient(patientId);
        return appointmentService.book(patientId, specialty, date, doctorId, time);
    }

    @Transactional(readOnly = true)
    public CompleteHistoryResponse viewCompleteHistory(Long patientId) {
        PatientResponse patient = patientService.getProfile(patientId);
        List<AppointmentResponse> upcomingAppointments = appointmentService.getUpcomingAppointments(patientId);
        List<AppointmentResponse> pastAppointments = appointmentService.getPastAppointments(patientId);
        List<MedicalRecordResponse> records = medicalRecordService.getRecords(patientId);
        List<PrescriptionResponse> prescriptions = prescriptionService.getByPatient(patientId);
        List<LabOrderResponse> labOrders = laboratoryService.getByPatient(patientId);
        return new CompleteHistoryResponse(patient, upcomingAppointments, pastAppointments,
                records, prescriptions, labOrders);
    }

    @Transactional
    public PrescriptionResponse generatePrescription(Long patientId, List<String> medications) {
        patientService.getPatient(patientId);
        List<String> allergies = medicalRecordService.getAllergies(patientId);
        if (medications == null || medications.isEmpty()) {
            throw new BadRequestException("At least one medication is required");
        }
        for (String medication : medications) {
            String medicationName = medication.split("\\|", -1)[0].trim().toLowerCase(Locale.ROOT);
            boolean conflict = allergies.stream().anyMatch(allergy -> {
                String normalizedAllergy = allergy.toLowerCase(Locale.ROOT);
                return medicationName.contains(normalizedAllergy) || normalizedAllergy.contains(medicationName);
            });
            if (conflict) throw new ConflictException("Prescription blocked because medication may match a recorded allergy: " + medicationName);
        }
        return prescriptionService.generate(patientId, medications);
    }

    @Transactional
    public LabOrderResponse requestExams(Long patientId, List<String> exams) {
        patientService.getPatient(patientId);
        medicalRecordService.getRecords(patientId);
        return laboratoryService.requestExams(patientId, exams);
    }

    @Transactional
    public MedicalRecordResponse recordMedicalHistory(MedicalRecordRequest request) {
        patientService.getPatient(request.patientId());
        return medicalRecordService.record(request);
    }
}
