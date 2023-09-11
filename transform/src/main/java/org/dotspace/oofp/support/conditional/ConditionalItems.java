package org.dotspace.oofp.support.conditional;

public class ConditionalItems {

	public static ConditionalItem of(String reader, String writer) {
		return new ConditionalItem(reader, writer);
	}
}
