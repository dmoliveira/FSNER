package lbd.FSNER.Component.Statistic;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractFilterProbability;
import lbd.fsner.label.encoding.Label;

public class SimpleFilterProbability extends AbstractFilterProbability{

	private static final long serialVersionUID = 1L;

	@Override
	public void addLabel(int pLabel) {

		mTotalAssignedLabel++;
		mTotalAssignedTermPerLabel[pLabel]++;

		if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getLabel(pLabel))) {
			mTotalAssignedEntityLabel++;
		}
	}

}
