package lbd.FSNER.Filter.ScoreCalculatorModel;

import java.util.ArrayList;

import lbd.FSNER.Filter.Component.Entity;
import lbd.FSNER.Filter.Component.Term;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.EntityUtils;
import lbd.data.handler.ISequence;

public class FSCMEntity extends AbstractFilterScoreCalculatorModel{

	private static final long serialVersionUID = 1L;
	protected ArrayList<Entity> entityList;

	@Override
	public double calculateScoreLabeling(ISequence pSequenceLabel, int pIndex) {

		double sequenceScore = calculateScore(pSequenceLabel, pIndex);

		Entity entity = EntityUtils.getEntity(pSequenceLabel.getToken(pIndex), entityList);

		sequenceScore = (entity != null && sequenceScore > entity.getMinScore())? sequenceScore : 0;

		return(sequenceScore);
	}

	@Override
	public double calculateScore(ISequence sequenceLabel, int index) {

		double termsTotalScore = 0;

		Entity entity = EntityUtils.getEntity(sequenceLabel.getToken(index), entityList);

		Term term;

		if(entity != null) {
			for(int i = 0; i < sequenceLabel.length(); i++){

				term = entity.getTerm(sequenceLabel.getToken(i));

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
