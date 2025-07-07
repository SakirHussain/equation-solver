package com.example.equation.model;

import java.util.Map;

// internal operator node
public final class OperatorNode implements Node {
    
    private final char op;
    private final Node left;
    private final Node right;
    
    public OperatorNode(char op, Node left, Node right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }
    

    public char getOp() {
        return op;
    }    

    public Node getLeft() {
        return left;
    }
    
    public Node getRight() {
        return right;
    }
    
    // evaluate the operator node
    @Override
    public double evaluate(Map<String, Double> vars) {
        double leftValue = left.evaluate(vars);
        double rightValue = right.evaluate(vars);
        
        return switch (op) {
            case '+' -> leftValue + rightValue;
            case '-' -> leftValue - rightValue;
            case '*' -> leftValue * rightValue;
            case '/' -> {
                if (rightValue == 0.0) {
                    throw new ArithmeticException("Division by zero");
                }
                yield leftValue / rightValue;
            }
            default -> throw new IllegalArgumentException("Unsupported operator: " + op);
        };
    }
    
    @Override
    public String toString() {
        return "OperatorNode{op=" + op + ", left=" + left + ", right=" + right + "}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OperatorNode that = (OperatorNode) obj;
        return op == that.op && left.equals(that.left) && right.equals(that.right);
    }
    
    @Override
    public int hashCode() {
        return Character.hashCode(op) + 31 * (left.hashCode() + 31 * right.hashCode());
    }
} 