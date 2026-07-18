package com.kisaan.kisaan_backend.repository;

import com.kisaan.kisaan_backend.model.Farmer;
import com.kisaan.kisaan_backend.model.PlantDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlantDiagnosisRepository extends JpaRepository<PlantDiagnosis, Long> {
    List<PlantDiagnosis> findByFarmerOrderByCreatedAtDesc(Farmer farmer);
}
