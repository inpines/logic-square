package org.dotspace.oofp.support.orm.specification;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QuerySpecificationsTest {
    @Test
    void testFindByParameters_null() {
        Specification<Object> spec = QuerySpecifications.findByParameters(null);
        assertNotNull(spec);
        assertInstanceOf(ParametersSpecification.class, spec);
    }

    @Test
    void testFindByParameters_emptyMap() {
        Specification<Object> spec = QuerySpecifications.findByParameters(Collections.emptyMap());
        assertNotNull(spec);
        assertInstanceOf(ParametersSpecification.class, spec);
    }

    @Test
    void testFindByParameters_normalMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "value");
        Specification<Object> spec = QuerySpecifications.findByParameters(map);
        assertNotNull(spec);
        assertInstanceOf(ParametersSpecification.class, spec);
    }

    @Test
    void testFindByPredicateExpressions() {
        SpecificationBuilder<Object> builder = Mockito.mock(SpecificationBuilder.class);
        Specification<Object> expected = (root, query, cb) -> null;
        Mockito.when(builder.build()).thenReturn(expected);
        Specification<Object> result = QuerySpecifications.findByPredicateExpressions(builder);
        assertSame(expected, result);
        Mockito.verify(builder).build();
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<QuerySpecifications> constructor = QuerySpecifications.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        QuerySpecifications instance = constructor.newInstance();
        assertNotNull(instance);
    }
}