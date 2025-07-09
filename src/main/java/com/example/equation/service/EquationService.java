package com.example.equation.service;

import com.example.equation.dto.EquationDto;
import com.example.equation.dto.EquationSummaryDto;
import com.example.equation.model.EquationEntity;
import com.example.equation.model.Node;
import com.example.equation.model.Token;
import com.example.equation.model.TokenValue;
import com.example.equation.repository.EquationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for equation operations.
 * Provides high-level business logic for storing, retrieving, and evaluating equations.
 * Coordinates between parser, repository, and evaluation services.
 */
@Service
public class EquationService {
    
    private final EquationRepository equationRepository;
    private final ParserService parserService;
    private final EvaluationService evaluationService;
    
    /**
     * Constructor injection of dependencies.
     * 
     * @param equationRepository repository for equation persistence
     * @param parserService service for parsing mathematical expressions
     * @param evaluationService service for evaluating expression trees
     */
    public EquationService(EquationRepository equationRepository, 
                          ParserService parserService, 
                          EvaluationService evaluationService) {
        this.equationRepository = equationRepository;
        this.parserService = parserService;
        this.evaluationService = evaluationService;
    }
    
    /**
     * Stores a mathematical expression by parsing it and generating an AST hash.
     * If a mathematically equivalent equation already exists, returns the existing equation's ID.
     * Uses AST structural hashing to detect mathematical equivalence regardless of:
     * - Whitespace differences
     * - Redundant parentheses
     * 
     * <p>Process:</p>
     * <ol>
     *   <li>Parse infix expression to build AST</li>
     *   <li>Generate AST hash from tree structure</li>
     *   <li>Check if equation with same AST hash exists</li>
     *   <li>If exists, return existing ID</li>
     *   <li>If not, tokenize and convert to postfix</li>
     *   <li>Create and save EquationEntity with AST hash</li>
     * </ol>
     * 
     * @param infix the mathematical expression in infix notation
     * @return the unique ID of the stored equation (existing or newly created)
     * @throws IllegalArgumentException if infix is null or empty
     * @throws com.example.equation.exception.EquationSyntaxException if parsing fails
     */
    public Long storeEquation(String infix) {
        if (infix == null || infix.trim().isEmpty()) {
            throw new IllegalArgumentException("Infix expression cannot be null or empty");
        }
        
        String trimmedInfix = infix.trim();
        
        // Parse to build AST and generate hash for duplicate detection
        Node expressionTree = parserService.parseExpression(trimmedInfix);
        String astHash = expressionTree.generateHash();
        
        // Check if mathematically equivalent equation already exists
        Optional<EquationEntity> existingEquation = equationRepository.findByAstHash(astHash);
        if (existingEquation.isPresent()) {
            return existingEquation.get().getId();
        }
        
        // Parse and convert to postfix for storage
        List<TokenValue> infixTokens = parserService.tokenize(trimmedInfix);
        List<TokenValue> postfixTokens = parserService.infixToPostfix(infixTokens);
        
        // Extract token types for storage (without values)
        List<Token> postfixTypes = postfixTokens.stream()
            .map(TokenValue::getType)
            .toList();
        
        // Create and save entity with AST hash
        EquationEntity equation = new EquationEntity(null, trimmedInfix, postfixTypes, astHash);
        EquationEntity savedEquation = equationRepository.save(equation);
        
        return savedEquation.getId();
    }
    
    /**
     * Retrieves all stored equations as DTOs.
     * 
     * @return list of equation DTOs, empty list if no equations exist
     */
    public List<EquationDto> getAllEquations() {
        List<EquationEntity> entities = equationRepository.findAll();
        
        return entities.stream()
            .map(this::convertToDto)
            .toList();
    }
    
    /**
     * Retrieves all stored equations as simplified summary DTOs.
     * Contains only ID and infix expression for efficient listing.
     * 
     * @return list of equation summary DTOs, empty list if no equations exist
     */
    public List<EquationSummaryDto> getAllEquationSummaries() {
        List<EquationEntity> entities = equationRepository.findAll();
        
        return entities.stream()
            .map(this::convertToSummaryDto)
            .toList();
    }
    
    /**
     * Evaluates a stored equation with the provided variable values.
     * 
     * <p>Process:</p>
     * <ol>
     *   <li>Fetch equation by ID</li>
     *   <li>Parse the infix expression to build expression tree</li>
     *   <li>Evaluate the tree with provided variables</li>
     * </ol>
     * 
     * @param id the unique ID of the equation to evaluate
     * @param variables map of variable names to their values
     * @return the computed result of the equation
     * @throws IllegalArgumentException if id is null or variables map is null
     * @throws IllegalStateException if equation with given ID is not found
     * @throws com.example.equation.exception.VariableNotProvidedException if required variables are missing
     * @throws ArithmeticException for arithmetic errors like division by zero
     */
    public double evaluateEquation(Long id, Map<String, Double> variables) {
        if (id == null) {
            throw new IllegalArgumentException("Equation ID cannot be null");
        }
        if (variables == null) {
            throw new IllegalArgumentException("Variables map cannot be null");
        }
        
        // Fetch equation from repository
        Optional<EquationEntity> equationOpt = equationRepository.findById(id);
        if (equationOpt.isEmpty()) {
            throw new IllegalStateException("Equation with ID " + id + " not found");
        }
        
        EquationEntity equation = equationOpt.get();
        
        // Parse infix to build expression tree
        Node expressionTree = parserService.parseExpression(equation.getInfix());
        
        // Evaluate with provided variables
        return evaluationService.evaluate(expressionTree, variables);
    }
    
    /**
     * Converts an EquationEntity to an EquationDto.
     * 
     * @param entity the entity to convert
     * @return the corresponding DTO
     */
    private EquationDto convertToDto(EquationEntity entity) {
        return new EquationDto(
            entity.getId(),
            entity.getInfix(),
            entity.getPostfix()
        );
    }
    
    /**
     * Converts an EquationEntity to an EquationSummaryDto.
     * 
     * @param entity the entity to convert
     * @return the corresponding summary DTO
     */
    private EquationSummaryDto convertToSummaryDto(EquationEntity entity) {
        return new EquationSummaryDto(
            entity.getId(),
            entity.getInfix()
        );
    }
    

} 