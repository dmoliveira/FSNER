package lbd.FSNER.LabelFile.LabelCalculatorModel;

import java.util.ArrayList;
import java.util.HashMap;

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
	protected double filterProbability = 0.0;
	protected double alpha = 0.0; //0.8
	protected final double SCORE_THRESHOLD = 0;
	protected final double COMMON_TERM_PERCENTAGE_THRESHOLD = 1.0;
	protected boolean showProbabilityForLabel = false;

	public LCMSumScore(AbstractTermRestrictionChecker termRestrictionChecker) {
		super(termRestrictionChecker);
	}

	@Override
	public int calculateMostProbablyLabel(int index,
			HashMap<String, SequenceLabel> proccessedSequenceMap,
			ArrayList<AbstractDataPreprocessor> dataProcessorList,
			ArrayList<AbstractFilter> filterList) {

		int mostProbablyLabel = LabelEncoding.getOutsideLabel();
		int lastFilterPreprocessingTypeNameIndex = -1;
		double score = SCORE_THRESHOLD;

		boolean wasFilterActivated = false;

		String filterInstanceId;
		String term = Symbol.EMPTY;

		SequenceLabel sequenceLabelProcessed = null;
		FilterProbability filterProbability = null;
		AbstractDataPreprocessor dataPreprocessor = null;

		mLabelProbability = new double [LabelEncoding.getAlphabetSize()];
		mNormalizationFactor  = new int[LabelEncoding.getAlphabetSize()];

		double vFilterLabelMaxProbability;
		int vFilterLabelChoosed;

		for(AbstractFilter filter : filterList) {

			if(lastFilterPreprocessingTypeNameIndex != filter.getFilterPreprocessingTypeNameIndex()) {

				dataPreprocessor = dataProcessorList.get(filter.getFilterPreprocessingTypeNameIndex());
				sequenceLabelProcessed = proccessedSequenceMap.get(filter.getPreprocesingTypeName());
				lastFilterPreprocessingTypeNameIndex = filter.getFilterPreprocessingTypeNameIndex();

				term = sequenceLabelProcessed.getTerm(index);
			}

			if(filter.getFilterState() == FilterState.Active &&
					(!isUnrealibleSituation || filter.isToUseFilterInUnreliableSituation()) &&
					dataPreprocessor.getCommonTermProbability(term) < COMMON_TERM_PERCENTAGE_THRESHOLD) {// <

				//-- Get filter instance id determined by the index
				filterProbability = filter.getFilterProbability();
				filterInstanceId = filter.getSequenceInstanceId(sequenceLabelProcessed, index);

				if((!filterInstanceId.isEmpty() &&
						(!filter.considerFilterProbability() ||
								(filterProbability.getProbability(filterInstanceId) > this.alpha && // >
										filterProbability.getInstanceFrequency(filterInstanceId) > filter.getInstanceFrequencyThreshould())))) {

					score = filter.calculateScore(sequenceLabelProcessed, index);

					//if(score > SCORE_THRESHOLD) {

					vFilterLabelMaxProbability = 0;
					vFilterLabelChoosed = LabelEncoding.BILOU.Outside.ordinal();
					for(int i = 0; i < LabelEncoding.getAlphabetSize(); i++) {
						mLabelProbability[i] += filterProbability.getProbability(filterInstanceId, i);
						mNormalizationFactor[i]++;
						if(vFilterLabelMaxProbability < filterProbability.getProbability(filterInstanceId, i)) {
							vFilterLabelMaxProbability = filterProbability.getProbability(filterInstanceId, i);
							vFilterLabelChoosed = i;
						}
					}

					wasFilterActivated = true;

					filterProbability.addToFilterStatisticForAssignedLabels(term,
							LabelEncoding.isEntity(sequenceLabelProcessed.getLabel(index)), LabelEncoding.isEntity(vFilterLabelChoosed));
					//}
				}
			}
		}

		if(wasFilterActivated) {
			mostProbablyLabel = getMostProbablyLabel(term, mLabelProbability, mNormalizationFactor);
		}

		return (mostProbablyLabel);
	}

	protected int getMostProbablyLabel(String term, double [] labelProbability, int [] normalizationFactor) {

		int mostProbablyLabel = LabelEncoding.getOutsideLabel();
		double maxProbability = -1;
		double probability = -1;

		if(showProbabilityForLabel && term.length() > 2) {
			System.out.println("Term: " + term);
		}

		for(int i = 0; i < LabelEncoding.getAlphabetSize(); i++) {

			probability = labelProbability[i]/normalizationFactor[i];
			if(showProbabilityForLabel && term.length() > 2) {
				System.out.println("Prob(" + LabelEncoding.BILOU.values()[i].name() + "): " + probability);
			}

			if(probability > maxProbability) {
				maxProbability = probability;
				mostProbablyLabel = i;
			}
		}

		if(showProbabilityForLabel && term.length() > 2) {
			System.out.println(">> Chosen Label: " + LabelEncoding.BILOU.values()[mostProbablyLabel].name());
		}
		if(showProbabilityForLabel && term.length() > 2) {
			System.out.println("-----------------");
		}

		return((maxProbability >= filterProbability)? mostProbablyLabel : LabelEncoding.getOutsideLabel());
	}

	public void setFilterProbability(double filterProbability) {
		this.filterProbability = filterProbability;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

}
