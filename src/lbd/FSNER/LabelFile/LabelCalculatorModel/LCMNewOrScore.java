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

public class LCMNewOrScore extends AbstractLabelFileLabelCalculatorModel{

	private static final long serialVersionUID = 1L;

	//-- Filter Restriction
	protected final double SCORE_THRESHOLD = 0;

	//-- Common Term Restriction
	protected final double COMMON_TERM_PERCENTAGE_THRESHOLD = 1.0;//0.5

	public LCMNewOrScore(AbstractTermRestrictionChecker termRestrictionChecker) {
		super(termRestrictionChecker);
	}

	@Override
	public int calculateMostProbablyLabel(int pIndex,
			Map<String, SequenceLabel> pProccessedSequenceMap,
			List<AbstractDataPreprocessor> pDataProcessorList,
			List<AbstractFilter> pFilterList) {

		mLabelProbability = new double[LabelEncoding.getAlphabetSize()];
		mNormalizationFactor = new int[LabelEncoding.getAlphabetSize()];

		int [] vLabelVoting = new int [LabelEncoding.getAlphabetSize()];
		int vTotalScoreNumber = 0;
		String vFilterInstanceId;
		String vTerm = "";

		SequenceLabel vSequenceLabelProcessed = null;
		FilterProbability vFilterProbability = null;
		AbstractDataPreprocessor vDataPreprocessor = null;

		int vLastFilterPreprocessingTypeNameIndex = -1;

		for(AbstractFilter cFilter : pFilterList) {

			if(vLastFilterPreprocessingTypeNameIndex != cFilter.getFilterPreprocessingTypeNameIndex()) {

				vDataPreprocessor = pDataProcessorList.get(cFilter.getFilterPreprocessingTypeNameIndex());
				vSequenceLabelProcessed = pProccessedSequenceMap.get(cFilter.getPreprocesingTypeName());
				vLastFilterPreprocessingTypeNameIndex = cFilter.getFilterPreprocessingTypeNameIndex();

				vTerm = vSequenceLabelProcessed.getTerm(pIndex);
			}

			if(cFilter.getFilterState() == FilterState.Active &&
					(!mIsUnrealibleSituation || cFilter.isToUseFilterInUnreliableSituation()) &&
					vDataPreprocessor.getCommonTermProbability(vTerm) < COMMON_TERM_PERCENTAGE_THRESHOLD) {

				//-- Get filter instance id determined by the index
				vFilterProbability = cFilter.getFilterProbability();
				vFilterInstanceId = cFilter.getSequenceInstanceId(vSequenceLabelProcessed, pIndex);

				if(!vFilterInstanceId.isEmpty()) {
					vTotalScoreNumber++;
				}

				if(!vFilterInstanceId.isEmpty() &&
						(!cFilter.considerFilterProbability() ||
								vFilterProbability.getInstanceFrequency(vFilterInstanceId) > cFilter.getInstanceFrequencyThreshould())) {

					int vLabel = LabelEncoding.BILOU.Outside.ordinal();
					double vProbability = 0;

					for(int cLabel = 0; cLabel < LabelEncoding.getAlphabetSize(); cLabel++) {
						if(vFilterProbability.getProbability(vFilterInstanceId, cLabel) > vProbability) {
							vLabel = cLabel;
							vProbability = vFilterProbability.getProbability(vFilterInstanceId, cLabel);
						}

						mLabelProbability[cLabel] += vFilterProbability.getProbability(vFilterInstanceId, cLabel);
						mNormalizationFactor[cLabel] ++;
					}

					vFilterProbability.addToFilterStatisticForAssignedLabels(vTerm,
							LabelEncoding.isEntity(vSequenceLabelProcessed.getLabel(pIndex)), vLabel != LabelEncoding.BILOU.Outside.ordinal());
					vLabelVoting[vLabel]++;
				}
			}
		}

		if(vTotalScoreNumber == 0) {
			addAsUnknownTerm(vTerm);
		}

		return (getMostProbablyEntityLabel(vLabelVoting));
	}

	protected int getMostProbablyEntityLabel(int [] pLabelVoting) {
		int vLabelMostVoted = LabelEncoding.BILOU.Outside.ordinal();
		int vVotingNumber = 0;

		for(int cLabel = 0; cLabel < LabelEncoding.getAlphabetSize(); cLabel++) {
			if(cLabel != 3 && vVotingNumber < pLabelVoting[cLabel]) {
				vLabelMostVoted = cLabel;
				vVotingNumber = pLabelVoting[cLabel];
			}
		}

		return vLabelMostVoted;
	}

}
