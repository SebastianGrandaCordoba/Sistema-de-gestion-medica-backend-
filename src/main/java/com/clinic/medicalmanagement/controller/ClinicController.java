package com.clinic.medicalmanagement.controller;

import com.clinic.medicalmanagement.dto.ApiDtos.*;
import com.clinic.medicalmanagement.service.AppointmentService;
import com.clinic.medicalmanagement.service.ClinicFacade;
import com.clinic.medicalmanagement.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinica")
@RequiredArgsConstructor
public class ClinicController {
    private final ClinicFacade clinicFacade;
    private final PatientService patientService;
    private final AppointmentService appointmentService;

    @PostMapping("/paciente")
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse registerPatient(@Valid @RequestBody PatientRequest request) {
        return patientService.register(request);
    }

    @GetMapping("/paciente/{patientId}")
    public PatientResponse getPatientProfile(@PathVariable Long patientId) {
        return patientService.getProfile(patientId);
    }

    @PostMapping("/cita")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse bookAppointment(@Valid @RequestBody AppointmentRequest request) {
        return clinicFacade.bookAppointment(request.patientId(), request.specialty(), request.date());
    }

    @DeleteMapping("/cita/{appointmentId}")
    public AppointmentResponse cancelAppointment(@PathVariable Long appointmentId) {
        return appointmentService.cancel(appointmentId);
    }

    @GetMapping("/historia/{pacienteId}")
    public CompleteHistoryResponse getCompleteHistory(@PathVariable Long pacienteId) {
        return clinicFacade.viewCompleteHistory(pacienteId);
    }

    @PostMapping("/historia")
    @ResponseStatus(HttpStatus.CREATED)
    public MedicalRecordResponse recordMedicalHistory(@Valid @RequestBody MedicalRecordRequest request) {
        return clinicFacade.recordMedicalHistory(request);
    }

    @PostMapping("/prescripcion")
    @ResponseStatus(HttpStatus.CREATED)
    public PrescriptionResponse generatePrescription(@Valid @RequestBody PrescriptionRequest request) {
        return clinicFacade.generatePrescription(request.patientId(), request.medications());
    }

    @PostMapping("/laboratorio")
    @ResponseStatus(HttpStatus.CREATED)
    public LabOrderResponse requestExams(@Valid @RequestBody LabRequest request) {
        return clinicFacade.requestExams(request.patientId(), request.exams());
    }

    @GetMapping("/medicos")
    public List<DoctorResponse> getDoctors(@RequestParam(required = false, name = "especialidad") String specialty) {
        return appointmentService.getDoctors(specialty);
    }
}
