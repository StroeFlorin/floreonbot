package dev.stroe.floreonbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class GeminiService {

    private final RestTemplate restTemplate;
    private final String geminiApiKey;
    private static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-04-17:generateContent";

    public GeminiService(
            RestTemplate restTemplate,
            @Value("${gemini.api.key}") String geminiApiKey) {
        this.restTemplate = restTemplate;
        this.geminiApiKey = geminiApiKey;
    }

    public String ask(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Please provide a question!";
        }

        try {
            String apiUrl = GEMINI_API_BASE_URL + "?key=" + geminiApiKey;
            HttpEntity<String> requestEntity = createAskRequestEntity(text);
            String responseBody = callGeminiApi(apiUrl, requestEntity);
            return processAskResponse(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I encountered an error while processing your request.";
        }
    }

    private HttpEntity<String> createAskRequestEntity(String text) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ObjectNode contents = mapper.createObjectNode();
        contents.put("role", "user");
        ObjectNode parts = mapper.createObjectNode();
        parts.put("text", text);
        contents.set("parts", mapper.createArrayNode().add(parts));
        rootNode.set("contents", mapper.createArrayNode().add(contents));
        // Set required headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            return new HttpEntity<>(mapper.writeValueAsString(rootNode), headers);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create request body", e);
        }
    }

    private String callGeminiApi(String apiUrl, HttpEntity<String> entity) {
        // Make the API call and return the response body
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
        return response.getBody();
    }

    private String processAskResponse(String responseBody) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJson = mapper.readTree(responseBody);
        JsonNode candidates = responseJson.path("candidates");

        if (!candidates.isArray() || candidates.size() == 0) {
            return "Sorry, I couldn't generate a response.";
        }

        JsonNode content = candidates.get(0).path("content");
        if (content.isMissingNode()) {
            return "Sorry, I couldn't generate a response.";
        }

        JsonNode parts = content.path("parts");
        if (!parts.isArray() || parts.size() == 0) {
            return "Sorry, I couldn't generate a response.";
        }

        JsonNode part = parts.get(0);
        if (!part.has("text")) {
            return "Sorry, I couldn't generate a response.";
        }

        return part.get("text").asText();
    }
}
