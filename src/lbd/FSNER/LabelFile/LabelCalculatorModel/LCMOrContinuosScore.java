package lbd.FSNER.LabelFile.LabelCalculatorModel;

import lbd.FSNER.Component.Statistic.FilterProbabilityHandler;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractTermRestrictionChecker;
import lbd.data.handler.ISequence;
import lbd.fsner.label.encoding.Label;

public class LCMOrContinuosScore extends LCMOrDiscreteScore{

	private static final long serialVersionUID = 1L;

	public LCMOrContinuosScore(
			AbstractTermRestrictionChecker pTermRestrictionChecker) {
		super(pTermRestrictionChecker);
	}

	@Override
	protected void calculateLabelProbability(int pIndex, String pTerm,
			ISequence pPreprocessedSequence,
			FilterProbabilityHandler pFilterProbability,
			String pFilterInstanceId) {

		if (!pFilterInstanceId.isEmpty()) {
			for(int cLabel = 0; cLabel < mLabelProbability.length; cLabel++) {
				double vLabelProbability = pFilterProbability.getProbability(pFilterInstanceId, cLabel);
				mLabelProbability[cLabel] += vLabelProbability;
				pFilterProbability.addToFilterStatisticForAssignedLabels(pTerm,
						Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(pPreprocessedSequence.getLabel(pIndex))), true);
			}
		}
	}
}
