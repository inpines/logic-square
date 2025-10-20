package org.dotspace.oofp.utils.builder;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class GeneralBuildingWritersTest {

    @Test
    void set_WithBiConsumerAndValue_ReturnsGeneralBuildingWriter() {
        // Arrange
        TestObject obj = new TestObject();
        BiConsumer<TestObject, String> setter = TestObject::setName;
        String value = "test";

        // Act
        GeneralBuildingWriter<TestObject> writer = GeneralBuildingWriters.set(setter, value);
        writer.write(obj);

        // Assert
        assertNotNull(writer);
        assertEquals("test", obj.getName());
    }

    @Test
    void set_WithGetterSetterAndValue_ReturnsGeneralBuildingWriter() {
        // Arrange
        TestObject obj = new TestObject();
        obj.setChild(new TestChild());
        Function<TestObject, TestChild> getter = TestObject::getChild;
        BiConsumer<TestChild, String> setter = TestChild::setValue;
        String value = "child_value";

        // Act
        GeneralBuildingWriter<TestObject> writer = GeneralBuildingWriters.set(getter, setter, value);
        writer.write(obj);

        // Assert
        assertNotNull(writer);
        assertEquals("child_value", obj.getChild().getValue());
    }

    @Test
    void setForEach_WithGetterItemSelectorAndCollection_ReturnsGeneralBuildingWriter() {
        // Arrange
        TestObject obj = new TestObject();
        obj.setItems(new ArrayList<>());
        Function<TestObject, Collection<String>> getter = TestObject::getItems;
        Function<Integer, String> itemSelector = Object::toString;
        Collection<Integer> collection = Arrays.asList(1, 2, 3);

        // Act
        GeneralBuildingWriter<TestObject> writer = GeneralBuildingWriters.setForEach(getter, itemSelector, collection);
        writer.write(obj);

        // Assert
        assertNotNull(writer);
        assertEquals(3, obj.getItems().size());
        assertTrue(obj.getItems().containsAll(Arrays.asList("1", "2", "3")));
    }

    @Test
    void setForEach_WithSetterAndCollection_ReturnsGeneralBuildingWriter() {
        // Arrange
        TestObject obj = new TestObject();
        obj.setItems(new ArrayList<>());
        BiConsumer<TestObject, String> setter = (o, item) -> o.getItems().add(item);
        Collection<String> collection = Arrays.asList("a", "b", "c");

        // Act
        GeneralBuildingWriter<TestObject> writer = GeneralBuildingWriters.setForEach(setter, collection);
        writer.write(obj);

        // Assert
        assertNotNull(writer);
        assertEquals(3, obj.getItems().size());
        assertTrue(obj.getItems().containsAll(Arrays.asList("a", "b", "c")));
    }

    @Test
    void set_WithNullValue_SetsNullValue() {
        // Arrange
        TestObject obj = new TestObject();
        BiConsumer<TestObject, String> setter = TestObject::setName;

        // Act
        GeneralBuildingWriter<TestObject> writer = GeneralBuildingWriters.set(setter, null);
        writer.write(obj);

        // Assert
        assertNull(obj.getName());
    }

    @Test
    void setForEach_WithEmptyCollection_DoesNotModifyTarget() {
        // Arrange
        TestObject obj = new TestObject();
        obj.setItems(new ArrayList<>());
        BiConsumer<TestObject, String> setter = (o, item) -> o.getItems().add(item);
        Collection<String> emptyCollection = new ArrayList<>();

        // Act
        GeneralBuildingWriter<TestObject> writer = GeneralBuildingWriters.setForEach(setter, emptyCollection);
        writer.write(obj);

        // Assert
        assertTrue(obj.getItems().isEmpty());
    }

    @Test
    void filter_WithPredicateAndCondition_WritesWhenConditionMatches() {
        // Arrange
        TestObject obj = new TestObject();
        BiConsumer<TestObject, String> setter = TestObject::setName;
        Predicate<String> predicate = s -> s != null && s.length() > 3;
        String condition = "test";

        // Act
        GeneralBuildingWriter<TestObject> writer = GeneralBuildingWriters.set(setter, "value")
                .filter(predicate, condition);
        writer.write(obj);

        // Assert
        assertEquals("value", obj.getName());
    }

    @Test
    void filter_WithPredicateAndCondition_DoesNotWriteWhenConditionFails() {
        // Arrange
        TestObject obj = new TestObject();
        BiConsumer<TestObject, String> setter = TestObject::setName;
        Predicate<String> predicate = s -> s != null && s.length() > 5;
        String condition = "test";

        // Act
        GeneralBuildingWriter<TestObject> writer = GeneralBuildingWriters.set(setter, "value")
                .filter(predicate, condition);
        writer.write(obj);

        // Assert
        assertNull(obj.getName());
    }

    @Test
    void filter_WithObjectPredicate_WritesWhenPredicateMatches() {
        // Arrange
        TestObject obj = new TestObject();
        obj.setName("existing");
        BiConsumer<TestObject, String> setter = TestObject::setName;
        Predicate<TestObject> predicate = o -> o.getName() != null;

        // Act
        GeneralBuildingWriter<TestObject> writer = GeneralBuildingWriters.set(setter, "new_value")
                .filter(predicate);
        writer.write(obj);

        // Assert
        assertEquals("new_value", obj.getName());
    }

    @Test
    void when_WithPredicate_WritesWhenConditionMatches() {
        // Arrange
        TestObject obj = new TestObject();
        obj.setName("test");
        BiConsumer<TestObject, String> setter = (o, v) -> o.setName(o.getName() + v);
        Predicate<TestObject> condition = o -> o.getName().startsWith("test");

        // Act
        GeneralBuildingWriter<TestObject> writer = GeneralBuildingWriters.set(setter, "_suffix")
                .when(condition);
        writer.write(obj);

        // Assert
        assertEquals("test_suffix", obj.getName());
    }

    // Test helper classes
    @Setter
    @Getter
    static class TestObject {
        private String name;
        private TestChild child;
        private List<String> items;

    }

    @Setter
    @Getter
    static class TestChild {
        private String value;

    }
}