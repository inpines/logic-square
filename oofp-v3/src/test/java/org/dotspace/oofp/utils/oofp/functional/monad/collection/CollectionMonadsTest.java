package org.dotspace.oofp.utils.oofp.functional.monad.collection;

import org.dotspace.oofp.utils.functional.monad.collection.CollectionMonads;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CollectionMonadsTest {

    @Test
    void getValue_withExistingKey_returnsValue() {
        Map<String, String> map = Map.of("key1", "value1");
        
        String result = CollectionMonads.getValue("key1", map);
        
        assertEquals("value1", result);
    }

    @Test
    void getValue_withNonExistingKey_returnsNull() {
        Map<String, String> map = Map.of("key1", "value1");
        
        String result = CollectionMonads.getValue("nonExistingKey", map);
        
        assertNull(result);
    }

    @Test
    void getValue_withNullMap_returnsNull() {
        String result = CollectionMonads.getValue("key1", null);
        
        assertNull(result);
    }

    @Test
    void getValue_withNullKey_returnsNull() {
        Map<String, String> map = Map.of("key1", "value1");
        
        String result = CollectionMonads.getValue(null, map);
        
        assertNull(result);
    }

    @Test
    void getValue_withSupplier_existingKey_returnsValue() {
        Map<String, String> map = Map.of("key1", "value1");
        
        String result = CollectionMonads.getValue("key1", map, () -> "default");
        
        assertEquals("value1", result);
    }

    @Test
    void getValue_withSupplier_nonExistingKey_returnsDefaultValue() {
        Map<String, String> map = Map.of("key1", "value1");
        
        String result = CollectionMonads.getValue("nonExistingKey", map, () -> "default");
        
        assertEquals("default", result);
    }

    @Test
    void getValue_withSupplier_nullMap_returnsDefaultValue() {
        String result = CollectionMonads.getValue("key1", null, () -> "default");
        
        assertEquals("default", result);
    }

    @Test
    void listValues_withExistingKeys_returnsValues() {
        Map<String, String> map = Map.of("key1", "value1", "key2", "value2");
        List<String> keys = List.of("key1", "key2");
        
        List<String> result = CollectionMonads.listValues(keys, map);
        
        assertEquals(2, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
    }

    @Test
    void listValues_withMixedKeys_returnsOnlyExistingValues() {
        Map<String, String> map = Map.of("key1", "value1", "key2", "value2");
        List<String> keys = List.of("key1", "nonExistingKey", "key2");
        
        List<String> result = CollectionMonads.listValues(keys, map);
        
        assertEquals(2, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
    }

    @Test
    void listValues_withNonExistingKeys_returnsEmptyList() {
        Map<String, String> map = Map.of("key1", "value1");
        List<String> keys = List.of("nonExistingKey1", "nonExistingKey2");
        
        List<String> result = CollectionMonads.listValues(keys, map);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void listValues_withEmptyKeysList_returnsEmptyList() {
        Map<String, String> map = Map.of("key1", "value1");
        List<String> keys = List.of();
        
        List<String> result = CollectionMonads.listValues(keys, map);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void listValues_withNullMap_returnsEmptyList() {
        List<String> keys = List.of("key1", "key2");
        
        List<String> result = CollectionMonads.listValues(keys, null);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void listValues_withNullValues_includesNulls() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", null);
        List<String> keys = List.of("key1", "key2");
        
        List<String> result = CollectionMonads.listValues(keys, map);
        
        assertEquals(2, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains(null));
    }
}