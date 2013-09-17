package lbd.FSNER.Utils.Collections;

import java.io.Serializable;

public class Pair<L,R> implements Serializable{

	private static final long serialVersionUID = 1L;

	protected final L mLeft;
	protected final R mRight;

	public Pair(L pLeft, R pRight) {
		this.mLeft = pLeft;
		this.mRight = pRight;
	}

	public L getLeft() { return mLeft; }
	public R getRight() { return mRight; }

	@Override
	public int hashCode() { return mLeft.hashCode() ^ mRight.hashCode(); }

	@Override
	public boolean equals(Object pPair) {
		if (pPair == null) {
			return false;
		}
		if (!(pPair instanceof Pair)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		Pair vPair = (Pair) pPair;
		return this.mLeft.equals(vPair.getLeft()) &&
				this.mRight.equals(vPair.getRight());
	}

}