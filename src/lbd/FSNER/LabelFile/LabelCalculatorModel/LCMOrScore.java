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

public class LCMOrScore extends AbstractLabelFileLabelCalculatorModel{

	//-- Filter Restriction
	protected final double FILTER_PROBABILITY = 0.90;//0.7
	protected final double SCORE_THRESHOLD = 0;
	protected final int INSTANCE_FREQUENCY_THRESHOLD = 1;//1 -- Not Used more, moved to Filter

	//-- Common Term Restriction
	protected final double COMMON_TERM_PERCENTAGE_THRESHOLD = 1.0;//0.5

	public LCMOrScore(AbstractTermRestrictionChecker termRestrictionChecker) {
		super(termRestrictionChecker);
	}

	@Override
	public int calculateMostProbablyLabel(int index,
			HashMap<String, SequenceLabel> proccessedSequenceMap,
			ArrayList<AbstractDataPreprocessor> dataProcessorList,
			ArrayList<AbstractFilter> filterList) {

		int mostProbablyLabel = LabelEncoding.getOutsideLabel();
		double score = SCORE_THRESHOLD;
		int totalScoreNumber = 0;
		String filterInstanceId;
		String term = "";

		SequenceLabel sequenceLabelProcessed = null;
		FilterProbability filterProbability = null;
		AbstractDataPreprocessor dataPreprocessor = null;

		int lastFilterPreprocessingTypeNameIndex = -1;

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

				if(!filterInstanceId.isEmpty()) {
					totalScoreNumber++;
				}

				if((!filterInstanceId.isEmpty() &&
						(!filter.considerFilterProbability() ||
								(filterProbability.getProbability(filterInstanceId) > FILTER_PROBABILITY && // >
										filterProbability.getInstanceFrequency(filterInstanceId) > filter.getInstanceFrequencyThreshould())))) {

					score = filter.calculateScore(sequenceLabelProcessed, index);

					if(score > SCORE_THRESHOLD) {

						filterProbability.addToFilterStatisticForAssignedLabels(term,
								LabelEncoding.isEntity(sequenceLabelProcessed.getLabel(index)), true);

						mostProbablyLabel = filterProbability.getMostProbablyLabel(filterInstanceId);

						break;
					}
				}
			}
		}

		if(totalScoreNumber == 0) {

			addAsUnknownTerm(term);

			/*if(proccessedSequenceMap.get(filterList.get(0).getPreprocesingTypeName()).getLabel(index) != 3) {
				System.out.println(term);
			}*/
		}

		return (mostProbablyLabel);
	}

}
