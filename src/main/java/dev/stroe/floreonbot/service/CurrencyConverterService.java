package dev.stroe.floreonbot.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CurrencyConverterService {
  private final RestTemplate restTemplate;

    @Value("${exchangerate.api.key}")
    private String apiKey;
    
    public CurrencyConverterService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public Double convertCurrency(String fromCurrency, String toCurrency, double amount) {
        String url = String.format("https://v6.exchangerate-api.com/v6/%s/pair/%s/%s/%f",
                apiKey, fromCurrency, toCurrency, amount);
                
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        
        if (response != null && "success".equals(response.get("result"))) {
            return (Double) response.get("conversion_result");
        } else {
            String errorType = response != null ? (String) response.get("error-type") : "null";
            String errorInfo = response != null ? "Base: " + response.get("base_code") + 
                               ", Target: " + response.get("target_code") + 
                               ", Amount: " + amount : "No response";
            throw new RuntimeException("Failed to convert currency. Error: " + errorType + ". " + errorInfo);
        }
    }
}
