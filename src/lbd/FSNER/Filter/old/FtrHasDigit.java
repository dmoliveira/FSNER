package lbd.FSNER.Filter.old;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public class FtrHasDigit extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	public FtrHasDigit(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator) {
		super(ClassName.getSingleName(FtrHasDigit.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);

		this.mFilterClassName = "Ort" + preprocessingTypeNameIndex;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceIteration(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
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

		String id = Symbol.EMPTY;

		if(hasDigit(pPreprocessedSequence.getToken(pIndex))) {
			id = "id:" + this.mId + ".hasDigit";
		}

		return (id);
	}

	protected boolean hasDigit(String pToken) {

		boolean vHasDigit = false;

		for(int i = 0; i < pToken.length(); i++) {
			if(Character.isDigit(pToken.charAt(i))) {
				vHasDigit = true;
				break;
			}
		}

		return(vHasDigit);
	}

}
