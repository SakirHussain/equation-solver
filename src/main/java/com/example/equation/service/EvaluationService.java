package com.example.equation.service;

import com.example.equation.exception.VariableNotProvidedException;
import com.example.equation.model.Node;
import com.example.equation.model.OperandNode;
import com.example.equation.model.OperatorNode;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class EvaluationService {
    
    public double evaluate(Node root, Map<String, Double> variables) {
        if (root == null) {
            throw new IllegalArgumentException("Expression tree root cannot be null");
        }
        if (variables == null) {
            throw new IllegalArgumentException("Variables map cannot be null");
        }
        
        return evaluateNode(root, variables);
    }
    
    
    private double evaluateNode(Node node, Map<String, Double> variables) {
        if (node instanceof OperandNode operandNode) {
            return evaluateOperand(operandNode, variables);
        } else if (node instanceof OperatorNode operatorNode) {
            return evaluateOperator(operatorNode, variables);
        } else {
            throw new IllegalArgumentException("Unknown node type: " + node.getClass().getSimpleName());
        }
    }
    
    
    private double evaluateOperand(OperandNode operandNode, Map<String, Double> variables) {
        String symbol = operandNode.getSymbol();
        
        try {
            return Double.parseDouble(symbol);
        } catch (NumberFormatException e) {
            if (!variables.containsKey(symbol)) {
                throw new VariableNotProvidedException(symbol);
            }
            return variables.get(symbol);
        }
    }
    
    
    private double evaluateOperator(OperatorNode operatorNode, Map<String, Double> variables) {
        double leftValue = evaluateNode(operatorNode.getLeft(), variables);
        double rightValue = evaluateNode(operatorNode.getRight(), variables);
        char operator = operatorNode.getOp();
        
        return switch (operator) {
            case '+' -> leftValue + rightValue;
            case '-' -> leftValue - rightValue;
            case '*' -> leftValue * rightValue;
            case '/' -> {
                if (rightValue == 0.0) {
                    throw new ArithmeticException("Division by zero");
                }
                yield leftValue / rightValue;
            }
            case '^' -> Math.pow(leftValue, rightValue);
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };
    }
} 