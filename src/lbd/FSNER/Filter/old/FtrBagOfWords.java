package lbd.FSNER.Filter.old;

import java.util.HashMap;

import lbd.FSNER.Filter.Component.BagOfWords;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.CommonEnum.Flexibility;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public class FtrBagOfWords extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected static HashMap<Integer, Integer> bagOfWordsIdMap;
	protected static HashMap<Integer, BagOfWords> bagOfWordsMap;

	protected Flexibility flexibility;
	protected boolean isGeneralUse;
	protected double threshold;

	public FtrBagOfWords(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator,
			Flexibility flexibility, boolean isGeneralUse, double threshold) {
		super(ClassName.getSingleName(BagOfWords.class.getName()) + ".Flx:" + flexibility.name() +
				".isGeneralUse:" + isGeneralUse + ".thres:" + threshold,
				preprocessingTypeNameIndex, scoreCalculator);

		if(bagOfWordsIdMap == null) {
			bagOfWordsIdMap = new HashMap<Integer, Integer>();
			bagOfWordsMap = new HashMap<Integer, BagOfWords>();
		}

		if(!bagOfWordsIdMap.containsKey(preprocessingTypeNameIndex)) {
			bagOfWordsIdMap.put(preprocessingTypeNameIndex, mId);
			bagOfWordsMap.put(mId, new BagOfWords());
		}

		this.flexibility = flexibility;
		this.isGeneralUse = isGeneralUse;
		this.threshold = threshold;
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
			ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(ISequence pPreprocessedSequence, int index) {

		BagOfWords bagOfWords = bagOfWordsMap.get(mId);

		if(bagOfWords != null) {
			bagOfWords.addSequence(pPreprocessedSequence, index);
		}
	}

	@Override
	public void loadActionAfterSequenceIteration(
			ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjust(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(ISequence pSequence,
			ISequence pPreprocessedSequence, int pIndex) {

		String id = Symbol.EMPTY;
		int idNumber = bagOfWordsMap.get(bagOfWordsIdMap.get(
				mPreprocessingTypeIndex)).getBagOfWordsId(
						pPreprocessedSequence, pIndex, flexibility, isGeneralUse, threshold);

		if(idNumber > -1) {
			id = "id:" + this.mId + Symbol.DOT + idNumber + ".flx:" + flexibility.name() +
					".isGenUse:" + isGeneralUse + ".thres:" + threshold;
		}

		return (id);
	}

}
