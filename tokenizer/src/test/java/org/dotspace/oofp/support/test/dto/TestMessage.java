package org.dotspace.oofp.support.test.dto;

import java.util.HashMap;
import java.util.Map;

public class TestMessage {

	private Map<String, Object> header = new HashMap<>();
	
	private Map<String, Object> body = new HashMap<>();

	public Map<String, Object> getHeader() {
		return header;
	}

	public void setHeader(Map<String, Object> header) {
		this.header = header;
	}

	public Map<String, Object> getBody() {
		return body;
	}

	public void setBody(Map<String, Object> body) {
		this.body = body;
	}
	
	
}
