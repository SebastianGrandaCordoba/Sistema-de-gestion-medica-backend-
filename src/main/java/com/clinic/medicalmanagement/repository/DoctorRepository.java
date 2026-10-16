package com.clinic.medicalmanagement.repository;

import com.clinic.medicalmanagement.domain.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialtyIgnoreCaseOrderByFullNameAsc(String specialty);
}
