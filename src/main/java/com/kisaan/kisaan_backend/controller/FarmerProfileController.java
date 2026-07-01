package com.kisaan.kisaan_backend.controller;

import com.kisaan.kisaan_backend.dto.FarmProfileRequest;
import com.kisaan.kisaan_backend.model.Farmer;
import com.kisaan.kisaan_backend.service.FarmerProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/farmer")
public class FarmerProfileController {

    @Autowired
    private FarmerProfileService farmerProfileService;

    @GetMapping("/profile")
    public ResponseEntity<Farmer> getProfile(Principal principal) {
        Farmer farmer = farmerProfileService.getProfile(principal.getName());
        return ResponseEntity.ok(farmer);
    }

    @PutMapping("/profile")
    public ResponseEntity<Farmer> updateProfile(
            Principal principal,
            @RequestBody FarmProfileRequest request) {
        Farmer updated = farmerProfileService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok(updated);
    }
}