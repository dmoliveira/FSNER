package lbd.FSNER.Model;

import java.util.HashMap;

import lbd.FSNER.Component.Statistic.SimpleFilterProbability;
import lbd.data.handler.ISequence;
import lbd.data.handler.SequenceSegment;

public abstract class AbstractDataPreprocessor extends AbstractActivity {

	private static final long serialVersionUID = 1L;

	protected HashMap<String, SimpleFilterProbability> mCommonTermMap;

	public AbstractDataPreprocessor(String pActivityName, String pInitializeFile) {
		super(pActivityName);

		mInitializeFile = pInitializeFile;
		mCommonTermMap = new HashMap<String, SimpleFilterProbability>();
	}

	public ISequence preprocessingSequence(ISequence pSequence) {

		ISequence vPreprocessedSequence = new SequenceSegment();

		for(int i = 0; i < pSequence.length(); i++) {
			vPreprocessedSequence.add(preprocessingToken(pSequence.getToken(i), pSequence.getLabel(i)), pSequence.getLabel(i));
		}

		return (vPreprocessedSequence);
	}

	public abstract String preprocessingToken(String pToken, int pLabel);

	public void computeCommonTermsInSequence(ISequence pPreprocessedSequence) {

		SimpleFilterProbability vFilterProbability;
		String vTerm;
		int vLabel;

		for(int i = 0; i < pPreprocessedSequence.length(); i++) {

			vTerm = pPreprocessedSequence.getToken(i).toLowerCase();

			if(!mCommonTermMap.containsKey(vTerm)) {
				mCommonTermMap.put(vTerm, new SimpleFilterProbability());
			}

			vLabel = pPreprocessedSequence.getLabel(i);
			vFilterProbability = mCommonTermMap.get(vTerm);
			vFilterProbability.addLabel(vLabel);
		}
	}

	public double getCommonTokenProbability(String pToken) {

		double vCommonTermPercent = 0;
		String vTokenLowerCase = pToken.toLowerCase();

		if(mCommonTermMap.containsKey(vTokenLowerCase)) {
			vCommonTermPercent = 1 - mCommonTermMap.get(vTokenLowerCase).getProbability();
		}

		return(vCommonTermPercent);
	}
}
