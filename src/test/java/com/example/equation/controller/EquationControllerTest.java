package com.example.equation.controller;

import com.example.equation.dto.*;
import com.example.equation.exception.EquationSyntaxException;
import com.example.equation.exception.VariableNotProvidedException;
import com.example.equation.service.EquationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import com.example.equationservice.EquationServiceApplication;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for EquationController REST endpoints.
 * Tests all HTTP endpoints, request/response handling, validation, and error scenarios.
 */
@WebMvcTest(controllers = EquationController.class)
@ContextConfiguration(classes = EquationServiceApplication.class)
class EquationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private EquationService equationService;
    
    @Nested
    @DisplayName("POST /api/equations/store")
    class StoreEquationTests {
        
        @Test
        @DisplayName("Should store equation and return 201 Created with ID")
        void shouldStoreEquationSuccessfully() throws Exception {
            // Given
            StoreEquationRequest request = new StoreEquationRequest("x^2 + 2*x + 1");
            Long expectedId = 1L;
            when(equationService.storeEquation(request.getEquation())).thenReturn(expectedId);
            
            // When & Then
            mockMvc.perform(post("/api/equations/store")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedId));
            
            verify(equationService).storeEquation(request.getEquation());
        }
        
        @Test
        @DisplayName("Should return 400 Bad Request for blank equation")
        void shouldRejectBlankEquation() throws Exception {
            // Given
            StoreEquationRequest request = new StoreEquationRequest("");
            
            // When & Then
            mockMvc.perform(post("/api/equations/store")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.equation").value("Equation cannot be blank"));
            
            verifyNoInteractions(equationService);
        }
        
        @Test
        @DisplayName("Should return 400 Bad Request for null equation")
        void shouldRejectNullEquation() throws Exception {
            // Given
            StoreEquationRequest request = new StoreEquationRequest(null);
            
            // When & Then
            mockMvc.perform(post("/api/equations/store")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.equation").value("Equation cannot be blank"));
            
            verifyNoInteractions(equationService);
        }
        
        @Test
        @DisplayName("Should return 400 Bad Request for invalid syntax")
        void shouldHandleInvalidSyntax() throws Exception {
            // Given
            StoreEquationRequest request = new StoreEquationRequest("x + + 1");
            when(equationService.storeEquation(anyString()))
                .thenThrow(new EquationSyntaxException("Invalid syntax"));
            
            // When & Then
            mockMvc.perform(post("/api/equations/store")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid Equation Syntax"))
                .andExpect(jsonPath("$.message").value("Invalid syntax"));
        }
        
        @Test
        @DisplayName("Should handle complex mathematical expressions")
        void shouldStoreComplexExpression() throws Exception {
            // Given
            StoreEquationRequest request = new StoreEquationRequest("(a + b) * c^2 - d / (e + f)");
            Long expectedId = 5L;
            when(equationService.storeEquation(request.getEquation())).thenReturn(expectedId);
            
            // When & Then
            mockMvc.perform(post("/api/equations/store")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedId));
        }
    }
    
    @Nested
    @DisplayName("GET /api/equations")
    class GetAllEquationsTests {
        
        @Test
        @DisplayName("Should return empty list when no equations exist")
        void shouldReturnEmptyListWhenNoEquations() throws Exception {
            // Given
            when(equationService.getAllEquationSummaries()).thenReturn(List.of());
            
            // When & Then
            mockMvc.perform(get("/api/equations")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
            
            verify(equationService).getAllEquationSummaries();
        }
        
        @Test
        @DisplayName("Should return list of equation summaries")
        void shouldReturnEquationSummaries() throws Exception {
            // Given
            List<EquationSummaryDto> summaries = List.of(
                new EquationSummaryDto(1L, "x + 1"),
                new EquationSummaryDto(2L, "y^2"),
                new EquationSummaryDto(3L, "z * 3")
            );
            when(equationService.getAllEquationSummaries()).thenReturn(summaries);
            
            // When & Then
            mockMvc.perform(get("/api/equations")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].infix").value("x + 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].infix").value("y^2"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].infix").value("z * 3"));
            
            verify(equationService).getAllEquationSummaries();
        }
        
        @Test
        @DisplayName("Should handle service exceptions gracefully")
        void shouldHandleServiceExceptions() throws Exception {
            // Given
            when(equationService.getAllEquationSummaries())
                .thenThrow(new RuntimeException("Database error"));
            
            // When & Then
            mockMvc.perform(get("/api/equations")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
        }
    }
    
    @Nested
    @DisplayName("POST /api/equations/{id}/evaluate")
    class EvaluateEquationTests {
        
        @Test
        @DisplayName("Should evaluate equation successfully")
        void shouldEvaluateEquationSuccessfully() throws Exception {
            // Given
            Long equationId = 1L;
            Map<String, Double> variables = Map.of("x", 3.0, "y", 4.0);
            EvaluateEquationRequest request = new EvaluateEquationRequest(variables);
            double expectedResult = 25.0; // Example: x^2 + y^2 = 9 + 16 = 25
            
            when(equationService.evaluateEquation(equationId, variables)).thenReturn(expectedResult);
            
            // When & Then
            mockMvc.perform(post("/api/equations/{id}/evaluate", equationId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value(expectedResult));
            
            verify(equationService).evaluateEquation(equationId, variables);
        }
        
        @Test
        @DisplayName("Should return 400 Bad Request for empty variables map")
        void shouldRejectEmptyVariables() throws Exception {
            // Given
            Long equationId = 1L;
            EvaluateEquationRequest request = new EvaluateEquationRequest(Map.of());
            
            // When & Then
            mockMvc.perform(post("/api/equations/{id}/evaluate", equationId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.variables").value("Variables map cannot be empty"));
            
            verifyNoInteractions(equationService);
        }
        
        @Test
        @DisplayName("Should return 404 Not Found for non-existent equation")
        void shouldHandleNonExistentEquation() throws Exception {
            // Given
            Long nonExistentId = 999L;
            Map<String, Double> variables = Map.of("x", 1.0);
            EvaluateEquationRequest request = new EvaluateEquationRequest(variables);
            
            when(equationService.evaluateEquation(nonExistentId, variables))
                .thenThrow(new IllegalStateException("Equation with ID 999 not found"));
            
            // When & Then
            mockMvc.perform(post("/api/equations/{id}/evaluate", nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Equation Not Found"))
                .andExpect(jsonPath("$.message").value("Equation with ID 999 not found"));
        }
        
        @Test
        @DisplayName("Should return 400 Bad Request for missing variables")
        void shouldHandleMissingVariables() throws Exception {
            // Given
            Long equationId = 1L;
            Map<String, Double> incompleteVariables = Map.of("x", 1.0);
            EvaluateEquationRequest request = new EvaluateEquationRequest(incompleteVariables);
            
            when(equationService.evaluateEquation(equationId, incompleteVariables))
                .thenThrow(new VariableNotProvidedException("y"));
            
            // When & Then
            mockMvc.perform(post("/api/equations/{id}/evaluate", equationId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Missing Variable"))
                .andExpect(jsonPath("$.missingVariable").value("y"));
        }
        
        @Test
        @DisplayName("Should return 400 Bad Request for arithmetic errors")
        void shouldHandleArithmeticErrors() throws Exception {
            // Given
            Long equationId = 1L;
            Map<String, Double> variables = Map.of("x", 10.0, "y", 0.0);
            EvaluateEquationRequest request = new EvaluateEquationRequest(variables);
            
            when(equationService.evaluateEquation(equationId, variables))
                .thenThrow(new ArithmeticException("Division by zero"));
            
            // When & Then
            mockMvc.perform(post("/api/equations/{id}/evaluate", equationId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Arithmetic Error"))
                .andExpect(jsonPath("$.message").value("Division by zero"));
        }
        
        @Test
        @DisplayName("Should handle complex evaluation with multiple variables")
        void shouldEvaluateComplexExpression() throws Exception {
            // Given
            Long equationId = 2L;
            Map<String, Double> variables = Map.of(
                "a", 2.0,
                "b", 3.0, 
                "c", 4.0,
                "x", 5.0
            );
            EvaluateEquationRequest request = new EvaluateEquationRequest(variables);
            double expectedResult = 76.0; // Example complex calculation
            
            when(equationService.evaluateEquation(equationId, variables)).thenReturn(expectedResult);
            
            // When & Then
            mockMvc.perform(post("/api/equations/{id}/evaluate", equationId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(expectedResult));
        }
    }
    
    @Nested
    @DisplayName("Content Type and JSON Processing")
    class ContentTypeTests {
        
        @Test
        @DisplayName("Should require JSON content type for POST requests")
        void shouldRequireJsonContentType() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/equations/store")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("x + 1"))
                .andExpect(status().isUnsupportedMediaType());
        }
        
        @Test
        @DisplayName("Should handle malformed JSON gracefully")
        void shouldHandleMalformedJson() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/equations/store")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json"))
                .andExpect(status().isBadRequest());
        }
    }
} 