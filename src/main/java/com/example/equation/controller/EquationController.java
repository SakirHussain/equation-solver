package com.example.equation.controller;

import com.example.equation.dto.*;
import com.example.equation.service.EquationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for equation management operations.
 * Provides endpoints for storing, listing, and evaluating mathematical equations.
 */
@RestController
@RequestMapping("/api/equations")
public class EquationController {
    
    private final EquationService equationService;
    
    /**
     * Constructor injection of EquationService.
     * 
     * @param equationService service for equation operations
     */
    public EquationController(EquationService equationService) {
        this.equationService = equationService;
    }
    
    /**
     * Stores a new mathematical equation.
     * 
     * <p>Accepts a mathematical expression in infix notation, parses it,
     * converts to postfix notation, and stores it in the repository.</p>
     * 
     * @param request the equation storage request containing the expression
     * @return response with the unique ID of the stored equation
     * @throws com.example.equation.exception.EquationSyntaxException if the equation syntax is invalid
     */
    @PostMapping("/store")
    @ResponseStatus(HttpStatus.CREATED)
    public StoreEquationResponse storeEquation(@Valid @RequestBody StoreEquationRequest request) {
        Long equationId = equationService.storeEquation(request.getEquation());
        return new StoreEquationResponse(equationId);
    }
    
    /**
     * Retrieves all stored equations.
     * 
     * <p>Returns a simplified list containing only the ID and infix expression
     * of each stored equation, optimized for listing purposes.</p>
     * 
     * @return list of equation summaries, empty list if no equations exist
     */
    @GetMapping
    public List<EquationSummaryDto> getAllEquations() {
        return equationService.getAllEquationSummaries();
    }
    
    /**
     * Evaluates a stored equation with provided variable values.
     * 
     * <p>Fetches the equation by ID, builds the expression tree, and computes
     * the result using the provided variable values.</p>
     * 
     * @param id the unique ID of the equation to evaluate
     * @param request the evaluation request containing variable values
     * @return response with the computed result
     * @throws IllegalStateException if equation with given ID is not found
     * @throws com.example.equation.exception.VariableNotProvidedException if required variables are missing
     * @throws ArithmeticException for arithmetic errors like division by zero
     */
    @PostMapping("/{id}/evaluate")
    public EvaluateEquationResponse evaluateEquation(
            @PathVariable Long id,
            @Valid @RequestBody EvaluateEquationRequest request) {
        
        double result = equationService.evaluateEquation(id, request.getVariables());
        return new EvaluateEquationResponse(result);
    }
} 