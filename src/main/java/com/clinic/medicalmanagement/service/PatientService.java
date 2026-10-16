package com.clinic.medicalmanagement.service;

import com.clinic.medicalmanagement.domain.Patient;
import com.clinic.medicalmanagement.dto.ApiDtos.PatientRequest;
import com.clinic.medicalmanagement.dto.ApiDtos.PatientResponse;
import com.clinic.medicalmanagement.exception.ConflictException;
import com.clinic.medicalmanagement.exception.ResourceNotFoundException;
import com.clinic.medicalmanagement.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;

    @Transactional
    public PatientResponse register(PatientRequest request) {
        String documentNumber = request.documentNumber().trim();
        if (patientRepository.existsByDocumentNumberIgnoreCase(documentNumber)) {
            throw new ConflictException("A patient with this document number already exists");
        }
        Patient patient = new Patient();
        patient.setDocumentNumber(documentNumber);
        patient.setFullName(request.fullName().trim());
        patient.setEmail(request.email().trim().toLowerCase());
        patient.setPhone(request.phone().trim());
        patient.setBirthDate(request.birthDate());
        patient.setGender(request.gender().trim());
        try {
            return toResponse(patientRepository.saveAndFlush(patient));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("A patient with this document number already exists");
        }
    }

    @Transactional(readOnly = true)
    public Patient getPatient(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient " + patientId + " was not found"));
    }

    @Transactional(readOnly = true)
    public PatientResponse getProfile(Long patientId) { return toResponse(getPatient(patientId)); }

    public PatientResponse toResponse(Patient patient) {
        return new PatientResponse(patient.getId(), patient.getDocumentNumber(), patient.getFullName(),
                patient.getEmail(), patient.getPhone(), patient.getBirthDate(), patient.getGender());
    }
}
