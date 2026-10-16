package com.clinic.medicalmanagement.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lab_results")
@Getter @Setter @NoArgsConstructor
public class LabResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private LabOrder labOrder;
    @Column(nullable = false, length = 100)
    private String examName;
    @Column(name = "result_value", nullable = false, length = 100)
    private String value;
    @Column(nullable = false, length = 100)
    private String referenceRange;

    public LabResult(String examName, String value, String referenceRange) {
        this.examName = examName;
        this.value = value;
        this.referenceRange = referenceRange;
    }
}
