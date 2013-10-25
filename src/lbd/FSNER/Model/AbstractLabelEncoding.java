package lbd.FSNER.Model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lbd.fsner.label.encoding.Label;

public abstract class AbstractLabelEncoding {

	protected Map<String, Label> mLabelMap;

	public AbstractLabelEncoding() {
		createLabelMap();
	}

	protected abstract void createLabelMap();

	public abstract boolean isEntity(Label pLabel);

	public abstract boolean isOutside(Label pLabel);

	public abstract Label getOutsideLabel();

	public int getLabelIndex(String pLabelValue) {
		if(!mLabelMap.containsKey(pLabelValue)) {
			throw new ArrayIndexOutOfBoundsException("Error: Label Value '"
					+ pLabelValue + "' doesn't exists.");
		}

		return mLabelMap.get(pLabelValue).getOrdinal();
	}

	public Set<Label> getLabels() {
		return new HashSet<Label>(mLabelMap.values());
	}

}
