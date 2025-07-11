package com.example.equation;

import com.example.equation.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest(classes = com.example.equationservice.EquationServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ComprehensiveEquationTest {
    
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
    @DisplayName("1. Basic Arithmetic - Full Stack Validation")
    void basicArithmetic_FullStackValidation() {
        // Expression: x + y * 2 (tests operator precedence)
        String expression = "x + y * 2";
        
        StoreEquationRequest request = new StoreEquationRequest(expression);
        HttpEntity<StoreEquationRequest> storeEntity = new HttpEntity<>(request, createJsonHeaders());
        
        ResponseEntity<StoreEquationResponse> storeResponse = restTemplate.postForEntity(
            getBaseUrl() + "/store", storeEntity, StoreEquationResponse.class);
        
        // Validate HTTP layer
        assertThat(storeResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(storeResponse.getBody().getId()).isNotNull();
        Long equationId = storeResponse.getBody().getId();
        
        // Validate Service + Repository layer
        ResponseEntity<EquationSummaryDto[]> getAllResponse = restTemplate.getForEntity(
            getBaseUrl(), EquationSummaryDto[].class);
        assertThat(getAllResponse.getBody()).isNotEmpty();
        
        // Find our equation in the results
        EquationSummaryDto ourEquation = java.util.Arrays.stream(getAllResponse.getBody())
            .filter(eq -> eq.getId().equals(equationId))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Equation not found in repository"));
        assertThat(ourEquation.getInfix()).isEqualTo("x+y*2"); // Service trims whitespace
        
        // Validate Parser + AST + Evaluation
        EvaluateEquationRequest evalRequest = new EvaluateEquationRequest(Map.of("x", 10.0, "y", 3.0));
        HttpEntity<EvaluateEquationRequest> evalEntity = new HttpEntity<>(evalRequest, createJsonHeaders());
        
        ResponseEntity<EvaluateEquationResponse> evalResponse = restTemplate.postForEntity(
            getBaseUrl() + "/" + equationId + "/evaluate", evalEntity, EvaluateEquationResponse.class);
        
        assertThat(evalResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Expected: 10 + 3 * 2 = 10 + 6 = 16 (validates precedence)
        assertThat(evalResponse.getBody().getResult()).isEqualTo(16.0);
    }
    
    @Test
    @DisplayName("2. Operator Precedence - Different Structures")
    void operatorPrecedence_DifferentStructures() {
        // Test: a+b*c vs (a+b)*c should produce different results  
        String expr1 = "a + b * c";      // = a + (b*c)
        String expr2 = "(a + b) * c";    // = (a+b) * c
        
        // Store both expressions
        Long id1 = storeEquation(expr1);
        Long id2 = storeEquation(expr2);
        
        // Should be different equations (different AST structures)
        assertThat(id1).isNotEqualTo(id2);
        
        // Validate different evaluation results
        Map<String, Double> vars = Map.of("a", 2.0, "b", 3.0, "c", 4.0);
        assertThat(evaluateEquation(id1, vars)).isEqualTo(14.0); // 2 + (3*4) = 14
        assertThat(evaluateEquation(id2, vars)).isEqualTo(20.0); // (2+3) * 4 = 20
    }
    
    @Test
    @DisplayName("3. Whitespace Equivalence - AST Duplicate Detection")
    void whitespaceEquivalence_ASTDuplicateDetection() {
        // Test: Different whitespace should produce same AST hash
        String expr1 = "x+y*2";
        String expr2 = " x + y * 2 ";
        String expr3 = "x  +  y  *  2";
        
        // Store expressions with different whitespace
        Long id1 = storeEquation(expr1);
        Long id2 = storeEquation(expr2);
        Long id3 = storeEquation(expr3);
        
        // All should return same ID (duplicate detection via AST hash)
        assertThat(id1).isEqualTo(id2).isEqualTo(id3);
        
        // Verify our whitespace expressions all map to the same stored equation
        ResponseEntity<EquationSummaryDto[]> getAllResponse = restTemplate.getForEntity(
            getBaseUrl(), EquationSummaryDto[].class);
        long countOfOurExpression = java.util.Arrays.stream(getAllResponse.getBody())
            .filter(eq -> eq.getId().equals(id1))
            .count();
        assertThat(countOfOurExpression).isEqualTo(1); // Only one instance of our expression
        
        // Should evaluate to same result
        Map<String, Double> vars = Map.of("x", 5.0, "y", 3.0);
        assertThat(evaluateEquation(id1, vars)).isEqualTo(11.0); // 5 + 3*2 = 11
    }
    
    @Test
    @DisplayName("4. Parentheses Structure - Redundant Parentheses Detection")
    void parenthesesStructure_RedundantParenthesesDetection() {
        // Test: Redundant parentheses should be detected as equivalent
        String expr1 = "a/d*c";           // Following operator precedence
        String expr2 = "(a/d)*c";         // Explicit parentheses
        String expr3 = "((a/d)*c)";       // Extra redundant parentheses
        
        Long id1 = storeEquation(expr1);
        Long id2 = storeEquation(expr2);
        Long id3 = storeEquation(expr3);
        
        // All should be detected as mathematically equivalent
        assertThat(id1).isEqualTo(id2).isEqualTo(id3);
        
        // Validate mathematical correctness
        Map<String, Double> vars = Map.of("a", 12.0, "d", 3.0, "c", 4.0);
        assertThat(evaluateEquation(id1, vars)).isEqualTo(16.0); // (12/3)*4 = 4*4 = 16
    }
    
    @Test
    @DisplayName("5. Complex Expression - Circle Area Formula")
    void complexExpression_CircleAreaFormula() {
        // Test: Complex mathematical formula with decimals and exponentiation
        String expression = "3.14159 * radius^2";
        Long equationId = storeEquation(expression);
        
        // Test with real-world values
        Map<String, Double> vars = Map.of("radius", 5.0);
        double result = evaluateEquation(equationId, vars);
        
        // Expected: π * 5² = 3.14159 * 25 = 78.53975
        assertThat(result).isCloseTo(78.53975, within(0.00001));
        
        // Verify expression stored correctly
        EquationSummaryDto[] equations = getAllEquations();
        EquationSummaryDto ourEquation = java.util.Arrays.stream(equations)
            .filter(eq -> eq.getId().equals(equationId))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Circle area equation not found"));
        assertThat(ourEquation.getInfix()).isEqualTo(expression);
    }
    
    @Test
    @DisplayName("6. Quadratic Formula - Multi-Variable Complex Expression")
    void quadraticFormula_MultiVariableComplexExpression() {
        // Test: ax² + bx + c
        String expression = "a*x^2 + b*x + c";
        Long equationId = storeEquation(expression);
        
        // Test with quadratic: 2x² + 3x + 1, where x = 4
        Map<String, Double> vars = Map.of("a", 2.0, "b", 3.0, "c", 1.0, "x", 4.0);
        double result = evaluateEquation(equationId, vars);
        
        // Expected: 2*16 + 3*4 + 1 = 32 + 12 + 1 = 45
        assertThat(result).isEqualTo(45.0);
    }
    
    @Test
    @DisplayName("7. Financial Formula - Compound Interest")
    void financialFormula_CompoundInterest() {
        // Test: P * (1 + r/n)^(n*t) - compound interest formula
        String expression = "P * (1 + r/n)^(n*t)";
        Long equationId = storeEquation(expression);
        
        // $1000, 5% annual rate, compounded quarterly, for 3 years
        Map<String, Double> vars = Map.of(
            "P", 1000.0,    // Principal
            "r", 0.05,      // Annual interest rate
            "n", 4.0,       // Quarterly compounding
            "t", 3.0        // 3 years
        );
        
        double amount = evaluateEquation(equationId, vars);
        // Expected: 1000 * (1.0125)^12 ≈ 1160.755
        assertThat(amount).isCloseTo(1160.755, within(0.001));
    }
    
    @Test
    @DisplayName("8. Variable Distinction - Different Variables Are Separate")
    void variableDistinction_DifferentVariablesAreSeparate() {
        // Test: Same structure, different variables should be separate equations
        String expr1 = "a + b";
        String expr2 = "x + y";
        
        Long id1 = storeEquation(expr1);
        Long id2 = storeEquation(expr2);
        
        // Should create separate equations (different AST hashes)
        assertThat(id1).isNotEqualTo(id2);
        
        // Both should evaluate correctly with their respective variables
        assertThat(evaluateEquation(id1, Map.of("a", 5.0, "b", 3.0))).isEqualTo(8.0);
        assertThat(evaluateEquation(id2, Map.of("x", 10.0, "y", 7.0))).isEqualTo(17.0);
        
        // Verify two equations stored
        assertThat(getAllEquations()).hasSize(2);
    }
    
    // =================== ERROR HANDLING END-TO-END ===================
    
    @Test
    @DisplayName("9. Parser Syntax Error - Invalid Expression Handling")
    void parserSyntaxError_InvalidExpressionHandling() {
        // Test: Invalid syntax should be caught by parser and returned as HTTP error
        StoreEquationRequest request = new StoreEquationRequest("x + + y");
        HttpEntity<StoreEquationRequest> entity = new HttpEntity<>(request, createJsonHeaders());
        
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
            getBaseUrl() + "/store", entity, JsonNode.class);
        
        // Validate error propagation through entire stack
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error").asText()).isEqualTo("Invalid Equation Syntax");
        
        // Test another invalid syntax
        StoreEquationRequest invalidRequest = new StoreEquationRequest("3 * @ 2");
        HttpEntity<StoreEquationRequest> invalidEntity = new HttpEntity<>(invalidRequest, createJsonHeaders());
        
        ResponseEntity<JsonNode> invalidResponse = restTemplate.postForEntity(
            getBaseUrl() + "/store", invalidEntity, JsonNode.class);
        
        assertThat(invalidResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    
    @Test
    @DisplayName("10. Missing Variables - Variable Validation")
    void missingVariables_VariableValidation() {
        // Test: Valid expression, but missing variables during evaluation
        String expression = "x + y + z";
        Long equationId = storeEquation(expression);
        
        // Try to evaluate with missing variable 'z'
        EvaluateEquationRequest evalRequest = new EvaluateEquationRequest(Map.of("x", 1.0, "y", 2.0));
        HttpEntity<EvaluateEquationRequest> evalEntity = new HttpEntity<>(evalRequest, createJsonHeaders());
        
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
            getBaseUrl() + "/" + equationId + "/evaluate", evalEntity, JsonNode.class);
        
        // Validate error handling through service → HTTP layers
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error").asText()).isEqualTo("Missing Variable");
        assertThat(response.getBody().get("missingVariable").asText()).isEqualTo("z");
    }
    
    @Test
    @DisplayName("11. Division by Zero - Arithmetic Exception Handling")
    void divisionByZero_ArithmeticExceptionHandling() {
        // Test: Valid expression that causes division by zero
        String expression = "x / y";
        Long equationId = storeEquation(expression);
        
        // Evaluate with zero divisor
        Map<String, Double> vars = Map.of("x", 10.0, "y", 0.0);
        EvaluateEquationRequest evalRequest = new EvaluateEquationRequest(vars);
        HttpEntity<EvaluateEquationRequest> evalEntity = new HttpEntity<>(evalRequest, createJsonHeaders());
        
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
            getBaseUrl() + "/" + equationId + "/evaluate", evalEntity, JsonNode.class);
        
        // Validate arithmetic exception handling
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error").asText()).isEqualTo("Arithmetic Error");
    }
    
    @Test
    @DisplayName("12. Non-existent Equation - Repository Lookup Error")
    void nonExistentEquation_RepositoryLookupError() {
        // Test: Attempt to evaluate equation that doesn't exist
        EvaluateEquationRequest request = new EvaluateEquationRequest(Map.of("x", 1.0));
        HttpEntity<EvaluateEquationRequest> entity = new HttpEntity<>(request, createJsonHeaders());
        
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
            getBaseUrl() + "/999/evaluate", entity, JsonNode.class);
        
        // Validate repository → service → HTTP error propagation
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("error").asText()).isEqualTo("Equation Not Found");
    }
    
    // =================== EDGE CASES & VALIDATION ===================
    
    @Test
    @DisplayName("13. Empty/Null Input - Input Validation")
    void emptyNullInput_InputValidation() {
        // Test empty equation
        StoreEquationRequest emptyRequest = new StoreEquationRequest("");
        HttpEntity<StoreEquationRequest> emptyEntity = new HttpEntity<>(emptyRequest, createJsonHeaders());
        
        ResponseEntity<JsonNode> emptyResponse = restTemplate.postForEntity(
            getBaseUrl() + "/store", emptyEntity, JsonNode.class);
        
        assertThat(emptyResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(emptyResponse.getBody().get("error").asText()).isEqualTo("Validation Failed");
        
        // Test whitespace-only equation
        StoreEquationRequest whitespaceRequest = new StoreEquationRequest("   ");
        HttpEntity<StoreEquationRequest> whitespaceEntity = new HttpEntity<>(whitespaceRequest, createJsonHeaders());
        
        ResponseEntity<JsonNode> whitespaceResponse = restTemplate.postForEntity(
            getBaseUrl() + "/store", whitespaceEntity, JsonNode.class);
        
        assertThat(whitespaceResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    
    @Test
    @DisplayName("14. Malformed JSON - HTTP Content Validation")
    void malformedJSON_HTTPContentValidation() {
        // Test malformed JSON request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{invalid json", headers);
        
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
            getBaseUrl() + "/store", entity, JsonNode.class);
        
        // Should handle malformed JSON gracefully
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        // Test wrong content type
        HttpHeaders textHeaders = new HttpHeaders();
        textHeaders.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> textEntity = new HttpEntity<>("some text", textHeaders);
        
        ResponseEntity<JsonNode> textResponse = restTemplate.postForEntity(
            getBaseUrl() + "/store", textEntity, JsonNode.class);
        
        assertThat(textResponse.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
    
    @Test
    @DisplayName("15. Application Context - Spring Boot Integration")
    void applicationContext_SpringBootIntegration() {
        // Test: Full application context loads and all components work together
        
        // Store multiple different types of equations
        Long basicId = storeEquation("x + 1");
        Long complexId = storeEquation("s * t^2"); // Simple variable names
        Long formulaId = storeEquation("(a + b) * c / d");
        
        // Verify all stored successfully
        assertThat(basicId).isNotNull();
        assertThat(complexId).isNotNull();
        assertThat(formulaId).isNotNull();
        assertThat(getAllEquations().length).isGreaterThanOrEqualTo(3); // At least these 3
        
        // Verify all can be evaluated
        assertThat(evaluateEquation(basicId, Map.of("x", 5.0))).isEqualTo(6.0);
        assertThat(evaluateEquation(complexId, Map.of("s", 0.5, "t", 2.0))).isEqualTo(2.0);
        assertThat(evaluateEquation(formulaId, Map.of("a", 10.0, "b", 5.0, "c", 4.0, "d", 2.0))).isEqualTo(30.0);
        
        // Test health check endpoint
        ResponseEntity<String> healthResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/health", String.class);
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    
    // =================== HELPER METHODS ===================
    
    private Long storeEquation(String expression) {
        StoreEquationRequest request = new StoreEquationRequest(expression);
        HttpEntity<StoreEquationRequest> entity = new HttpEntity<>(request, createJsonHeaders());
        
        ResponseEntity<StoreEquationResponse> response = restTemplate.postForEntity(
            getBaseUrl() + "/store", entity, StoreEquationResponse.class);
        
        assertThat(response.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
        return response.getBody().getId();
    }
    
    private double evaluateEquation(Long equationId, Map<String, Double> variables) {
        EvaluateEquationRequest request = new EvaluateEquationRequest(variables);
        HttpEntity<EvaluateEquationRequest> entity = new HttpEntity<>(request, createJsonHeaders());
        
        ResponseEntity<EvaluateEquationResponse> response = restTemplate.postForEntity(
            getBaseUrl() + "/" + equationId + "/evaluate", entity, EvaluateEquationResponse.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody().getResult();
    }
    
    private EquationSummaryDto[] getAllEquations() {
        ResponseEntity<EquationSummaryDto[]> response = restTemplate.getForEntity(
            getBaseUrl(), EquationSummaryDto[].class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }
} 