package com.example.equation.model;

import java.util.List;

// equation with id, infix, postfix, and AST hash
public class EquationEntity {
    
    private Long id;
    private String infix;
    private List<Token> postfix;
    private String astHash;
    
    public EquationEntity() {
    }
    

    public EquationEntity(Long id, String infix, List<Token> postfix, String astHash) {
        this.id = id;
        this.infix = infix;
        this.postfix = postfix;
        this.astHash = astHash;
    }


    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    
    public String getInfix() {
        return infix;
    }
    
    public void setInfix(String infix) {
        this.infix = infix;
    }
    
    
    public List<Token> getPostfix() {
        return postfix;
    }
    
    public void setPostfix(List<Token> postfix) {
        this.postfix = postfix;
    }
    
    public String getAstHash() {
        return astHash;
    }
    
    public void setAstHash(String astHash) {
        this.astHash = astHash;
    }
    
    @Override
    public String toString() {
        return "EquationEntity{" +
                "id=" + id +
                ", infix='" + infix + "'" +
                ", postfix=" + postfix +
                ", astHash='" + astHash + "'" +
                "}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EquationEntity that = (EquationEntity) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 