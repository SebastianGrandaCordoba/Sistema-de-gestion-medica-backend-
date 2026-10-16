package com.clinic.medicalmanagement.repository;

import com.clinic.medicalmanagement.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    boolean existsByDocumentNumberIgnoreCase(String documentNumber);
    Optional<Patient> findByDocumentNumberIgnoreCase(String documentNumber);
}
