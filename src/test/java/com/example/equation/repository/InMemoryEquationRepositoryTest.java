package com.example.equation.repository;

import com.example.equation.model.EquationEntity;
import com.example.equation.model.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for InMemoryEquationRepository covering CRUD operations,
 * thread safety, and edge cases.
 */
class InMemoryEquationRepositoryTest {
    
    private InMemoryEquationRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new InMemoryEquationRepository();
    }
    
    @Nested
    @DisplayName("Save Operation Tests")
    class SaveTests {
        
        @Test
        @DisplayName("Should save new equation and generate ID")
        void shouldSaveNewEquationWithGeneratedId() {
            // Given
            EquationEntity equation = new EquationEntity(null, "x+1", List.of(), "test-hash");
            
            // When
            EquationEntity saved = repository.save(equation);
            
            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getId()).isEqualTo(1L);
            assertThat(saved.getInfix()).isEqualTo("x+1");
            assertThat(saved.getPostfix()).isEmpty();
        }
        
        @Test
        @DisplayName("Should save multiple equations with sequential IDs")
        void shouldSaveMultipleEquationsWithSequentialIds() {
            // Given
            EquationEntity equation1 = new EquationEntity(null, "x+1", List.of(), "hash1");
            EquationEntity equation2 = new EquationEntity(null, "y*2", List.of(), "hash2");
            EquationEntity equation3 = new EquationEntity(null, "z^3", List.of(), "hash3");
            
            // When
            EquationEntity saved1 = repository.save(equation1);
            EquationEntity saved2 = repository.save(equation2);
            EquationEntity saved3 = repository.save(equation3);
            
            // Then
            assertThat(saved1.getId()).isEqualTo(1L);
            assertThat(saved2.getId()).isEqualTo(2L);
            assertThat(saved3.getId()).isEqualTo(3L);
        }
        
        @Test
        @DisplayName("Should update existing equation when ID is provided")
        void shouldUpdateExistingEquation() {
            // Given - save initial equation
            EquationEntity original = new EquationEntity(null, "x+1", List.of(), "original-hash");
            EquationEntity saved = repository.save(original);
            Long id = saved.getId();
            
            // When - update with same ID
            EquationEntity updated = new EquationEntity(id, "x+2", List.of(Token.VARIABLE), "updated-hash");
            EquationEntity result = repository.save(updated);
            
            // Then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getInfix()).isEqualTo("x+2");
            assertThat(result.getPostfix()).hasSize(1);
            
            // Verify the original is replaced
            Optional<EquationEntity> found = repository.findById(id);
            assertThat(found).isPresent();
            assertThat(found.get().getInfix()).isEqualTo("x+2");
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException for null equation")
        void shouldThrowExceptionForNullEquation() {
            // When & Then
            assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Equation entity cannot be null");
        }
    }
    
    @Nested
    @DisplayName("Find By ID Tests")
    class FindByIdTests {
        
        @Test
        @DisplayName("Should find existing equation by ID")
        void shouldFindExistingEquationById() {
            // Given
            EquationEntity equation = new EquationEntity(null, "a*b+c", List.of(), "test-hash");
            EquationEntity saved = repository.save(equation);
            
            // When
            Optional<EquationEntity> found = repository.findById(saved.getId());
            
            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(saved.getId());
            assertThat(found.get().getInfix()).isEqualTo("a*b+c");
        }
        
        @Test
        @DisplayName("Should return empty Optional for non-existent ID")
        void shouldReturnEmptyForNonExistentId() {
            // When
            Optional<EquationEntity> found = repository.findById(999L);
            
            // Then
            assertThat(found).isEmpty();
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException for null ID")
        void shouldThrowExceptionForNullId() {
            // When & Then
            assertThatThrownBy(() -> repository.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID cannot be null");
        }
    }
    
    @Nested
    @DisplayName("Find All Tests")
    class FindAllTests {
        
        @Test
        @DisplayName("Should return empty list when no equations exist")
        void shouldReturnEmptyListWhenNoEquations() {
            // When
            List<EquationEntity> equations = repository.findAll();
            
            // Then
            assertThat(equations).isEmpty();
        }
        
        @Test
        @DisplayName("Should return all saved equations")
        void shouldReturnAllSavedEquations() {
            // Given
            EquationEntity eq1 = repository.save(new EquationEntity(null, "x+1", List.of(), "hash1"));
            EquationEntity eq2 = repository.save(new EquationEntity(null, "y*2", List.of(), "hash2"));
            EquationEntity eq3 = repository.save(new EquationEntity(null, "z^3", List.of(), "hash3"));
            
            // When
            List<EquationEntity> equations = repository.findAll();
            
            // Then
            assertThat(equations).hasSize(3);
            assertThat(equations).extracting(EquationEntity::getId)
                .containsExactlyInAnyOrder(eq1.getId(), eq2.getId(), eq3.getId());
            assertThat(equations).extracting(EquationEntity::getInfix)
                .containsExactlyInAnyOrder("x+1", "y*2", "z^3");
        }
        
        @Test
        @DisplayName("Should return snapshot that is not affected by concurrent modifications")
        void shouldReturnSnapshotNotAffectedByConcurrentModifications() {
            // Given
            repository.save(new EquationEntity(null, "x+1", List.of(), "hash1"));
            repository.save(new EquationEntity(null, "y*2", List.of(), "hash2"));
            
            // When
            List<EquationEntity> snapshot = repository.findAll();
            
            // Then - add more equations after getting snapshot
            repository.save(new EquationEntity(null, "z^3", List.of(), "hash3"));
            
            // Verify snapshot is unchanged
            assertThat(snapshot).hasSize(2);
            assertThat(repository.findAll()).hasSize(3);
        }
    }
    
    @Nested
    @DisplayName("Utility Methods Tests")
    class UtilityMethodsTests {
        
        @Test
        @DisplayName("Should return correct size")
        void shouldReturnCorrectSize() {
            // Given
            assertThat(repository.size()).isZero();
            
            // When
            repository.save(new EquationEntity(null, "x+1", List.of(), "hash1"));
            repository.save(new EquationEntity(null, "y*2", List.of(), "hash2"));
            
            // Then
            assertThat(repository.size()).isEqualTo(2);
        }
        
        @Test
        @DisplayName("Should clear all equations")
        void shouldClearAllEquations() {
            // Given
            repository.save(new EquationEntity(null, "x+1", List.of(), "hash1"));
            repository.save(new EquationEntity(null, "y*2", List.of(), "hash2"));
            assertThat(repository.size()).isEqualTo(2);
            
            // When
            repository.clear();
            
            // Then
            assertThat(repository.size()).isZero();
            assertThat(repository.findAll()).isEmpty();
        }
    }
    
    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {
        
        @Test
        @DisplayName("Should handle concurrent saves without race conditions")
        void shouldHandleConcurrentSaves() throws InterruptedException {
            // Given
            int threadCount = 10;
            int equationsPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            
            // When - multiple threads saving equations concurrently
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < equationsPerThread; j++) {
                            String infix = String.format("thread%d_eq%d", threadId, j);
                            repository.save(new EquationEntity(null, infix, List.of(), "hash" + j));
                            successCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // Then
            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
            executor.shutdown();
            
            assertThat(successCount.get()).isEqualTo(threadCount * equationsPerThread);
            assertThat(repository.size()).isEqualTo(threadCount * equationsPerThread);
            
            // Verify all IDs are unique
            List<EquationEntity> allEquations = repository.findAll();
            List<Long> ids = allEquations.stream().map(EquationEntity::getId).toList();
            assertThat(ids).doesNotHaveDuplicates();
        }
        
        @Test
        @DisplayName("Should handle concurrent reads and writes safely")
        void shouldHandleConcurrentReadsAndWrites() throws InterruptedException {
            // Given
            int writerThreads = 5;
            int readerThreads = 10;
            int operationsPerThread = 50;
            ExecutorService executor = Executors.newFixedThreadPool(writerThreads + readerThreads);
            CountDownLatch latch = new CountDownLatch(writerThreads + readerThreads);
            
            // Pre-populate with some data
            for (int i = 0; i < 10; i++) {
                repository.save(new EquationEntity(null, "initial_" + i, List.of(), "initial-hash" + i));
            }
            
            // When - concurrent writers
            for (int i = 0; i < writerThreads; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            repository.save(new EquationEntity(null, "writer" + threadId + "_" + j, List.of(), "writer-hash" + j));
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // And concurrent readers
            for (int i = 0; i < readerThreads; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            repository.findAll();
                            repository.findById(1L);
                            // Small delay to increase chance of interleaving
                            Thread.yield();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // Then
            assertThat(latch.await(15, TimeUnit.SECONDS)).isTrue();
            executor.shutdown();
            
            // Verify final state
            assertThat(repository.size()).isEqualTo(10 + (writerThreads * operationsPerThread));
            
            // Verify all operations completed successfully (no exceptions thrown)
            List<EquationEntity> finalEquations = repository.findAll();
            assertThat(finalEquations).hasSize(10 + (writerThreads * operationsPerThread));
        }
        
        @Test
        @DisplayName("Should maintain ID uniqueness under high concurrency")
        void shouldMaintainIdUniquenessUnderHighConcurrency() throws InterruptedException {
            // Given
            int threadCount = 20;
            int equationsPerThread = 50;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch completionLatch = new CountDownLatch(threadCount);
            
            // When - all threads start simultaneously
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await(); // Wait for all threads to be ready
                        for (int j = 0; j < equationsPerThread; j++) {
                            repository.save(new EquationEntity(null, "concurrent_" + j, List.of(), "concurrent-hash" + j));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        completionLatch.countDown();
                    }
                });
            }
            
            startLatch.countDown(); // Start all threads simultaneously
            
            // Then
            assertThat(completionLatch.await(10, TimeUnit.SECONDS)).isTrue();
            executor.shutdown();
            
            List<EquationEntity> allEquations = repository.findAll();
            assertThat(allEquations).hasSize(threadCount * equationsPerThread);
            
            // Verify all IDs are unique and sequential
            List<Long> ids = allEquations.stream()
                .map(EquationEntity::getId)
                .sorted()
                .toList();
            
            assertThat(ids).doesNotHaveDuplicates();
            assertThat(ids.get(0)).isEqualTo(1L);
            assertThat(ids.get(ids.size() - 1)).isEqualTo((long) (threadCount * equationsPerThread));
        }
    }
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Should handle complete CRUD workflow")
        void shouldHandleCompleteCrudWorkflow() {
            // Create
            EquationEntity equation = new EquationEntity(null, "x^2+2*x+1", List.of(), "create-hash");
            EquationEntity saved = repository.save(equation);
            assertThat(saved.getId()).isNotNull();
            
            // Read by ID
            Optional<EquationEntity> found = repository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getInfix()).isEqualTo("x^2+2*x+1");
            
            // Update
            EquationEntity updated = new EquationEntity(saved.getId(), "x^2+3*x+2", List.of(Token.VARIABLE), "update-hash");
            EquationEntity savedUpdate = repository.save(updated);
            assertThat(savedUpdate.getId()).isEqualTo(saved.getId());
            assertThat(savedUpdate.getInfix()).isEqualTo("x^2+3*x+2");
            
            // Read all
            List<EquationEntity> all = repository.findAll();
            assertThat(all).hasSize(1);
            assertThat(all.get(0).getInfix()).isEqualTo("x^2+3*x+2");
            
            // Verify update was persisted
            Optional<EquationEntity> reFound = repository.findById(saved.getId());
            assertThat(reFound).isPresent();
            assertThat(reFound.get().getInfix()).isEqualTo("x^2+3*x+2");
        }
    }
} 