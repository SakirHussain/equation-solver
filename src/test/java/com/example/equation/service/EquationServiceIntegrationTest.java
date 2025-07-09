package com.example.equation.service;

import com.example.equation.dto.EquationDto;
import com.example.equation.exception.VariableNotProvidedException;
import com.example.equation.repository.InMemoryEquationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for EquationService using real components (no mocks).
 * Demonstrates the complete workflow from storing to evaluating equations.
 */
class EquationServiceIntegrationTest {
    
    private EquationService equationService;
    private InMemoryEquationRepository repository;
    
    @BeforeEach
    void setUp() {
        // Use real components - no mocking
        repository = new InMemoryEquationRepository();
        ParserService parserService = new ParserService();
        EvaluationService evaluationService = new EvaluationService();
        
        equationService = new EquationService(repository, parserService, evaluationService);
    }
    
    @Test
    @DisplayName("Complete workflow: Store multiple equations, retrieve all, and evaluate them")
    void shouldHandleCompleteWorkflowWithRealComponents() {
        // Store several equations
        Long circleId = equationService.storeEquation("3.14159 * radius^2");
        Long quadraticId = equationService.storeEquation("a*x^2 + b*x + c");
        Long simpleId = equationService.storeEquation("x + y * 2");
        
        // Verify IDs are generated sequentially
        assertThat(circleId).isEqualTo(1L);
        assertThat(quadraticId).isEqualTo(2L);
        assertThat(simpleId).isEqualTo(3L);
        
        // Retrieve all equations
        List<EquationDto> allEquations = equationService.getAllEquations();
        assertThat(allEquations).hasSize(3);
        
        // Verify equation details
        EquationDto circleDto = allEquations.stream()
            .filter(dto -> dto.getId().equals(circleId))
            .findFirst()
            .orElseThrow();
        
        assertThat(circleDto.getInfix()).isEqualTo("3.14159 * radius^2");
        assertThat(circleDto.getPostfix()).isNotEmpty(); // Should contain postfix tokens
        
        // Evaluate circle area equation
        Map<String, Double> circleVars = Map.of("radius", 5.0);
        double circleArea = equationService.evaluateEquation(circleId, circleVars);
        
        // Expected: 3.14159 * 5^2 = 3.14159 * 25 = 78.53975
        assertThat(circleArea).isCloseTo(78.53975, within(0.00001));
        
        // Evaluate quadratic equation: 2x^2 + 3x + 1 with x=4
        Map<String, Double> quadraticVars = Map.of("a", 2.0, "b", 3.0, "c", 1.0, "x", 4.0);
        double quadraticResult = equationService.evaluateEquation(quadraticId, quadraticVars);
        
        // Expected: 2*4^2 + 3*4 + 1 = 2*16 + 12 + 1 = 32 + 12 + 1 = 45
        assertThat(quadraticResult).isEqualTo(45.0);
        
        // Evaluate simple equation
        Map<String, Double> simpleVars = Map.of("x", 10.0, "y", 3.0);
        double simpleResult = equationService.evaluateEquation(simpleId, simpleVars);
        
        // Expected: 10 + 3 * 2 = 10 + 6 = 16
        assertThat(simpleResult).isEqualTo(16.0);
    }
    
    @Test
    @DisplayName("Should handle complex mathematical expressions")
    void shouldHandleComplexMathematicalExpressions() {
        // Store compound interest formula: P * (1 + r/n)^(n*t)
        Long compoundId = equationService.storeEquation("P * (1 + r/n)^(n*t)");
        
        // Calculate compound interest: $1000, 5% annual rate, compounded quarterly, for 3 years
        Map<String, Double> vars = Map.of(
            "P", 1000.0,    // Principal
            "r", 0.05,      // Annual interest rate (5%)
            "n", 4.0,       // Compounding frequency (quarterly)
            "t", 3.0        // Time in years
        );
        
        double amount = equationService.evaluateEquation(compoundId, vars);
        
        // Expected: 1000 * (1 + 0.05/4)^(4*3) = 1000 * (1.0125)^12 ≈ 1160.755
        assertThat(amount).isCloseTo(1160.755, within(0.001));
    }
    
    @Test
    @DisplayName("Should handle negative numbers and operator precedence correctly")
    void shouldHandleNegativeNumbersAndPrecedence() {
        // Store expression with negative numbers - use parentheses for clarity
        Long complexId = equationService.storeEquation("0 - x^2 + y * (0 - 3) / z");
        
        Map<String, Double> vars = Map.of(
            "x", 4.0,
            "y", 6.0,
            "z", 2.0
        );
        
        double result = equationService.evaluateEquation(complexId, vars);
        
        // Expected: 0 - 4^2 + 6 * (0 - 3) / 2 = -16 + 6 * (-3) / 2 = -16 + (-18) / 2 = -16 + (-9) = -25
        assertThat(result).isEqualTo(-25.0);
    }
    
    @Test
    @DisplayName("Should handle parentheses and complex precedence")
    void shouldHandleParenthesesAndComplexPrecedence() {
        // Store expression testing operator precedence with parentheses
        Long precedenceId = equationService.storeEquation("(x + y) * z^2 - w / (a + b)");
        
        Map<String, Double> vars = Map.of(
            "x", 2.0,
            "y", 3.0,
            "z", 4.0,
            "w", 20.0,
            "a", 1.0,
            "b", 4.0
        );
        
        double result = equationService.evaluateEquation(precedenceId, vars);
        
        // Expected: (2+3) * 4^2 - 20/(1+4) = 5 * 16 - 20/5 = 80 - 4 = 76
        assertThat(result).isEqualTo(76.0);
    }
    
    @Test
    @DisplayName("Should handle error scenarios appropriately")
    void shouldHandleErrorScenarios() {
        // Store equation requiring variables
        Long equationId = equationService.storeEquation("x + y + z");
        
        // Test missing variable
        Map<String, Double> incompleteVars = Map.of("x", 1.0, "y", 2.0);
        
        assertThatThrownBy(() -> equationService.evaluateEquation(equationId, incompleteVars))
            .isInstanceOf(VariableNotProvidedException.class)
            .hasMessage("Variable 'z' is not provided in the variable map");
        
        // Test division by zero
        Long divisionId = equationService.storeEquation("x / y");
        Map<String, Double> zeroVars = Map.of("x", 10.0, "y", 0.0);
        
        assertThatThrownBy(() -> equationService.evaluateEquation(divisionId, zeroVars))
            .isInstanceOf(ArithmeticException.class)
            .hasMessageContaining("Division by zero");
        
        // Test non-existent equation
        assertThatThrownBy(() -> equationService.evaluateEquation(999L, Map.of("x", 1.0)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Equation with ID 999 not found");
    }
    
    @Test
    @DisplayName("Should demonstrate repository persistence")
    void shouldDemonstrateRepositoryPersistence() {
        // Store equations
        equationService.storeEquation("x^2");
        equationService.storeEquation("y^3");
        equationService.storeEquation("z^4");
        
        // Verify they persist in repository
        assertThat(repository.size()).isEqualTo(3);
        
        // Retrieve and verify
        List<EquationDto> equations = equationService.getAllEquations();
        assertThat(equations).hasSize(3);
        
        // Verify each equation can be evaluated
        Map<String, Double> testVars = Map.of("x", 2.0, "y", 3.0, "z", 2.0);
        
        for (EquationDto equation : equations) {
            double result = equationService.evaluateEquation(equation.getId(), testVars);
            assertThat(result).isPositive(); // All results should be positive with these inputs
        }
    }
    
    @Test
    @DisplayName("Should handle whitespace trimming properly")
    void shouldHandleWhitespaceTrimming() {
        // Store equation with various whitespace
        Long id1 = equationService.storeEquation("  x + y  ");
        Long id2 = equationService.storeEquation("\t x * y \n");
        Long id3 = equationService.storeEquation("   x^y   ");
        
        // Retrieve and verify trimmed expressions
        List<EquationDto> equations = equationService.getAllEquations();
        
        EquationDto eq1 = equations.stream().filter(e -> e.getId().equals(id1)).findFirst().orElseThrow();
        EquationDto eq2 = equations.stream().filter(e -> e.getId().equals(id2)).findFirst().orElseThrow();
        EquationDto eq3 = equations.stream().filter(e -> e.getId().equals(id3)).findFirst().orElseThrow();
        
        assertThat(eq1.getInfix()).isEqualTo("x + y");
        assertThat(eq2.getInfix()).isEqualTo("x * y");
        assertThat(eq3.getInfix()).isEqualTo("x^y");
        
        // Verify all can be evaluated
        Map<String, Double> vars = Map.of("x", 3.0, "y", 2.0);
        assertThat(equationService.evaluateEquation(id1, vars)).isEqualTo(5.0);  // 3 + 2
        assertThat(equationService.evaluateEquation(id2, vars)).isEqualTo(6.0);  // 3 * 2
        assertThat(equationService.evaluateEquation(id3, vars)).isEqualTo(9.0);  // 3^2
    }
} 