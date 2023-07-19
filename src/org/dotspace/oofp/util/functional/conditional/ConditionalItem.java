package org.dotspace.oofp.util.functional.conditional;

import java.util.ArrayList;
import java.util.List;

public class ConditionalItem {

	private String reader;
	
	private List<ConditionalTransition> transitions;
	
	private String writer;

	protected ConditionalItem(String reader, String writer) {
		this();
		this.reader = reader;
		this.writer = writer;
	}

	public ConditionalItem() {
		super();
		this.transitions = new ArrayList<>();
	}
	
	public String getReader() {
		return reader;
	}

	public void setReader(String reader) {
		this.reader = reader;
	}

	public List<ConditionalTransition> getTransitions() {
		return transitions;
	}

	public void setTransitions(List<ConditionalTransition> transitions) {
		this.transitions = transitions;
	}

	public String getWriter() {
		return writer;
	}

	public void setWriter(String writer) {
		this.writer = writer;
	}

	public ConditionalItem add(ConditionalTransition transition) {
		transitions.add(transition);
		return this;
	}
	
}
