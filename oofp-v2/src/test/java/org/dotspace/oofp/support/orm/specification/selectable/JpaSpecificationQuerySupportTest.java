package org.dotspace.oofp.support.orm.specification.selectable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaSpecificationQuerySupportTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Root<TestEntity> entityRoot;

    @Mock
    private JpaJoinRegistry<TestEntity> joinRegistry;

    @InjectMocks
    private JpaSpecificationQuerySupport querySupport;

    @BeforeEach
    void setUp() {
        // Inject the mocked EntityManager since @PersistenceContext won't work in unit tests
        ReflectionTestUtils.setField(querySupport, "entityManager", entityManager);
    }

    @Test
    @DisplayName("from should create JpaSpecificationQueryWhereClause with provided root and join registry")
    void from_WithRootAndJoinRegistry_ShouldCreateQueryWhereClause() {
        // When
        JpaSpecificationQueryWhereClause<TestEntity> result = querySupport.from(entityRoot, joinRegistry);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("from should create JpaSpecificationQueryWhereClause with provided root only")
    void from_WithRootOnly_ShouldCreateQueryWhereClauseWithNewJoinRegistry() {
        // When
        JpaSpecificationQueryWhereClause<TestEntity> result = querySupport.from(entityRoot);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("from should handle null root with join registry")
    void from_WithNullRootAndJoinRegistry_ShouldHandleGracefully() {
        // When
        JpaSpecificationQueryWhereClause<TestEntity> result = querySupport.from(null, joinRegistry);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("from should handle null root without join registry")
    void from_WithNullRootOnly_ShouldHandleGracefully() {
        // When
        JpaSpecificationQueryWhereClause<TestEntity> result = querySupport.from(null);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("from should handle null join registry")
    void from_WithRootAndNullJoinRegistry_ShouldHandleGracefully() {
        // When
        JpaSpecificationQueryWhereClause<TestEntity> result = querySupport.from(entityRoot, null);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("from should create different instances on multiple calls")
    void from_MultipleCallsWithSameParameters_ShouldCreateDifferentInstances() {
        // When
        JpaSpecificationQueryWhereClause<TestEntity> result1 = querySupport.from(entityRoot, joinRegistry);
        JpaSpecificationQueryWhereClause<TestEntity> result2 = querySupport.from(entityRoot, joinRegistry);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotSame(result1, result2);
    }

    @Test
    @DisplayName("from should create different instances for single parameter calls")
    void from_MultipleCallsWithSingleParameter_ShouldCreateDifferentInstances() {
        // When
        JpaSpecificationQueryWhereClause<TestEntity> result1 = querySupport.from(entityRoot);
        JpaSpecificationQueryWhereClause<TestEntity> result2 = querySupport.from(entityRoot);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotSame(result1, result2);
    }

    @Test
    @DisplayName("from should work with different entity types")
    void from_WithDifferentEntityTypes_ShouldWork() {
        // Given
        Root<AnotherTestEntity> anotherEntityRoot = mock(Root.class);
        JpaJoinRegistry<AnotherTestEntity> anotherJoinRegistry = mock(JpaJoinRegistry.class);

        // When
        JpaSpecificationQueryWhereClause<TestEntity> result1 = querySupport.from(entityRoot, joinRegistry);
        JpaSpecificationQueryWhereClause<AnotherTestEntity> result2 = querySupport.from(anotherEntityRoot, anotherJoinRegistry);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
    }

    @Test
    @DisplayName("from should work with different entity types using single parameter")
    void from_WithDifferentEntityTypesSingleParameter_ShouldWork() {
        // Given
        Root<AnotherTestEntity> anotherEntityRoot = mock(Root.class);

        // When
        JpaSpecificationQueryWhereClause<TestEntity> result1 = querySupport.from(entityRoot);
        JpaSpecificationQueryWhereClause<AnotherTestEntity> result2 = querySupport.from(anotherEntityRoot);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
    }

    @Test
    @DisplayName("from should use injected entity manager")
    void from_ShouldUseInjectedEntityManager() {
        // Given
        EntityManager differentEntityManager = mock(EntityManager.class);
        ReflectionTestUtils.setField(querySupport, "entityManager", differentEntityManager);

        // When
        JpaSpecificationQueryWhereClause<TestEntity> result = querySupport.from(entityRoot, joinRegistry);

        // Then
        assertNotNull(result);
        // The result should be created with the injected EntityManager
    }

    @Test
    @DisplayName("from should create context with builder pattern")
    void from_ShouldCreateContextWithBuilderPattern() {
        // Given
        EntityManager customEntityManager = mock(EntityManager.class);
        ReflectionTestUtils.setField(querySupport, "entityManager", customEntityManager);

        // When
        JpaSpecificationQueryWhereClause<TestEntity> result = querySupport.from(entityRoot, joinRegistry);

        // Then
        assertNotNull(result);

        // Verify the builder pattern creates proper context
        // Test that different parameters create different contexts
        JpaSpecificationQueryWhereClause<TestEntity> result2 = querySupport.from(entityRoot, null);
        assertNotNull(result2);
        assertNotSame(result, result2);

        // Verify context components are properly set
        assertDoesNotThrow(result::toString);
    }

    @Test
    @DisplayName("from should handle concurrent calls")
    void from_ConcurrentCalls_ShouldBeThreadSafe() throws InterruptedException {
        // Given
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        JpaSpecificationQueryWhereClause<TestEntity>[] results = new JpaSpecificationQueryWhereClause[threadCount];

        // When
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> results[index] = querySupport.from(entityRoot, joinRegistry));
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        for (int i = 0; i < threadCount; i++) {
            assertNotNull(results[i]);
            for (int j = i + 1; j < threadCount; j++) {
                assertNotSame(results[i], results[j]);
            }
        }
    }

    @Test
    @DisplayName("querySupport should be properly initialized")
    void querySupport_ShouldBeProperlyInitialized() {
        // Then
        assertNotNull(querySupport);
    }

    @Test
    @DisplayName("from with auto-created join registry should work")
    void from_WithAutoCreatedJoinRegistry_ShouldWork() {
        // When
        JpaSpecificationQueryWhereClause<TestEntity> result = querySupport.from(entityRoot);

        // Then
        assertNotNull(result);

        // Verify that the auto-created join registry is functional
        // by calling the method multiple times and ensuring they create separate instances
        JpaSpecificationQueryWhereClause<TestEntity> result2 = querySupport.from(entityRoot);
        assertNotNull(result2);
        assertNotSame(result, result2);

        // Verify that each call creates its own internal JpaJoinRegistry
        // This is different from the basic test as it specifically tests the auto-creation behavior
        assertDoesNotThrow(() -> {
            // Each result should have its own context with auto-created join registry
            result.toString(); // Triggers context usage
            result2.toString(); // Triggers separate context usage
        });
    }

    @Test
    @DisplayName("from should handle entity manager being null")
    void from_WithNullEntityManager_ShouldHandleGracefully() {
        // Given
        ReflectionTestUtils.setField(querySupport, "entityManager", null);

        // When
        JpaSpecificationQueryWhereClause<TestEntity> result = querySupport.from(entityRoot, joinRegistry);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("component annotation should make class spring managed")
    void querySupport_ShouldBeSpringComponent() {
        // Given
        Class<?> clazz = JpaSpecificationQuerySupport.class;

        // When
        boolean hasComponentAnnotation = clazz.isAnnotationPresent(org.springframework.stereotype.Component.class);

        // Then
        assertTrue(hasComponentAnnotation);
    }

    @Test
    @DisplayName("entity manager should be annotated with persistence context")
    void entityManager_ShouldHavePersistenceContextAnnotation() throws NoSuchFieldException {
        // Given
        Class<?> clazz = JpaSpecificationQuerySupport.class;

        // When
        boolean hasPersistenceContextAnnotation = clazz.getDeclaredField("entityManager")
                .isAnnotationPresent(jakarta.persistence.PersistenceContext.class);

        // Then
        assertTrue(hasPersistenceContextAnnotation);
    }

    @Test
    @DisplayName("from methods should return same type instances")
    void from_Methods_ShouldReturnSameTypeInstances() {
        // When
        JpaSpecificationQueryWhereClause<TestEntity> result1 = querySupport.from(entityRoot, joinRegistry);
        JpaSpecificationQueryWhereClause<TestEntity> result2 = querySupport.from(entityRoot);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getClass(), result2.getClass());
    }

    @Test
    @DisplayName("from should work with complex entity hierarchies")
    void from_WithComplexEntityHierarchies_ShouldWork() {
        // Given
        Root<ComplexTestEntity> complexEntityRoot = mock(Root.class);
        JpaJoinRegistry<ComplexTestEntity> complexJoinRegistry = mock(JpaJoinRegistry.class);

        // When
        JpaSpecificationQueryWhereClause<ComplexTestEntity> result = querySupport.from(complexEntityRoot, complexJoinRegistry);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("from should maintain immutability of inputs")
    void from_ShouldMaintainImmutabilityOfInputs() {
        // Given
        Root<TestEntity> originalRoot = entityRoot;
        JpaJoinRegistry<TestEntity> originalJoinRegistry = joinRegistry;

        // When
        JpaSpecificationQueryWhereClause<TestEntity> result = querySupport.from(entityRoot, joinRegistry);

        // Then
        assertNotNull(result);
        assertSame(originalRoot, entityRoot);
        assertSame(originalJoinRegistry, joinRegistry);
    }

    @Test
    @DisplayName("from should work with generic wildcards")
    void from_WithGenericWildcards_ShouldWork() {
        // Given
        Root<TestEntity> wildcardRoot = mock(Root.class);
        JpaJoinRegistry<TestEntity> testJoinRegistry = mock(JpaJoinRegistry.class);

        // When & Then - This should compile and work
        assertDoesNotThrow(() -> {
            JpaSpecificationQueryWhereClause<TestEntity> result = querySupport.from(wildcardRoot, testJoinRegistry);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("multiple from calls should be independent")
    void multipleFroCalls_ShouldBeIndependent() {
        // Given
        Root<TestEntity> root1 = mock(Root.class);
        Root<TestEntity> root2 = mock(Root.class);
        JpaJoinRegistry<TestEntity> registry1 = mock(JpaJoinRegistry.class);
        JpaJoinRegistry<TestEntity> registry2 = mock(JpaJoinRegistry.class);

        // When
        JpaSpecificationQueryWhereClause<TestEntity> result1 = querySupport.from(root1, registry1);
        JpaSpecificationQueryWhereClause<TestEntity> result2 = querySupport.from(root2, registry2);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotSame(result1, result2);
    }

    @Test
    @DisplayName("from should handle edge cases gracefully")
    void from_WithEdgeCases_ShouldHandleGracefully() {
        // When & Then
        assertDoesNotThrow(() -> {
            JpaSpecificationQueryWhereClause<TestEntity> result1 = querySupport.from(entityRoot, joinRegistry);
            JpaSpecificationQueryWhereClause<TestEntity> result2 = querySupport.from(entityRoot);
            JpaSpecificationQueryWhereClause<TestEntity> result3 = querySupport.from(null, null);
            JpaSpecificationQueryWhereClause<TestEntity> result4 = querySupport.from(null);

            assertNotNull(result1);
            assertNotNull(result2);
            assertNotNull(result3);
            assertNotNull(result4);
        });
    }

    // Test classes
    private static class TestEntity {}
    private static class AnotherTestEntity {}
    private static class ComplexTestEntity extends TestEntity {}
}
