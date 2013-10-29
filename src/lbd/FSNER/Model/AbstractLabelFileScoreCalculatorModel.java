package lbd.FSNER.Model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lbd.data.handler.ISequence;


public abstract class AbstractLabelFileScoreCalculatorModel implements Serializable {

	private static final long serialVersionUID = 1L;
	protected boolean isUnrealibleSituation;

	public abstract double calculateScore(int pIndex,
			ISequence pSequence,
			Map<String, ISequence> pPreprocessedSequenceMap,
			List<AbstractDataPreprocessor> pDataProcessorList,
			List<AbstractFilter> pFilterList);
}
