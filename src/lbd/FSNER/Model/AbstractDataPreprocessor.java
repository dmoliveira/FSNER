package lbd.FSNER.Model;

import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.SequenceLabelElement;
import lbd.FSNER.Component.Statistic.SimpleFilterProbability;
import lbd.data.handler.DataSequence;

public abstract class AbstractDataPreprocessor extends AbstractActivity {

	private static final long serialVersionUID = 1L;

	protected HashMap<String, SimpleFilterProbability> mCommonTermMap;

	public AbstractDataPreprocessor(String pActivityName, String pInitializeFile) {
		super(pActivityName);

		mInitializeFile = pInitializeFile;
		mCommonTermMap = new HashMap<String, SimpleFilterProbability>();
	}

	public SequenceLabel preprocessingSequence(DataSequence pSequence) {

		SequenceLabel vSequenceLabel = new SequenceLabel();

		for(int i = 0; i < pSequence.length(); i++) {
			vSequenceLabel.add(preprocessingTerm((String)pSequence.x(i), pSequence.y(i)));
		}

		return (vSequenceLabel);
	}

	public abstract SequenceLabelElement preprocessingTerm(String pTerm, int pLabel);

	public void computeCommonTermsInSequence(SequenceLabel pSequenceLabel) {

		SimpleFilterProbability vFilterProbability;
		String vTerm;
		int vLabel;

		for(int i = 0; i < pSequenceLabel.size(); i++) {

			vTerm = pSequenceLabel.getTerm(i).toLowerCase();

			if(!mCommonTermMap.containsKey(vTerm)) {
				mCommonTermMap.put(vTerm, new SimpleFilterProbability());
			}

			vLabel = pSequenceLabel.getLabel(i);
			vFilterProbability = mCommonTermMap.get(vTerm);
			vFilterProbability.addLabel(vLabel);
		}
	}

	public double getCommonTermProbability(String pTerm) {

		double vCommonTermPercent = 0;

		if(mCommonTermMap.containsKey(pTerm.toLowerCase())) {
			vCommonTermPercent = 1 - mCommonTermMap.get(pTerm.toLowerCase()).getProbability();
		}

		return(vCommonTermPercent);
	}
}
