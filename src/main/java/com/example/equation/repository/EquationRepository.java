package com.example.equation.repository;

import com.example.equation.model.EquationEntity;

import java.util.List;
import java.util.Optional;

public interface EquationRepository {
    
    EquationEntity save(EquationEntity equation);
    
    Optional<EquationEntity> findById(Long id);
    
    List<EquationEntity> findAll();
    
    /**
     * Finds an equation by its AST hash.
     * This enables fast lookup of mathematically equivalent equations.
     * 
     * @param astHash the AST hash string
     * @return Optional containing the equation if found, empty otherwise
     */
    Optional<EquationEntity> findByAstHash(String astHash);
} 