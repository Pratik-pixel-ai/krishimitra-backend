package com.kisaan.kisaan_backend.service;

import com.kisaan.kisaan_backend.model.Farmer;
import com.kisaan.kisaan_backend.repository.FarmerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final FarmerRepository farmerRepository;
    private final WebClient.Builder webClientBuilder;

    public String chat(String phone, String userMessage) {
        Farmer farmer = farmerRepository.findByPhone(phone).orElse(null);;
        String prompt = buildPrompt(farmer, userMessage);
        return callGroq(prompt);
    }

    private String buildPrompt(Farmer farmer, String userMessage) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are Kisaan AI, an expert agricultural assistant for Indian farmers. ");
        sb.append("Answer in simple, practical language. If farmer writes in Hindi, respond in Hindi. ");
        sb.append("Always give specific, actionable advice.\n\n");

        if (farmer != null) {
            sb.append("=== FARMER INFORMATION ===\n");
            sb.append("Name: ").append(farmer.getFullName()).append("\n");
            sb.append("Location: ").append(farmer.getDistrict()).append(", ").append(farmer.getState()).append("\n");

            if (farmer.getSoilType() != null) {
                sb.append("Soil Type: ").append(farmer.getSoilType()).append("\n");
            }
            if (farmer.getFarmSize() != null) {
                sb.append("Farm Size: ").append(farmer.getFarmSize()).append(" acres\n");
            }
            if (farmer.getCropsGrown() != null && !farmer.getCropsGrown().isBlank()) {
                sb.append("Crops Grown: ").append(farmer.getCropsGrown()).append("\n");
            }

            sb.append("\n");
        }

        sb.append("=== FARMER'S QUESTION ===\n");
        sb.append(userMessage).append("\n\n");
        sb.append("Provide helpful, practical farming advice based on the above context.");

        return sb.toString();
    }

    private String callGroq(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "max_tokens", 1024
            );

            System.out.println("Calling Groq API...");

            String rawResponse = webClientBuilder.build()
                    .post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + groqApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Groq Response: " + rawResponse);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawResponse);
            return root.path("choices").get(0)
                    .path("message").path("content").asText();

        } catch (Exception e) {
            System.out.println("Groq Error: " + e.getMessage());
            e.printStackTrace();
            return "Sorry, I could not process your request right now. Please try again.";
        }
    }
}