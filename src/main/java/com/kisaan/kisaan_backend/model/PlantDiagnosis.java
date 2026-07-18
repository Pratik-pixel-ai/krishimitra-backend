package com.kisaan.kisaan_backend.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plant_diagnoses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantDiagnosis {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    private String cropName;
    @Column(nullable = false) private String diseaseName;
    @Column(nullable = false) private boolean healthy;
    @Column(nullable = false) private int confidence;
    @Column(columnDefinition = "TEXT") private String description;

    @ElementCollection @CollectionTable(name = "plant_diagnosis_organic_treatment", joinColumns = @JoinColumn(name = "diagnosis_id"))
    @Column(name = "tip", columnDefinition = "TEXT") @Builder.Default
    private List<String> organicTreatment = new ArrayList<>();
    @ElementCollection @CollectionTable(name = "plant_diagnosis_chemical_treatment", joinColumns = @JoinColumn(name = "diagnosis_id"))
    @Column(name = "tip", columnDefinition = "TEXT") @Builder.Default
    private List<String> chemicalTreatment = new ArrayList<>();
    @ElementCollection @CollectionTable(name = "plant_diagnosis_prevention", joinColumns = @JoinColumn(name = "diagnosis_id"))
    @Column(name = "tip", columnDefinition = "TEXT") @Builder.Default
    private List<String> preventionTips = new ArrayList<>();

    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @jakarta.persistence.PrePersist
    void prePersist() { createdAt = LocalDateTime.now(); }
}
