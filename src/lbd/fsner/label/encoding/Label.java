package lbd.fsner.label.encoding;

public class Label {

	protected String mValue;
	protected int mOrdinal;

	public Label(String pValue, int pOrdinal) {
		mValue = pValue;
		mOrdinal = pOrdinal;
	}

	public String getValue() {
		return mValue;
	}

	public void setValue(String pValue) {
		mValue = pValue;
	}

	public int getOrdinal() {
		return mOrdinal;
	}

	public void setOrdinal(int pOrdinal) {
		mOrdinal = pOrdinal;
	}
}
