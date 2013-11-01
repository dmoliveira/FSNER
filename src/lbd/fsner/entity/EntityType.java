package lbd.fsner.entity;

import lbd.fsner.label.encoding.Label;

public enum EntityType {

	Person("PER"),
	Organization("ORG"),
	Location("LOC"),
	Event("EVT"),
	Miscellaneous("MISC"),
	Player("PLA"),
	Team("TEAM"),
	Venue("VNUE"),
	Company("CMPN"),
	All("ALL");

	private final String mValue;

	EntityType(String pValue) {
		mValue = pValue;
	}

	public String getValue() {
		return mValue;
	}

	public static EntityType getEntityType(int pLabel) {
		return EntityType.values()[pLabel / Label.values().length];
	}
}
