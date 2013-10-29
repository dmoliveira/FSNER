package lbd.FSNER.Filter.ScoreCalculatorModel;

import lbd.FSNER.Filter.Component.Entity;
import lbd.FSNER.Filter.Component.Term;
import lbd.data.handler.ISequence;

public class FSCMEntityless extends FSCMEntity{

	private static final long serialVersionUID = 1L;

	@Override
	public double calculateScoreLabeling(ISequence sequenceLabel, int index) {

		return(calculateScoreEntityLess(sequenceLabel, index));
	}

	public double calculateScoreEntityLess(ISequence sequenceLabel, int index) {

		double termsTotalScore = 0;

		for(Entity entity : entityList) {

			termsTotalScore = calculateScoreEntityLessForEntity(entity, sequenceLabel, index);

			if(termsTotalScore > entity.getMinScore()) {
				break;
			}

			termsTotalScore = 0;
		}

		return(termsTotalScore);
	}

	public double calculateScoreEntityLessForEntity(Entity entity, ISequence sequenceLabel, int index) {

		double termsTotalScore = 0;

		Term term;

		for(int i = 0; i < sequenceLabel.length(); i++){

			term = entity.getTerm(sequenceLabel.getToken(i));

			if(term != null && !term.getId().equals(entity.getId())) {
				termsTotalScore += term.getScore();
			}
		}

		return(termsTotalScore);
	}
}
