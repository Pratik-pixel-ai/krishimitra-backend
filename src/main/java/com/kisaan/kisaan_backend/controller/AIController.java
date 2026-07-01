package com.kisaan.kisaan_backend.controller;

import com.kisaan.kisaan_backend.dto.ChatRequest;
import com.kisaan.kisaan_backend.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ChatRequest request) {

        String phone = userDetails.getUsername();
        String response = aiService.chat(phone, request.getMessage());
        return ResponseEntity.ok(Map.of("response", response));
    }
}