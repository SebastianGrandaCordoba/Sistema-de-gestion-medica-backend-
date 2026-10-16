package com.clinic.medicalmanagement.service;

import com.clinic.medicalmanagement.domain.MedicalRecord;
import com.clinic.medicalmanagement.domain.Patient;
import com.clinic.medicalmanagement.dto.ApiDtos.MedicalRecordRequest;
import com.clinic.medicalmanagement.dto.ApiDtos.MedicalRecordResponse;
import com.clinic.medicalmanagement.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientService patientService;

    @Transactional
    public MedicalRecordResponse record(MedicalRecordRequest request) {
        Patient patient = patientService.getPatient(request.patientId());
        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setConsultation(request.consultation().trim());
        record.setDiagnosis(request.diagnosis() == null ? null : request.diagnosis().trim());
        record.setAllergies(request.allergies() == null ? null : request.allergies().trim());
        return toResponse(medicalRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getRecords(Long patientId) {
        patientService.getPatient(patientId);
        return medicalRecordRepository.findByPatientIdOrderByRecordedAtDesc(patientId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<String> getAllergies(Long patientId) {
        patientService.getPatient(patientId);
        return medicalRecordRepository.findByPatientIdOrderByRecordedAtDesc(patientId).stream()
                .map(MedicalRecord::getAllergies).filter(value -> value != null && !value.isBlank())
                .flatMap(value -> java.util.Arrays.stream(value.split(",")))
                .map(String::trim).filter(value -> !value.isBlank()).distinct().toList();
    }

    private MedicalRecordResponse toResponse(MedicalRecord record) {
        return new MedicalRecordResponse(record.getId(), record.getConsultation(), record.getDiagnosis(),
                record.getAllergies(), record.getRecordedAt());
    }
}
