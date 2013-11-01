package lbd.fsner.entity;

import java.util.List;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Utils.Symbol;
import lbd.Utils.FrequencyMap;
import lbd.fsner.label.encoding.Label;

public class Entity {

	//TODO: Implement other entity attributes - sequence, position in seq., weight, etc;
	protected String mValue;
	protected int mIndex;

	protected FrequencyMap<EntityType> mEntityTypeFrequency;

	public Entity(String pValue, EntityType pEntityType) {
		this(pValue, pEntityType, -1);
	}

	public Entity(String pValue, EntityType pEntityType, int pIndex) {
		mValue = pValue;
		mIndex = pIndex;

		mEntityTypeFrequency = new FrequencyMap<EntityType>();
		if(pEntityType != null) {
			mEntityTypeFrequency.add(pEntityType);
		}
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
		return mEntityTypeFrequency.getMax();
	}

	public void setEntityType(EntityType pEntityType) {
		mEntityTypeFrequency.clear();
		mEntityTypeFrequency.add(pEntityType);
	}

	public int getIndex() {
		return mIndex;
	}

	public void setIndex(int pIndex) {
		mIndex = pIndex;
	}

	public List<Label> getLabels() {
		return Parameters.DataHandler.mLabelEncoding.getLabels(getValue());
	}

	public int size() {
		return getValue().split(Symbol.SPACE).length;
	}

	@Override
	public String toString() {
		return mValue + Symbol.COLON + mEntityTypeFrequency.getMax();
	}

}
