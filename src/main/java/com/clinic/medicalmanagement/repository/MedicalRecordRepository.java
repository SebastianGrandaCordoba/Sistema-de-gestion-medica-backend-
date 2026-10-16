package com.clinic.medicalmanagement.repository;

import com.clinic.medicalmanagement.domain.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatientIdOrderByRecordedAtDesc(Long patientId);
}
