package com.clinic.medicalmanagement.service;

import com.clinic.medicalmanagement.domain.LabOrder;
import com.clinic.medicalmanagement.domain.LabResult;
import com.clinic.medicalmanagement.domain.Patient;
import com.clinic.medicalmanagement.dto.ApiDtos.LabOrderResponse;
import com.clinic.medicalmanagement.dto.ApiDtos.LabResultResponse;
import com.clinic.medicalmanagement.exception.BadRequestException;
import com.clinic.medicalmanagement.repository.LabOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LaboratoryService {
    private record SimulatedResult(String canonicalName, String value, String referenceRange) {}
    private static final Map<String, SimulatedResult> EXAM_CATALOG = Map.of(
            "hemograma", new SimulatedResult("Complete blood count", "Hemoglobin: 14.2 g/dL", "Hemoglobin: 12.0-17.5 g/dL"),
            "glicemia", new SimulatedResult("Blood glucose", "92 mg/dL", "70-99 mg/dL (fasting)"),
            "perfil lipidico", new SimulatedResult("Lipid profile", "Total cholesterol: 172 mg/dL", "Desirable total cholesterol: <200 mg/dL")
    );

    private final LabOrderRepository labOrderRepository;
    private final PatientService patientService;

    @Transactional
    public LabOrderResponse requestExams(Long patientId, List<String> exams) {
        Patient patient = patientService.getPatient(patientId);
        if (exams == null || exams.isEmpty()) throw new BadRequestException("At least one exam is required");
        LabOrder order = new LabOrder();
        order.setPatient(patient);
        for (String exam : exams) {
            SimulatedResult result = EXAM_CATALOG.get(normalize(exam));
            if (result == null) throw new BadRequestException("Unsupported exam: " + exam);
            order.addResult(new LabResult(result.canonicalName(), result.value(), result.referenceRange()));
        }
        return toResponse(labOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<LabOrderResponse> getByPatient(Long patientId) {
        patientService.getPatient(patientId);
        return labOrderRepository.findByPatientIdOrderByRequestedAtDesc(patientId).stream()
                .map(this::toResponse).toList();
    }

    private String normalize(String value) {
        return java.text.Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private LabOrderResponse toResponse(LabOrder order) {
        List<LabResultResponse> results = order.getResults().stream()
                .map(r -> new LabResultResponse(r.getExamName(), r.getValue(), r.getReferenceRange())).toList();
        return new LabOrderResponse(order.getId(), order.getPatient().getId(), order.getRequestedAt(), results);
    }
}
