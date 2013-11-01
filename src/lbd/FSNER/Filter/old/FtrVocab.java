package lbd.FSNER.Filter.old;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public class FtrVocab extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected HashMap<String, HashMap<String, Object>> vocabEntityMap;

	protected final int MIN_SEQUENCE_SIZE = 6;
	protected int mWindowSideSize;
	protected double threshold;

	public FtrVocab(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator, double threshold, int windowSideSize) {

		super(ClassName.getSingleName(FtrVocab.class.getName()) + ".thres:" + threshold,
				preprocessingTypeNameIndex, scoreCalculator);

		vocabEntityMap = new HashMap<String, HashMap<String, Object>>();
		this.mFilterClassName = "Voc";
		this.threshold = threshold;
		this.mWindowSideSize = windowSideSize;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceIteration(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(ISequence pPreprocessedSequence, int pIndex) {

		if(pPreprocessedSequence.length() >= MIN_SEQUENCE_SIZE) {
			addSequence(pPreprocessedSequence, pIndex);
		}

	}

	protected void addSequence(ISequence pPreprocessedSequence, int pIndex) {

		String vTerm = pPreprocessedSequence.getToken(pIndex);
		HashMap<String, Object> vVocabMap;

		int vStartIndex = (pIndex > mWindowSideSize)? pIndex - mWindowSideSize : 0;
		int vEndIndex = (pIndex + mWindowSideSize < pPreprocessedSequence.length())? pIndex + mWindowSideSize : pPreprocessedSequence.length();

		if(!vocabEntityMap.containsKey(vTerm)) {
			vocabEntityMap.put(vTerm, new HashMap<String, Object>());
		}

		vVocabMap = vocabEntityMap.get(vTerm);

		for(int i = vStartIndex; i < vEndIndex; i++) {
			vVocabMap.put(pPreprocessedSequence.getToken(i), null);
		}
	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceIteration(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjust(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(ISequence pSequence,
			ISequence pPreprocessedSequence, int pIndex) {

		String vId = Symbol.EMPTY;

		if(pPreprocessedSequence.length() >= MIN_SEQUENCE_SIZE) {

			vId = getVocabId(pPreprocessedSequence, pIndex);

			if(!vId.isEmpty()) {
				vId = "id:" + this.mId + "." + vId;
			}
		}

		return (vId);
	}

	protected String getVocabId(ISequence pPreprocessedSequence, int pIndex) {

		String vId = Symbol.EMPTY;
		double vVocabTermsNumber;
		boolean vWasReachedThreshold = false;

		int vStartIndex = (pIndex > mWindowSideSize)? pIndex - mWindowSideSize : 0;
		int vEndIndex = (pIndex + mWindowSideSize < pPreprocessedSequence.length())? pIndex + mWindowSideSize : pPreprocessedSequence.length();

		Iterator<Entry<String, HashMap<String, Object>>> ite = vocabEntityMap.entrySet().iterator();
		Entry<String, HashMap<String, Object>> entry;
		HashMap<String, Object> vocab;

		while(ite.hasNext() && !vWasReachedThreshold) {

			entry = ite.next();

			vVocabTermsNumber = 0;
			vocab = entry.getValue();

			for(int i = vStartIndex; i < vEndIndex; i++) {

				if(vocab.containsKey(pPreprocessedSequence.getToken(pIndex))) {
					vVocabTermsNumber++;

					if(vVocabTermsNumber/pPreprocessedSequence.length() >= threshold) {
						vId = entry.getKey();
						vWasReachedThreshold = true;
						break;
					}
				}
			}
		}

		return(vId);
	}

}
