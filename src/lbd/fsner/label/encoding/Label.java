package lbd.fsner.label.encoding;

import lbd.FSNER.Configuration.Parameters;

public enum Label {
	Beginning("B"),
	Inside("I"),
	Last("L"),
	Outside("O"),
	UnitToken("U");

	private final String mValue;

	Label(String pValue) {
		mValue = pValue;
	}

	public String getValue() {
		return mValue;
	}

	public static Label getLabel(int pIndex) {
		return Label.values()[pIndex % Parameters.DataHandler.mLabelEncoding.getLabels().size()];
	}

	public static Label getLabel(String pName) {
		return Label.valueOf(pName);
	}
}
