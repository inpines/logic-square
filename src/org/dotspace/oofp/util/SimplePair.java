package org.dotspace.oofp.util;

public class SimplePair<L, R> {

	public static <L, R> SimplePair<L, R> of(L left, R right) {
		return new SimplePair<L, R>(left, right);
	}

	private L left;
	private R right;

	protected SimplePair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	public R getValue() {
		return right;
	}

	public L getKey() {
		return left;
	}

}
