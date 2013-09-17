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

public class FtrVocab2 extends AbstractFilter{

	private static final long serialVersionUID = 1L;
	protected HashMap<String, Integer> vocabMap;
	protected HashMap<String, Object> selectedVocabMap;

	protected final int TERM_MIN_SIZE = 4;//4 (Standard)

	protected int maxFrequency;
	protected int minFrequency;
	protected double averageVocabFrequency;
	protected double stdevVocabFrequency;

	//-- Parameters
	protected int windowSideSize;

	public FtrVocab2(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator, int windowSideSize) {
		super(ClassName.getSingleName(FtrVocab2.class.getName()) +
				".Ws:" + windowSideSize,
				preprocessingTypeNameIndex, scoreCalculator);

		vocabMap = new HashMap<String, Integer>();
		selectedVocabMap = new HashMap<String, Object>();

		this.mCommonFilterName = "Vcb" + preprocessingTypeNameIndex;
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

		int startIndex;
		int endIndex;

		String term;

		if(LabelEncoding.isEntity(sequenceLabelProcessed.getLabel(index))) {

			startIndex = (index > windowSideSize)? index - windowSideSize : 0;
			endIndex = (index + windowSideSize < sequenceLabelProcessed.size())?
					index + windowSideSize : sequenceLabelProcessed.size();

			for(int i = startIndex; i < endIndex; i++) {

				term = sequenceLabelProcessed.getTerm(i);

				if(i != index && !term.isEmpty() && term.length() >= TERM_MIN_SIZE) {
					if(vocabMap.containsKey(term)) {
						vocabMap.put(term, vocabMap.get(term) + 1);
					} else {
						vocabMap.put(term, 1);
					}
				}
			}
		}

	}

	@Override
	public void loadActionAfterSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {

		calculateAverageVocabFrequency();
		calculateStdevVocabFrequency();
		selectVocab();
	}

	protected void calculateAverageVocabFrequency() {

		Iterator<Entry<String, Integer>> ite = vocabMap.entrySet().iterator();
		averageVocabFrequency = 0;

		//-- Calc average frequency
		while(ite.hasNext()) {
			averageVocabFrequency += ite.next().getValue();
		}

		averageVocabFrequency /= vocabMap.size();
	}

	protected void calculateStdevVocabFrequency() {

		Iterator<Entry<String, Integer>> ite = vocabMap.entrySet().iterator();
		stdevVocabFrequency = 0;

		//-- Calc standard deviation
		while(ite.hasNext()) {
			stdevVocabFrequency += Math.pow((ite.next().getValue() - averageVocabFrequency), 2);
		}

		stdevVocabFrequency = Math.sqrt(stdevVocabFrequency/vocabMap.size());

	}

	protected void selectVocab() {

		Iterator<Entry<String, Integer>> ite = vocabMap.entrySet().iterator();
		Entry<String, Integer> entry;

		maxFrequency = 0;
		minFrequency = Integer.MAX_VALUE;

		//-- Select vocabulary terms
		while(ite.hasNext()) {

			entry = ite.next();

			if(entry.getValue() > maxFrequency) {
				maxFrequency = entry.getValue();
			} else if(entry.getValue() < minFrequency) {
				minFrequency = entry.getValue();
			}

			if(entry.getValue() > averageVocabFrequency && entry.getValue() < stdevVocabFrequency) {
				selectedVocabMap.put(entry.getKey(), null);
				//System.out.println(entry.getKey() + " f:" + entry.getValue() + " (" + entry.getValue()/averageVocabFrequency + "%)");
			}
		}

		//-- For Debug Purpose only
		//System.out.println("\n-----\nMinFrequency: " + minFrequency + " MaxFrequency: " + maxFrequency +
		//" Avg: " + averageVocabFrequency + " SD: " + stdevVocabFrequency + "\n");
	}

	@Override
	public void adjust(SequenceLabel sequenceProcessedLabel) {
		//TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(
			SequenceLabel sequenceLabelProcessed, int index) {

		return ((selectedVocabMap.containsKey(sequenceLabelProcessed.getTerm(index)))?
				"id:" + this.mId + "." + sequenceLabelProcessed.getTerm(index) : Symbol.EMPTY);
	}

}
