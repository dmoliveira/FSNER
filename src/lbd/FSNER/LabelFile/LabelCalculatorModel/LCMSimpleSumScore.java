package lbd.FSNER.LabelFile.LabelCalculatorModel;

import java.util.List;
import java.util.Map;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.Statistic.FilterProbability;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilter.FilterState;
import lbd.FSNER.Model.AbstractLabelFileLabelCalculatorModel;
import lbd.FSNER.Model.AbstractTermRestrictionChecker;
import lbd.FSNER.Utils.LabelEncoding;

public class LCMSimpleSumScore extends AbstractLabelFileLabelCalculatorModel {

	private static final long serialVersionUID = 1L;
	protected final double COMMON_TERM_PERCENTAGE_THRESHOLD = 1.0;

	public LCMSimpleSumScore(
			AbstractTermRestrictionChecker termRestrictionChecker) {
		super(termRestrictionChecker);
	}

	// This method only can be used with ALL filter that are over the same
	// preprocessor type
	@Override
	public int calculateMostProbablyLabel(int pIndex,
			Map<String, SequenceLabel> pProccessedSequenceMap,
			List<AbstractDataPreprocessor> pDataProcessorList,
			List<AbstractFilter> pFilterList) {

		if (pProccessedSequenceMap == null
				|| pProccessedSequenceMap.size() == 0
				|| pDataProcessorList == null || pFilterList == null
				|| pFilterList.size() == 0) {
			throw new NullPointerException();
		}

		String vFilterInstanceId;
		FilterProbability vFilterProbability;

		mLabelProbability = new double[LabelEncoding.BILOU.values().length];
		mNormalizationFactor = new int[LabelEncoding.BILOU.values().length];
		SequenceLabel vSequence = pProccessedSequenceMap.get(pFilterList.get(0)
				.getPreprocesingTypeName());

		double vTotalFiltersAssignedLabelsInTrain = 0;
		double vCommonTermPercentage = pDataProcessorList.get(
				pFilterList.get(0).getFilterPreprocessingTypeNameIndex())
				.getCommonTermProbability(vSequence.getTerm(pIndex));

		if (vCommonTermPercentage == COMMON_TERM_PERCENTAGE_THRESHOLD) {
			return LabelEncoding.BILOU.Outside.ordinal();
		}

		for (AbstractFilter cFilter : pFilterList) {
			if (cFilter.getFilterState() == FilterState.Active
					&& !(vFilterInstanceId = cFilter.getSequenceInstanceId(
							vSequence, pIndex)).isEmpty()) {
				vTotalFiltersAssignedLabelsInTrain += cFilter.getFilterProbability().getTotalAssignedLabelsInTrain();
			}
		}

		for (AbstractFilter cFilter : pFilterList) {
			if (cFilter.getFilterState() == FilterState.Active
					&& !(vFilterInstanceId = cFilter.getSequenceInstanceId(
							vSequence, pIndex)).isEmpty()) {

				vFilterProbability = cFilter.getFilterProbability();

				for (LabelEncoding.BILOU cLabel : LabelEncoding.BILOU.values()) {
					mLabelProbability[cLabel.ordinal()] += vFilterProbability
							.getProbability(vFilterInstanceId, cLabel.ordinal());
					mNormalizationFactor[cLabel.ordinal()]++;
				}
			}
		}

		for (int cLabel = 0; cLabel < mLabelProbability.length; cLabel++) {
			mLabelProbability[cLabel] /= mNormalizationFactor[cLabel];
		}

		if (LabelEncoding.isEntity(vSequence.getLabel(pIndex))) {
			/*System.out.println(MessageFormat.format(
					"{0} - B({1}) I({2}) L({3}) O({4}) U({5})",
					vSequence.getTerm(pIndex), mLabelProbability[LabelEncoding.BILOU.Beginning.ordinal()],
					mLabelProbability[LabelEncoding.BILOU.Inside.ordinal()], mLabelProbability[LabelEncoding.BILOU.Last.ordinal()],
					mLabelProbability[LabelEncoding.BILOU.Outside.ordinal()], mLabelProbability[LabelEncoding.BILOU.UnitToken.ordinal()]));*/
		}

		return getMaxProbabilityLabel(mLabelProbability);
	}

	protected int getMaxProbabilityLabel(double[] pLabelProbability) {

		double vMaxLabelProbability = 0;
		int vLabel = LabelEncoding.BILOU.Outside.ordinal();
		for (int cIndex = 0; cIndex < pLabelProbability.length; cIndex++) {
			if (pLabelProbability[cIndex] > vMaxLabelProbability) {
				vMaxLabelProbability = pLabelProbability[cIndex];
				vLabel = cIndex;
			}
		}

		return vLabel;
	}
}
