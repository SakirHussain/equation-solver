package com.example.equation.service;

import com.example.equation.exception.VariableNotProvidedException;
import com.example.equation.model.Node;
import com.example.equation.model.OperandNode;
import com.example.equation.model.OperatorNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for EvaluationService covering all evaluation scenarios,
 * error conditions, and edge cases including exponent precedence and negative numbers.
 */
class EvaluationServiceTest {
    
    private EvaluationService evaluationService;
    private ParserService parserService;
    
    @BeforeEach
    void setUp() {
        evaluationService = new EvaluationService();
        parserService = new ParserService();
    }
    
    @Nested
    @DisplayName("Basic Arithmetic Operations")
    class BasicArithmeticTests {
        
        @Test
        @DisplayName("Should evaluate simple addition")
        void shouldEvaluateAddition() {
            // Given: 3 + 2
            Node root = new OperatorNode('+', 
                new OperandNode("3"), 
                new OperandNode("2"));
            Map<String, Double> variables = new HashMap<>();
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(5.0);
        }
        
        @Test
        @DisplayName("Should evaluate simple subtraction")
        void shouldEvaluateSubtraction() {
            // Given: 10 - 3
            Node root = new OperatorNode('-', 
                new OperandNode("10"), 
                new OperandNode("3"));
            Map<String, Double> variables = new HashMap<>();
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(7.0);
        }
        
        @Test
        @DisplayName("Should evaluate simple multiplication")
        void shouldEvaluateMultiplication() {
            // Given: 4 * 5
            Node root = new OperatorNode('*', 
                new OperandNode("4"), 
                new OperandNode("5"));
            Map<String, Double> variables = new HashMap<>();
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(20.0);
        }
        
        @Test
        @DisplayName("Should evaluate simple division")
        void shouldEvaluateDivision() {
            // Given: 15 / 3
            Node root = new OperatorNode('/', 
                new OperandNode("15"), 
                new OperandNode("3"));
            Map<String, Double> variables = new HashMap<>();
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(5.0);
        }
        
        @Test
        @DisplayName("Should evaluate simple exponentiation")
        void shouldEvaluateExponentiation() {
            // Given: 2 ^ 3
            Node root = new OperatorNode('^', 
                new OperandNode("2"), 
                new OperandNode("3"));
            Map<String, Double> variables = new HashMap<>();
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(8.0);
        }
    }
    
    @Nested
    @DisplayName("Exponent Precedence Tests")
    class ExponentPrecedenceTests {
        
        @Test
        @DisplayName("Should handle exponent precedence correctly: 2^3^2 = 2^(3^2) = 2^9 = 512")
        void shouldHandleExponentPrecedence() {
            // Given: Expression "2^3^2" parsed with right associativity
            // This should be evaluated as 2^(3^2) = 2^9 = 512
            Node root = parserService.parseExpression("2^3^2");
            Map<String, Double> variables = new HashMap<>();
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(512.0);
        }
        
        @Test
        @DisplayName("Should handle mixed operations with exponent: 2*3^2 = 2*(3^2) = 18")
        void shouldHandleMixedOperationsWithExponent() {
            // Given: Expression "2*3^2"
            Node root = parserService.parseExpression("2*3^2");
            Map<String, Double> variables = new HashMap<>();
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(18.0);
        }
        
        @Test
        @DisplayName("Should handle parentheses with exponents: (2+3)^2 = 25")
        void shouldHandleParenthesesWithExponents() {
            // Given: Expression "(2+3)^2"
            Node root = parserService.parseExpression("(2+3)^2");
            Map<String, Double> variables = new HashMap<>();
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(25.0);
        }
        
        @Test
        @DisplayName("Should handle fractional exponents: 9^0.5 = 3")
        void shouldHandleFractionalExponents() {
            // Given: Expression "9^0.5"
            Node root = parserService.parseExpression("9^0.5");
            Map<String, Double> variables = new HashMap<>();
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(3.0);
        }
    }
    
    @Nested
    @DisplayName("Negative Numbers Tests")
    class NegativeNumberTests {
        
        @Test
        @DisplayName("Should handle negative numbers in subtraction: 5 - -3 = 8")
        void shouldHandleDoubleNegative() {
            // Given: Expression "5--3" (5 minus negative 3)
            // Note: This might need special tokenization handling
            Node root = new OperatorNode('-', 
                new OperandNode("5"), 
                new OperandNode("-3"));
            Map<String, Double> variables = new HashMap<>();
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(8.0);
        }
        
        @Test
        @DisplayName("Should handle negative numbers in multiplication: -2 * 3 = -6")
        void shouldHandleNegativeMultiplication() {
            // Given: -2 * 3
            Node root = new OperatorNode('*', 
                new OperandNode("-2"), 
                new OperandNode("3"));
            Map<String, Double> variables = new HashMap<>();
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(-6.0);
        }
        
        @Test
        @DisplayName("Should handle negative numbers in exponentiation: -2^2 = 4")
        void shouldHandleNegativeBaseExponentiation() {
            // Given: (-2)^2 = 4
            Node root = new OperatorNode('^', 
                new OperandNode("-2"), 
                new OperandNode("2"));
            Map<String, Double> variables = new HashMap<>();
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(4.0);
        }
        
        @Test
        @DisplayName("Should handle negative exponents: 2^-2 = 0.25")
        void shouldHandleNegativeExponent() {
            // Given: 2^(-2) = 0.25
            Node root = new OperatorNode('^', 
                new OperandNode("2"), 
                new OperandNode("-2"));
            Map<String, Double> variables = new HashMap<>();
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(0.25);
        }
    }
    
    @Nested
    @DisplayName("Variable Evaluation Tests")
    class VariableEvaluationTests {
        
        @Test
        @DisplayName("Should evaluate expression with single variable")
        void shouldEvaluateWithSingleVariable() {
            // Given: x + 5, where x = 3
            Node root = new OperatorNode('+', 
                new OperandNode("x"), 
                new OperandNode("5"));
            Map<String, Double> variables = Map.of("x", 3.0);
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(8.0);
        }
        
        @Test
        @DisplayName("Should evaluate expression with multiple variables")
        void shouldEvaluateWithMultipleVariables() {
            // Given: x * y + z, where x=2, y=3, z=4
            Node root = new OperatorNode('+',
                new OperatorNode('*', 
                    new OperandNode("x"), 
                    new OperandNode("y")),
                new OperandNode("z"));
            Map<String, Double> variables = Map.of("x", 2.0, "y", 3.0, "z", 4.0);
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(10.0); // 2*3+4 = 10
        }
        
        @Test
        @DisplayName("Should evaluate complex expression with variables and exponents")
        void shouldEvaluateComplexExpressionWithVariables() {
            // Given: x^2 + y*z, where x=3, y=2, z=4
            Node root = new OperatorNode('+',
                new OperatorNode('^', 
                    new OperandNode("x"), 
                    new OperandNode("2")),
                new OperatorNode('*', 
                    new OperandNode("y"), 
                    new OperandNode("z")));
            Map<String, Double> variables = Map.of("x", 3.0, "y", 2.0, "z", 4.0);
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(17.0); // 3^2 + 2*4 = 9 + 8 = 17
        }
    }
    
    @Nested
    @DisplayName("Missing Variables Error Tests")
    class MissingVariablesTests {
        
        @Test
        @DisplayName("Should throw VariableNotProvidedException for single missing variable")
        void shouldThrowExceptionForMissingVariable() {
            // Given: x + 5, but x is not provided
            Node root = new OperatorNode('+', 
                new OperandNode("x"), 
                new OperandNode("5"));
            Map<String, Double> variables = new HashMap<>();
            
            // When & Then
            assertThatThrownBy(() -> evaluationService.evaluate(root, variables))
                .isInstanceOf(VariableNotProvidedException.class)
                .hasMessage("Variable 'x' is not provided in the variable map")
                .satisfies(ex -> {
                    VariableNotProvidedException vnpe = (VariableNotProvidedException) ex;
                    assertThat(vnpe.getVariableName()).isEqualTo("x");
                });
        }
        
        @Test
        @DisplayName("Should throw VariableNotProvidedException for missing variable in complex expression")
        void shouldThrowExceptionForMissingVariableInComplexExpression() {
            // Given: x * y + z, where only x and y are provided
            Node root = new OperatorNode('+',
                new OperatorNode('*', 
                    new OperandNode("x"), 
                    new OperandNode("y")),
                new OperandNode("z"));
            Map<String, Double> variables = Map.of("x", 2.0, "y", 3.0);
            
            // When & Then
            assertThatThrownBy(() -> evaluationService.evaluate(root, variables))
                .isInstanceOf(VariableNotProvidedException.class)
                .hasMessage("Variable 'z' is not provided in the variable map")
                .satisfies(ex -> {
                    VariableNotProvidedException vnpe = (VariableNotProvidedException) ex;
                    assertThat(vnpe.getVariableName()).isEqualTo("z");
                });
        }
        
        @Test
        @DisplayName("Should throw VariableNotProvidedException for case-sensitive variable names")
        void shouldThrowExceptionForCaseSensitiveVariables() {
            // Given: X (uppercase) in expression, but x (lowercase) is provided
            Node root = new OperandNode("X");
            Map<String, Double> variables = Map.of("x", 5.0);
            
            // When & Then
            assertThatThrownBy(() -> evaluationService.evaluate(root, variables))
                .isInstanceOf(VariableNotProvidedException.class)
                .hasMessage("Variable 'X' is not provided in the variable map")
                .satisfies(ex -> {
                    VariableNotProvidedException vnpe = (VariableNotProvidedException) ex;
                    assertThat(vnpe.getVariableName()).isEqualTo("X");
                });
        }
    }
    
    @Nested
    @DisplayName("Arithmetic Error Tests")
    class ArithmeticErrorTests {
        
        @Test
        @DisplayName("Should throw ArithmeticException for division by zero with constants")
        void shouldThrowExceptionForDivisionByZero() {
            // Given: 5 / 0
            Node root = new OperatorNode('/', 
                new OperandNode("5"), 
                new OperandNode("0"));
            Map<String, Double> variables = new HashMap<>();
            
            // When & Then
            assertThatThrownBy(() -> evaluationService.evaluate(root, variables))
                .isInstanceOf(ArithmeticException.class)
                .hasMessage("Division by zero");
        }
        
        @Test
        @DisplayName("Should throw ArithmeticException for division by zero with variables")
        void shouldThrowExceptionForDivisionByZeroWithVariables() {
            // Given: x / y, where x=10, y=0
            Node root = new OperatorNode('/', 
                new OperandNode("x"), 
                new OperandNode("y"));
            Map<String, Double> variables = Map.of("x", 10.0, "y", 0.0);
            
            // When & Then
            assertThatThrownBy(() -> evaluationService.evaluate(root, variables))
                .isInstanceOf(ArithmeticException.class)
                .hasMessage("Division by zero");
        }
        
        @Test
        @DisplayName("Should throw ArithmeticException for division by zero in complex expression")
        void shouldThrowExceptionForDivisionByZeroInComplexExpression() {
            // Given: (x + y) / (z - 3), where x=2, y=3, z=3 (making denominator 0)
            Node root = new OperatorNode('/',
                new OperatorNode('+', 
                    new OperandNode("x"), 
                    new OperandNode("y")),
                new OperatorNode('-', 
                    new OperandNode("z"), 
                    new OperandNode("3")));
            Map<String, Double> variables = Map.of("x", 2.0, "y", 3.0, "z", 3.0);
            
            // When & Then
            assertThatThrownBy(() -> evaluationService.evaluate(root, variables))
                .isInstanceOf(ArithmeticException.class)
                .hasMessage("Division by zero");
        }
    }
    
    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {
        
        @Test
        @DisplayName("Should throw IllegalArgumentException for null root")
        void shouldThrowExceptionForNullRoot() {
            // Given
            Map<String, Double> variables = new HashMap<>();
            
            // When & Then
            assertThatThrownBy(() -> evaluationService.evaluate(null, variables))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expression tree root cannot be null");
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException for null variables map")
        void shouldThrowExceptionForNullVariablesMap() {
            // Given
            Node root = new OperandNode("5");
            
            // When & Then
            assertThatThrownBy(() -> evaluationService.evaluate(root, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Variables map cannot be null");
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException for unsupported operator")
        void shouldThrowExceptionForUnsupportedOperator() {
            // Given: Invalid operator '%'
            Node root = new OperatorNode('%', 
                new OperandNode("5"), 
                new OperandNode("3"));
            Map<String, Double> variables = new HashMap<>();
            
            // When & Then
            assertThatThrownBy(() -> evaluationService.evaluate(root, variables))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported operator: %");
        }
    }
    
    @Nested
    @DisplayName("Integration Tests with Parser")
    class IntegrationWithParserTests {
        
        @Test
        @DisplayName("Should evaluate parsed expression: (x+y)*z^2")
        void shouldEvaluateParsedExpression() {
            // Given: Expression "(x+y)*z^2" with x=1, y=2, z=3
            Node root = parserService.parseExpression("(x+y)*z^2");
            Map<String, Double> variables = Map.of("x", 1.0, "y", 2.0, "z", 3.0);
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isEqualTo(27.0); // (1+2)*3^2 = 3*9 = 27
        }
        
        @Test
        @DisplayName("Should evaluate parsed expression with decimals: 3.14*radius^2")
        void shouldEvaluateParsedExpressionWithDecimals() {
            // Given: Expression "3.14*radius^2" with radius=2
            Node root = parserService.parseExpression("3.14*radius^2");
            Map<String, Double> variables = Map.of("radius", 2.0);
            
            // When
            double result = evaluationService.evaluate(root, variables);
            
            // Then
            assertThat(result).isCloseTo(12.56, within(0.01)); // 3.14*2^2 = 3.14*4 = 12.56
        }
    }
} 