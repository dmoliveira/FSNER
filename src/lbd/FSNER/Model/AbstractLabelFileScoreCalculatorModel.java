package lbd.FSNER.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import lbd.FSNER.Component.SequenceLabel;
import lbd.data.handler.DataSequence;


public abstract class AbstractLabelFileScoreCalculatorModel implements Serializable {

	private static final long serialVersionUID = 1L;
	protected boolean isUnrealibleSituation;

	public abstract double calculateScore(int index,
			DataSequence pSequence,
			Map<String, SequenceLabel> proccessedSequenceMap,
			ArrayList<AbstractDataPreprocessor> dataProcessorList,
			ArrayList<AbstractFilter> filterList);

	public void setIsUnrealibleSituation(boolean isUnrealibleSituation) {
		this.isUnrealibleSituation = isUnrealibleSituation;
	}

	public boolean isUnrealibleSituation() {
		return(isUnrealibleSituation);
	}
}
