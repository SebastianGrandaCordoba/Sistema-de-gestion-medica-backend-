package com.clinic.medicalmanagement.config;

import com.clinic.medicalmanagement.domain.Doctor;
import com.clinic.medicalmanagement.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DoctorDataInitializer {
    private final DoctorRepository doctorRepository;

    @Bean
    CommandLineRunner seedDoctors() {
        return args -> {
            if (doctorRepository.count() == 0) {
                doctorRepository.saveAll(List.of(
                        new Doctor("Dr. Olivia Bennett", "Cardiology"),
                        new Doctor("Dr. Ethan Carter", "Cardiology"),
                        new Doctor("Dr. Sophia Mitchell", "Dermatology"),
                        new Doctor("Dr. Liam Foster", "Dermatology"),
                        new Doctor("Dr. Ava Reynolds", "Pediatrics")
                ));
            }
        };
    }
}
