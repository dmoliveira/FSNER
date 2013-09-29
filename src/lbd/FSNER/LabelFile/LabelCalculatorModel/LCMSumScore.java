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
import lbd.FSNER.Utils.Symbol;

public class LCMSumScore extends AbstractLabelFileLabelCalculatorModel{

	private static final long serialVersionUID = 1L;

	//-- Filter Restriction
	protected double vFilterProbability = 0.0;
	protected double vAlpha = 0.0; //0.8
	protected final double SCORE_THRESHOLD = 0;
	protected final double COMMON_TERM_PERCENTAGE_THRESHOLD = 1.0;
	protected boolean vShowProbabilityForLabel = false;

	public LCMSumScore(AbstractTermRestrictionChecker pTermRestrictionChecker) {
		super(pTermRestrictionChecker);
	}

	@Override
	public int calculateMostProbablyLabel(int pIndex,
			Map<String, SequenceLabel> pProccessedSequenceMap,
			List<AbstractDataPreprocessor> pDataProcessorList,
			List<AbstractFilter> pFilterList) {

		int vMostProbablyLabel = LabelEncoding.getOutsideLabel();
		int vLastFilterPreprocessingTypeNameIndex = -1;
		double vScore = SCORE_THRESHOLD;

		boolean vWasFilterActivated = false;

		String vFilterInstanceId;
		String vTerm = Symbol.EMPTY;

		SequenceLabel vSequenceLabelProcessed = null;
		FilterProbability vFilterProbability = null;
		AbstractDataPreprocessor vDataPreprocessor = null;

		mLabelProbability = new double [LabelEncoding.getAlphabetSize()];
		mNormalizationFactor  = new int[LabelEncoding.getAlphabetSize()];

		double vFilterLabelMaxProbability;
		int vFilterLabelChoosed;

		for(AbstractFilter cFilter : pFilterList) {

			if(vLastFilterPreprocessingTypeNameIndex != cFilter.getFilterPreprocessingTypeNameIndex()) {

				vDataPreprocessor = pDataProcessorList.get(cFilter.getFilterPreprocessingTypeNameIndex());
				vSequenceLabelProcessed = pProccessedSequenceMap.get(cFilter.getPreprocesingTypeName());
				vLastFilterPreprocessingTypeNameIndex = cFilter.getFilterPreprocessingTypeNameIndex();

				vTerm = vSequenceLabelProcessed.getTerm(pIndex);
			}

			if(cFilter.getFilterState() == FilterState.Active &&
					(!mIsUnrealibleSituation || cFilter.isToUseFilterInUnreliableSituation()) &&
					vDataPreprocessor.getCommonTermProbability(vTerm) < COMMON_TERM_PERCENTAGE_THRESHOLD) {// <

				//-- Get filter instance id determined by the index
				vFilterProbability = cFilter.getFilterProbability();
				vFilterInstanceId = cFilter.getSequenceInstanceId(vSequenceLabelProcessed, pIndex);

				if((!vFilterInstanceId.isEmpty() &&
						(!cFilter.considerFilterProbability() ||
								(vFilterProbability.getProbability(vFilterInstanceId) > this.vAlpha && // >
										vFilterProbability.getInstanceFrequency(vFilterInstanceId) > cFilter.getInstanceFrequencyThreshould())))) {

					vScore = cFilter.calculateScore(vSequenceLabelProcessed, pIndex);

					//if(score > SCORE_THRESHOLD) {

					vFilterLabelMaxProbability = 0;
					vFilterLabelChoosed = LabelEncoding.getOutsideLabel();
					for(int i = 0; i < LabelEncoding.getAlphabetSize(); i++) {
						//TODO: Test normalization only when prob. > 0
						mLabelProbability[i] += vFilterProbability.getProbability(vFilterInstanceId, i);
						mNormalizationFactor[i]++;
						if(vFilterLabelMaxProbability < vFilterProbability.getProbability(vFilterInstanceId, i)) {
							vFilterLabelMaxProbability = vFilterProbability.getProbability(vFilterInstanceId, i);
							vFilterLabelChoosed = i;
						}
					}

					vWasFilterActivated = true;

					vFilterProbability.addToFilterStatisticForAssignedLabels(vTerm,
							LabelEncoding.isEntity(vSequenceLabelProcessed.getLabel(pIndex)), LabelEncoding.isEntity(vFilterLabelChoosed));
					//}
				}
			}
		}

		if(vWasFilterActivated) {
			vMostProbablyLabel = getMostProbablyLabel(vTerm, mLabelProbability, mNormalizationFactor);
		}

		return (vMostProbablyLabel);
	}

	protected int getMostProbablyLabel(String term, double [] labelProbability, int [] normalizationFactor) {

		int mostProbablyLabel = LabelEncoding.getOutsideLabel();
		double maxProbability = -1;
		double probability = -1;

		if(vShowProbabilityForLabel && term.length() > 2) {
			System.out.println("Term: " + term);
		}

		for(int i = 0; i < LabelEncoding.getAlphabetSize(); i++) {

			probability = labelProbability[i]/normalizationFactor[i];
			if(vShowProbabilityForLabel && term.length() > 2) {
				System.out.println("Prob(" + LabelEncoding.BILOU.values()[i].name() + "): " + probability);
			}

			if(probability > maxProbability) {
				maxProbability = probability;
				mostProbablyLabel = i;
			}
		}

		if(vShowProbabilityForLabel && term.length() > 2) {
			System.out.println(">> Chosen Label: " + LabelEncoding.BILOU.values()[mostProbablyLabel].name());
		}
		if(vShowProbabilityForLabel && term.length() > 2) {
			System.out.println("-----------------");
		}

		return((maxProbability >= vFilterProbability)? mostProbablyLabel : LabelEncoding.getOutsideLabel());
	}

	public void setFilterProbability(double filterProbability) {
		this.vFilterProbability = filterProbability;
	}

	public void setAlpha(double alpha) {
		this.vAlpha = alpha;
	}

}
