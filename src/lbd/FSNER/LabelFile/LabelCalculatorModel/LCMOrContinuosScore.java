package lbd.FSNER.LabelFile.LabelCalculatorModel;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.Statistic.FilterProbabilityHandler;
import lbd.FSNER.Model.AbstractTermRestrictionChecker;
import lbd.FSNER.Utils.LabelEncoding;

public class LCMOrContinuosScore extends LCMOrDiscreteScore{

	private static final long serialVersionUID = 1L;

	public LCMOrContinuosScore(
			AbstractTermRestrictionChecker pTermRestrictionChecker) {
		super(pTermRestrictionChecker);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void calculateLabelProbability(int pIndex, String pTerm,
			SequenceLabel pSequenceLabelProcessed,
			FilterProbabilityHandler pFilterProbability,
			String pFilterInstanceId) {

		if (!pFilterInstanceId.isEmpty()) {
			for(Enum cLabel : LabelEncoding.getLabels()) {
				double vLabelProbability = pFilterProbability.getProbability(pFilterInstanceId, cLabel.ordinal());
				mLabelProbability[cLabel.ordinal()] += vLabelProbability;
				pFilterProbability.addToFilterStatisticForAssignedLabels(pTerm,
						LabelEncoding.isEntity(pSequenceLabelProcessed.getLabel(pIndex)), true);
			}
		}
	}
}
