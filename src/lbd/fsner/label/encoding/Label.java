package lbd.fsner.label.encoding;

import lbd.FSNER.Configuration.Parameters;
import lbd.fsner.entity.EntityType;

public enum Label {
	Beginning,
	Inside,
	Last,
	Outside,
	UnitToken;

	public static int getOrdinalLabel(EntityType pEntityType, Label pLabel) {
		return (pEntityType.ordinal() * Label.values().length) + pLabel.ordinal();
	}

	public static Label getCanonicalLabel(int pLabelIndex) {
		return Label.values()[pLabelIndex % Parameters.DataHandler.mLabelEncoding.getLabels().size()];
	}

	public static Label getCanonicalLabel(String pName) {
		return Label.valueOf(pName);
	}
}
