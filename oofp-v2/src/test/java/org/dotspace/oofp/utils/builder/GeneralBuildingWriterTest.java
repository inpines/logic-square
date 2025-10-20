package org.dotspace.oofp.utils.builder;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class GeneralBuildingWriterTest {

    @Test
    void simpleValueWriter_WithValidValue_SetsValue() {
        // Arrange
        TestObject obj = new TestObject();
        BiConsumer<TestObject, String> setter = TestObject::setName;
        String value = "test";

        // Act
        GeneralBuildingWriter.SimpleValueWriter<TestObject, String> writer = 
            GeneralBuildingWriter.SimpleValueWriter.<TestObject, String>builder()
                .withValue(value)
                .withSetter(setter)
                .build();
        writer.write(obj);

        // Assert
        assertEquals("test", obj.getName());
    }

    @Test
    void simpleValueWriter_WithNullValue_SetsNull() {
        // Arrange
        TestObject obj = new TestObject();
        BiConsumer<TestObject, String> setter = TestObject::setName;

        // Act
        GeneralBuildingWriter.SimpleValueWriter<TestObject, String> writer = 
            GeneralBuildingWriter.SimpleValueWriter.<TestObject, String>builder()
                .withValue(null)
                .withSetter(setter)
                .build();
        writer.write(obj);

        // Assert
        assertNull(obj.getName());
    }

    @Test
    void simpleValueWriter_WithPredicateFalse_DoesNotSetValue() {
        // Arrange
        TestObject obj = new TestObject();
        BiConsumer<TestObject, String> setter = TestObject::setName;
        Predicate<String> predicate = s -> false;

        // Act
        GeneralBuildingWriter.SimpleValueWriter<TestObject, String> writer = 
            GeneralBuildingWriter.SimpleValueWriter.<TestObject, String>builder()
                .withValue("test")
                .withPredicate(predicate)
                .withSetter(setter)
                .build();
        writer.write(obj);

        // Assert
        assertNull(obj.getName());
    }

    @Test
    void simpleValueWriter_WithNullInstance_DoesNothing() {
        // Arrange
        BiConsumer<TestObject, String> setter = TestObject::setName;

        // Act & Assert (no exception should be thrown)
        GeneralBuildingWriter.SimpleValueWriter<TestObject, String> writer = 
            GeneralBuildingWriter.SimpleValueWriter.<TestObject, String>builder()
                .withValue("test")
                .withSetter(setter)
                .build();
        assertDoesNotThrow(() -> writer.write(null));
    }

    @Test
    void applyValueWriter_WithValidApplier_SetsComputedValue() {
        // Arrange
        TestObject obj = new TestObject();
        obj.setAge(25);
        Function<TestObject, String> applier = o -> "Age: " + o.getAge();
        BiConsumer<TestObject, String> setter = TestObject::setName;

        // Act
        GeneralBuildingWriter.ApplyValueWriter<TestObject, String> writer = 
            GeneralBuildingWriter.ApplyValueWriter.<TestObject, String>builder()
                .withApplier(applier)
                .withSetter(setter)
                .build();
        writer.write(obj);

        // Assert
        assertEquals("Age: 25", obj.getName());
    }

    @Test
    void applyValueWriter_WithPredicateFalse_DoesNotSetValue() {
        // Arrange
        TestObject obj = new TestObject();
        obj.setAge(25);
        Function<TestObject, String> applier = o -> "Age: " + o.getAge();
        BiConsumer<TestObject, String> setter = TestObject::setName;
        Predicate<String> predicate = s -> false;

        // Act
        GeneralBuildingWriter.ApplyValueWriter<TestObject, String> writer = 
            GeneralBuildingWriter.ApplyValueWriter.<TestObject, String>builder()
                .withApplier(applier)
                .withPredicate(predicate)
                .withSetter(setter)
                .build();
        writer.write(obj);

        // Assert
        assertNull(obj.getName());
    }

    @Test
    void applyValueWriter_WithApplierReturningNull_HandlesNullValue() {
        // Arrange
        TestObject obj = new TestObject();
        Function<TestObject, String> applier = o -> null;
        BiConsumer<TestObject, String> setter = TestObject::setName;

        // Act
        GeneralBuildingWriter.ApplyValueWriter<TestObject, String> writer = 
            GeneralBuildingWriter.ApplyValueWriter.<TestObject, String>builder()
                .withApplier(applier)
                .withSetter(setter)
                .build();
        writer.write(obj);

        // Assert
        assertNull(obj.getName());
    }

    @Test
    void applyValueWriter_WithNullInstance_DoesNothing() {
        // Arrange
        Function<TestObject, String> applier = o -> "test";
        BiConsumer<TestObject, String> setter = TestObject::setName;

        // Act & Assert (no exception should be thrown)
        GeneralBuildingWriter.ApplyValueWriter<TestObject, String> writer = 
            GeneralBuildingWriter.ApplyValueWriter.<TestObject, String>builder()
                .withApplier(applier)
                .withSetter(setter)
                .build();
        assertDoesNotThrow(() -> writer.write(null));
    }

    @Test
    void simpleValueWriter_WithDefaultPredicate_AlwaysAccepts() {
        // Arrange
        TestObject obj = new TestObject();
        BiConsumer<TestObject, String> setter = TestObject::setName;

        // Act
        GeneralBuildingWriter.SimpleValueWriter<TestObject, String> writer = 
            GeneralBuildingWriter.SimpleValueWriter.<TestObject, String>builder()
                .withValue("test")
                .withSetter(setter)
                .build();
        writer.write(obj);

        // Assert
        assertEquals("test", obj.getName());
    }

    @Test
    void applyValueWriter_WithDefaultPredicate_AlwaysAccepts() {
        // Arrange
        TestObject obj = new TestObject();
        obj.setAge(30);
        Function<TestObject, Integer> applier = TestObject::getAge;
        BiConsumer<TestObject, Integer> setter = TestObject::setAge;

        // Act
        GeneralBuildingWriter.ApplyValueWriter<TestObject, Integer> writer = 
            GeneralBuildingWriter.ApplyValueWriter.<TestObject, Integer>builder()
                .withApplier(applier)
                .withSetter(setter)
                .build();
        writer.write(obj);

        // Assert
        assertEquals(30, obj.getAge());
    }

    // Test helper class
    @Setter
    @Getter
    static class TestObject {
        private String name;
        private Integer age;
    }
}