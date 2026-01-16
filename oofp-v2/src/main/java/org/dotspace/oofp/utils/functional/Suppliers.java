package org.dotspace.oofp.utils.functional;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Suppliers {

	private static final Log logger = LogFactory.getLog(Suppliers.class);

	private Suppliers() {
		super();
	}
	
	public static <T> Supplier<List<T>> newList(final Class<T> clazz) {
		
		return () -> {

			String clazzName = clazz.getCanonicalName();

			logger.debug(String.format(
					"newList: %1$s -> get List<%1$s> instance by new ArrayList<%1$s>()", 
					clazzName));
			return new ArrayList<>();

		};
	}

	public static <K, V> Supplier<Map<K, List<V>>> newListOfItemsByKey(
			final Class<K> keyClazz, final Class<V> itemsClazz) {
		return () -> {

			String itemsClazzName = itemsClazz.getCanonicalName();
			String keyClazzName = keyClazz.getCanonicalName();
			logger.debug(String.format(
							"newListOfItemsByKey: (%1$s, %2$s) -> get Map<%2$s, "
									+ "List<%1$s>> instance by new HashMap<%2$s, List<%1$s>>()",
							itemsClazzName, keyClazzName));
			
			return new HashMap<>();

		};
	}

	public static <K, V> Supplier<Map<K, V>> newHashMap(Class<K> keyClazz, Class<V> valueClazz) {
		
		return () -> {
			
			String itemsClazzName = valueClazz.getCanonicalName();
			
			String keyClazzName = keyClazz.getCanonicalName();
			
			logger.debug(String.format("newItemByKey: (%1$s, %2$s) -> "
					+ "get Map<%2$s, %1$s> instance by new HashMap<%2$s, %1$s>()",
					itemsClazzName, keyClazzName));
			
			return new HashMap<>();

		};
	}

	public static Supplier<InputStream> supplyInputStreamOf(
			Path path, BiFunction<Exception, Path, RuntimeException> exceptionGenerator) {
		return () -> {
			try {
				return Files.newInputStream(path);
			} catch (IOException e) {
				throw exceptionGenerator.apply(e, path);
			}
		};
	}

	public static Supplier<InputStream> supplyInputStreamOf(
			File file, BiFunction<Exception, File, RuntimeException> exceptionGenerator) {
		return () -> {
			try {
				return new FileInputStream(file);
			} catch (IOException e) {
				throw exceptionGenerator.apply(e, file);
			}
		};
	}

	public static Supplier<Reader> supplyReaderOf(
			Path path, Charset charset, BiFunction<Path, Charset, String> exceptionMessageProvider,
			BiFunction<Exception, String, RuntimeException> exceptionGenerator) {
		return () -> {
			try {
				return Files.newBufferedReader(path, charset);
			} catch (IOException e) {
				throw exceptionGenerator.apply(e, exceptionMessageProvider.apply(path, charset));
			}
		};
	}

	public static Supplier<InputStream> supplyInputStreamOf(String content, Charset charset) {
		return () -> new ByteArrayInputStream(content.getBytes(charset));
	}

	public static Supplier<InputStream> supplyInputStreamOf(byte[] bytes) {
		return () -> new ByteArrayInputStream(bytes);
	}

}
