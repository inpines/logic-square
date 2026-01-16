package org.dotspace.oofp.utils.oofp.builder;

import org.dotspace.oofp.utils.builder.operation.WriteOperation;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class WriteOperationTest {

    @Test
    void simpleValueWriter_WithValidValue_SetsValue() {
        // Arrange
        TestObject obj = new TestObject();
        BiConsumer<TestObject, String> setter = TestObject::setName;
        String value = "test";

        // Act
        WriteOperation.SimpleValueWriter<TestObject, String> writer =
            WriteOperation.SimpleValueWriter.<TestObject, String>builder()
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
        WriteOperation.SimpleValueWriter<TestObject, String> writer = 
            WriteOperation.SimpleValueWriter.<TestObject, String>builder()
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
        WriteOperation.SimpleValueWriter<TestObject, String> writer = 
            WriteOperation.SimpleValueWriter.<TestObject, String>builder()
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
        WriteOperation.SimpleValueWriter<TestObject, String> writer = 
            WriteOperation.SimpleValueWriter.<TestObject, String>builder()
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
        WriteOperation.ApplyValueWriter<TestObject, String> writer = 
            WriteOperation.ApplyValueWriter.<TestObject, String>builder()
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
        WriteOperation.ApplyValueWriter<TestObject, String> writer = 
            WriteOperation.ApplyValueWriter.<TestObject, String>builder()
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
        WriteOperation.ApplyValueWriter<TestObject, String> writer = 
            WriteOperation.ApplyValueWriter.<TestObject, String>builder()
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
        WriteOperation.ApplyValueWriter<TestObject, String> writer = 
            WriteOperation.ApplyValueWriter.<TestObject, String>builder()
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
        WriteOperation.SimpleValueWriter<TestObject, String> writer = 
            WriteOperation.SimpleValueWriter.<TestObject, String>builder()
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
        WriteOperation.ApplyValueWriter<TestObject, Integer> writer = 
            WriteOperation.ApplyValueWriter.<TestObject, Integer>builder()
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

    // 測試專用簡單資料類
    static class Person {
        String name;
        String displayName;
        Integer age;
        final List<String> tags = new ArrayList<>();

        Person(String name, Integer age) {
            this.name = name;
            this.age = age;
        }
    }

    // 建立一個可觀察呼叫順序的 writer
    private static <T> WriteOperation<T> recordingWriter(List<String> calls) {
        return instance -> calls.add("write");
    }

    // ===== 測試區塊：預設方法 peekBefore / peekAfter =====
    @Nested
    class PeekMethods {

        @Test
        @DisplayName("peekBefore：observer 應先於 write 被呼叫（順序：observer -> write）")
        void peekBefore_shouldInvokeObserverThenWrite() {
            List<String> calls = new ArrayList<>();
            WriteOperation<Person> base = recordingWriter(calls);
            Consumer<Person> observer = p -> calls.add("peek");

            WriteOperation<Person> decorated = base.peekBefore(observer);

            decorated.write(new Person("A", 18));
            assertEquals(List.of("peek", "write"), calls);
        }

        @Test
        @DisplayName("peekAfter：observer 應後於 write 被呼叫（順序：write -> observer）")
        void peekAfter_shouldInvokeWriteThenObserver() {
            List<String> calls = new ArrayList<>();
            WriteOperation<Person> base = recordingWriter(calls);
            Consumer<Person> observer = p -> calls.add("peek");

            WriteOperation<Person> decorated = base.peekAfter(observer);

            decorated.write(new Person("A", 18));
            assertEquals(List.of("write", "peek"), calls);
        }

        @Test
        @DisplayName("peekBefore 傳入 null 會丟出 NullPointerException（對應 @NonNull）")
        void peekBefore_nullObserver_shouldThrowNPE() {
            WriteOperation<Person> base = p -> {};
            assertThrows(NullPointerException.class, () -> base.peekBefore(null));
        }

        @Test
        @DisplayName("peekAfter 傳入 null 會丟出 NullPointerException（對應 @NonNull）")
        void peekAfter_nullObserver_shouldThrowNPE() {
            WriteOperation<Person> base = p -> {};
            assertThrows(NullPointerException.class, () -> base.peekAfter(null));
        }
    }

    // ===== 測試區塊：fromFunction =====
    @Nested
    class FromFunctionFactory {

        @Test
        @DisplayName("fromFunction：以 getter(instance) 的結果呼叫 setter(instance, value)")
        void fromFunction_shouldApplyGetterThenSetViaSetter() {
            Person p = new Person("alice", 20);

            // getter：依據 name 產生 displayName 值
            Function<Person, String> getter = person -> person.name == null ? null : person.name.toUpperCase(Locale.ROOT);

            // setter：把上方結果寫入 displayName
            BiConsumer<Person, String> setter = (person, val) -> person.displayName = val;

            WriteOperation<Person> writer = WriteOperation.from(setter, getter);

            writer.write(p);
            assertEquals("ALICE", p.displayName);
        }

        @Test
        @DisplayName("fromFunction：參數為 null 時拋 NPE（對應 @NonNull）")
        void fromFunction_nulls_shouldThrowNPE() {
            Function<Person, String> getter = person -> "X";
            BiConsumer<Person, String> setter = (person, v) -> {};
            assertThrows(NullPointerException.class, () -> WriteOperation.from(null, getter));
            assertThrows(NullPointerException.class, () -> WriteOperation.from(setter, null));
        }
    }

    // ===== 測試區塊：when(predicate, setter, value) =====
    @Nested
    class WhenWithSetterAndValue {

        @Test
        @DisplayName("predicate=true 時，應呼叫 setter(instance, value)")
        void when_predicateTrue_shouldSetValue() {
            Person p = new Person("bob", 30);
            AtomicInteger setCount = new AtomicInteger(0);

            Predicate<String> predicate = v -> v != null && v.length() >= 3;
            BiConsumer<Person, String> setter = (person, v) -> {
                person.displayName = v;
                setCount.incrementAndGet();
            };

            WriteOperation<Person> writer = WriteOperation.when(predicate, setter, "OKAY");

            writer.write(p);
            assertEquals("OKAY", p.displayName);
            assertEquals(1, setCount.get());
        }

        @Test
        @DisplayName("predicate=false 時，不應呼叫 setter")
        void when_predicateFalse_shouldNotSet() {
            Person p = new Person("bob", 30);
            BiConsumer<Person, String> setter = (person, v) -> person.displayName = v;

            WriteOperation<Person> writer = WriteOperation.when(v -> false, setter, "NOPE");

            writer.write(p);
            assertNull(p.displayName);
        }

        @Test
        @DisplayName("空參數拋 NPE（對應 @NonNull）")
        void when_setterValue_nullsShouldThrowNPE() {
            Predicate<String> predicate = v -> true;
            BiConsumer<Person, String> setter = (person, v) -> {};
            assertThrows(NullPointerException.class, () -> WriteOperation.when(null, setter, "X"));
            assertThrows(NullPointerException.class, () -> WriteOperation.when(predicate, null, "X"));
            // value 允許為 null 與否取決於你的實作：若 @NonNull 未加在 value 上，則不測 NPE。
        }
    }

    // ===== 測試區塊：when(predicate, getter, setter) =====
    @Nested
    class WhenWithGetterAndSetter {

        @Test
        @DisplayName("predicate(getter(instance))=true 時，呼叫 setter(instance, valueFromGetter)")
        void when_withGetter_predicateTrue_shouldSet() {
            Person p = new Person("cindy", 25);

            Function<Person, String> getter = person -> person.name == null ? null : "[" + person.name + "]";
            Predicate<String> predicate = v -> v != null && v.startsWith("[c");
            BiConsumer<Person, String> setter = (person, v) -> person.displayName = v;

            WriteOperation<Person> writer =
                    WriteOperation.when(predicate, getter, setter);

            writer.write(p);
            assertEquals("[cindy]", p.displayName);
        }

        @Test
        @DisplayName("predicate(getter(instance))=false 時，不應呼叫 setter")
        void when_withGetter_predicateFalse_shouldNotSet() {
            Person p = new Person("david", 40);

            Function<Person, String> getter = person -> person.name == null ? null : "[" + person.name + "]";
            Predicate<String> predicate = v -> v != null && v.startsWith("{"); // 故意設不匹配
            BiConsumer<Person, String> setter = (person, v) -> person.displayName = v;

            WriteOperation<Person> writer =
                    WriteOperation.when(predicate, getter, setter);

            writer.write(p);
            assertNull(p.displayName);
        }

        @Test
        @DisplayName("空參數拋 NPE（對應 @NonNull）")
        void when_withGetter_nullsShouldThrowNPE() {
            Predicate<String> predicate = v -> true;
            Function<Person, String> getter = p -> "X";
            BiConsumer<Person, String> setter = (p, v) -> {};
            assertThrows(NullPointerException.class, () -> WriteOperation.when(null, getter, setter));
            assertThrows(NullPointerException.class, () -> WriteOperation.when(predicate, null, setter));
            assertThrows(NullPointerException.class, () -> WriteOperation.when(predicate, getter, null));
        }
    }

    // ===== 測試區塊：setForEach =====
    @Nested
    class SetForEachFactory {

        @Test
        @DisplayName("setForEach：針對來源集合逐一選取、過濾後加入 instance 的目標集合")
        void setForEach_shouldAddFilteredMappedItemsIntoTargetCollection() {
            Person p = new Person("eva", 28);
            p.tags.add("keep"); // 先有一筆，確保 writer 是在原集合上附加而非替換

            // getter：回傳 instance 上的目標集合（要被寫入/附加）
            Function<Person, Collection<String>> getTags = person -> person.tags;

            // itemSelector：將 D 轉成 I
            Function<Integer, String> select = num -> "N" + num;

            // predicate：只保留偶數（e.g., N2, N4）
            Predicate<String> evenOnly = s -> {
                try {
                    int n = Integer.parseInt(s.substring(1));
                    return n % 2 == 0;
                } catch (Exception e) {
                    return false;
                }
            };

            // 來源集合 D
            Collection<Integer> source = List.of(1, 2, 3, 4);

            WriteOperation<Person> writer =
                    WriteOperation.setForEach(getTags, select, evenOnly, source);

            writer.write(p);

            assertTrue(p.tags.contains("keep"));
            assertTrue(p.tags.containsAll(List.of("N2", "N4")));
            assertEquals(3, p.tags.size()); // keep, N2, N4
        }

        @Test
        @DisplayName("setForEach：predicate 全不通過時，不應有變動")
        void setForEach_allFilteredOut_shouldDoNothing() {
            Person p = new Person("fox", 33);

            Function<Person, Collection<String>> getTags = person -> person.tags;
            Function<Integer, String> select = Object::toString;
            Predicate<String> never = s -> false;

            WriteOperation<Person> writer =
                    WriteOperation.setForEach(getTags, select, never, List.of(10, 20));

            writer.write(p);
            assertTrue(p.tags.isEmpty());
        }

        @Test
        @DisplayName("空參數拋 NPE（對應 @NonNull）")
        void setForEach_nullsShouldThrowNPE() {
            Function<Person, Collection<String>> getTags = person -> person.tags;
            Function<Integer, String> select = Object::toString;
            Predicate<String> predicate = s -> true;
            Collection<Integer> src = List.of(1, 2);

            assertThrows(NullPointerException.class, () -> WriteOperation.setForEach(null, select, predicate, src));
            assertThrows(NullPointerException.class, () -> WriteOperation.setForEach(getTags, null, predicate, src));
            assertThrows(NullPointerException.class, () -> WriteOperation.setForEach(getTags, select, null, src));
            assertThrows(NullPointerException.class, () -> WriteOperation.setForEach(getTags, select, predicate, null));
        }
    }

}