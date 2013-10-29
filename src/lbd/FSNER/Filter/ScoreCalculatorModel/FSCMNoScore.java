package lbd.FSNER.Filter.ScoreCalculatorModel;

import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.data.handler.ISequence;

public class FSCMNoScore extends AbstractFilterScoreCalculatorModel{

	private static final long serialVersionUID = 1L;

	@Override
	protected double calculateScore(ISequence pSequenceLabel, int pIndex) {
		return (1);
	}

}
