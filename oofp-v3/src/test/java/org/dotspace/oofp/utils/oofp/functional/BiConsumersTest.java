package org.dotspace.oofp.utils.oofp.functional;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.dotspace.oofp.utils.functional.BiConsumers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BiConsumersTest {

    @Test
    @DisplayName("forListOf(): 回傳可將元素加入 List 的 BiConsumer")
    void forListOf_addsElementToList() {
        BiConsumer<List<String>, String> consumer = BiConsumers.forListOf();
        List<String> list = new ArrayList<>();
        
        consumer.accept(list, "test");
        
        assertEquals(1, list.size());
        assertEquals("test", list.get(0));
    }

    @Test
    @DisplayName("forListOf(): 可處理不同型別")
    void forListOf_handlesGenericTypes() {
        BiConsumer<List<Integer>, Integer> consumer = BiConsumers.forListOf();
        List<Integer> list = new ArrayList<>();
        
        consumer.accept(list, 42);
        
        assertEquals(1, list.size());
        assertEquals(42, list.get(0));
    }

    @Test
    @DisplayName("forMapOf(): 回傳可將值放入 Map 指定 key 的 BiConsumer")
    void forMapOf_putsValueIntoMapWithKey() {
        BiConsumer<Map<String, Integer>, Integer> consumer = BiConsumers.forMapOf("key1");
        Map<String, Integer> map = new HashMap<>();
        
        consumer.accept(map, 100);
        
        assertEquals(1, map.size());
        assertEquals(100, map.get("key1"));
    }

    @Test
    @DisplayName("forMapOf(): 覆寫相同 key 的值")
    void forMapOf_overwritesExistingKey() {
        BiConsumer<Map<String, String>, String> consumer = BiConsumers.forMapOf("name");
        Map<String, String> map = new HashMap<>();
        map.put("name", "old");
        
        consumer.accept(map, "new");
        
        assertEquals(1, map.size());
        assertEquals("new", map.get("name"));
    }

    @Test
    @DisplayName("forProcessorExecutorsIndexOf(): 回傳可將 Function 放入 Map 的 BiConsumer")
    void forProcessorExecutorsIndexOf_putsFunctionIntoMap() {
        UnaryOperator<String> processor = String::toUpperCase;
        BiConsumer<Map<String, Function<String, String>>, Function<String, String>> consumer = 
            BiConsumers.forProcessorExecutorsIndexOf("proc1", processor);
        Map<String, Function<String, String>> map = new HashMap<>();
        
        consumer.accept(map, String::toLowerCase);
        
        assertEquals(1, map.size());
        assertEquals(processor, map.get("proc1"));
    }
}