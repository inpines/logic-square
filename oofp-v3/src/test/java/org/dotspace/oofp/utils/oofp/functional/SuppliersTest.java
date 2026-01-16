package org.dotspace.oofp.utils.oofp.functional;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

import org.dotspace.oofp.utils.functional.Suppliers;
import org.junit.jupiter.api.*;

class SuppliersTest {

    // --------- 功能性測試（不驗證日誌）---------

    @Test
    @DisplayName("newList：回傳 ArrayList，且每次 get() 都是新實例")
    void newList_returnsArrayList_andFreshInstances() {
        Supplier<List<String>> sup = Suppliers.newList(String.class);

        List<String> l1 = sup.get();
        List<String> l2 = sup.get();

        assertNotNull(l1);
        assertNotNull(l2);
        assertInstanceOf(ArrayList.class, l1);
        assertInstanceOf(ArrayList.class, l2);
        assertNotSame(l1, l2);

        // 型別安全：可正常加入元素
        l1.add("a");
        assertEquals(List.of("a"), l1);
        assertTrue(l2.isEmpty());
    }

    @Test
    @DisplayName("newListOfItemsByKey：回傳 HashMap<K, List<V>>，每次 get() 都是新實例")
    void newListOfItemsByKey_returnsHashMap_ofList_andFreshInstances() {
        Supplier<Map<Integer, List<String>>> sup =
                Suppliers.newListOfItemsByKey(Integer.class, String.class);

        Map<Integer, List<String>> m1 = sup.get();
        Map<Integer, List<String>> m2 = sup.get();

        assertNotNull(m1);
        assertNotNull(m2);
        assertInstanceOf(HashMap.class, m1);
        assertInstanceOf(HashMap.class, m2);
        assertNotSame(m1, m2);

        // 型別安全：可正常放入 K -> List<V>
        m1.put(1, new ArrayList<>(List.of("x", "y")));
        assertEquals(List.of("x", "y"), m1.get(1));
        assertTrue(m2.isEmpty());
    }

    @Test
    @DisplayName("newHashMap：回傳 HashMap<K,V>，每次 get() 都是新實例")
    void newHashMap_returnsHashMap_andFreshInstances() {
        Supplier<Map<String, Integer>> sup =
                Suppliers.newHashMap(String.class, Integer.class);

        Map<String, Integer> m1 = sup.get();
        Map<String, Integer> m2 = sup.get();

        assertNotNull(m1);
        assertNotNull(m2);
        assertInstanceOf(HashMap.class, m1);
        assertInstanceOf(HashMap.class, m2);
        assertNotSame(m1, m2);

        m1.put("k", 10);
        assertEquals(10, m1.get("k"));
        assertTrue(m2.isEmpty());
    }

    @Test
    void supplyInputStreamOf_Path_shouldReadContent() throws Exception {
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.writeString(tempFile, "hello-path");

        byte[] bytes = Files.readAllBytes(tempFile);
        Supplier<InputStream> supplier = Suppliers.supplyInputStreamOf(bytes);

        try (InputStream in = supplier.get()) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("hello-path", text);
        }
    }

    @Test
    void supplyInputStreamOf_File_shouldReadContent() throws Exception {
        File tempFile = File.createTempFile("test", ".txt");
        Files.writeString(tempFile.toPath(), "hello-file");
        byte[] bytes = Files.readAllBytes(tempFile.toPath());

        Supplier<InputStream> supplier = Suppliers.supplyInputStreamOf(bytes);

        try (InputStream in = supplier.get()) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("hello-file", text);
        }
    }

    @Test
    void supplyInputStreamOf_String_shouldReadContent() throws Exception {
        Supplier<InputStream> supplier =
                Suppliers.supplyInputStreamOf("hello-string", StandardCharsets.UTF_8);

        try (InputStream in = supplier.get()) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("hello-string", text);
        }
    }

    @Test
    void supplyInputStreamOf_Bytes_shouldReadContent() throws Exception {
        byte[] bytes = "hello-bytes".getBytes(StandardCharsets.UTF_8);
        Supplier<InputStream> supplier = Suppliers.supplyInputStreamOf(bytes);

        try (InputStream in = supplier.get()) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("hello-bytes", text);
        }
    }

    @Test
    void supplyReaderOf_shouldReadUtf8Content() throws Exception {
        // 建立臨時檔並寫入 UTF-8 中文內容
        Path tempFile = Files.createTempFile("test", ".properties");
        Files.writeString(tempFile, "greeting=哈囉", StandardCharsets.UTF_8);
        String content = Files.readString(tempFile);

        Supplier<Reader> supplier = Suppliers.supplyReaderOf(content, StandardCharsets.UTF_8);

        Properties props = new Properties();
        try (Reader reader = supplier.get()) {
            props.load(reader);
        }

        assertEquals("哈囉", props.getProperty("greeting"));
    }

}
