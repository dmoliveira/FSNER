package lbd.FSNER.LabelFile.ScoreCalculatorModel;

import java.util.List;
import java.util.Map;

import lbd.FSNER.Component.Statistic.FilterProbabilityHandler;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilter.FilterState;
import lbd.FSNER.Model.AbstractLabelFileScoreCalculatorModel;
import lbd.data.handler.ISequence;
import lbd.fsner.label.encoding.Label;

public class LFSCMOrScore extends AbstractLabelFileScoreCalculatorModel{

	private static final long serialVersionUID = 1L;

	//-- Filter Restriction
	protected final double FILTER_PROBABILITY = 0.90;//0.7
	protected final double SCORE_THRESHOLD = 0;
	protected final int INSTANCE_FREQUENCY_THRESHOLD = 1;//1 -- Not Used more, moved to Filter

	//-- Common Term Restriction
	protected final double COMMON_TERM_PERCENTAGE_THRESHOLD = 1.0;//0.5

	@Override
	public double calculateScore(int pIndex,
			ISequence pSequence,
			Map<String, ISequence> pPreprocessedSequenceMap,
			List<AbstractDataPreprocessor> pDataProcessorList,
			List<AbstractFilter> pFilterList) {

		double score = SCORE_THRESHOLD;
		String filterInstanceIndexId;
		String term;

		ISequence sequenceLabelProcessed;
		FilterProbabilityHandler filterProbability;
		AbstractDataPreprocessor dataPreprocessor;

		for(AbstractFilter filter : pFilterList) {

			//-- Get common term percentage
			dataPreprocessor = pDataProcessorList.get(filter.getFilterPreprocessingTypeIndex());
			sequenceLabelProcessed = pPreprocessedSequenceMap.get(filter.getPreprocesingTypeName());

			term = sequenceLabelProcessed.getToken(pIndex);

			if(filter.getFilterState() == FilterState.Active &&
					!isUnrealibleSituation &&
					dataPreprocessor.getCommonTokenProbability(term) < COMMON_TERM_PERCENTAGE_THRESHOLD) {// <

				//-- Get filter instance id determined by the index
				filterProbability = filter.getFilterProbability();
				filterInstanceIndexId = filter.getSequenceInstanceId(pSequence, sequenceLabelProcessed, pIndex);

				if(!filterInstanceIndexId.isEmpty() && filterProbability.getProbability(filterInstanceIndexId) > FILTER_PROBABILITY) {

					score = filter.calculateScore(sequenceLabelProcessed, pIndex);

					if(score > SCORE_THRESHOLD) {

						filterProbability.addToFilterStatisticForAssignedLabels(term,
								Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(sequenceLabelProcessed.getLabel(pIndex))), true);

						break;
					}
				}
			}
		}

		return (score);
	}

}
