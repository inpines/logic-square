package org.dotspace.oofp.util.functional.monad;

public class MonadBindingOperator {

	private MonadBindingType type;
	private String name;
	private Object options;

	protected MonadBindingOperator(
			MonadBindingType type, String name, Object options) {
		this.type = type;
		this.name = name;
		this.options = options;
	}

	public MonadBindingType getType() {
		return type;
	}

	public void setType(MonadBindingType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getOptions() {
		return options;
	}

	public void setOptions(Object options) {
		this.options = options;
	}
}
