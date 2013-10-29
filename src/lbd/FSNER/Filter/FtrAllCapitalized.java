package lbd.FSNER.Filter;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public class FtrAllCapitalized extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	public FtrAllCapitalized(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator) {
		super(ClassName.getSingleName(FtrAllCapitalized.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);

		this.mFilterClassName = "Ort" + preprocessingTypeNameIndex;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceIteration(
			ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
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

		if(isAllCapitalized(pPreprocessedSequence.getToken(index))) {
			id = "id:" + this.mId + ".isAllCapitalized";
		}

		return (id);
	}

	protected boolean isAllCapitalized(String term) {

		boolean isAllCapitalized = term.equals(term.toUpperCase());

		if(isAllCapitalized) {
			for(int i = 0; i < term.length(); i++) {
				if(!Character.isLetter(term.charAt(i))) {
					isAllCapitalized = false;
					break;
				}
			}
		}

		return(isAllCapitalized);
	}
}
