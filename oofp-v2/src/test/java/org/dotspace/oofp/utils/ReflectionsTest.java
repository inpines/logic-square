package org.dotspace.oofp.utils;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

class ReflectionsTest {

    static class NoDefaultConstructorClass {
        public NoDefaultConstructorClass(String required) {}
    }

    @Test
    void testGetFields() {
        List<Field> fields = Reflections.getFields(ReflectionsTestClass.class);
        
        assertNotNull(fields);
        assertFalse(fields.isEmpty());
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("privateField")));
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("publicField")));
    }

    @Test
    void testGetFieldsWithNullClass() {
        assertThrows(IllegalArgumentException.class, () -> {
            Reflections.getFields(null);
        });
    }

    @Test
    void testConstructWithDefaultConstructor() {
        ReflectionsTestClass result = Reflections.construct(ReflectionsTestClass.class);
        
        assertNotNull(result);
        assertInstanceOf(ReflectionsTestClass.class, result);
    }

    @Test
    void testConstructWithNoDefaultConstructor() {
        NoDefaultConstructorClass result = Reflections.construct(NoDefaultConstructorClass.class);
        
        assertNull(result);
    }

    @Test
    void testConstructWithNullClass() {
        Object result = Reflections.construct(null);
        
        assertNull(result);
    }

    @Test
    void testConstructWithExceptionDuringConstruction() {
        // Test with a class that throws exception in constructor
        class ExceptionClass {
            public ExceptionClass() {
                throw new RuntimeException("Constructor exception");
            }
        }
        
        ExceptionClass result = Reflections.construct(ExceptionClass.class);
        assertNull(result);
    }

    @Test
    void testGetField() {
        Field field = Reflections.getField(ReflectionsTestClass.class, "privateField");
        
        assertNotNull(field);
        assertEquals("privateField", field.getName());
    }

    @Test
    void testGetFieldNotFound() {
        Field field = Reflections.getField(ReflectionsTestClass.class, "nonExistentField");
        
        assertNull(field);
    }

    @Test
    void testGetFieldWithNullClass() {
        assertThrows(IllegalArgumentException.class, () -> Reflections.getField(null, "someField"));
    }

    @Test
    void testGetFieldWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> Reflections.getField(ReflectionsTestClass.class, null));
    }

    @Test
    void testInvokeMethod() {
        ReflectionsTestClass testObj = new ReflectionsTestClass();
        Object[] args = {"Hello", 123};
        
        Object result = Reflections.invoke(testObj, "testMethod", args);
        
        assertEquals("Hello123", result);
    }

    @Test
    void testInvokeVoidMethod() {
        ReflectionsTestClass testObj = new ReflectionsTestClass();
        Object[] args = {};
        
        Object result = Reflections.invoke(testObj, "voidMethod", args);
        
        assertNull(result);
    }

    @Test
    void testInvokeNonExistentMethod() {
        ReflectionsTestClass testObj = new ReflectionsTestClass();
        Object[] args = {"test"};
        
        Object result = Reflections.invoke(testObj, "nonExistentMethod", args);
        
        assertNull(result);
    }

    @Test
    void testInvokeWithNullObject() {
        Object[] args = {"test"};
        
        assertThrows(NullPointerException.class, () -> {
            Reflections.invoke(null, "someMethod", args);
        });
    }

    @Test
    void testInvokeWithNullMethodName() {
        ReflectionsTestClass testObj = new ReflectionsTestClass();
        Object[] args = {"test"};
        
        Object result = Reflections.invoke(testObj, null, args);
        
        assertNull(result);
    }

    @Test
    void testInvokeWithNullArgs() {
        ReflectionsTestClass testObj = new ReflectionsTestClass();
        
        assertThrows(NullPointerException.class, () -> {
            Reflections.invoke(testObj, "testMethod", null);
        });
    }

    @Test
    void testInvokeWithWrongParameterTypes() {
        ReflectionsTestClass testObj = new ReflectionsTestClass();
        Object[] args = {123, "wrong"}; // Wrong parameter types
        
        Object result = Reflections.invoke(testObj, "testMethod", args);
        
        assertNull(result);
    }

    @Test
    void testInvokeWithException() {
        ReflectionsTestClass testObj = new ReflectionsTestClass();
        Object[] args = {"test"};
        
        // Mock ReflectionUtils to throw exception
        try (MockedStatic<ReflectionUtils> mockedReflectionUtils = mockStatic(ReflectionUtils.class)) {
            mockedReflectionUtils.when(() -> ReflectionUtils.invokeMethod(any(), any(), any()))
                    .thenThrow(new RuntimeException("Invocation exception"));
            
            Object result = Reflections.invoke(testObj, "testMethod", args);
            
            assertNull(result);
        }
    }

    @Test
    void testPrivateConstructor() {
        // Test that the private constructor exists and works
        Constructor<?>[] constructors = Reflections.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        
        Constructor<?> constructor = constructors[0];
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        
        // Test instantiation through reflection
        assertDoesNotThrow(() -> {
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    void testGetFieldsWithMockedReflectionUtils() {
        try (MockedStatic<ReflectionUtils> mockedReflectionUtils = mockStatic(ReflectionUtils.class)) {
            mockedReflectionUtils.when(() -> ReflectionUtils.doWithFields(eq(ReflectionsTestClass.class), any()))
                    .thenAnswer(invocation -> {
                        // Simulate adding fields to the list
                        return null;
                    });
            
            List<Field> fields = Reflections.getFields(ReflectionsTestClass.class);
            
            assertNotNull(fields);
            assertTrue(fields.isEmpty()); // Should be empty since we mocked it to not add anything
        }
    }
}