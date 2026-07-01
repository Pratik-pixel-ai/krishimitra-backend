package com.kisaan.kisaan_backend.service;

import com.kisaan.kisaan_backend.dto.AuthResponse;
import com.kisaan.kisaan_backend.dto.LoginRequest;
import com.kisaan.kisaan_backend.dto.RegisterRequest;
import com.kisaan.kisaan_backend.model.Farmer;
import com.kisaan.kisaan_backend.repository.FarmerRepository;
import com.kisaan.kisaan_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final FarmerRepository farmerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (farmerRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }

        Farmer farmer = Farmer.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .state(request.getState())
                .district(request.getDistrict())
                .build();

        farmerRepository.save(farmer);

        String token = jwtUtil.generateToken(farmer.getPhone());

        return AuthResponse.builder()
                .token(token)
                .fullName(farmer.getFullName())
                .phone(farmer.getPhone())
                .state(farmer.getState())
                .district(farmer.getDistrict())
                .message("Registration successful! Welcome to Kisaan AI.")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getPhone(),
                        request.getPassword()
                )
        );

        Farmer farmer = farmerRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        String token = jwtUtil.generateToken(farmer.getPhone());

        return AuthResponse.builder()
                .token(token)
                .fullName(farmer.getFullName())
                .phone(farmer.getPhone())
                .state(farmer.getState())
                .district(farmer.getDistrict())
                .message("Login successful! Welcome back, " + farmer.getFullName())
                .build();
    }
}