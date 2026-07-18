package com.kisaan.kisaan_backend.controller;

import com.kisaan.kisaan_backend.dto.PlantDiagnosisResponse;
import com.kisaan.kisaan_backend.service.PlantDiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/ai/diagnose")
@RequiredArgsConstructor
public class PlantDiagnosisController {
    private final PlantDiagnosisService diagnosisService;

    @PostMapping
    public ResponseEntity<PlantDiagnosisResponse> diagnose(@AuthenticationPrincipal UserDetails user,
            @RequestParam("image") MultipartFile image, @RequestParam(value = "cropName", required = false) String cropName) {
        return ResponseEntity.ok(diagnosisService.diagnose(user.getUsername(), image, cropName));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PlantDiagnosisResponse>> history(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(diagnosisService.history(user.getUsername()));
    }
}
