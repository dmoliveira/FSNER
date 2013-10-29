package lbd.fsner.label.encoding;

import lbd.FSNER.Configuration.Parameters;
import lbd.fsner.entity.EntityType;

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

	public static int getOrdinalLabel(EntityType pEntityType, Label pCanonicalLabel) {
		return (pEntityType.ordinal() * EntityType.values().length) + pCanonicalLabel.ordinal();
	}

	public static Label getCanonicalLabel(int pIndex) {
		return Label.values()[pIndex % Parameters.DataHandler.mLabelEncoding.getLabels().size()];
	}

	public static Label getCanonicalLabel(String pName) {
		return Label.valueOf(pName);
	}
}
