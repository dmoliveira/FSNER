package lbd.FSNER.LabelFile.ScoreCalculatorModel;

import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.Statistic.FilterProbability;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilter.FilterState;
import lbd.FSNER.Model.AbstractLabelFileScoreCalculatorModel;
import lbd.FSNER.Utils.LabelEncoding;

public class LFSCMOrScore extends AbstractLabelFileScoreCalculatorModel{

	//-- Filter Restriction
	protected final double FILTER_PROBABILITY = 0.90;//0.7
	protected final double SCORE_THRESHOLD = 0;
	protected final int INSTANCE_FREQUENCY_THRESHOLD = 1;//1 -- Not Used more, moved to Filter

	//-- Common Term Restriction
	protected final double COMMON_TERM_PERCENTAGE_THRESHOLD = 1.0;//0.5

	@Override
	public double calculateScore(int index,
			HashMap<String, SequenceLabel> proccessedSequenceMap,
			ArrayList<AbstractDataPreprocessor> dataProcessorList,
			ArrayList<AbstractFilter> filterList) {

		double score = SCORE_THRESHOLD;
		String filterInstanceIndexId;
		String term;

		SequenceLabel sequenceLabelProcessed;
		FilterProbability filterProbability;
		AbstractDataPreprocessor dataPreprocessor;

		for(AbstractFilter filter : filterList) {

			//-- Get common term percentage
			dataPreprocessor = dataProcessorList.get(filter.getFilterPreprocessingTypeNameIndex());
			sequenceLabelProcessed = proccessedSequenceMap.get(filter.getPreprocesingTypeName());

			term = sequenceLabelProcessed.getTerm(index);

			if(filter.getFilterState() == FilterState.Active &&
					(!isUnrealibleSituation || filter.isToUseFilterInUnreliableSituation()) &&
					dataPreprocessor.getCommonTermProbability(term) < COMMON_TERM_PERCENTAGE_THRESHOLD) {// <

				//-- Get filter instance id determined by the index
				filterProbability = filter.getFilterProbability();
				filterInstanceIndexId = filter.getSequenceInstanceId(sequenceLabelProcessed, index);

				if((!filterInstanceIndexId.isEmpty() &&
						(!filter.considerFilterProbability() ||
								(filterProbability.getProbability(filterInstanceIndexId) > FILTER_PROBABILITY && // >
										filterProbability.getInstanceFrequency(filterInstanceIndexId) > filter.getInstanceFrequencyThreshould())))) {

					score = filter.calculateScore(sequenceLabelProcessed, index);

					if(score > SCORE_THRESHOLD) {

						filterProbability.addToFilterStatisticForAssignedLabels(term,
								LabelEncoding.isEntity(sequenceLabelProcessed.getLabel(index)), true);

						break;
					}
				}
			}
		}

		return (score);
	}

}
