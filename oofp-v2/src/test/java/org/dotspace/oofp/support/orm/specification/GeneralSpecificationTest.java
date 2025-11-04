package org.dotspace.oofp.support.orm.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GeneralSpecificationTest {

    private CriteriaBuilder cb;
    private CriteriaQuery<?> query;
    private Root<Object> root;

    @BeforeEach
    void setUp() {
        cb = mock(CriteriaBuilder.class);
        query = mock(CriteriaQuery.class);
        root = mock(Root.class);
    }

    @Test
    void testToPredicate_WithMultiplePredicatesAndOrders() {
        // predicates
        Predicate p1 = mock(Predicate.class);
        Predicate p2 = mock(Predicate.class);

        CriteriaPredicateExpression<Object> expr1 = mock(CriteriaPredicateExpression.class);
        CriteriaPredicateExpression<Object> expr2 = mock(CriteriaPredicateExpression.class);
        when(expr1.getPredicate(cb, root, "f1")).thenReturn(Optional.of(p1));
        when(expr2.getPredicate(cb, root, "f2")).thenReturn(Optional.of(p2));

        Map<String, CriteriaPredicateExpression<Object>> predicates = Map.of("f1", expr1, "f2", expr2);

        // paths & orders
        Path<Object> path1 = mock(Path.class);
        Path<Object> path2 = mock(Path.class);
        when(root.get("f1")).thenReturn(path1);
        when(root.get("f2")).thenReturn(path2);

        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);

        CriteriaOrder o1 = mock(CriteriaOrder.class);
        CriteriaOrder o2 = mock(CriteriaOrder.class);
        when(o1.getName()).thenReturn("f1");
        when(o1.getType()).thenReturn("ascending");
        when(o2.getName()).thenReturn("f2");
        when(o2.getType()).thenReturn("descending");

        when(cb.asc(path1)).thenReturn(order1);
        when(cb.desc(path2)).thenReturn(order2);

        List<CriteriaOrder> orders = List.of(o1, o2);

        // ensure cb.and returns a non-null predicate
        Predicate combined = mock(Predicate.class);
        when(cb.and(any(Predicate[].class))).thenReturn(combined);

        GeneralSpecification<Object> spec = new GeneralSpecification<>(predicates, orders);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        assertSame(combined, result);

        // verify cb.and received both predicates (order not important for array content here)
        ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
        verify(cb).and(captor.capture());
        Predicate[] passed = captor.getValue();
        assertEquals(2, passed.length);
        assertTrue(Arrays.asList(passed).containsAll(List.of(p1, p2)));

        // verify orderBy called with the resolved orders in the same sequence as orders list
        ArgumentCaptor<List<Order>> orderCaptor = ArgumentCaptor.forClass(List.class);
        verify(query).orderBy(orderCaptor.capture());
        List<Order> passedOrders = orderCaptor.getValue();
        assertEquals(List.of(order1, order2), passedOrders);
    }

    @Test
    void testToPredicate_WithEmptyPredicatesAndOrders() {
        // cb.and returns a predicate even when no criteria
        Predicate combined = mock(Predicate.class);
        when(cb.and(any(Predicate[].class))).thenReturn(combined);

        GeneralSpecification<Object> spec = new GeneralSpecification<>(Map.of(), List.of());

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        assertSame(combined, result);

        ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
        verify(cb).and(captor.capture());
        assertEquals(0, captor.getValue().length);

        // empty orders => orderBy called with empty list
        ArgumentCaptor<List<Order>> orderCaptor = ArgumentCaptor.forClass(List.class);
        verify(query).orderBy(orderCaptor.capture());
        assertTrue(orderCaptor.getValue().isEmpty());
    }

    @Test
    void testToPredicate_IgnoresInvalidOrderTypesAndNullAscDesc() {
        // one valid predicate
        Predicate p = mock(Predicate.class);
        CriteriaPredicateExpression<Object> expr = mock(CriteriaPredicateExpression.class);
        when(expr.getPredicate(cb, root, "f1")).thenReturn(Optional.of(p));
        Map<String, CriteriaPredicateExpression<Object>> predicates = Map.of("f1", expr);

        // order declared ascending but cb.asc returns null -> should be ignored
        Path<Object> path1 = mock(Path.class);
        when(root.get("f1")).thenReturn(path1);
        when(cb.asc(path1)).thenReturn(null); // simulate CriteriaBuilder returning null

        CriteriaOrder o = mock(CriteriaOrder.class);
        when(o.getName()).thenReturn("f1");
        when(o.getType()).thenReturn("ascending");

        when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        GeneralSpecification<Object> spec = new GeneralSpecification<>(predicates, List.of(o));

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);

        // because cb.asc returned null the order list should be empty
        ArgumentCaptor<List<Order>> orderCaptor = ArgumentCaptor.forClass(List.class);
        verify(query).orderBy(orderCaptor.capture());
        assertTrue(orderCaptor.getValue().isEmpty());
    }

    @Test
    void testToPredicate_IgnoresPredicateExpressionsThatReturnEmpty() {
        // predicate expression returns Optional.empty -> ignored
        CriteriaPredicateExpression<Object> expr = mock(CriteriaPredicateExpression.class);
        when(expr.getPredicate(cb, root, "f1")).thenReturn(Optional.empty());
        Map<String, CriteriaPredicateExpression<Object>> predicates = Map.of("f1", expr);

        when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        GeneralSpecification<Object> spec = new GeneralSpecification<>(predicates, List.of());

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);

        ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
        verify(cb).and(captor.capture());
        assertEquals(0, captor.getValue().length);
    }
}
