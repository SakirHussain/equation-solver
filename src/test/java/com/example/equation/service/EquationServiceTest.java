package com.example.equation.service;

import com.example.equation.dto.EquationDto;
import com.example.equation.exception.EquationSyntaxException;
import com.example.equation.exception.VariableNotProvidedException;
import com.example.equation.model.EquationEntity;
import com.example.equation.model.OperandNode;
import com.example.equation.model.Token;
import com.example.equation.model.TokenValue;
import com.example.equation.repository.EquationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for EquationService covering all business logic,
 * integration between components, and error handling scenarios.
 */
class EquationServiceTest {
    
    @Mock
    private EquationRepository equationRepository;
    
    @Mock
    private ParserService parserService;
    
    @Mock
    private EvaluationService evaluationService;
    
    private EquationService equationService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        equationService = new EquationService(equationRepository, parserService, evaluationService);
    }
    
    @Nested
    @DisplayName("Store Equation Tests")
    class StoreEquationTests {
        
        @Test
        @DisplayName("Should store valid equation and return ID")
        void shouldStoreValidEquationAndReturnId() {
            // Given
            String infix = "x + 2 * 3";
            Long expectedId = 1L;
            
            // Mock AST parsing for hash generation
            when(parserService.parseExpression(infix)).thenReturn(new OperandNode("test"));
            
            // Mock duplicate check - no existing equation
            when(equationRepository.findByAstHash(any())).thenReturn(Optional.empty());
            
            // Mock parser service calls
            when(parserService.tokenize(infix)).thenReturn(List.of(
                // Mock TokenValue objects would be returned here
            ));
            when(parserService.infixToPostfix(any())).thenReturn(List.of(
                // Mock postfix TokenValue objects
            ));
            when(parserService.buildExpressionTree(any())).thenReturn(new OperandNode("test"));
            
            // Mock repository save
            EquationEntity savedEntity = new EquationEntity(expectedId, infix, List.of(), "test");
            when(equationRepository.save(any(EquationEntity.class))).thenReturn(savedEntity);
            
            // When
            Long actualId = equationService.storeEquation(infix);
            
            // Then
            assertThat(actualId).isEqualTo(expectedId);
            
            // Verify interactions
            verify(parserService).parseExpression(infix); // AST parsing for hash
            verify(equationRepository).findByAstHash("test"); // Duplicate check using hash
            verify(parserService).tokenize(infix);
            verify(parserService).infixToPostfix(any());
            verify(equationRepository).save(argThat(entity -> 
                entity.getInfix().equals(infix) && entity.getId() == null));
        }
        
        @Test
        @DisplayName("Should trim whitespace from infix expression")
        void shouldTrimWhitespaceFromInfixExpression() {
            // Given
            String infixWithWhitespace = "  x + 1  ";
            String trimmedInfix = "x + 1";
            Long expectedId = 1L;
            
            when(parserService.parseExpression(trimmedInfix)).thenReturn(new OperandNode("test"));
            when(equationRepository.findByAstHash("test")).thenReturn(Optional.empty()); // No duplicates
            when(parserService.tokenize(trimmedInfix)).thenReturn(List.of());
            when(parserService.infixToPostfix(any())).thenReturn(List.of());
            when(parserService.buildExpressionTree(any())).thenReturn(new OperandNode("test"));
            
            EquationEntity savedEntity = new EquationEntity(expectedId, trimmedInfix, List.of(), "test");
            when(equationRepository.save(any())).thenReturn(savedEntity);
            
            // When
            Long actualId = equationService.storeEquation(infixWithWhitespace);
            
            // Then
            assertThat(actualId).isEqualTo(expectedId);
            verify(equationRepository).findByAstHash("test"); // Duplicate check using hash
            verify(parserService).tokenize(trimmedInfix);
            verify(parserService).infixToPostfix(any());
            verify(equationRepository).save(argThat(entity -> 
                entity.getInfix().equals(trimmedInfix)));
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException for null infix")
        void shouldThrowExceptionForNullInfix() {
            // When & Then
            assertThatThrownBy(() -> equationService.storeEquation(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Infix expression cannot be null or empty");
            
            // Verify no interactions with dependencies
            verifyNoInteractions(parserService, equationRepository);
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException for empty infix")
        void shouldThrowExceptionForEmptyInfix() {
            // When & Then
            assertThatThrownBy(() -> equationService.storeEquation("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Infix expression cannot be null or empty");
            
            verifyNoInteractions(parserService, equationRepository);
        }
        
        @Test
        @DisplayName("Should propagate EquationSyntaxException from parser")
        void shouldPropagateEquationSyntaxException() {
            // Given
            String invalidInfix = "x + + 1";
            when(parserService.parseExpression(invalidInfix))
                .thenThrow(new EquationSyntaxException("Invalid syntax"));
            
            // When & Then
            assertThatThrownBy(() -> equationService.storeEquation(invalidInfix))
                .isInstanceOf(EquationSyntaxException.class)
                .hasMessage("Invalid syntax");
            
            verify(parserService).parseExpression(invalidInfix); // Called for AST generation
            verify(equationRepository, never()).save(any());
            verify(equationRepository, never()).findByAstHash(any());
        }
        
        @Test
        @DisplayName("Should return existing ID for duplicate equations")
        void shouldReturnExistingIdForDuplicateEquations() {
            // Given
            String equation = "x + 1";
            EquationEntity existingEntity = new EquationEntity(42L, equation, List.of(Token.VARIABLE, Token.OPERATOR, Token.NUMBER), "test-hash");
            
            // Mock AST generation for duplicate check
            when(parserService.parseExpression(equation)).thenReturn(new OperandNode("test"));
            when(equationRepository.findByAstHash("test")).thenReturn(Optional.of(existingEntity));
            
            // When
            Long resultId = equationService.storeEquation(equation);
            
            // Then
            assertThat(resultId).isEqualTo(42L);
            
            // Verify that save was never called (no new entity created)
            verify(equationRepository, never()).save(any(EquationEntity.class));
            verify(equationRepository).findByAstHash("test"); // Called to check for duplicates using hash
            verify(parserService).parseExpression(equation); // Called to generate AST hash
        }
        
        @Test
        @DisplayName("Should store new equation when no duplicate exists")
        void shouldStoreNewEquationWhenNoDuplicateExists() {
            // Given
            String equation = "y * 2";
            EquationEntity savedEntity = new EquationEntity(1L, equation, List.of(Token.VARIABLE, Token.OPERATOR, Token.NUMBER), "test");
            
            // Mock AST generation for hash
            when(parserService.parseExpression(equation)).thenReturn(new OperandNode("test"));
            when(equationRepository.findByAstHash("test")).thenReturn(Optional.empty()); // No existing equations
            when(parserService.tokenize(equation)).thenReturn(List.of(
                new TokenValue(Token.VARIABLE, "y"),
                new TokenValue(Token.OPERATOR, "*"),
                new TokenValue(Token.NUMBER, "2")
            ));
            when(parserService.infixToPostfix(any())).thenReturn(List.of(
                new TokenValue(Token.VARIABLE, "y"),
                new TokenValue(Token.NUMBER, "2"),
                new TokenValue(Token.OPERATOR, "*")
            ));
            when(parserService.buildExpressionTree(any())).thenReturn(new OperandNode("test"));
            when(equationRepository.save(any(EquationEntity.class))).thenReturn(savedEntity);
            
            // When
            Long resultId = equationService.storeEquation(equation);
            
            // Then
            assertThat(resultId).isEqualTo(1L);
            verify(equationRepository).save(any(EquationEntity.class));
            verify(equationRepository).findByAstHash("test"); // Called to check for duplicates using hash
            verify(parserService).parseExpression(equation); // Called for AST hash generation
            verify(parserService).tokenize(equation);
            verify(parserService).infixToPostfix(any());
        }
        
        @Test
        @DisplayName("Should handle whitespace differences when checking duplicates")
        void shouldHandleWhitespaceWhenCheckingDuplicates() {
            // Given - existing equation without extra spaces
            String existingEquation = "x + 1";
            String newEquationWithSpaces = "  x + 1  "; // Same equation but with extra whitespace
            String trimmedEquation = "x + 1";
            EquationEntity existingEntity = new EquationEntity(5L, existingEquation, List.of(), "test");
            
            // Mock AST generation - both expressions produce same hash
            when(parserService.parseExpression(trimmedEquation)).thenReturn(new OperandNode("test"));
            when(equationRepository.findByAstHash("test")).thenReturn(Optional.of(existingEntity));
            
            // When
            Long resultId = equationService.storeEquation(newEquationWithSpaces);
            
            // Then
            assertThat(resultId).isEqualTo(5L); // Should return existing ID
            verify(equationRepository, never()).save(any()); // No new entity saved
            verify(parserService).parseExpression(trimmedEquation); // Parser called for AST hash
            verify(equationRepository).findByAstHash("test"); // Duplicate check using hash
        }
    }
    
    @Nested
    @DisplayName("Get All Equations Tests")
    class GetAllEquationsTests {
        
        @Test
        @DisplayName("Should return empty list when no equations exist")
        void shouldReturnEmptyListWhenNoEquations() {
            // Given
            when(equationRepository.findAll()).thenReturn(List.of());
            
            // When
            List<EquationDto> result = equationService.getAllEquations();
            
            // Then
            assertThat(result).isEmpty();
            verify(equationRepository).findAll();
        }
        
        @Test
        @DisplayName("Should convert all entities to DTOs")
        void shouldConvertAllEntitiesToDtos() {
            // Given
            List<EquationEntity> entities = List.of(
                new EquationEntity(1L, "x+1", List.of(Token.VARIABLE, Token.OPERATOR, Token.NUMBER), "hash1"),
                new EquationEntity(2L, "y*2", List.of(Token.VARIABLE, Token.OPERATOR, Token.NUMBER), "hash2"),
                new EquationEntity(3L, "z^3", List.of(Token.VARIABLE, Token.OPERATOR, Token.NUMBER), "hash3")
            );
            when(equationRepository.findAll()).thenReturn(entities);
            
            // When
            List<EquationDto> result = equationService.getAllEquations();
            
            // Then
            assertThat(result).hasSize(3);
            
            // Verify first DTO
            EquationDto dto1 = result.get(0);
            assertThat(dto1.getId()).isEqualTo(1L);
            assertThat(dto1.getInfix()).isEqualTo("x+1");
            assertThat(dto1.getPostfix()).containsExactly(Token.VARIABLE, Token.OPERATOR, Token.NUMBER);
            
            // Verify second DTO
            EquationDto dto2 = result.get(1);
            assertThat(dto2.getId()).isEqualTo(2L);
            assertThat(dto2.getInfix()).isEqualTo("y*2");
            
            // Verify third DTO
            EquationDto dto3 = result.get(2);
            assertThat(dto3.getId()).isEqualTo(3L);
            assertThat(dto3.getInfix()).isEqualTo("z^3");
            
            verify(equationRepository).findAll();
        }
        
        @Test
        @DisplayName("Should handle repository exceptions gracefully")
        void shouldHandleRepositoryExceptions() {
            // Given
            when(equationRepository.findAll()).thenThrow(new RuntimeException("Database error"));
            
            // When & Then
            assertThatThrownBy(() -> equationService.getAllEquations())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
        }
    }
    
    @Nested
    @DisplayName("Evaluate Equation Tests")
    class EvaluateEquationTests {
        
        @Test
        @DisplayName("Should evaluate equation successfully")
        void shouldEvaluateEquationSuccessfully() {
            // Given
            Long equationId = 1L;
            String infix = "x^2 + y*3";
            Map<String, Double> variables = Map.of("x", 2.0, "y", 4.0);
            double expectedResult = 16.0; // 2^2 + 4*3 = 4 + 12 = 16
            
            // Mock repository
            EquationEntity equation = new EquationEntity(equationId, infix, List.of(), "test-hash");
            when(equationRepository.findById(equationId)).thenReturn(Optional.of(equation));
            
            // Mock parser - use concrete OperandNode instead of mocking sealed interface
            com.example.equation.model.Node mockNode = new com.example.equation.model.OperandNode("5");
            when(parserService.parseExpression(infix)).thenReturn(mockNode);
            
            // Mock evaluator
            when(evaluationService.evaluate(any(), eq(variables))).thenReturn(expectedResult);
            
            // When
            double result = equationService.evaluateEquation(equationId, variables);
            
            // Then
            assertThat(result).isEqualTo(expectedResult);
            
            verify(equationRepository).findById(equationId);
            verify(parserService).parseExpression(infix);
            verify(evaluationService).evaluate(any(), eq(variables));
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException for null ID")
        void shouldThrowExceptionForNullId() {
            // Given
            Map<String, Double> variables = Map.of("x", 1.0);
            
            // When & Then
            assertThatThrownBy(() -> equationService.evaluateEquation(null, variables))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Equation ID cannot be null");
            
            verifyNoInteractions(equationRepository, parserService, evaluationService);
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException for null variables")
        void shouldThrowExceptionForNullVariables() {
            // When & Then
            assertThatThrownBy(() -> equationService.evaluateEquation(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Variables map cannot be null");
            
            verifyNoInteractions(equationRepository, parserService, evaluationService);
        }
        
        @Test
        @DisplayName("Should throw IllegalStateException for non-existent equation")
        void shouldThrowExceptionForNonExistentEquation() {
            // Given
            Long nonExistentId = 999L;
            Map<String, Double> variables = Map.of("x", 1.0);
            when(equationRepository.findById(nonExistentId)).thenReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> equationService.evaluateEquation(nonExistentId, variables))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Equation with ID 999 not found");
            
            verify(equationRepository).findById(nonExistentId);
            verifyNoInteractions(parserService, evaluationService);
        }
        
        @Test
        @DisplayName("Should propagate VariableNotProvidedException from evaluator")
        void shouldPropagateVariableNotProvidedException() {
            // Given
            Long equationId = 1L;
            String infix = "x + y";
            Map<String, Double> incompleteVariables = Map.of("x", 1.0); // missing 'y'
            
            EquationEntity equation = new EquationEntity(equationId, infix, List.of(), "test-hash");
            when(equationRepository.findById(equationId)).thenReturn(Optional.of(equation));
            com.example.equation.model.Node mockNode = new com.example.equation.model.OperandNode("5");
            when(parserService.parseExpression(infix)).thenReturn(mockNode);
            when(evaluationService.evaluate(any(), eq(incompleteVariables)))
                .thenThrow(new VariableNotProvidedException("y"));
            
            // When & Then
            assertThatThrownBy(() -> equationService.evaluateEquation(equationId, incompleteVariables))
                .isInstanceOf(VariableNotProvidedException.class)
                .hasMessage("Variable 'y' is not provided in the variable map");
            
            verify(evaluationService).evaluate(any(), eq(incompleteVariables));
        }
        
        @Test
        @DisplayName("Should propagate ArithmeticException from evaluator")
        void shouldPropagateArithmeticException() {
            // Given
            Long equationId = 1L;
            String infix = "x / y";
            Map<String, Double> variables = Map.of("x", 10.0, "y", 0.0); // division by zero
            
            EquationEntity equation = new EquationEntity(equationId, infix, List.of(), "test-hash");
            when(equationRepository.findById(equationId)).thenReturn(Optional.of(equation));
            com.example.equation.model.Node mockNode = new com.example.equation.model.OperandNode("5");
            when(parserService.parseExpression(infix)).thenReturn(mockNode);
            when(evaluationService.evaluate(any(), eq(variables)))
                .thenThrow(new ArithmeticException("Division by zero"));
            
            // When & Then
            assertThatThrownBy(() -> equationService.evaluateEquation(equationId, variables))
                .isInstanceOf(ArithmeticException.class)
                .hasMessage("Division by zero");
        }
    }
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Should handle complete workflow: store, retrieve, evaluate")
        void shouldHandleCompleteWorkflow() {
            // Given
            String infix = "radius^2 * 3.14159";
            Long equationId = 1L;
            Map<String, Double> variables = Map.of("radius", 5.0);
            double expectedArea = 78.53975; // 5^2 * 3.14159
            
            // Mock store workflow - AST-based duplicate checking
            EquationEntity savedEntity = new EquationEntity(equationId, infix, List.of(), "test");
            when(parserService.parseExpression(infix)).thenReturn(new OperandNode("test"));
            when(equationRepository.findByAstHash("test")).thenReturn(Optional.empty()); // No duplicates
            when(parserService.tokenize(infix)).thenReturn(List.of());
            when(parserService.infixToPostfix(any())).thenReturn(List.of());
            when(parserService.buildExpressionTree(any())).thenReturn(new OperandNode("test"));
            when(equationRepository.save(any())).thenReturn(savedEntity);
            
            // Mock retrieve workflow
            when(equationRepository.findAll()).thenReturn(List.of(savedEntity));
            
            // Mock evaluate workflow
            when(equationRepository.findById(equationId)).thenReturn(Optional.of(savedEntity));
            when(evaluationService.evaluate(any(), eq(variables))).thenReturn(expectedArea);
            
            // When - Store equation
            Long storedId = equationService.storeEquation(infix);
            
            // Then - Verify storage
            assertThat(storedId).isEqualTo(equationId);
            
            // When - Retrieve all equations
            List<EquationDto> equations = equationService.getAllEquations();
            
            // Then - Verify retrieval
            assertThat(equations).hasSize(1);
            assertThat(equations.get(0).getId()).isEqualTo(equationId);
            assertThat(equations.get(0).getInfix()).isEqualTo(infix);
            
            // When - Evaluate equation
            double result = equationService.evaluateEquation(equationId, variables);
            
            // Then - Verify evaluation
            assertThat(result).isEqualTo(expectedArea);
            
            // Verify all interactions occurred
            verify(parserService, times(2)).parseExpression(infix); // Called for store (hash) and evaluate
            verify(equationRepository).findByAstHash("test"); // Called for duplicate check
            verify(parserService).tokenize(infix);
            verify(parserService).infixToPostfix(any());
            verify(equationRepository).save(any());
            verify(equationRepository).findAll(); // Called once for retrieve
            verify(equationRepository).findById(equationId);
            verify(evaluationService).evaluate(any(), eq(variables));
        }
    }
} 