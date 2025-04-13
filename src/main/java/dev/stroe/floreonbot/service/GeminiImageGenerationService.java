package dev.stroe.floreonbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Optional;

@Service
public class GeminiImageGenerationService {

    private static final Logger log = LoggerFactory.getLogger(GeminiImageGenerationService.class);
    private static final String GEMINI_IMAGE_MODEL_ID = "gemini-2.0-flash-exp-image-generation";
    private static final String GEMINI_IMAGE_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + GEMINI_IMAGE_MODEL_ID + ":generateContent";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String geminiApiKey;

    public GeminiImageGenerationService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key}") String geminiApiKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.geminiApiKey = geminiApiKey;
    }

    public byte[] generateImage(String prompt) throws ImageGenerationException {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new ImageGenerationException("Prompt cannot be empty.");
        }

        try {
            String apiUrl = GEMINI_IMAGE_API_URL + "?key=" + geminiApiKey;
            HttpEntity<String> requestEntity = createRequestEntity(prompt);

            log.info("Sending image generation request to Gemini.");
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return processResponse(response.getBody());
            } else {
                String errorMsg = String.format("Failed to generate image. Status: %s, Body: %s", response.getStatusCode(), response.getBody());
                log.error(errorMsg);
                throw new ImageGenerationException("Sorry, I couldn't generate the image. The API returned an error: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Gemini API error during image generation: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw new ImageGenerationException("Sorry, there was an API error (" + ex.getStatusCode() + ") while generating the image.", ex);
        } catch (Exception e) {
            log.error("Exception during image generation: {}", e.getMessage(), e);
            throw new ImageGenerationException("An unexpected error occurred while generating the image.", e);
        }
    }

    private HttpEntity<String> createRequestEntity(String prompt) {
        ObjectNode rootNode = objectMapper.createObjectNode();

        ArrayNode contentsArray = objectMapper.createArrayNode();
        ObjectNode contentObject = objectMapper.createObjectNode();
        contentObject.put("role", "user");

        ArrayNode partsArray = objectMapper.createArrayNode();
        ObjectNode textPart = objectMapper.createObjectNode();
        textPart.put("text", prompt);
        partsArray.add(textPart);
        contentObject.set("parts", partsArray);
        contentsArray.add(contentObject);
        rootNode.set("contents", contentsArray);

        ObjectNode generationConfig = objectMapper.createObjectNode();
        ArrayNode responseModalities = objectMapper.createArrayNode();
        responseModalities.add("image");
        responseModalities.add("text");
        generationConfig.set("responseModalities", responseModalities);
        generationConfig.put("responseMimeType", "text/plain");
        rootNode.set("generationConfig", generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String requestBody = objectMapper.writeValueAsString(rootNode);
            log.debug("Gemini Image Request Body: {}", requestBody);
            return new HttpEntity<>(requestBody, headers);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create request body for image generation", e);
        }
    }

    private byte[] processResponse(String responseBody) throws ImageGenerationException {
        try {
            log.debug("Gemini Image Response Body: {}", responseBody);
            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode candidates = responseJson.path("candidates");

            if (!candidates.isArray() || candidates.isEmpty()) {
                log.warn("No candidates found in Gemini image response.");
                throw new ImageGenerationException("Sorry, the image generation resulted in no candidates.");
            }

            Optional<String> base64ImageDataOpt = findBase64ImageData(candidates);

            if (base64ImageDataOpt.isPresent()) {
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(base64ImageDataOpt.get());
                    log.info("Successfully decoded image data.");
                    return imageBytes;
                } catch (IllegalArgumentException e) {
                    log.error("Failed to decode Base64 image data: {}", e.getMessage());
                    throw new ImageGenerationException("Sorry, the received image data was corrupted.", e);
                }
            } else {
                log.warn("No image data found in Gemini response parts.");
                String errorMessage = extractTextResponse(responseJson);
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    throw new ImageGenerationException("Image generation failed: " + errorMessage);
                } else {
                    throw new ImageGenerationException("Sorry, I couldn't find the generated image data in the response.");
                }
            }

        } catch (Exception e) {
            if (e instanceof ImageGenerationException) {
                throw (ImageGenerationException) e;
            }
            log.error("Failed to parse Gemini image response or decode image: {}", e.getMessage(), e);
            throw new ImageGenerationException("Sorry, I encountered an error processing the image response.", e);
        }
    }

    private Optional<String> findBase64ImageData(JsonNode candidates) {
        for (JsonNode candidate : candidates) {
            JsonNode content = candidate.path("content");
            JsonNode parts = content.path("parts");
            if (parts.isArray()) {
                for (JsonNode part : parts) {
                    JsonNode inlineData = part.path("inlineData");
                    if (!inlineData.isMissingNode() && inlineData.has("mimeType") && inlineData.path("mimeType").asText().startsWith("image/") && inlineData.has("data")) {
                        return Optional.of(inlineData.path("data").asText());
                    }
                }
            }
        }
        return Optional.empty();
    }

    private String extractTextResponse(JsonNode responseJson) {
        StringBuilder textResponse = new StringBuilder();
        JsonNode candidates = responseJson.path("candidates");
        if (candidates.isArray()) {
            for (JsonNode candidate : candidates) {
                JsonNode content = candidate.path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray()) {
                    for (JsonNode part : parts) {
                        if (part.has("text")) {
                            textResponse.append(part.get("text").asText()).append("\n");
                        }
                    }
                }
            }
        }
        return textResponse.toString().trim();
    }

    public static class ImageGenerationException extends Exception {
        public ImageGenerationException(String message) {
            super(message);
        }

        public ImageGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
