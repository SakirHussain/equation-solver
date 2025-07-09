package com.example.equation.service;

import com.example.equation.model.OperandNode;
import com.example.equation.model.OperatorNode;
import com.example.equation.repository.InMemoryEquationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for AST hashing and mathematical equivalence detection.
 * Demonstrates that mathematically equivalent expressions are detected regardless of:
 * - Whitespace differences
 * - Redundant parentheses
 */
class EquationServiceASTTest {
    
    private EquationService equationService;
    private InMemoryEquationRepository repository;
    private ParserService parserService;
    private EvaluationService evaluationService;
    
    @BeforeEach
    void setUp() {
        repository = new InMemoryEquationRepository();
        parserService = new ParserService();
        evaluationService = new EvaluationService();
        equationService = new EquationService(repository, parserService, evaluationService);
    }
    
    @Test
    @DisplayName("Should detect mathematical equivalence regardless of whitespace")
    void shouldDetectEquivalenceRegardlessOfWhitespace() {
        // Given - expressions that are mathematically equivalent but have different whitespace
        String expr1 = "x+y*2";
        String expr2 = " x + y * 2 ";
        String expr3 = "x  +  y  *  2";
        
        // When - store the first expression
        Long id1 = equationService.storeEquation(expr1);
        
        // When - try to store equivalent expressions with different whitespace
        Long id2 = equationService.storeEquation(expr2);
        Long id3 = equationService.storeEquation(expr3);
        
        // Then - all should return the same ID (no duplicates created)
        assertThat(id1).isEqualTo(id2).isEqualTo(id3);
        assertThat(repository.size()).isEqualTo(1); // Only one equation stored
    }
    
    @Test
    @DisplayName("Should detect mathematical equivalence regardless of redundant parentheses")
    void shouldDetectEquivalenceRegardlessOfParentheses() {
        // Given - expressions that are mathematically equivalent due to operator precedence
        String expr1 = "a/d*c";           // Following DMAS: ((a/d)*c)
        String expr2 = "(a/d)*c";         // Explicit parentheses, same structure
        String expr3 = "((a/d)*c)";       // Extra redundant parentheses
        
        // When - store the expressions
        Long id1 = equationService.storeEquation(expr1);
        Long id2 = equationService.storeEquation(expr2);
        Long id3 = equationService.storeEquation(expr3);
        
        // Then - all should return the same ID (mathematically equivalent)
        assertThat(id1).isEqualTo(id2).isEqualTo(id3);
        assertThat(repository.size()).isEqualTo(1); // Only one equation stored
    }
    
    @Test
    @DisplayName("Should treat different variables as distinct equations")
    void shouldTreatDifferentVariablesAsDistinct() {
        // Given - expressions with different variable names
        String expr1 = "a + b";
        String expr2 = "x + y";
        
        // When - store the expressions
        Long id1 = equationService.storeEquation(expr1);
        Long id2 = equationService.storeEquation(expr2);
        
        // Then - should create different equations (different variables)
        assertThat(id1).isNotEqualTo(id2);
        assertThat(repository.size()).isEqualTo(2); // Two distinct equations
    }
    
    @Test
    @DisplayName("Should treat different operation order as distinct equations")
    void shouldTreatDifferentOperationOrderAsDistinct() {
        // Given - expressions with different operation order (non-commutative)
        String expr1 = "a + b";
        String expr2 = "b + a";
        
        // When - store the expressions
        Long id1 = equationService.storeEquation(expr1);
        Long id2 = equationService.storeEquation(expr2);
        
        // Then - should create different equations (different order)
        assertThat(id1).isNotEqualTo(id2);
        assertThat(repository.size()).isEqualTo(2); // Two distinct equations
    }
    
    @Test
    @DisplayName("Should demonstrate AST hash generation works correctly")
    void shouldDemonstrateASTHashGeneration() {
        // Given - manually construct equivalent ASTs
        OperandNode a = new OperandNode("a");
        OperandNode b = new OperandNode("b");
        OperatorNode tree1 = new OperatorNode('+', a, b);
        
        OperandNode a2 = new OperandNode("a");
        OperandNode b2 = new OperandNode("b");
        OperatorNode tree2 = new OperatorNode('+', a2, b2);
        
        // When - generate hashes
        String hash1 = tree1.generateHash();
        String hash2 = tree2.generateHash();
        
        // Then - identical structures should produce identical hashes
        assertThat(hash1).isEqualTo(hash2).isEqualTo("(a+b)");
    }
    
    @Test
    @DisplayName("Should show different structures produce different hashes")
    void shouldShowDifferentStructuresProduceDifferentHashes() {
        // Given - different AST structures
        OperandNode a = new OperandNode("a");
        OperandNode b = new OperandNode("b");
        OperatorNode aplusb = new OperatorNode('+', a, b);   // a+b → "(a+b)"
        OperatorNode bplusa = new OperatorNode('+', b, a);   // b+a → "(b+a)"
        
        // When - generate hashes
        String hash1 = aplusb.generateHash();
        String hash2 = bplusa.generateHash();
        
        // Then - different structures should produce different hashes
        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(hash1).isEqualTo("(a+b)");
        assertThat(hash2).isEqualTo("(b+a)");
    }
} 