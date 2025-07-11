package com.example.equation.controller;

import com.example.equation.dto.*;
import com.example.equation.service.EquationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/equations")
public class EquationController {
    
    private final EquationService equationService;
    
    
    public EquationController(EquationService equationService) {
        this.equationService = equationService;
    }
    
    
    @PostMapping("/store")
    @ResponseStatus(HttpStatus.CREATED)
    public StoreEquationResponse storeEquation(@Valid @RequestBody StoreEquationRequest request) {
        Long equationId = equationService.storeEquation(request.getEquation());
        return new StoreEquationResponse(equationId);
    }
    
    
    @GetMapping
    public List<EquationSummaryDto> getAllEquations() {
        return equationService.getAllEquationSummaries();
    }
    
    
    @PostMapping("/{id}/evaluate")
    public EvaluateEquationResponse evaluateEquation(
            @PathVariable Long id,
            @Valid @RequestBody EvaluateEquationRequest request) {
        
        double result = equationService.evaluateEquation(id, request.getVariables());
        return new EvaluateEquationResponse(result);
    }
} 