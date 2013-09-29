package lbd.FSNER.Filter.ScoreCalculatorModel;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;

public class FSCMNoScore extends AbstractFilterScoreCalculatorModel{

	private static final long serialVersionUID = 1L;

	@Override
	protected double calculateScore(SequenceLabel sequenceLabel, int index) {
		return (1);
	}

}
