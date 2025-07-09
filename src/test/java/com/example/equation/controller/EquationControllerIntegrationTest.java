package com.example.equation.controller;

import com.example.equation.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for EquationController using full Spring Boot context with TestRestTemplate.
 * Tests the complete flow from HTTP request to response.
 */
@SpringBootTest(classes = com.example.equationservice.EquationServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EquationControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @LocalServerPort
    private int port;
    
    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/equations";
    }
    
    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
    
    @Test
    @DisplayName("Should handle complete REST API workflow")
    void shouldHandleCompleteWorkflow() {
        // Store an equation
        StoreEquationRequest storeRequest = new StoreEquationRequest("x^2 + y");
        HttpEntity<StoreEquationRequest> storeEntity = new HttpEntity<>(storeRequest, createJsonHeaders());
        
        ResponseEntity<StoreEquationResponse> storeResponse = restTemplate.postForEntity(
            getBaseUrl() + "/store", storeEntity, StoreEquationResponse.class);
        
        assertThat(storeResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(storeResponse.getBody()).isNotNull();
        assertThat(storeResponse.getBody().getId()).isNotNull();
        
        Long equationId = storeResponse.getBody().getId();
        
        // Get all equations
        ResponseEntity<EquationSummaryDto[]> getAllResponse = restTemplate.getForEntity(
            getBaseUrl(), EquationSummaryDto[].class);
        
        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllResponse.getBody()).isNotNull();
        assertThat(getAllResponse.getBody()).hasSize(1);
        assertThat(getAllResponse.getBody()[0].getId()).isEqualTo(equationId);
        assertThat(getAllResponse.getBody()[0].getInfix()).isEqualTo("x^2 + y");
        
        // Evaluate the equation
        EvaluateEquationRequest evalRequest = new EvaluateEquationRequest(Map.of("x", 3.0, "y", 4.0));
        HttpEntity<EvaluateEquationRequest> evalEntity = new HttpEntity<>(evalRequest, createJsonHeaders());
        
        ResponseEntity<EvaluateEquationResponse> evalResponse = restTemplate.postForEntity(
            getBaseUrl() + "/" + equationId + "/evaluate", evalEntity, EvaluateEquationResponse.class);
        
        assertThat(evalResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(evalResponse.getBody()).isNotNull();
        assertThat(evalResponse.getBody().getResult()).isEqualTo(13.0); // 3^2 + 4 = 9 + 4 = 13
    }
    
    @Test
    @DisplayName("Should handle validation errors properly")
    void shouldHandleValidationErrors() {
        // Test blank equation
        StoreEquationRequest invalidRequest = new StoreEquationRequest("");
        HttpEntity<StoreEquationRequest> entity = new HttpEntity<>(invalidRequest, createJsonHeaders());
        
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
            getBaseUrl() + "/store", entity, JsonNode.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error").asText()).isEqualTo("Validation Failed");
        assertThat(response.getBody().get("fieldErrors").get("equation")).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle equation syntax errors")
    void shouldHandleEquationSyntaxErrors() {
        StoreEquationRequest invalidSyntax = new StoreEquationRequest("x + + 1");
        HttpEntity<StoreEquationRequest> entity = new HttpEntity<>(invalidSyntax, createJsonHeaders());
        
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
            getBaseUrl() + "/store", entity, JsonNode.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error").asText()).isEqualTo("Invalid Equation Syntax");
    }
    
    @Test
    @DisplayName("Should handle missing variables during evaluation")
    void shouldHandleMissingVariables() {
        // Store equation first
        StoreEquationRequest storeRequest = new StoreEquationRequest("x + y + z");
        HttpEntity<StoreEquationRequest> storeEntity = new HttpEntity<>(storeRequest, createJsonHeaders());
        
        ResponseEntity<StoreEquationResponse> storeResponse = restTemplate.postForEntity(
            getBaseUrl() + "/store", storeEntity, StoreEquationResponse.class);
        
        assertThat(storeResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long equationId = storeResponse.getBody().getId();
        
        // Try to evaluate with missing variable
        EvaluateEquationRequest incompleteRequest = new EvaluateEquationRequest(Map.of("x", 1.0, "y", 2.0));
        HttpEntity<EvaluateEquationRequest> evalEntity = new HttpEntity<>(incompleteRequest, createJsonHeaders());
        
        ResponseEntity<JsonNode> evalResponse = restTemplate.postForEntity(
            getBaseUrl() + "/" + equationId + "/evaluate", evalEntity, JsonNode.class);
        
        assertThat(evalResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(evalResponse.getBody()).isNotNull();
        assertThat(evalResponse.getBody().get("error").asText()).isEqualTo("Missing Variable");
        assertThat(evalResponse.getBody().get("missingVariable").asText()).isEqualTo("z");
    }
    
    @Test
    @DisplayName("Should handle equation not found")
    void shouldHandleEquationNotFound() {
        EvaluateEquationRequest request = new EvaluateEquationRequest(Map.of("x", 1.0));
        HttpEntity<EvaluateEquationRequest> entity = new HttpEntity<>(request, createJsonHeaders());
        
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
            getBaseUrl() + "/999/evaluate", entity, JsonNode.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error").asText()).isEqualTo("Equation Not Found");
    }
    
    @Test
    @DisplayName("Should handle empty variables map validation")
    void shouldHandleEmptyVariablesValidation() {
        // Store equation first
        StoreEquationRequest storeRequest = new StoreEquationRequest("x + 1");
        HttpEntity<StoreEquationRequest> storeEntity = new HttpEntity<>(storeRequest, createJsonHeaders());
        
        ResponseEntity<StoreEquationResponse> storeResponse = restTemplate.postForEntity(
            getBaseUrl() + "/store", storeEntity, StoreEquationResponse.class);
        
        assertThat(storeResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long equationId = storeResponse.getBody().getId();
        
        // Try to evaluate with empty variables map
        EvaluateEquationRequest emptyRequest = new EvaluateEquationRequest(Map.of());
        HttpEntity<EvaluateEquationRequest> evalEntity = new HttpEntity<>(emptyRequest, createJsonHeaders());
        
        ResponseEntity<JsonNode> evalResponse = restTemplate.postForEntity(
            getBaseUrl() + "/" + equationId + "/evaluate", evalEntity, JsonNode.class);
        
        assertThat(evalResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(evalResponse.getBody()).isNotNull();
        assertThat(evalResponse.getBody().get("error").asText()).isEqualTo("Validation Failed");
        assertThat(evalResponse.getBody().get("fieldErrors").get("variables")).isNotNull();
    }
} 