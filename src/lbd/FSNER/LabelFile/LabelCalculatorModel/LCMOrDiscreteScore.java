package lbd.FSNER.LabelFile.LabelCalculatorModel;

import java.util.List;
import java.util.Map;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.Statistic.FilterProbabilityHandler;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilter.FilterState;
import lbd.FSNER.Model.AbstractLabelFileLabelCalculatorModel;
import lbd.FSNER.Model.AbstractTermRestrictionChecker;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;
import lbd.fsner.label.encoding.Label;

public class LCMOrDiscreteScore extends AbstractLabelFileLabelCalculatorModel {

	private static final long serialVersionUID = 1L;

	// -- Common Term Restriction
	protected final double COMMON_TERM_PERCENTAGE_THRESHOLD = 1.0;

	public LCMOrDiscreteScore(AbstractTermRestrictionChecker pTermRestrictionChecker) {
		super(pTermRestrictionChecker);
	}

	@Override
	public int calculateMostProbablyLabelSub(int pIndex, ISequence pSequence,
			Map<String, SequenceLabel> pProccessedSequenceMap,
			List<AbstractDataPreprocessor> pDataProcessorList,
			List<AbstractFilter> pFilterList) {

		int vMostProbablyLabel = Parameters.DataHandler.mLabelEncoding.getOutsideLabel().ordinal();
		String vTerm = Symbol.EMPTY;

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

	protected void calculateLabelProbability(int pIndex, String pTerm,
			SequenceLabel pSequenceLabelProcessed,
			FilterProbabilityHandler pFilterProbability,
			String pFilterInstanceId) {

		if (!pFilterInstanceId.isEmpty() && pFilterProbability.getInstanceFrequency(pFilterInstanceId) > 0) {

			int vFilterMostProbablyLabel = pFilterProbability.getMostProbablyLabel(pFilterInstanceId);
			Label vLabel = Label.getLabel(vFilterMostProbablyLabel);

			if (Parameters.DataHandler.mLabelEncoding.isEntity(vLabel)) {
				mLabelProbability[vFilterMostProbablyLabel] += 1;
				pFilterProbability.addToFilterStatisticForAssignedLabels(pTerm,
						Parameters.DataHandler.mLabelEncoding.isEntity(Label.getLabel(pSequenceLabelProcessed.getLabel(pIndex))), true);
			}
		}
	}

	protected int chooseMostProbablyLabel() {
		int vMostProbablyLabel;
		double vMaxVote = 0;
		int vFinalLabel = Parameters.DataHandler.mLabelEncoding.getOutsideLabel().ordinal();
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
