package lbd.FSNER.Filter.ScoreCalculatorModel;

import java.util.ArrayList;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Filter.Component.Entity;
import lbd.FSNER.Filter.Component.Term;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.SupportEntity;

public class FSCMEntity extends AbstractFilterScoreCalculatorModel{

	private static final long serialVersionUID = 1L;
	protected ArrayList<Entity> entityList;

	@Override
	public double calculateScoreInLabel(SequenceLabel sequenceLabel, int index) {

		double sequenceScore = calculateScore(sequenceLabel, index);

		Entity entity = SupportEntity.getEntity(sequenceLabel.getTerm(index), entityList);

		sequenceScore = (entity != null && sequenceScore > entity.getMinScore())? sequenceScore : 0;

		return(sequenceScore);
	}

	@Override
	public double calculateScore(SequenceLabel sequenceLabel, int index) {

		double termsTotalScore = 0;

		Entity entity = SupportEntity.getEntity(sequenceLabel.getTerm(index), entityList);

		Term term;

		if(entity != null) {
			for(int i = 0; i < sequenceLabel.size(); i++){

				term = entity.getTerm(sequenceLabel.getTerm(i));

				if(term != null && !term.getId().equals(entity.getId())) {
					termsTotalScore += term.getScore();
				}
			}
		}

		return(termsTotalScore);
	}

	public void setEntityList(ArrayList<Entity> entityList) {
		this.entityList = entityList;
	}

	public void calculateAverageAndStandardDeviationScore(ArrayList<Entity> entityList) {
		for(Entity entity : entityList) {
			calculateAverageAndStandardDeviationScore(entity);
		}
	}

	protected void calculateAverageAndStandardDeviationScore(Entity entity) {

		double standardDeviation;
		double average;

		standardDeviation = 0;
		average = entity.getAverageScore()/entity.getScoreSequenceList().size();

		entity.setAverageScore(average);

		for(Double score : entity.getScoreSequenceList()) {
			standardDeviation += Math.pow(score - average, 2);
		}

		standardDeviation /= entity.getScoreSequenceList().size();
		standardDeviation = Math.sqrt(standardDeviation);
		entity.setStardardDeviationScore(standardDeviation);

		//-- Additional Calculation - Probability to be Entity
		entity.calculateProbabilityToBeEntity();
	}
}
