package org.dotspace.oofp.support.tokenizer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TokenizationResult<T> {

	private T root;
	
	private Map<String, Object> mappingItems = new HashMap<>();
	
	private Long tokenizedTextSize;

	public Long getTokenizedTextSize() {
		return tokenizedTextSize;
	}

	public void setTokenizedTextSize(Long tokenizedTextSize) {
		this.tokenizedTextSize = tokenizedTextSize;
	}

	public T getRoot() {
		return root;
	}

	public void setRoot(T root) {
		this.root = root;
	}

	public Collection<Entry<String, Object>> getMappingItemEntries() {
		return mappingItems.entrySet();
	}

	public void put(String key, Object mappingItem) {
		mappingItems.put(key, mappingItem);
	}

	public Object findMappingItem(String key) {
		return mappingItems.get(key);
	}
	
}
