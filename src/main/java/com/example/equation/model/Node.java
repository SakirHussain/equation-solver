package com.example.equation.model;

import java.util.Map;

// ast node
public sealed interface Node permits OperandNode, OperatorNode {    
    double evaluate(Map<String, Double> vars);
    
    /**
     * Generates a unique hash string representing the structure and content of this AST node.
     * Two mathematically equivalent expressions will produce the same hash regardless of:
     * - Whitespace differences
     * - Redundant parentheses
     * 
     * The hash preserves:
     * - Variable names (a+b ≠ x+y)
     * - Operation order (a+b ≠ b+a, respecting non-commutative intent)
     * - Mathematical structure
     * 
     * @return a unique hash string for this AST structure
     */
    String generateHash();
} 