package com.example.equation.model;

import java.util.Map;

// ast node
public sealed interface Node permits OperandNode, OperatorNode {    
    double evaluate(Map<String, Double> vars);
    
    String generateHash();
} 