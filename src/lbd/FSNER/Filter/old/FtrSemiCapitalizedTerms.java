package lbd.FSNER.Filter.old;

import lbd.FSNER.DataPreprocessor.DPSemiCapitalizationTermsOnly;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.data.handler.ISequence;

public class FtrSemiCapitalizedTerms extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	public FtrSemiCapitalizedTerms(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator) {

		super(ClassName.getSingleName(FtrSemiCapitalizedTerms.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);

		this.mFilterClassName = "Cap" + preprocessingTypeNameIndex;
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
			ISequence pPreprocessedSequence, int pIndex) {

		return ("id:" + mId + "isSemiCapitalized:" +
				DPSemiCapitalizationTermsOnly.isSemiCapitalized(pPreprocessedSequence.getToken(pIndex)));
	}
}
