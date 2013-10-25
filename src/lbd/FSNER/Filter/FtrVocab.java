package lbd.FSNER.Filter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.DataSequence;

public class FtrVocab extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected HashMap<String, HashMap<String, Object>> vocabEntityMap;

	protected final int MIN_SEQUENCE_SIZE = 6;
	protected int windowSideSize;
	protected double threshold;

	public FtrVocab(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator, double threshold, int windowSideSize) {

		super(ClassName.getSingleName(FtrVocab.class.getName()) + ".thres:" + threshold,
				preprocessingTypeNameIndex, scoreCalculator);

		vocabEntityMap = new HashMap<String, HashMap<String, Object>>();
		this.mFilterClassName = "Voc";
		this.threshold = threshold;
		this.windowSideSize = windowSideSize;
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
	public void loadActionBeforeSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProcessed, int index) {

		if(sequenceLabelProcessed.size() >= MIN_SEQUENCE_SIZE &&
				LabelEncoding.isEntity(sequenceLabelProcessed.getLabel(index))) {
			addSequence(sequenceLabelProcessed, index);
		}

	}

	protected void addSequence(SequenceLabel sequenceLabelProcessed, int index) {

		String term = sequenceLabelProcessed.getTerm(index);
		HashMap<String, Object> vocabMap;

		int startIndex = (index > windowSideSize)? index - windowSideSize : 0;
		int endIndex = (index + windowSideSize < sequenceLabelProcessed.size())? index + windowSideSize : sequenceLabelProcessed.size();

		if(!vocabEntityMap.containsKey(term)) {
			vocabEntityMap.put(term, new HashMap<String, Object>());
		}

		vocabMap = vocabEntityMap.get(term);

		for(int i = startIndex; i < endIndex; i++) {
			vocabMap.put(sequenceLabelProcessed.getTerm(i), null);
		}
	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjust(SequenceLabel sequenceProcessedLabel) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(DataSequence pSequence,
			SequenceLabel sequenceLabelProcessed, int index) {

		String id = Symbol.EMPTY;

		if(sequenceLabelProcessed.size() >= MIN_SEQUENCE_SIZE) {

			id = getVocabId(sequenceLabelProcessed, index);

			if(!id.isEmpty()) {
				id = "id:" + this.mId + "." + id;
			}
		}

		return (id);
	}

	protected String getVocabId(SequenceLabel sequenceLabelProcessed, int index) {

		String id = Symbol.EMPTY;
		double vocabTermsNumber;
		boolean wasReachedThreshold = false;

		int startIndex = (index > windowSideSize)? index - windowSideSize : 0;
		int endIndex = (index + windowSideSize < sequenceLabelProcessed.size())? index + windowSideSize : sequenceLabelProcessed.size();

		Iterator<Entry<String, HashMap<String, Object>>> ite = vocabEntityMap.entrySet().iterator();
		Entry<String, HashMap<String, Object>> entry;
		HashMap<String, Object> vocab;

		while(ite.hasNext() && !wasReachedThreshold) {

			entry = ite.next();

			vocabTermsNumber = 0;
			vocab = entry.getValue();

			for(int i = startIndex; i < endIndex; i++) {

				if(vocab.containsKey(sequenceLabelProcessed.getTerm(index))) {
					vocabTermsNumber++;

					if(vocabTermsNumber/sequenceLabelProcessed.size() >= threshold) {
						id = entry.getKey();
						wasReachedThreshold = true;
						break;
					}
				}
			}
		}

		return(id);
	}

}
