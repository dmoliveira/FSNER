package lbd.FSNER.LabelFile.LabelCalculatorModel;

import java.util.List;
import java.util.Map;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.Statistic.FilterProbabilityHandler;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilter.FilterState;
import lbd.FSNER.Model.AbstractLabelFileLabelCalculatorModel;
import lbd.FSNER.Model.AbstractTermRestrictionChecker;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.data.handler.DataSequence;

public class LCMOrDiscreteScore extends AbstractLabelFileLabelCalculatorModel {

	private static final long serialVersionUID = 1L;

	// -- Common Term Restriction
	protected final double COMMON_TERM_PERCENTAGE_THRESHOLD = 1.0;

	public LCMOrDiscreteScore(AbstractTermRestrictionChecker termRestrictionChecker) {
		super(termRestrictionChecker);
	}

	@Override
	public int calculateMostProbablyLabelSub(int pIndex, DataSequence pSequence,
			Map<String, SequenceLabel> pProccessedSequenceMap,
			List<AbstractDataPreprocessor> pDataProcessorList,
			List<AbstractFilter> pFilterList) {

		int vMostProbablyLabel = LabelEncoding.getOutsideLabel();
		String vTerm = "";

		SequenceLabel vSequenceLabelProcessed = null;
		AbstractDataPreprocessor vDataPreprocessor = null;

		int vLastFilterPreprocessingTypeNameIndex = -1;

		for (AbstractFilter cFilter : pFilterList) {

			if (vLastFilterPreprocessingTypeNameIndex != cFilter.getFilterPreprocessingTypeIndex()) {

				vDataPreprocessor = pDataProcessorList.get(cFilter.getFilterPreprocessingTypeIndex());
				vSequenceLabelProcessed = pProccessedSequenceMap.get(cFilter.getPreprocesingTypeName());
				vLastFilterPreprocessingTypeNameIndex = cFilter.getFilterPreprocessingTypeIndex();

				vTerm = vSequenceLabelProcessed.getTerm(pIndex);
			}

			if (cFilter.getFilterState() == FilterState.Active
					&& vDataPreprocessor.getCommonTermProbability(vTerm) < COMMON_TERM_PERCENTAGE_THRESHOLD) {

				// -- Get filter instance id determined by the index
				FilterProbabilityHandler vFilterProbability = cFilter.getFilterProbability();
				String vFilterInstanceId = cFilter.getSequenceInstanceId(pSequence, vSequenceLabelProcessed, pIndex);

				calculateLabelProbability(pIndex, vTerm,
						vSequenceLabelProcessed, vFilterProbability,
						vFilterInstanceId);
			}
		}

		vMostProbablyLabel = chooseMostProbablyLabel();

		return vMostProbablyLabel;
	}

	protected void calculateLabelProbability(int pIndex, String vTerm,
			SequenceLabel vSequenceLabelProcessed,
			FilterProbabilityHandler vFilterProbability,
			String vFilterInstanceId) {
		if (!vFilterInstanceId.isEmpty() && vFilterProbability.getInstanceFrequency(vFilterInstanceId) > 0) {

			int vFilterMostProbablyLabel = vFilterProbability.getMostProbablyLabel(vFilterInstanceId);

			if (LabelEncoding.isEntity(vFilterMostProbablyLabel)) {
				mLabelProbability[vFilterMostProbablyLabel] += 1;
				vFilterProbability.addToFilterStatisticForAssignedLabels(vTerm,
						LabelEncoding.isEntity(vSequenceLabelProcessed.getLabel(pIndex)), true);
			}
		}
	}

	protected int chooseMostProbablyLabel() {
		int vMostProbablyLabel;
		double vMaxVote = 0;
		int vFinalLabel = LabelEncoding.getOutsideLabel();
		for(int i = 0; i < mLabelProbability.length; i++) {
			if(vMaxVote < mLabelProbability[i]) {
				vMaxVote = mLabelProbability[i];
				vFinalLabel = i;
			}
		}
		vMostProbablyLabel = vFinalLabel;
		return vMostProbablyLabel;
	}
}
