package com.example.equation.model;

/**
 * Represents a token with its type and actual string value from the expression.
 * Used during parsing to maintain both the token classification and original text.
 */
public class TokenValue {
    private final Token type;
    private final String value;
    
    /**
     * Creates a new token value.
     * 
     * @param type the token type classification
     * @param value the actual string value from the expression
     */
    public TokenValue(Token type, String value) {
        this.type = type;
        this.value = value;
    }
    
    /**
     * Gets the token type.
     * 
     * @return the token type
     */
    public Token getType() {
        return type;
    }
    
    /**
     * Gets the actual string value.
     * 
     * @return the string value
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return String.format("TokenValue{type=%s, value='%s'}", type, value);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TokenValue other)) return false;
        return type == other.type && value.equals(other.value);
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() * 31 + value.hashCode();
    }
} 