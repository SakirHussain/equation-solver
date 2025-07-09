package com.example.equation.service;

import com.example.equation.exception.EquationSyntaxException;
import com.example.equation.model.Node;
import com.example.equation.model.OperandNode;
import com.example.equation.model.OperatorNode;
import com.example.equation.model.Token;
import com.example.equation.model.TokenValue;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for parsing mathematical expressions from infix notation to postfix notation
 * and building abstract syntax trees.
 */
@Service
public class ParserService {
    
    // Regex patterns for tokenization
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("[a-zA-Z]+");
    private static final Pattern OPERATOR_PATTERN = Pattern.compile("[+\\-*/^]");
    private static final Pattern PAREN_PATTERN = Pattern.compile("[()]");
    
    /**
     * Tokenizes an infix expression into a list of token values.
     * Supports decimal numbers, variables, operators (+, -, *, /, ^), and parentheses.
     * 
     * @param infix the infix expression to tokenize
     * @return list of token values representing the expression
     * @throws EquationSyntaxException if the expression contains invalid characters
     */
    public List<TokenValue> tokenize(String infix) {
        if (infix == null || infix.trim().isEmpty()) {
            throw new EquationSyntaxException("Expression cannot be null or empty");
        }
        
        List<TokenValue> tokens = new ArrayList<>();
        String cleanInfix = infix.replaceAll("\\s+", ""); // Remove whitespace
        
        int i = 0;
        while (i < cleanInfix.length()) {
            char ch = cleanInfix.charAt(i);
            
            // Try to match patterns at current position
            String remaining = cleanInfix.substring(i);
            
            Matcher numberMatcher = NUMBER_PATTERN.matcher(remaining);
            Matcher variableMatcher = VARIABLE_PATTERN.matcher(remaining);
            Matcher operatorMatcher = OPERATOR_PATTERN.matcher(remaining);
            Matcher parenMatcher = PAREN_PATTERN.matcher(remaining);
            
            if (numberMatcher.lookingAt()) {
                String numberValue = numberMatcher.group();
                tokens.add(new TokenValue(Token.NUMBER, numberValue));
                i += numberMatcher.end();
            } else if (variableMatcher.lookingAt()) {
                String variableValue = variableMatcher.group();
                tokens.add(new TokenValue(Token.VARIABLE, variableValue));
                i += variableMatcher.end();
            } else if (operatorMatcher.lookingAt()) {
                String operatorValue = operatorMatcher.group();
                tokens.add(new TokenValue(Token.OPERATOR, operatorValue));
                i += operatorMatcher.end();
            } else if (parenMatcher.lookingAt()) {
                if (ch == '(') {
                    tokens.add(new TokenValue(Token.LPAREN, "("));
                } else {
                    tokens.add(new TokenValue(Token.RPAREN, ")"));
                }
                i++;
            } else {
                throw new EquationSyntaxException("Illegal character '" + ch + "' at position " + i);
            }
        }
        
        if (tokens.isEmpty()) {
            throw new EquationSyntaxException("Expression contains no valid tokens");
        }
        
        return tokens;
    }
    
    /**
     * Converts an infix token list to postfix notation using the Shunting-Yard algorithm.
     * Handles operator precedence and associativity rules.
     * 
     * @param infixTokens the infix token values to convert
     * @return list of token values in postfix notation
     * @throws EquationSyntaxException if parentheses are unbalanced
     */
    public List<TokenValue> infixToPostfix(List<TokenValue> infixTokens) {
        if (infixTokens == null || infixTokens.isEmpty()) {
            throw new EquationSyntaxException("Token list cannot be null or empty");
        }
        
        List<TokenValue> output = new ArrayList<>();
        Stack<TokenValue> operatorStack = new Stack<>();
        
        for (TokenValue tokenValue : infixTokens) {
            switch (tokenValue.getType()) {
                case NUMBER:
                case VARIABLE:
                    output.add(tokenValue);
                    break;
                    
                case OPERATOR:
                    while (!operatorStack.isEmpty() && 
                           operatorStack.peek().getType() == Token.OPERATOR &&
                           hasHigherOrEqualPrecedence(operatorStack.peek().getValue().charAt(0), tokenValue.getValue().charAt(0))) {
                        output.add(operatorStack.pop());
                    }
                    operatorStack.push(tokenValue);
                    break;
                    
                case LPAREN:
                    operatorStack.push(tokenValue);
                    break;
                    
                case RPAREN:
                    boolean foundLeftParen = false;
                    while (!operatorStack.isEmpty()) {
                        TokenValue op = operatorStack.pop();
                        if (op.getType() == Token.LPAREN) {
                            foundLeftParen = true;
                            break;
                        }
                        output.add(op);
                    }
                    if (!foundLeftParen) {
                        throw new EquationSyntaxException("Unbalanced parentheses: missing left parenthesis");
                    }
                    break;
            }
        }
        
        // Pop remaining operators from stack
        while (!operatorStack.isEmpty()) {
            TokenValue op = operatorStack.pop();
            if (op.getType() == Token.LPAREN || op.getType() == Token.RPAREN) {
                throw new EquationSyntaxException("Unbalanced parentheses: missing right parenthesis");
            }
            output.add(op);
        }
        
        return output;
    }
    
    /**
     * Builds an expression tree from postfix token values using a stack-based algorithm.
     * 
     * @param postfixTokens the postfix token values to convert to a tree
     * @return the root node of the expression tree
     * @throws EquationSyntaxException if the postfix expression is invalid
     */
    public Node buildExpressionTree(List<TokenValue> postfixTokens) {
        if (postfixTokens == null || postfixTokens.isEmpty()) {
            throw new EquationSyntaxException("Postfix token list cannot be null or empty");
        }
        
        Stack<Node> nodeStack = new Stack<>();
        
        for (TokenValue tokenValue : postfixTokens) {
            switch (tokenValue.getType()) {
                case NUMBER:
                case VARIABLE:
                    nodeStack.push(new OperandNode(tokenValue.getValue()));
                    break;
                    
                case OPERATOR:
                    if (nodeStack.size() < 2) {
                        throw new EquationSyntaxException("Invalid postfix expression: insufficient operands for operator");
                    }
                    Node right = nodeStack.pop();
                    Node left = nodeStack.pop();
                    char operator = tokenValue.getValue().charAt(0);
                    nodeStack.push(new OperatorNode(operator, left, right));
                    break;
                    
                default:
                    throw new EquationSyntaxException("Unexpected token in postfix expression: " + tokenValue.getType());
            }
        }
        
        if (nodeStack.size() != 1) {
            throw new EquationSyntaxException("Invalid postfix expression: should result in exactly one root node");
        }
        
        return nodeStack.pop();
    }
    
    /**
     * Convenience method that parses an infix expression string into an expression tree.
     * This method combines tokenization, postfix conversion, and tree building.
     * 
     * @param infix the infix expression string to parse
     * @return the root node of the expression tree
     * @throws EquationSyntaxException if the expression is invalid
     */
    public Node parseExpression(String infix) {
        List<TokenValue> tokens = tokenize(infix);
        List<TokenValue> postfixTokens = infixToPostfix(tokens);
        return buildExpressionTree(postfixTokens);
    }
    
    /**
     * Determines if operator1 has higher or equal precedence compared to operator2.
     * Also handles associativity rules.
     * 
     * Precedence (high to low):
     * ^ (power) - right associative
     * *, / (multiply/divide) - left associative
     * +, - (add/subtract) - left associative
     */
    private boolean hasHigherOrEqualPrecedence(char operator1, char operator2) {
        int prec1 = getOperatorPrecedence(operator1);
        int prec2 = getOperatorPrecedence(operator2);
        
        // For right-associative operators (^), only pop if strictly higher precedence
        if (operator2 == '^') {
            return prec1 > prec2;
        }
        
        // For left-associative operators (+, -, *, /), pop if higher or equal precedence
        return prec1 >= prec2;
    }
    
    /**
     * Gets the precedence level of an operator.
     * Higher numbers indicate higher precedence.
     */
    private int getOperatorPrecedence(char operator) {
        return switch (operator) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            case '^' -> 3;
            default -> throw new EquationSyntaxException("Unknown operator: " + operator);
        };
    }
} 