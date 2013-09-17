package lbd.FSNER.Model;

import java.io.Serializable;

import lbd.FSNER.Component.SequenceLabel;

public abstract class AbstractFilterScoreCalculatorModel implements Serializable {

	private static final long serialVersionUID = 1L;

	public double calculateScoreInLoad(SequenceLabel sequenceLabel, int index) {
		return(calculateScore(sequenceLabel, index));
	}

	public double calculateScoreInLabel(SequenceLabel sequenceLabel, int index) {
		return(calculateScore(sequenceLabel, index));
	}

	protected abstract double calculateScore(SequenceLabel sequenceLabel, int index);
}
