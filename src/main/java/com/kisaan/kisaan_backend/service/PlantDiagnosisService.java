package com.kisaan.kisaan_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kisaan.kisaan_backend.dto.PlantDiagnosisResponse;
import com.kisaan.kisaan_backend.model.Farmer;
import com.kisaan.kisaan_backend.model.PlantDiagnosis;
import com.kisaan.kisaan_backend.repository.FarmerRepository;
import com.kisaan.kisaan_backend.repository.PlantDiagnosisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantDiagnosisService {
    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private final FarmerRepository farmerRepository;
    private final PlantDiagnosisRepository diagnosisRepository;
    private final WebClient.Builder webClientBuilder;
    // Built directly rather than injected: this deployment doesn't have an
    // ObjectMapper bean auto-configured, so constructor-injecting one fails
    // application startup entirely ("No qualifying bean of type ObjectMapper").
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}") private String geminiApiKey;
    @Value("${gemini.model}") private String geminiModel;

    public PlantDiagnosisResponse diagnose(String phone, MultipartFile image, String cropName) {
        validateImage(image);
        Farmer farmer = farmerRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("Farmer account was not found."));
        try {
            JsonNode assessment = callGemini(image, cropName);
            PlantDiagnosis saved = diagnosisRepository.save(PlantDiagnosis.builder()
                    .farmer(farmer).cropName(blankToNull(cropName))
                    .diseaseName(requiredText(assessment, "diseaseName", "Unknown condition"))
                    .healthy(assessment.path("healthy").asBoolean(false))
                    .confidence(Math.max(0, Math.min(100, assessment.path("confidence").asInt(50))))
                    .description(requiredText(assessment, "description", "No description returned."))
                    .organicTreatment(textList(assessment.path("organicTreatment")))
                    .chemicalTreatment(textList(assessment.path("chemicalTreatment")))
                    .preventionTips(textList(assessment.path("preventionTips"))).build());
            return response(saved);
        } catch (IOException e) {
            throw new IllegalStateException("The diagnosis service returned an unreadable response. Please try again.", e);
        }
    }

    public List<PlantDiagnosisResponse> history(String phone) {
        Farmer farmer = farmerRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("Farmer account was not found."));
        return diagnosisRepository.findByFarmerOrderByCreatedAtDesc(farmer).stream().map(this::response).toList();
    }

    private JsonNode callGemini(MultipartFile image, String cropName) throws IOException {
        ObjectNode body = objectMapper.createObjectNode();
        ArrayNode contents = body.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt(cropName));
        ObjectNode inlineData = parts.addObject().putObject("inline_data");
        byte[] payload = shrinkIfNeeded(image.getBytes());
        inlineData.put("mime_type", "image/jpeg");
        inlineData.put("data", Base64.getEncoder().encodeToString(payload));
        body.putObject("generationConfig").put("response_mime_type", "application/json").put("temperature", 0.2);

        String raw = webClientBuilder.build().post()
                .uri(uriBuilder -> uriBuilder.scheme("https").host("generativelanguage.googleapis.com")
                        .path("/v1beta/models/{model}:generateContent").queryParam("key", geminiApiKey)
                        .build(geminiModel))
                .contentType(MediaType.APPLICATION_JSON).bodyValue(objectMapper.writeValueAsString(body)).retrieve()
                .bodyToMono(String.class).block();
        JsonNode root = objectMapper.readTree(raw == null ? "{}" : raw);
        String json = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        if (json.isBlank()) throw new IllegalStateException("The diagnosis service did not return an assessment.");
        return objectMapper.readTree(stripCodeFence(json));
    }

    private static String prompt(String cropName) {
        String crop = cropName == null || cropName.isBlank() ? "not provided" : cropName.trim();
        return "You are a careful agricultural plant-health assistant for Indian farmers. Analyze the supplied plant image. "
                + "The reported crop is: " + crop + ". Do not claim certainty from an unclear image. "
                + "Return only valid JSON with this exact schema: {\"diseaseName\":string,\"healthy\":boolean,\"confidence\":integer 0-100,\"description\":string,\"organicTreatment\":[string],\"chemicalTreatment\":[string],\"preventionTips\":[string]}. "
                + "Give concise, safe treatments. For pesticides, state that local label directions and agricultural-extension guidance must be followed.";
    }

    private static void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) throw new IllegalArgumentException("Please upload a plant image.");
        if (image.getSize() > MAX_IMAGE_BYTES) throw new IllegalArgumentException("Image must be 10 MB or smaller.");
        String type = image.getContentType();
        if (type == null || !type.startsWith("image/")) throw new IllegalArgumentException("Only image files are supported.");
    }

    // Phone camera photos routinely arrive at 3000px+ / several MB. Sending that
    // straight through as base64 roughly triples the in-memory footprint (raw
    // bytes + base64 string + JSON encoding buffer), which is enough to exhaust
    // memory on a small hosting instance. Downscaling to a diagnosis-friendly
    // resolution keeps things fast and avoids that failure mode entirely.
    private static final int MAX_DIMENSION = 1600;

    private static byte[] shrinkIfNeeded(byte[] original) throws IOException {
        BufferedImage source = ImageIO.read(new java.io.ByteArrayInputStream(original));
        if (source == null) return original; // not a decodable raster image (e.g. unusual format); send as-is
        int width = source.getWidth();
        int height = source.getHeight();
        double scale = Math.min(1.0, (double) MAX_DIMENSION / Math.max(width, height));
        if (scale >= 1.0 && original.length <= 2L * 1024 * 1024) return original; // already small enough

        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(resized, "jpg", out);
        return out.toByteArray();
    }
    private static List<String> textList(JsonNode node) { List<String> values = new ArrayList<>(); if (node.isArray()) node.forEach(item -> { if (item.isTextual() && !item.asText().isBlank()) values.add(item.asText().trim()); }); return values; }
    private static String requiredText(JsonNode node, String key, String fallback) { String value = node.path(key).asText().trim(); return value.isEmpty() ? fallback : value; }
    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static String stripCodeFence(String value) { return value.replaceFirst("^\\s*```(?:json)?\\s*", "").replaceFirst("\\s*```\\s*$", "").trim(); }
    private PlantDiagnosisResponse response(PlantDiagnosis value) { return new PlantDiagnosisResponse(value.getId(), value.getCropName(), value.getDiseaseName(), value.isHealthy(), value.getConfidence(), value.getDescription(), value.getOrganicTreatment(), value.getChemicalTreatment(), value.getPreventionTips(), value.getCreatedAt()); }
}
