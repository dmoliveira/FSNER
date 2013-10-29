package lbd.FSNER.Filter.ScoreCalculatorModel;

import java.util.List;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.data.handler.ISequence;

public class FSCMMultiFilter extends AbstractFilterScoreCalculatorModel{

	private static final long serialVersionUID = 1L;
	protected List<AbstractFilter> filterList;

	@Override
	protected double calculateScore(ISequence sequenceLabel, int index) {

		double score = 0;
		int numberFilterActivated = 0;

		for(AbstractFilter activity : filterList) {

			score = activity.calculateScore(sequenceLabel, index);

			if(score > 0) {
				numberFilterActivated++;
			} else {
				break;
			}
		}

		return (numberFilterActivated == filterList.size())? 1 : 0;
	}

	public void setFilterList(List<AbstractFilter> filterList) {
		this.filterList = filterList;
	}

}
