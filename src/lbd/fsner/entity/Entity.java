package lbd.fsner.entity;

import lbd.FSNER.Utils.Symbol;

public class Entity {

	//TODO: Implement other entity attributes - sequence, position in seq., weight, etc;
	protected String mValue;
	protected EntityType mEntityType;
	protected int mIndex;

	public Entity(String pValue, EntityType pEntityType) {
		this(pValue, pEntityType, -1);
	}

	public Entity(String pValue, EntityType pEntityType, int pIndex) {
		mValue = pValue;
		mEntityType = pEntityType;
		mIndex = pIndex;
	}

	public void append(String pToken) {
		mValue += ((mValue.isEmpty())? Symbol.EMPTY : Symbol.SPACE) + pToken;
	}

	public String getValue() {
		return mValue;
	}

	public void setValue(String pValue) {
		mValue = pValue;
	}

	public EntityType getEntityType() {
		return mEntityType;
	}

	public void setEntityType(EntityType pEntityType) {
		mEntityType = pEntityType;
	}

	public int getIndex() {
		return mIndex;
	}

	public void setIndex(int pIndex) {
		mIndex = pIndex;
	}

	@Override
	public String toString() {
		return mValue + Symbol.COLON + mEntityType;
	}

}
