package com.clinic.medicalmanagement.service;

import com.clinic.medicalmanagement.domain.Patient;
import com.clinic.medicalmanagement.domain.Prescription;
import com.clinic.medicalmanagement.domain.PrescriptionMedication;
import com.clinic.medicalmanagement.dto.ApiDtos.MedicationResponse;
import com.clinic.medicalmanagement.dto.ApiDtos.PrescriptionResponse;
import com.clinic.medicalmanagement.exception.BadRequestException;
import com.clinic.medicalmanagement.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final PatientService patientService;

    @Transactional
    public PrescriptionResponse generate(Long patientId, List<String> medicationSpecifications) {
        Patient patient = patientService.getPatient(patientId);
        if (medicationSpecifications == null || medicationSpecifications.isEmpty()) {
            throw new BadRequestException("At least one medication is required");
        }
        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        for (String specification : medicationSpecifications) {
            String[] fields = specification.split("\\|", -1);
            if (fields.length != 3 || fields[0].isBlank() || fields[1].isBlank() || fields[2].isBlank()) {
                throw new BadRequestException("Each medication must use the format name|dose|duration");
            }
            prescription.addMedication(new PrescriptionMedication(fields[0].trim(), fields[1].trim(), fields[2].trim()));
        }
        return toResponse(prescriptionRepository.save(prescription));
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getByPatient(Long patientId) {
        patientService.getPatient(patientId);
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::toResponse).toList();
    }

    private PrescriptionResponse toResponse(Prescription prescription) {
        List<MedicationResponse> medications = prescription.getMedications().stream()
                .map(m -> new MedicationResponse(m.getName(), m.getDose(), m.getDuration())).toList();
        return new PrescriptionResponse(prescription.getId(), prescription.getPatient().getId(),
                prescription.getCreatedAt(), medications);
    }
}
