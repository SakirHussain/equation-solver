package com.example.equation.service;

import com.example.equation.exception.VariableNotProvidedException;
import com.example.equation.model.Node;
import com.example.equation.model.OperandNode;
import com.example.equation.model.OperatorNode;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for evaluating mathematical expression trees.
 * Provides recursive evaluation with proper error handling for missing variables
 * and arithmetic errors like division by zero.
 */
@Service
public class EvaluationService {
    
    /**
     * Recursively evaluates an expression tree with the provided variable values.
     * 
     * @param root the root node of the expression tree to evaluate
     * @param variables map of variable names to their values
     * @return the computed result of the expression
     * @throws VariableNotProvidedException if a variable referenced in the expression is not provided
     * @throws ArithmeticException for arithmetic errors like division by zero
     * @throws IllegalArgumentException for unsupported operators or null inputs
     */
    public double evaluate(Node root, Map<String, Double> variables) {
        if (root == null) {
            throw new IllegalArgumentException("Expression tree root cannot be null");
        }
        if (variables == null) {
            throw new IllegalArgumentException("Variables map cannot be null");
        }
        
        return evaluateNode(root, variables);
    }
    
    /**
     * Internal recursive method to evaluate a node in the expression tree.
     * 
     * @param node the current node to evaluate
     * @param variables map of variable names to their values
     * @return the computed result of this node
     */
    private double evaluateNode(Node node, Map<String, Double> variables) {
        if (node instanceof OperandNode operandNode) {
            return evaluateOperand(operandNode, variables);
        } else if (node instanceof OperatorNode operatorNode) {
            return evaluateOperator(operatorNode, variables);
        } else {
            throw new IllegalArgumentException("Unknown node type: " + node.getClass().getSimpleName());
        }
    }
    
    /**
     * Evaluates an operand node (number or variable).
     * 
     * @param operandNode the operand node to evaluate
     * @param variables map of variable names to their values
     * @return the numeric value of the operand
     * @throws VariableNotProvidedException if the operand is a variable not found in the map
     */
    private double evaluateOperand(OperandNode operandNode, Map<String, Double> variables) {
        String symbol = operandNode.getSymbol();
        
        // Try to parse as a number first
        try {
            return Double.parseDouble(symbol);
        } catch (NumberFormatException e) {
            // It's a variable, look it up in the variables map
            if (!variables.containsKey(symbol)) {
                throw new VariableNotProvidedException(symbol);
            }
            return variables.get(symbol);
        }
    }
    
    /**
     * Evaluates an operator node by recursively evaluating its operands
     * and applying the operator.
     * 
     * @param operatorNode the operator node to evaluate
     * @param variables map of variable names to their values
     * @return the result of applying the operator to the evaluated operands
     * @throws ArithmeticException for division by zero or other arithmetic errors
     * @throws IllegalArgumentException for unsupported operators
     */
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