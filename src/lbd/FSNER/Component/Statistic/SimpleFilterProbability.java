package lbd.FSNER.Component.Statistic;

import lbd.FSNER.Model.AbstractFilterProbability;
import lbd.FSNER.Utils.LabelEncoding;

public class SimpleFilterProbability extends AbstractFilterProbability{

	private static final long serialVersionUID = 1L;

	@Override
	public void addLabel(int pLabel) {

		mTotalAssignedLabel++;
		mTotalAssignedTermPerLabel[pLabel]++;

		if(LabelEncoding.isEntity(pLabel)) {
			mTotalAssignedEntityLabel++;
		}
	}

}
