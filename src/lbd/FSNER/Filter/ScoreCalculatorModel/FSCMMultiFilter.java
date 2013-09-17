package lbd.FSNER.Filter.ScoreCalculatorModel;

import java.util.ArrayList;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;

public class FSCMMultiFilter extends AbstractFilterScoreCalculatorModel{

	protected ArrayList<AbstractFilter> filterList;
	
	@Override
	protected double calculateScore(SequenceLabel sequenceLabel, int index) {
		
		double score = 0;
		int numberFilterActivated = 0;
		
		for(AbstractFilter activity : filterList) {

			score = activity.calculateScore(sequenceLabel, index);
			
			if(score > 0)
				numberFilterActivated++;
			else
				break;
		}
		
		return (numberFilterActivated == filterList.size())? 1 : 0;
	}
	
	public void setFilterList(ArrayList<AbstractFilter> filterList) {
		this.filterList = filterList;
	}

}
