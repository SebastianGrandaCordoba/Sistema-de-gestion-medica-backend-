package com.clinic.medicalmanagement.repository;

import com.clinic.medicalmanagement.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    boolean existsByDocumentNumberIgnoreCase(String documentNumber);
}
