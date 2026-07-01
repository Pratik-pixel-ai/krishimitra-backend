package com.kisaan.kisaan_backend.service;

import com.kisaan.kisaan_backend.dto.FarmProfileRequest;
import com.kisaan.kisaan_backend.model.Farmer;
import com.kisaan.kisaan_backend.repository.FarmerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FarmerProfileService {

    @Autowired
    private FarmerRepository farmerRepository;

    public Farmer getProfile(String phone) {
        return farmerRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));
    }

    public Farmer updateProfile(String phone, FarmProfileRequest request) {
        Farmer farmer = farmerRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        farmer.setSoilType(request.getSoilType());
        farmer.setFarmSize(request.getFarmSize());
        farmer.setCropsGrown(request.getCropsGrown());

        return farmerRepository.save(farmer);
    }
}