package org.dotspace.oofp.utils.functional;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import lombok.experimental.UtilityClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@UtilityClass
public class Suppliers {

	private static final Log logger = LogFactory.getLog(Suppliers.class);

	public <T> Supplier<List<T>> newList(final Class<T> clazz) {
		
		return () -> {

			String clazzName = clazz.getCanonicalName();

			logger.debug(String.format(
					"newList: %1$s -> get List<%1$s> instance by new ArrayList<%1$s>()", 
					clazzName));
			return new ArrayList<>();

		};
	}

	public <K, V> Supplier<Map<K, List<V>>> newListOfItemsByKey(
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

	public <K, V> Supplier<Map<K, V>> newHashMap(Class<K> keyClazz, Class<V> valueClazz) {
		
		return () -> {
			
			String itemsClazzName = valueClazz.getCanonicalName();
			
			String keyClazzName = keyClazz.getCanonicalName();
			
			logger.debug(String.format("newItemByKey: (%1$s, %2$s) -> "
					+ "get Map<%2$s, %1$s> instance by new HashMap<%2$s, %1$s>()",
					itemsClazzName, keyClazzName));
			
			return new HashMap<>();

		};
	}

	public Supplier<InputStream> supplyInputStreamOf(String content, Charset charset) {
		return () -> new ByteArrayInputStream(content.getBytes(charset));
	}

	public Supplier<InputStream> supplyInputStreamOf(byte[] bytes) {
		return () -> new ByteArrayInputStream(bytes);
	}

	public Supplier<Reader> supplyReaderOf(String content, Charset charset) {
		return () -> new InputStreamReader(new ByteArrayInputStream(content.getBytes(charset)), charset);
	}

	public Supplier<Reader> supplyReaderOf(byte[] bytes) {
		return () -> new InputStreamReader(new ByteArrayInputStream(bytes));
	}

}
