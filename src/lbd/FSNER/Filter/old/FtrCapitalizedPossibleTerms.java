package lbd.FSNER.Filter.old;

import java.util.HashMap;

import lbd.FSNER.DataPreprocessor.DPCapitalizationTermsOnly;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public class FtrCapitalizedPossibleTerms extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected HashMap<String, Object> termsCapitalized;

	public FtrCapitalizedPossibleTerms(
			int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator) {

		super(ClassName.getSingleName(FtrCapitalizedPossibleTerms.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);

		termsCapitalized = new HashMap<String, Object>();
		this.mFilterClassName = "Ort" + preprocessingTypeNameIndex;
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
	public void loadTermSequence(ISequence pPreprocessedSequence, int index) {
		if(index > 0 && DPCapitalizationTermsOnly.isCapitalized(pPreprocessedSequence.getToken(index))) {
			termsCapitalized.put(pPreprocessedSequence.getToken(index).toLowerCase(), null);
		}
	}

	@Override
	public void loadActionAfterSequenceIteration(ISequence pPreprocessedSequence) {
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
	protected String getSequenceInstanceIdSub(ISequence pSequence, ISequence pPreprocessedSequence, int index) {

		return ((termsCapitalized.containsKey(pPreprocessedSequence.getToken(index).toLowerCase()))?
				"id:" + this.mId + Symbol.DOT + pPreprocessedSequence.getToken(index).toLowerCase() : Symbol.EMPTY);
	}

}
