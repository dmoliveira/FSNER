package lbd.FSNER.LabelFile.ScoreCalculatorModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lbd.FSNER.Component.Statistic.FilterProbabilityHandler;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractLabelFileScoreCalculatorModel;
import lbd.data.handler.ISequence;
import lbd.fsner.label.encoding.Label;

public class LFSCMSimpleAndScore extends AbstractLabelFileScoreCalculatorModel{

	private static final long serialVersionUID = 1L;

	//-- Filter Restriction
	protected final double FILTER_PROBABILITY = 0.7;//0.7
	protected final double SCORE_THRESHOLD = 0;
	protected final int INSTANCE_FREQUENCY_THRESHOLD = 1;//1 -- Not Used more, moved to Filter
	protected final double FILTER_THRESHOLD = 1;

	//-- Common Term Restriction
	protected final double COMMON_TERM_PERCENTAGE_THRESHOLD = 1.0;//0.5

	@Override
	public double calculateScore(int pIndex,
			ISequence pSequence,
			Map<String, ISequence> pPreprocessedSequenceMap,
			List<AbstractDataPreprocessor> pDataProcessorList,
			List<AbstractFilter> pFilterList) {

		int filterActiveNumber = 0;
		double filterScore = SCORE_THRESHOLD;
		double finalScore = SCORE_THRESHOLD;
		String filterInstanceIndexId;

		String term;
		int label = -1;

		ISequence sequenceLabelProcessed = null;
		FilterProbabilityHandler filterProbability;
		AbstractDataPreprocessor dataPreprocessor;
		ArrayList<AbstractFilter> filterActiveList = new ArrayList<AbstractFilter>();

		for(AbstractFilter filter : pFilterList) {

			//-- Get common term percentage
			dataPreprocessor = pDataProcessorList.get(filter.getFilterPreprocessingTypeIndex());
			sequenceLabelProcessed = pPreprocessedSequenceMap.get(filter.getPreprocesingTypeName());

			term = sequenceLabelProcessed.getToken(pIndex);
			label = sequenceLabelProcessed.getLabel(pIndex);

			if(!isUnrealibleSituation &&
					dataPreprocessor.getCommonTokenProbability(term) < COMMON_TERM_PERCENTAGE_THRESHOLD) {

				//-- Get filter instance id determined by the index
				filterProbability = filter.getFilterProbability();
				filterInstanceIndexId = filter.getSequenceInstanceId(pSequence, sequenceLabelProcessed, pIndex);

				if(!filterInstanceIndexId.isEmpty() && filterProbability.getProbability(filterInstanceIndexId) >= FILTER_PROBABILITY) {

					filterScore = filter.calculateScore(sequenceLabelProcessed, pIndex);

					if(filterScore > SCORE_THRESHOLD) {

						filterActiveNumber++;
						filterActiveList.add(filter);

						//if(filterActiveNumber/((double)filterList.size()) >= FILTER_THRESHOLD) {
						if(filterActiveNumber > FILTER_THRESHOLD) {

							finalScore = 1;
							addToFilterStatistic(term, filterActiveList, Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(label)));

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
