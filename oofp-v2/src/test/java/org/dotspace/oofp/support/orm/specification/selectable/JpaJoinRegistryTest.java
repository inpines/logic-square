package org.dotspace.oofp.support.orm.specification.selectable;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaJoinRegistryTest {

    @Mock
    private Root<TestEntity> root;

    @Mock
    private Join<Object, Object> join1;

    @Mock
    private Join<Object, Object> join2;

    @Mock
    private Function<Join<?, ?>, String> resolver;

    private JpaJoinRegistry<TestEntity> joinRegistry;

    @BeforeEach
    void setUp() {
        joinRegistry = new JpaJoinRegistry<>(root);
    }

    @Test
    @DisplayName("constructor should create instance with root")
    void constructor_WithRoot_ShouldCreateInstance() {
        // Given & When
        JpaJoinRegistry<TestEntity> registry = new JpaJoinRegistry<>(root);

        // Then
        assertNotNull(registry);
    }

    @Test
    @DisplayName("join should create inner join by default")
    void join_WithAttributeName_ShouldCreateInnerJoin() {
        // Given
        String attributeName = "relatedEntity";
        when(root.join(attributeName, JoinType.INNER)).thenReturn(join1);

        // When
        Join<TestEntity, TestRelatedEntity> result = joinRegistry.join(attributeName);

        // Then
        assertNotNull(result);
        assertEquals(join1, result);
        verify(root).join(attributeName, JoinType.INNER);
    }

    @Test
    @DisplayName("join should create join with specified join type")
    void join_WithAttributeNameAndJoinType_ShouldCreateJoinWithSpecifiedType() {
        // Given
        String attributeName = "relatedEntity";
        JoinType joinType = JoinType.LEFT;
        when(root.join(attributeName, joinType)).thenReturn(join1);

        // When
        Join<TestEntity, TestRelatedEntity> result = joinRegistry.join(attributeName, joinType);

        // Then
        assertNotNull(result);
        assertEquals(join1, result);
        verify(root).join(attributeName, joinType);
    }

    @Test
    @DisplayName("join should return same instance for same attribute name")
    void join_WithSameAttributeName_ShouldReturnSameInstance() {
        // Given
        String attributeName = "relatedEntity";
        when(root.join(attributeName, JoinType.INNER)).thenReturn(join1);

        // When
        Join<TestEntity, TestRelatedEntity> result1 = joinRegistry.join(attributeName);
        Join<TestEntity, TestRelatedEntity> result2 = joinRegistry.join(attributeName);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertSame(result1, result2);
        verify(root, times(1)).join(attributeName, JoinType.INNER);
    }

    @Test
    @DisplayName("join should create different joins for different attribute names")
    void join_WithDifferentAttributeNames_ShouldCreateDifferentJoins() {
        // Given
        String attributeName1 = "relatedEntity1";
        String attributeName2 = "relatedEntity2";
        when(root.join(attributeName1, JoinType.INNER)).thenReturn(join1);
        when(root.join(attributeName2, JoinType.INNER)).thenReturn(join2);

        // When
        Join<TestEntity, TestRelatedEntity> result1 = joinRegistry.join(attributeName1);
        Join<TestEntity, TestRelatedEntity> result2 = joinRegistry.join(attributeName2);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotSame(result1, result2);
        verify(root).join(attributeName1, JoinType.INNER);
        verify(root).join(attributeName2, JoinType.INNER);
    }

    @Test
    @DisplayName("join should handle different join types for different attributes")
    void join_WithDifferentJoinTypesForDifferentAttributes_ShouldWork() {
        // Given
        String attributeName1 = "relatedEntity1";
        String attributeName2 = "relatedEntity2";
        when(root.join(attributeName1, JoinType.INNER)).thenReturn(join1);
        when(root.join(attributeName2, JoinType.LEFT)).thenReturn(join2);

        // When
        Join<TestEntity, TestRelatedEntity> innerJoin = joinRegistry.join(attributeName1, JoinType.INNER);
        Join<TestEntity, TestRelatedEntity> leftJoin = joinRegistry.join(attributeName2, JoinType.LEFT);

        // Then
        assertNotNull(innerJoin);
        assertNotNull(leftJoin);
        assertNotSame(innerJoin, leftJoin);
        verify(root).join(attributeName1, JoinType.INNER);
        verify(root).join(attributeName2, JoinType.LEFT);
    }

    @Test
    @DisplayName("join should return cached join even when called with different join type")
    void join_WithSameAttributeNameButDifferentJoinType_ShouldReturnCachedJoin() {
        // Given
        String attributeName = "relatedEntity";
        when(root.join(attributeName, JoinType.INNER)).thenReturn(join1);

        // When
        Join<TestEntity, TestRelatedEntity> innerJoin = joinRegistry.join(attributeName, JoinType.INNER);
        Join<TestEntity, TestRelatedEntity> leftJoin = joinRegistry.join(attributeName, JoinType.LEFT);

        // Then
        assertNotNull(innerJoin);
        assertNotNull(leftJoin);
        assertSame(innerJoin, leftJoin);
        verify(root, times(1)).join(attributeName, JoinType.INNER);
        verify(root, never()).join(attributeName, JoinType.LEFT);
    }

    @Test
    @DisplayName("apply should call resolver with existing join")
    void apply_WithExistingJoin_ShouldCallResolver() {
        // Given
        String attributeName = "relatedEntity";
        String expectedResult = "resolved";
        when(root.join(attributeName, JoinType.INNER)).thenReturn(join1);
        when(resolver.apply(join1)).thenReturn(expectedResult);

        joinRegistry.join(attributeName);

        // When
        String result = joinRegistry.apply(attributeName, resolver);

        // Then
        assertEquals(expectedResult, result);
        verify(resolver).apply(join1);
    }

    @Test
    @DisplayName("apply should call resolver with null for non-existing join")
    void apply_WithNonExistingJoin_ShouldCallResolverWithNull() {
        // Given
        String attributeName = "nonExistingEntity";
        String expectedResult = "null_handled";
        when(resolver.apply(null)).thenReturn(expectedResult);

        // When
        String result = joinRegistry.apply(attributeName, resolver);

        // Then
        assertEquals(expectedResult, result);
        verify(resolver).apply(null);
    }

    @Test
    @DisplayName("apply should handle resolver returning null")
    void apply_WithResolverReturningNull_ShouldReturnNull() {
        // Given
        String attributeName = "relatedEntity";
        when(root.join(attributeName, JoinType.INNER)).thenReturn(join1);
        when(resolver.apply(join1)).thenReturn(null);

        joinRegistry.join(attributeName);

        // When
        String result = joinRegistry.apply(attributeName, resolver);

        // Then
        assertNull(result);
        verify(resolver).apply(join1);
    }

    @Test
    @DisplayName("apply should handle resolver throwing exception")
    void apply_WithResolverThrowingException_ShouldPropagateException() {
        // Given
        String attributeName = "relatedEntity";
        RuntimeException expectedException = new RuntimeException("Resolver error");
        when(root.join(attributeName, JoinType.INNER)).thenReturn(join1);
        when(resolver.apply(join1)).thenThrow(expectedException);

        joinRegistry.join(attributeName);

        // When & Then
        RuntimeException actualException = assertThrows(RuntimeException.class, () ->
                joinRegistry.apply(attributeName, resolver));
        assertEquals(expectedException, actualException);
        verify(resolver).apply(join1);
    }

    @Test
    @DisplayName("hasJoin should return true for existing join")
    void hasJoin_WithExistingJoin_ShouldReturnTrue() {
        // Given
        String attributeName = "relatedEntity";
        when(root.join(attributeName, JoinType.INNER)).thenReturn(join1);

        joinRegistry.join(attributeName);

        // When
        boolean result = joinRegistry.hasJoin(attributeName);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("hasJoin should return false for non-existing join")
    void hasJoin_WithNonExistingJoin_ShouldReturnFalse() {
        // Given
        String attributeName = "nonExistingEntity";

        // When
        boolean result = joinRegistry.hasJoin(attributeName);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("hasJoin should return false for null attribute name")
    void hasJoin_WithNullAttributeName_ShouldReturnFalse() {
        // When
        boolean result = joinRegistry.hasJoin(null);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("getAllJoins should return empty map initially")
    void getAllJoins_Initially_ShouldReturnEmptyMap() {
        // When
        Map<String, Join<Object, Object>> result = joinRegistry.getAllJoins();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAllJoins should return map with created joins")
    void getAllJoins_WithCreatedJoins_ShouldReturnMapWithJoins() {
        // Given
        String attributeName1 = "relatedEntity1";
        String attributeName2 = "relatedEntity2";
        when(root.join(attributeName1, JoinType.INNER)).thenReturn(join1);
        when(root.join(attributeName2, JoinType.LEFT)).thenReturn(join2);

        joinRegistry.join(attributeName1);
        joinRegistry.join(attributeName2, JoinType.LEFT);

        // When
        Map<String, Join<Object, Object>> result = joinRegistry.getAllJoins();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(attributeName1));
        assertTrue(result.containsKey(attributeName2));
    }

    @Test
    @DisplayName("getAllJoins should return reference to internal map")
    void getAllJoins_ShouldReturnInternalMap() {
        // Given
        String attributeName = "relatedEntity";
        when(root.join(attributeName, JoinType.INNER)).thenReturn(join1);

        joinRegistry.join(attributeName);
        Map<String, Join<Object, Object>> firstCall = joinRegistry.getAllJoins();

        // When
        Map<String, Join<Object, Object>> secondCall = joinRegistry.getAllJoins();

        // Then
        assertSame(firstCall, secondCall);
        assertEquals(1, firstCall.size());
        assertTrue(firstCall.containsKey(attributeName));
    }

    @Test
    @DisplayName("join should handle null attribute name")
    void join_WithNullAttributeName_ShouldWork() {
        // Given
        when(root.join((String) null, JoinType.INNER)).thenReturn(join1);

        // When
        Join<TestEntity, TestRelatedEntity> result = joinRegistry.join(null);

        // Then
        assertNotNull(result);
        assertEquals(join1, result);
        verify(root).join((String) null, JoinType.INNER);
    }

    @Test
    @DisplayName("join should handle null join type")
    void join_WithNullJoinType_ShouldWork() {
        // Given
        String attributeName = "relatedEntity";
        when(root.join(attributeName, null)).thenReturn(join1);

        // When
        Join<TestEntity, TestRelatedEntity> result = joinRegistry.join(attributeName, null);

        // Then
        assertNotNull(result);
        assertEquals(join1, result);
        verify(root).join(attributeName, null);
    }

    @Test
    @DisplayName("join should handle empty attribute name")
    void join_WithEmptyAttributeName_ShouldWork() {
        // Given
        String attributeName = "";
        when(root.join(attributeName, JoinType.INNER)).thenReturn(join1);

        // When
        Join<TestEntity, TestRelatedEntity> result = joinRegistry.join(attributeName);

        // Then
        assertNotNull(result);
        verify(root).join(attributeName, JoinType.INNER);
    }

    @Test
    @DisplayName("join should handle complex attribute names")
    void join_WithComplexAttributeNames_ShouldWork() {
        // Given
        String attributeName1 = "related.nested.entity";
        String attributeName2 = "anotherRelated_entity123";
        when(root.join(attributeName1, JoinType.INNER)).thenReturn(join1);
        when(root.join(attributeName2, JoinType.INNER)).thenReturn(join2);

        // When
        Join<TestEntity, TestRelatedEntity> result1 = joinRegistry.join(attributeName1);
        Join<TestEntity, TestRelatedEntity> result2 = joinRegistry.join(attributeName2);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        verify(root).join(attributeName1, JoinType.INNER);
        verify(root).join(attributeName2, JoinType.INNER);
    }

    @Test
    @DisplayName("join should work with all join types")
    void join_WithAllJoinTypes_ShouldWork() {
        // Given
        String attributeName1 = "innerJoin";
        String attributeName2 = "leftJoin";
        String attributeName3 = "rightJoin";
        when(root.join(attributeName1, JoinType.INNER)).thenReturn(join1);
        when(root.join(attributeName2, JoinType.LEFT)).thenReturn(join1);
        when(root.join(attributeName3, JoinType.RIGHT)).thenReturn(join1);

        // When
        Join<TestEntity, TestRelatedEntity> innerResult = joinRegistry.join(attributeName1, JoinType.INNER);
        Join<TestEntity, TestRelatedEntity> leftResult = joinRegistry.join(attributeName2, JoinType.LEFT);
        Join<TestEntity, TestRelatedEntity> rightResult = joinRegistry.join(attributeName3, JoinType.RIGHT);

        // Then
        assertNotNull(innerResult);
        assertNotNull(leftResult);
        assertNotNull(rightResult);
        verify(root).join(attributeName1, JoinType.INNER);
        verify(root).join(attributeName2, JoinType.LEFT);
        verify(root).join(attributeName3, JoinType.RIGHT);
    }

    @Test
    @DisplayName("multiple operations should work together")
    void multipleOperations_ShouldWorkTogether() {
        // Given
        String attributeName = "relatedEntity";
        when(root.join(attributeName, JoinType.INNER)).thenReturn(join1);
        when(resolver.apply(join1)).thenReturn("resolved");

        // When
        Join<TestEntity, TestRelatedEntity> joinResult = joinRegistry.join(attributeName);
        boolean hasJoinResult = joinRegistry.hasJoin(attributeName);
        String applyResult = joinRegistry.apply(attributeName, resolver);
        Map<String, Join<Object, Object>> allJoins = joinRegistry.getAllJoins();

        // Then
        assertNotNull(joinResult);
        assertTrue(hasJoinResult);
        assertEquals("resolved", applyResult);
        assertNotNull(allJoins);
        assertEquals(1, allJoins.size());
        assertTrue(allJoins.containsKey(attributeName));
    }

    @Test
    @DisplayName("computeIfAbsent behavior should work correctly")
    void computeIfAbsent_ShouldWorkCorrectly() {
        // Given
        String attributeName = "relatedEntity";
        when(root.join(attributeName, JoinType.INNER)).thenReturn(join1);

        // When
        Join<TestEntity, TestRelatedEntity> firstCall = joinRegistry.join(attributeName);
        Join<TestEntity, TestRelatedEntity> secondCall = joinRegistry.join(attributeName);

        // Then
        assertSame(firstCall, secondCall);
        verify(root, times(1)).join(attributeName, JoinType.INNER);
    }

    @Test
    @DisplayName("generic casting should work correctly")
    void genericCasting_ShouldWorkCorrectly() {
        // Given
        String attributeName = "relatedEntity";
        when(root.join(attributeName, JoinType.LEFT)).thenReturn(join1);

        // When
        Join<TestEntity, TestRelatedEntity> result = joinRegistry.join(attributeName, JoinType.LEFT);

        // Then
        assertNotNull(result);
        assertEquals(join1, result);
    }

    @Test
    @DisplayName("map operations should be consistent")
    void mapOperations_ShouldBeConsistent() {
        // Given
        String attributeName1 = "entity1";
        String attributeName2 = "entity2";
        when(root.join(attributeName1, JoinType.INNER)).thenReturn(join1);
        when(root.join(attributeName2, JoinType.LEFT)).thenReturn(join2);

        // When
        joinRegistry.join(attributeName1);
        joinRegistry.join(attributeName2, JoinType.LEFT);

        // Then
        assertTrue(joinRegistry.hasJoin(attributeName1));
        assertTrue(joinRegistry.hasJoin(attributeName2));
        assertFalse(joinRegistry.hasJoin("nonExistent"));

        Map<String, Join<Object, Object>> allJoins = joinRegistry.getAllJoins();
        assertEquals(2, allJoins.size());
        assertEquals(join1, allJoins.get(attributeName1));
        assertEquals(join2, allJoins.get(attributeName2));
    }

    // Test classes
    private static class TestEntity {}
    private static class TestRelatedEntity {}
}
