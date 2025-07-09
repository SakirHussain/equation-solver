package com.example.equation.service;

import com.example.equation.exception.EquationSyntaxException;
import com.example.equation.model.Node;
import com.example.equation.model.OperandNode;
import com.example.equation.model.OperatorNode;
import com.example.equation.model.Token;
import com.example.equation.model.TokenValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for ParserService covering tokenization, infix-to-postfix conversion,
 * and expression tree building using the clean TokenValue API.
 */
class ParserServiceTest {
    
    private ParserService parserService;
    
    @BeforeEach
    void setUp() {
        parserService = new ParserService();
    }
    
    @Nested
    @DisplayName("Tokenization Tests")
    class TokenizeTests {
        
        @Test
        @DisplayName("Should tokenize simple expression '3*x+2'")
        void shouldTokenizeSimpleExpression() {
            // Given
            String expression = "3*x+2";
            
            // When
            List<TokenValue> tokens = parserService.tokenize(expression);
            
            // Then
            assertThat(tokens).hasSize(5);
            assertThat(tokens.get(0)).satisfies(token -> {
                assertThat(token.getType()).isEqualTo(Token.NUMBER);
                assertThat(token.getValue()).isEqualTo("3");
            });
            assertThat(tokens.get(1)).satisfies(token -> {
                assertThat(token.getType()).isEqualTo(Token.OPERATOR);
                assertThat(token.getValue()).isEqualTo("*");
            });
            assertThat(tokens.get(2)).satisfies(token -> {
                assertThat(token.getType()).isEqualTo(Token.VARIABLE);
                assertThat(token.getValue()).isEqualTo("x");
            });
            assertThat(tokens.get(3)).satisfies(token -> {
                assertThat(token.getType()).isEqualTo(Token.OPERATOR);
                assertThat(token.getValue()).isEqualTo("+");
            });
            assertThat(tokens.get(4)).satisfies(token -> {
                assertThat(token.getType()).isEqualTo(Token.NUMBER);
                assertThat(token.getValue()).isEqualTo("2");
            });
        }
        
        @Test
        @DisplayName("Should tokenize expression with decimals and parentheses")
        void shouldTokenizeComplexExpression() {
            // Given
            String expression = "(3.14 * radius) + 2.5";
            
            // When
            List<TokenValue> tokens = parserService.tokenize(expression);
            
            // Then
            assertThat(tokens).hasSize(7);
            assertThat(tokens.get(0).getType()).isEqualTo(Token.LPAREN);
            assertThat(tokens.get(1)).satisfies(token -> {
                assertThat(token.getType()).isEqualTo(Token.NUMBER);
                assertThat(token.getValue()).isEqualTo("3.14");
            });
            assertThat(tokens.get(2)).satisfies(token -> {
                assertThat(token.getType()).isEqualTo(Token.OPERATOR);
                assertThat(token.getValue()).isEqualTo("*");
            });
            assertThat(tokens.get(3)).satisfies(token -> {
                assertThat(token.getType()).isEqualTo(Token.VARIABLE);
                assertThat(token.getValue()).isEqualTo("radius");
            });
            assertThat(tokens.get(4).getType()).isEqualTo(Token.RPAREN);
            assertThat(tokens.get(5)).satisfies(token -> {
                assertThat(token.getType()).isEqualTo(Token.OPERATOR);
                assertThat(token.getValue()).isEqualTo("+");
            });
            assertThat(tokens.get(6)).satisfies(token -> {
                assertThat(token.getType()).isEqualTo(Token.NUMBER);
                assertThat(token.getValue()).isEqualTo("2.5");
            });
        }
        
        @Test
        @DisplayName("Should tokenize expression with all operators")
        void shouldTokenizeAllOperators() {
            // Given
            String expression = "a+b-c*d/e^f";
            
            // When
            List<TokenValue> tokens = parserService.tokenize(expression);
            
            // Then
            assertThat(tokens).hasSize(11);
            String[] expectedValues = {"a", "+", "b", "-", "c", "*", "d", "/", "e", "^", "f"};
            Token[] expectedTypes = {Token.VARIABLE, Token.OPERATOR, Token.VARIABLE, Token.OPERATOR, 
                                   Token.VARIABLE, Token.OPERATOR, Token.VARIABLE, Token.OPERATOR,
                                   Token.VARIABLE, Token.OPERATOR, Token.VARIABLE};
            
            for (int i = 0; i < tokens.size(); i++) {
                assertThat(tokens.get(i).getType()).isEqualTo(expectedTypes[i]);
                assertThat(tokens.get(i).getValue()).isEqualTo(expectedValues[i]);
            }
        }
        
        @Test
        @DisplayName("Should handle whitespace correctly")
        void shouldHandleWhitespace() {
            // Given
            String expression = "  3  *  x  +  2  ";
            
            // When
            List<TokenValue> tokens = parserService.tokenize(expression);
            
            // Then
            assertThat(tokens).hasSize(5);
            assertThat(tokens.get(0).getValue()).isEqualTo("3");
            assertThat(tokens.get(1).getValue()).isEqualTo("*");
            assertThat(tokens.get(2).getValue()).isEqualTo("x");
            assertThat(tokens.get(3).getValue()).isEqualTo("+");
            assertThat(tokens.get(4).getValue()).isEqualTo("2");
        }
        
        @Test
        @DisplayName("Should throw exception for null input")
        void shouldThrowExceptionForNullInput() {
            // When & Then
            assertThatThrownBy(() -> parserService.tokenize(null))
                .isInstanceOf(EquationSyntaxException.class)
                .hasMessage("Expression cannot be null or empty");
        }
        
        @Test
        @DisplayName("Should throw exception for empty input")
        void shouldThrowExceptionForEmptyInput() {
            // When & Then
            assertThatThrownBy(() -> parserService.tokenize("   "))
                .isInstanceOf(EquationSyntaxException.class)
                .hasMessage("Expression cannot be null or empty");
        }
        
        @Test
        @DisplayName("Should throw exception for illegal character")
        void shouldThrowExceptionForIllegalCharacter() {
            // Given
            String expression = "3*x@2";
            
            // When & Then
            assertThatThrownBy(() -> parserService.tokenize(expression))
                .isInstanceOf(EquationSyntaxException.class)
                .hasMessageContaining("Illegal character")
                .hasMessageContaining("@")
                .hasMessageContaining("position 3");
        }
    }
    
    @Nested
    @DisplayName("Infix to Postfix Conversion Tests")
    class InfixToPostfixTests {
        
        @Test
        @DisplayName("Should convert simple expression to postfix")
        void shouldConvertSimpleExpressionToPostfix() {
            // Given - 3*x+2
            List<TokenValue> infixTokens = List.of(
                new TokenValue(Token.NUMBER, "3"),
                new TokenValue(Token.OPERATOR, "*"),
                new TokenValue(Token.VARIABLE, "x"),
                new TokenValue(Token.OPERATOR, "+"),
                new TokenValue(Token.NUMBER, "2")
            );
            
            // When
            List<TokenValue> postfixTokens = parserService.infixToPostfix(infixTokens);
            
            // Then - Expected: 3 x * 2 +
            assertThat(postfixTokens).hasSize(5);
            assertThat(postfixTokens.get(0).getValue()).isEqualTo("3");
            assertThat(postfixTokens.get(1).getValue()).isEqualTo("x");
            assertThat(postfixTokens.get(2).getValue()).isEqualTo("*");
            assertThat(postfixTokens.get(3).getValue()).isEqualTo("2");
            assertThat(postfixTokens.get(4).getValue()).isEqualTo("+");
        }
        
        @Test
        @DisplayName("Should handle parentheses correctly")
        void shouldHandleParenthesesCorrectly() {
            // Given - (3 + 2) * x
            List<TokenValue> infixTokens = List.of(
                new TokenValue(Token.LPAREN, "("),
                new TokenValue(Token.NUMBER, "3"),
                new TokenValue(Token.OPERATOR, "+"),
                new TokenValue(Token.NUMBER, "2"),
                new TokenValue(Token.RPAREN, ")"),
                new TokenValue(Token.OPERATOR, "*"),
                new TokenValue(Token.VARIABLE, "x")
            );
            
            // When
            List<TokenValue> postfixTokens = parserService.infixToPostfix(infixTokens);
            
            // Then - Expected: 3 2 + x *
            assertThat(postfixTokens).hasSize(5);
            assertThat(postfixTokens.get(0).getValue()).isEqualTo("3");
            assertThat(postfixTokens.get(1).getValue()).isEqualTo("2");
            assertThat(postfixTokens.get(2).getValue()).isEqualTo("+");
            assertThat(postfixTokens.get(3).getValue()).isEqualTo("x");
            assertThat(postfixTokens.get(4).getValue()).isEqualTo("*");
        }
        
        @Test
        @DisplayName("Should handle operator precedence correctly")
        void shouldHandleOperatorPrecedence() {
            // Given - 2+3*4 should become 2 3 4 * +
            List<TokenValue> infixTokens = List.of(
                new TokenValue(Token.NUMBER, "2"),
                new TokenValue(Token.OPERATOR, "+"),
                new TokenValue(Token.NUMBER, "3"),
                new TokenValue(Token.OPERATOR, "*"),
                new TokenValue(Token.NUMBER, "4")
            );
            
            // When
            List<TokenValue> postfixTokens = parserService.infixToPostfix(infixTokens);
            
            // Then - Expected: 2 3 4 * +
            assertThat(postfixTokens).hasSize(5);
            assertThat(postfixTokens.get(0).getValue()).isEqualTo("2");
            assertThat(postfixTokens.get(1).getValue()).isEqualTo("3");
            assertThat(postfixTokens.get(2).getValue()).isEqualTo("4");
            assertThat(postfixTokens.get(3).getValue()).isEqualTo("*");
            assertThat(postfixTokens.get(4).getValue()).isEqualTo("+");
        }
        
        @Test
        @DisplayName("Should throw exception for unbalanced parentheses - missing left")
        void shouldThrowExceptionForMissingLeftParenthesis() {
            // Given - 3 + 2)
            List<TokenValue> infixTokens = List.of(
                new TokenValue(Token.NUMBER, "3"),
                new TokenValue(Token.OPERATOR, "+"),
                new TokenValue(Token.NUMBER, "2"),
                new TokenValue(Token.RPAREN, ")")
            );
            
            // When & Then
            assertThatThrownBy(() -> parserService.infixToPostfix(infixTokens))
                .isInstanceOf(EquationSyntaxException.class)
                .hasMessage("Unbalanced parentheses: missing left parenthesis");
        }
        
        @Test
        @DisplayName("Should throw exception for unbalanced parentheses - missing right")
        void shouldThrowExceptionForMissingRightParenthesis() {
            // Given - (3 + 2
            List<TokenValue> infixTokens = List.of(
                new TokenValue(Token.LPAREN, "("),
                new TokenValue(Token.NUMBER, "3"),
                new TokenValue(Token.OPERATOR, "+"),
                new TokenValue(Token.NUMBER, "2")
            );
            
            // When & Then
            assertThatThrownBy(() -> parserService.infixToPostfix(infixTokens))
                .isInstanceOf(EquationSyntaxException.class)
                .hasMessage("Unbalanced parentheses: missing right parenthesis");
        }
    }
    
    @Nested
    @DisplayName("Expression Tree Building Tests")
    class ExpressionTreeTests {
        
        @Test
        @DisplayName("Should build tree for simple postfix expression")
        void shouldBuildTreeForSimpleExpression() {
            // Given - 3 x *
            List<TokenValue> postfixTokens = List.of(
                new TokenValue(Token.NUMBER, "3"),
                new TokenValue(Token.VARIABLE, "x"),
                new TokenValue(Token.OPERATOR, "*")
            );
            
            // When
            Node tree = parserService.buildExpressionTree(postfixTokens);
            
            // Then
            assertThat(tree).isInstanceOf(OperatorNode.class);
            OperatorNode rootOp = (OperatorNode) tree;
            assertThat(rootOp.getOp()).isEqualTo('*');
            
            assertThat(rootOp.getLeft()).isInstanceOf(OperandNode.class);
            assertThat(rootOp.getRight()).isInstanceOf(OperandNode.class);
            
            OperandNode leftOperand = (OperandNode) rootOp.getLeft();
            OperandNode rightOperand = (OperandNode) rootOp.getRight();
            assertThat(leftOperand.getSymbol()).isEqualTo("3");
            assertThat(rightOperand.getSymbol()).isEqualTo("x");
        }
        
        @Test
        @DisplayName("Should build tree for single operand")
        void shouldBuildTreeForSingleOperand() {
            // Given
            List<TokenValue> postfixTokens = List.of(
                new TokenValue(Token.NUMBER, "42")
            );
            
            // When
            Node tree = parserService.buildExpressionTree(postfixTokens);
            
            // Then
            assertThat(tree).isInstanceOf(OperandNode.class);
            OperandNode operand = (OperandNode) tree;
            assertThat(operand.getSymbol()).isEqualTo("42");
        }
        
        @Test
        @DisplayName("Should throw exception for insufficient operands")
        void shouldThrowExceptionForInsufficientOperands() {
            // Given - Only one operand for binary operator
            List<TokenValue> postfixTokens = List.of(
                new TokenValue(Token.NUMBER, "3"),
                new TokenValue(Token.OPERATOR, "+")
            );
            
            // When & Then
            assertThatThrownBy(() -> parserService.buildExpressionTree(postfixTokens))
                .isInstanceOf(EquationSyntaxException.class)
                .hasMessage("Invalid postfix expression: insufficient operands for operator");
        }
    }
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Should parse complete expression using parseExpression convenience method")
        void shouldParseCompleteExpression() {
            // Given
            String expression = "3*x+2";
            
            // When
            Node tree = parserService.parseExpression(expression);
            
            // Then
            assertThat(tree).isInstanceOf(OperatorNode.class);
            OperatorNode rootOp = (OperatorNode) tree;
            assertThat(rootOp.getOp()).isEqualTo('+'); // Root should be + (lowest precedence)
            
            // Left side should be 3*x
            assertThat(rootOp.getLeft()).isInstanceOf(OperatorNode.class);
            OperatorNode leftOp = (OperatorNode) rootOp.getLeft();
            assertThat(leftOp.getOp()).isEqualTo('*');
            
            // Check operands
            OperandNode leftOperand = (OperandNode) leftOp.getLeft();
            OperandNode rightOperand = (OperandNode) leftOp.getRight();
            assertThat(leftOperand.getSymbol()).isEqualTo("3");
            assertThat(rightOperand.getSymbol()).isEqualTo("x");
            
            // Right side should be 2
            assertThat(rootOp.getRight()).isInstanceOf(OperandNode.class);
            OperandNode rightOperandRoot = (OperandNode) rootOp.getRight();
            assertThat(rightOperandRoot.getSymbol()).isEqualTo("2");
        }
        
        @Test
        @DisplayName("Should handle operator precedence correctly in full pipeline")
        void shouldHandleOperatorPrecedenceCorrectly() {
            // Given - 2+3*4 should be parsed as 2+(3*4), not (2+3)*4
            String expression = "2+3*4";
            
            // When
            Node tree = parserService.parseExpression(expression);
            
            // Then
            assertThat(tree).isInstanceOf(OperatorNode.class);
            OperatorNode rootOp = (OperatorNode) tree;
            assertThat(rootOp.getOp()).isEqualTo('+'); // Root should be +
            
            // Left side should be 2
            assertThat(rootOp.getLeft()).isInstanceOf(OperandNode.class);
            OperandNode leftOperand = (OperandNode) rootOp.getLeft();
            assertThat(leftOperand.getSymbol()).isEqualTo("2");
            
            // Right side should be 3*4
            assertThat(rootOp.getRight()).isInstanceOf(OperatorNode.class);
            OperatorNode rightOp = (OperatorNode) rootOp.getRight();
            assertThat(rightOp.getOp()).isEqualTo('*');
            
            OperandNode rightLeft = (OperandNode) rightOp.getLeft();
            OperandNode rightRight = (OperandNode) rightOp.getRight();
            assertThat(rightLeft.getSymbol()).isEqualTo("3");
            assertThat(rightRight.getSymbol()).isEqualTo("4");
        }
        
        @Test
        @DisplayName("Should handle complex expression with parentheses")
        void shouldHandleComplexExpressionWithParentheses() {
            // Given
            String expression = "(x+y)*z";
            
            // When
            Node tree = parserService.parseExpression(expression);
            
            // Then
            assertThat(tree).isInstanceOf(OperatorNode.class);
            OperatorNode rootOp = (OperatorNode) tree;
            assertThat(rootOp.getOp()).isEqualTo('*');
            
            // Left side should be (x+y)
            assertThat(rootOp.getLeft()).isInstanceOf(OperatorNode.class);
            OperatorNode leftOp = (OperatorNode) rootOp.getLeft();
            assertThat(leftOp.getOp()).isEqualTo('+');
            
            // Right side should be z
            assertThat(rootOp.getRight()).isInstanceOf(OperandNode.class);
            OperandNode rightOperand = (OperandNode) rootOp.getRight();
            assertThat(rightOperand.getSymbol()).isEqualTo("z");
        }
    }
} 