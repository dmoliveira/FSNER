package lbd.FSNER.LabelFile.ScoreCalculatorModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.Statistic.FilterProbability;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilter.FilterState;
import lbd.FSNER.Model.AbstractLabelFileScoreCalculatorModel;
import lbd.FSNER.Utils.LabelEncoding;

public class LFSCMAndScore extends AbstractLabelFileScoreCalculatorModel{

	//-- Filter Restriction
	protected final double FILTER_PROBABILITY = 0.7;//0.7
	protected final double SCORE_THRESHOLD = 0;
	protected final int INSTANCE_FREQUENCY_THRESHOLD = 1;//1 -- Not Used more, moved to Filter
	protected final int FILTER_TIMES_THRESHOLD = 1;

	//-- Common Term Restriction
	protected final double COMMON_TERM_PERCENTAGE_THRESHOLD = 1.0;//0.5

	@Override
	public double calculateScore(int index,
			HashMap<String, SequenceLabel> proccessedSequenceMap,
			ArrayList<AbstractDataPreprocessor> dataProcessorList,
			ArrayList<AbstractFilter> filterList) {

		double filterScore = SCORE_THRESHOLD;
		double finalScore = SCORE_THRESHOLD;
		String filterInstanceIndexId;

		String term;
		int label = -1;

		SequenceLabel sequenceLabelProcessed;
		FilterProbability filterProbability;
		AbstractDataPreprocessor dataPreprocessor;

		HashMap<Integer, HashMap<String, FilterProbability>> filterClassNameMap = new HashMap<Integer, HashMap<String, FilterProbability>>();

		for(AbstractFilter filter : filterList) {

			if(filterClassNameMap.containsKey(filter.getFilterPreprocessingTypeNameIndex()) &&
					filterClassNameMap.get(filter.getFilterPreprocessingTypeNameIndex()).containsKey(filter.getCommonFilterName())) {
				continue;
			}

			//-- Get common term percentage
			dataPreprocessor = dataProcessorList.get(filter.getFilterPreprocessingTypeNameIndex());
			sequenceLabelProcessed = proccessedSequenceMap.get(filter.getPreprocesingTypeName());

			term = sequenceLabelProcessed.getTerm(index);
			label = sequenceLabelProcessed.getLabel(index);

			if(filter.getFilterState() == FilterState.Active &&
					(!isUnrealibleSituation || filter.isToUseFilterInUnreliableSituation()) &&
					dataPreprocessor.getCommonTermProbability(term) < COMMON_TERM_PERCENTAGE_THRESHOLD) {// <

				//-- Get filter instance id determined by the index
				filterProbability = filter.getFilterProbability();
				filterInstanceIndexId = filter.getSequenceInstanceId(sequenceLabelProcessed, index);

				if((!filterInstanceIndexId.isEmpty() &&
						(!filter.considerFilterProbability() ||
								(filterProbability.getProbability(filterInstanceIndexId) >= FILTER_PROBABILITY &&
								filterProbability.getInstanceFrequency(filterInstanceIndexId) > filter.getInstanceFrequencyThreshould())))) {

					filterScore = filter.calculateScore(sequenceLabelProcessed, index);

					if(filterScore > SCORE_THRESHOLD) {

						if(!filterClassNameMap.containsKey(filter.getFilterPreprocessingTypeNameIndex())) {
							filterClassNameMap.put(filter.getFilterPreprocessingTypeNameIndex(), new HashMap<String, FilterProbability>());
						}

						filterClassNameMap.get(filter.getFilterPreprocessingTypeNameIndex()).put(filter.getCommonFilterName(), filterProbability);

						if(maxFilterTimeActivated(filterClassNameMap)) {

							finalScore = 1;

							addToFilterStatistic(term, filterClassNameMap.get(filter.getFilterPreprocessingTypeNameIndex()),
									LabelEncoding.isEntity(label));
							break;
						}
					}
				}
			}
		}

		return (finalScore);
	}

	protected boolean maxFilterTimeActivated(HashMap<Integer, HashMap<String, FilterProbability>> filterClassNameMap) {

		Iterator<Entry<Integer, HashMap<String, FilterProbability>>> ite = filterClassNameMap.entrySet().iterator();

		boolean isFilterSurpassThreshold = false;

		if(filterClassNameMap.containsKey(0) && filterClassNameMap.get(0).size() > 0) {
			return(true);
		}

		while(ite.hasNext()) {
			if(ite.next().getValue().size() > FILTER_TIMES_THRESHOLD) {
				isFilterSurpassThreshold = true;
				break;
			}
		}

		return(isFilterSurpassThreshold);
	}

	protected void addToFilterStatistic(String pTerm, HashMap<String, FilterProbability> filterClassName,
			boolean isEntity) {

		Iterator<Entry<String, FilterProbability>> ite = filterClassName.entrySet().iterator();
		FilterProbability filterProbability;

		while(ite.hasNext()) {
			filterProbability = ite.next().getValue();
			filterProbability.addToFilterStatisticForAssignedLabels(pTerm, isEntity, true);
		}
	}
}
