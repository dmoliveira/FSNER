package lbd.FSNER.Filter;

import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Filter.Component.BagOfWords;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.CommonEnum.Flexibility;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.DataSequence;

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
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProcessed, int index) {

		BagOfWords bagOfWords = bagOfWordsMap.get(mId);

		if(bagOfWords != null && LabelEncoding.isEntity(sequenceLabelProcessed.getLabel(index))) {
			bagOfWords.addSequence(sequenceLabelProcessed, index);
		}
	}

	@Override
	public void loadActionAfterSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
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
		int idNumber = bagOfWordsMap.get(bagOfWordsIdMap.get(
				mPreprocessingTypeIndex)).getBagOfWordsId(
						sequenceLabelProcessed, index, flexibility, isGeneralUse, threshold);

		if(idNumber > -1) {
			id = "id:" + this.mId + Symbol.DOT + idNumber + ".flx:" + flexibility.name() +
					".isGenUse:" + isGeneralUse + ".thres:" + threshold;
		}

		return (id);
	}

}
