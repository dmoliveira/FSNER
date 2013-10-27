package lbd.FSNER.LabelFile.ScoreCalculatorModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.Statistic.FilterProbabilityHandler;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilter.FilterState;
import lbd.FSNER.Model.AbstractLabelFileScoreCalculatorModel;
import lbd.data.handler.ISequence;
import lbd.fsner.label.encoding.Label;

public class LFSCMAndScore extends AbstractLabelFileScoreCalculatorModel{

	private static final long serialVersionUID = 1L;

	//-- Filter Restriction
	protected final double FILTER_PROBABILITY = 0.7;//0.7
	protected final double SCORE_THRESHOLD = 0;
	protected final int INSTANCE_FREQUENCY_THRESHOLD = 1;//1 -- Not Used more, moved to Filter
	protected final int FILTER_TIMES_THRESHOLD = 1;

	//-- Common Term Restriction
	protected final double COMMON_TERM_PERCENTAGE_THRESHOLD = 1.0;//0.5

	@Override
	public double calculateScore(int index,
			ISequence pSequence,
			Map<String, SequenceLabel> proccessedSequenceMap,
			ArrayList<AbstractDataPreprocessor> dataProcessorList,
			ArrayList<AbstractFilter> filterList) {

		double filterScore = SCORE_THRESHOLD;
		double finalScore = SCORE_THRESHOLD;
		String filterInstanceIndexId;

		String term;
		int label = -1;

		SequenceLabel sequenceLabelProcessed;
		FilterProbabilityHandler filterProbability;
		AbstractDataPreprocessor dataPreprocessor;

		HashMap<Integer, HashMap<String, FilterProbabilityHandler>> filterClassNameMap = new HashMap<Integer, HashMap<String, FilterProbabilityHandler>>();

		for(AbstractFilter filter : filterList) {

			if(filterClassNameMap.containsKey(filter.getFilterPreprocessingTypeIndex()) &&
					filterClassNameMap.get(filter.getFilterPreprocessingTypeIndex()).containsKey(filter.getFilterClassName())) {
				continue;
			}

			//-- Get common term percentage
			dataPreprocessor = dataProcessorList.get(filter.getFilterPreprocessingTypeIndex());
			sequenceLabelProcessed = proccessedSequenceMap.get(filter.getPreprocesingTypeName());

			term = sequenceLabelProcessed.getTerm(index);
			label = sequenceLabelProcessed.getLabel(index);

			if(filter.getFilterState() == FilterState.Active && !isUnrealibleSituation &&
					dataPreprocessor.getCommonTermProbability(term) < COMMON_TERM_PERCENTAGE_THRESHOLD) {// <

				//-- Get filter instance id determined by the index
				filterProbability = filter.getFilterProbability();
				filterInstanceIndexId = filter.getSequenceInstanceId(pSequence, sequenceLabelProcessed, index);

				if(!filterInstanceIndexId.isEmpty() && filterProbability.getProbability(filterInstanceIndexId) >= FILTER_PROBABILITY) {

					filterScore = filter.calculateScore(sequenceLabelProcessed, index);

					if(filterScore > SCORE_THRESHOLD) {

						if(!filterClassNameMap.containsKey(filter.getFilterPreprocessingTypeIndex())) {
							filterClassNameMap.put(filter.getFilterPreprocessingTypeIndex(), new HashMap<String, FilterProbabilityHandler>());
						}

						filterClassNameMap.get(filter.getFilterPreprocessingTypeIndex()).put(filter.getFilterClassName(), filterProbability);

						if(maxFilterTimeActivated(filterClassNameMap)) {

							finalScore = 1;

							addToFilterStatistic(term, filterClassNameMap.get(filter.getFilterPreprocessingTypeIndex()),
									Parameters.DataHandler.mLabelEncoding.isEntity(Label.getLabel(label)));
							break;
						}
					}
				}
			}
		}

		return (finalScore);
	}

	protected boolean maxFilterTimeActivated(HashMap<Integer, HashMap<String, FilterProbabilityHandler>> filterClassNameMap) {

		Iterator<Entry<Integer, HashMap<String, FilterProbabilityHandler>>> ite = filterClassNameMap.entrySet().iterator();

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

	protected void addToFilterStatistic(String pTerm, HashMap<String, FilterProbabilityHandler> filterClassName,
			boolean isEntity) {

		Iterator<Entry<String, FilterProbabilityHandler>> ite = filterClassName.entrySet().iterator();
		FilterProbabilityHandler filterProbability;

		while(ite.hasNext()) {
			filterProbability = ite.next().getValue();
			filterProbability.addToFilterStatisticForAssignedLabels(pTerm, isEntity, true);
		}
	}
}
