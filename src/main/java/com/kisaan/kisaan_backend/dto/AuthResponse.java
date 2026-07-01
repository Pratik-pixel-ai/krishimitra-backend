package com.kisaan.kisaan_backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String fullName;
    private String phone;
    private String state;
    private String district;
    private String message;
}