package dev.stroe.floreonbot.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import dev.stroe.floreonbot.exception.CurrencyConversionException;

@Service
public class CurrencyConverterService {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyConverterService.class);
    private final RestTemplate restTemplate;

    @Value("${exchangerate.api.key}")
    private String apiKey;
    
    public CurrencyConverterService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public Double convertCurrency(String fromCurrency, String toCurrency, double amount) {
        if (fromCurrency == null || toCurrency == null) {
            throw new IllegalArgumentException("Currency codes cannot be null");
        }
        
        String url = String.format("https://v6.exchangerate-api.com/v6/%s/pair/%s/%s/%.2f",
                apiKey, fromCurrency, toCurrency, amount);
                
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response == null) {
                throw new CurrencyConversionException("Empty response from currency API", 
                    fromCurrency, toCurrency, amount, "no_response", null);
            }
            
            String result = (String) response.getOrDefault("result", "");
            if ("success".equals(result)) {
                Object conversionResult = response.get("conversion_result");
                if (conversionResult == null) {
                    throw new CurrencyConversionException("Missing conversion result in API response", 
                        fromCurrency, toCurrency, amount, "missing_data", null);
                }
                return Double.valueOf(conversionResult.toString());
            } else {
                String errorType = (String) response.getOrDefault("error-type", "unknown");
                logger.error("Currency conversion error: {}, from: {}, to: {}, amount: {}", 
                    errorType, fromCurrency, toCurrency, amount);
                throw new CurrencyConversionException(
                    "Failed to convert currency", fromCurrency, toCurrency, amount, errorType, null);
            }
        } catch (HttpClientErrorException e) {
            logger.error("Client error during currency conversion", e);
            throw new CurrencyConversionException("Invalid request to currency API", 
                fromCurrency, toCurrency, amount, "client_error", e);
        } catch (HttpServerErrorException e) {
            logger.error("Server error during currency conversion", e);
            throw new CurrencyConversionException("Currency API service is unavailable", 
                fromCurrency, toCurrency, amount, "server_error", e);
        } catch (RestClientException e) {
            logger.error("Network error during currency conversion", e);
            throw new CurrencyConversionException("Network error connecting to currency API", 
                fromCurrency, toCurrency, amount, "network_error", e);
        } catch (Exception e) {
            logger.error("Unexpected error during currency conversion", e);
            throw new CurrencyConversionException("Unexpected error during currency conversion", 
                fromCurrency, toCurrency, amount, "unexpected_error", e);
        }
    }
}
