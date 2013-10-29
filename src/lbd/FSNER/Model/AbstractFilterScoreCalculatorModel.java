package lbd.FSNER.Model;

import java.io.Serializable;

import lbd.data.handler.ISequence;

public abstract class AbstractFilterScoreCalculatorModel implements Serializable {

	private static final long serialVersionUID = 1L;

	public double calculateScoreTraining(ISequence pSequence, int pIndex) {
		return(calculateScore(pSequence, pIndex));
	}

	public double calculateScoreLabeling(ISequence pSequence, int pIndex) {
		return(calculateScore(pSequence, pIndex));
	}

	protected abstract double calculateScore(ISequence pSequence, int pIndex);
}
