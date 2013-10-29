package lbd.FSNER.Filter;

import lbd.FSNER.DataPreprocessor.DPCapitalizationTermsOnly;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public class FtrCapitalizedTerms extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	public FtrCapitalizedTerms(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator) {

		super(ClassName.getSingleName(FtrCapitalizedTerms.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);

		//this.commonFilterName = "Ort" + preprocessingTypeNameIndex;
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
		// TODO Auto-generated method stub

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
	protected String getSequenceInstanceIdSub(ISequence pSequence,
			ISequence pPreprocessedSequence, int index) {

		String id = Symbol.EMPTY;

		if(DPCapitalizationTermsOnly.isCapitalized(pPreprocessedSequence.getToken(index))) {
			id = "id:" + id + ".isCapitalized";
		}

		return (id);
	}

}
