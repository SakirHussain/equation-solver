package com.example.equation.model;

import java.util.Map;

// leaf node
public final class OperandNode implements Node {
    
    private final String symbol;
    
    
    public OperandNode(String symbol) {
        this.symbol = symbol;
    }
    
    
    public String getSymbol() {
        return symbol;
    }
    
    // evaluate the operand node
    @Override
    public double evaluate(Map<String, Double> vars) {
        try {
            // try as number
            return Double.parseDouble(symbol);
        } catch (NumberFormatException e) {
            // try as variable
            if (vars.containsKey(symbol)) {
                return vars.get(symbol);
            } else {
                throw new IllegalArgumentException("Variable '" + symbol + "' not found in variable map");
            }
        }
    }
    
    @Override
    public String toString() {
        return "OperandNode{symbol='" + symbol + "'}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OperandNode that = (OperandNode) obj;
        return symbol.equals(that.symbol);
    }
    
    @Override
    public int hashCode() {
        return symbol.hashCode();
    }
    
    @Override
    public String generateHash() {
        // For operands, the hash is simply the symbol itself
        // This preserves variable names distinctly (a ≠ x)
        return symbol;
    }
} 