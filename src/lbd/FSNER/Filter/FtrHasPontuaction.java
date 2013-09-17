package lbd.FSNER.Filter;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;

public class FtrHasPontuaction extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected static String pontuation = "{([@#.,;:?!-+\'\"])}";

	public FtrHasPontuaction(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator) {
		super(ClassName.getSingleName(FtrHasPontuaction.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);

		this.mCommonFilterName = "Ort" + preprocessingTypeNameIndex;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProcessed, int index) {
		// TODO Auto-generated method stub

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
	protected String getSequenceInstanceIdSub(
			SequenceLabel sequenceLabelProcessed, int index) {

		String id = Symbol.EMPTY;

		if(hasPontuation(sequenceLabelProcessed.getTerm(index))) {
			id = "id:" + this.mId + ".hasPontuaction";
		}

		return (id);
	}

	protected boolean hasPontuation(String term) {

		boolean hasPontuation = false;


		for(int i = 0; i < term.length(); i++) {
			//if(term.indexOf(pontuation.charAt(i)) != -1) {
			if(!Character.isDigit(term.charAt(i)) && !Character.isLetter(term.charAt(i))) {
				hasPontuation = true;
				break;
			}
		}

		return(hasPontuation);
	}

}
