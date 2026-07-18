package com.kisaan.kisaan_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PlantDiagnosisResponse(
        Long id, String cropName, String diseaseName, boolean healthy, int confidence,
        String description, List<String> organicTreatment, List<String> chemicalTreatment,
        List<String> preventionTips, LocalDateTime createdAt) { }
