package lbd.FSNER.LabelFile.ScoreCalculatorModel;

import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.Statistic.FilterProbability;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractLabelFileScoreCalculatorModel;
import lbd.FSNER.Utils.LabelEncoding;

public class LFSCMSimpleAndScore extends AbstractLabelFileScoreCalculatorModel{

	//-- Filter Restriction
	protected final double FILTER_PROBABILITY = 0.7;//0.7
	protected final double SCORE_THRESHOLD = 0;
	protected final int INSTANCE_FREQUENCY_THRESHOLD = 1;//1 -- Not Used more, moved to Filter
	protected final double FILTER_THRESHOLD = 1;

	//-- Common Term Restriction
	protected final double COMMON_TERM_PERCENTAGE_THRESHOLD = 1.0;//0.5

	@Override
	public double calculateScore(int index,
			HashMap<String, SequenceLabel> proccessedSequenceMap,
			ArrayList<AbstractDataPreprocessor> dataProcessorList,
			ArrayList<AbstractFilter> filterList) {

		int filterActiveNumber = 0;
		double filterScore = SCORE_THRESHOLD;
		double finalScore = SCORE_THRESHOLD;
		String filterInstanceIndexId;

		String term;
		int label = -1;

		SequenceLabel sequenceLabelProcessed = null;
		FilterProbability filterProbability;
		AbstractDataPreprocessor dataPreprocessor;
		ArrayList<AbstractFilter> filterActiveList = new ArrayList<AbstractFilter>();

		for(AbstractFilter filter : filterList) {

			//-- Get common term percentage
			dataPreprocessor = dataProcessorList.get(filter.getFilterPreprocessingTypeNameIndex());
			sequenceLabelProcessed = proccessedSequenceMap.get(filter.getPreprocesingTypeName());

			term = sequenceLabelProcessed.getTerm(index);
			label = sequenceLabelProcessed.getLabel(index);

			if((!isUnrealibleSituation || filter.isToUseFilterInUnreliableSituation()) &&
					dataPreprocessor.getCommonTermProbability(term) < COMMON_TERM_PERCENTAGE_THRESHOLD) {

				//-- Get filter instance id determined by the index
				filterProbability = filter.getFilterProbability();
				filterInstanceIndexId = filter.getSequenceInstanceId(sequenceLabelProcessed, index);

				if((!filterInstanceIndexId.isEmpty() &&
						(!filter.considerFilterProbability() ||
								(filterProbability.getProbability(filterInstanceIndexId) >= FILTER_PROBABILITY &&
								filterProbability.getInstanceFrequency(filterInstanceIndexId) > filter.getInstanceFrequencyThreshould())))) {

					filterScore = filter.calculateScore(sequenceLabelProcessed, index);

					if(filterScore > SCORE_THRESHOLD) {

						filterActiveNumber++;
						filterActiveList.add(filter);

						//if(filterActiveNumber/((double)filterList.size()) >= FILTER_THRESHOLD) {
						if(filterActiveNumber > FILTER_THRESHOLD) {

							finalScore = 1;
							addToFilterStatistic(term, filterActiveList, LabelEncoding.isEntity(label));

							/*System.out.println("FA#" + filterActiveNumber + " sz(" + filterList.size() + ") = " +
									filterActiveNumber/((double)filterList.size()) +
									((LabelEncoding.isEntity(sequenceLabelProcessed.getLabel(index)))? "Right!" : "Wrong!"));*/
							break;
						}
					}
				}
			}
		}

		return (finalScore);
	}

	protected void addToFilterStatistic(String pTerm, ArrayList<AbstractFilter> filterActiveList,
			boolean isEntity) {

		for(AbstractFilter filter : filterActiveList) {
			filter.getFilterProbability().addToFilterStatisticForAssignedLabels(pTerm, isEntity, true);
		}
	}
}
