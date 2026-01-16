package org.dotspace.oofp.utils.oofp.functional;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

import org.dotspace.oofp.utils.functional.Casters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CastersTest {

    // 取一個參數化型別（List<String>）來測 castTypeAsClazz 非 Class 的情況
    static Type parameterizedListStringType() {
        class Holder { List<String> field; }
        try {
            Field f = Holder.class.getDeclaredField("field");
            return f.getGenericType(); // ParameterizedType
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    // ---------- forMap ----------

    @Test
    @DisplayName("forMap：傳入 Map 實例可轉回同一實例")
    void forMap_success() {
        Map<String, Integer> src = new HashMap<>();
        src.put("a", 1);

        Function<Object, Map<String, Integer>> f = Casters.forMap(String.class, Integer.class);
        Map<String, Integer> out = f.apply(src);

        assertSame(src, out);
        assertEquals(1, out.get("a"));
    }

    @Test
    @DisplayName("forMap：若傳入非 Map 會拋 ClassCastException")
    void forMap_wrongInstance_throws() {
        Function<Object, Map<String, Integer>> f = Casters.forMap(String.class, Integer.class);
        var data = List.of(1, 2);
        assertThrows(ClassCastException.class, () -> f.apply(data));
    }

    // ---------- forText ----------

    @Test
    @DisplayName("forText：String 轉型成功；null 回 null；非 String 拋 ClassCastException")
    void forText_behaviors() {
        Function<Object, String> f = Casters.forText();

        assertEquals("abc", f.apply("abc"));
        assertNull(f.apply(null)); // Class.cast(null) 允許回 null
        assertThrows(ClassCastException.class, () -> f.apply(123));
    }

    // ---------- forListOfResultMap ----------

    @Test
    @DisplayName("forListOfResultMap：正確清單可轉回同一實例")
    void forListOfResultMap_success() {
        List<Map<String, Object>> src = new ArrayList<>();
        src.add(Map.of("k", "v"));

        Function<Object, List<Map<String, Object>>> f = Casters.forListOfResultMap();
        List<Map<String, Object>> out = f.apply(src);

        assertSame(src, out);
        assertEquals("v", out.get(0).get("k"));
    }

    @Test
    @DisplayName("forListOfResultMap：非 List 會拋 ClassCastException")
    void forListOfResultMap_wrongType_throws() {
        Function<Object, List<Map<String, Object>>> f = Casters.forListOfResultMap();
        Map<String, Object> emptyMap = Map.of();
        assertThrows(ClassCastException.class, () -> f.apply(emptyMap));
    }

    // ---------- castTypeAsClazz ----------

    @Test
    @DisplayName("castTypeAsClazz：傳入 Class 回傳該 Class；非 Class/Null 回 null")
    void castTypeAsClazz_behaviors() {
        Function<Type, Class<String>> f = Casters.castTypeAsClazz();

        assertEquals(String.class, f.apply(String.class));
        assertNull(f.apply(parameterizedListStringType())); // 不是 Class
        assertNull(f.apply(null));
    }

    // ---------- cast ----------

    @Test
    @DisplayName("cast：型別相容回原值；不相容回 null；null 也回 null")
    void cast_generic() {
        Function<Object, Integer> castInt = Casters.cast();

        assertEquals(123, castInt.apply(123));
        assertNull(castInt.apply(null));
    }

    // ---------- forList ----------

    @Test
    @DisplayName("forList：List 轉回同一實例；非 List 拋 ClassCastException")
    void forList_behaviors() {
        List<Integer> src = new ArrayList<>(List.of(1, 2));
        Function<Object, List<Integer>> f = Casters.forList();

        List<Integer> out = f.apply(src);
        assertSame(src, out);

        Map<String, Integer> map = Map.of("a", 1);

        assertThrows(ClassCastException.class, () -> f.apply(map));
    }

    // ---------- forClazz ----------

    @Test
    @DisplayName("forClazz：回傳 instance 的實際類別；null 則為 empty")
    void forClazz_behaviors() {
        Function<Object, Optional<Class<String>>> f = Casters.forClazz();

        Optional<Class<String>> o1 = f.apply("hi");
        assertTrue(o1.isPresent());
        assertEquals(String.class, o1.get());

        Optional<Class<String>> o2 = f.apply(null);
        assertTrue(o2.isEmpty());
    }

    @Test
    void cast_shouldReturnInstanceWhenAssignable() {
        Function<Object, String> caster = Casters.cast(String.class);

        Object input = "hello";
        String result = caster.apply(input);

        assertEquals("hello", result);
    }

    @Test
    void cast_shouldReturnNullWhenNotAssignable() {
        Function<Object, Integer> caster = Casters.cast(Integer.class);

        Object input = "not an integer";
        Integer result = caster.apply(input);

        assertNull(result);
    }

    @Test
    void cast_shouldReturnNullWhenInputIsNull() {
        Function<Object, String> caster = Casters.cast(String.class);

        Object input = null;
        String result = caster.apply(input);

        assertNull(result);
    }

}
