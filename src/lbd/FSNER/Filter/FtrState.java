package lbd.FSNER.Filter;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;

public class FtrState extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	public FtrState(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator) {

		super(ClassName.getSingleName(FtrState.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void adjust(SequenceLabel sequenceProcessedLabel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProcessed, int index) {
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
	public void loadActionAfterSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(
			SequenceLabel sequenceLabelProcessed, int index) {
		String id = Symbol.EMPTY;

		if(index > 0){// && sequenceLabelProcessed.getLabel(index - 1) != LabelEncoding.BILOU.Outside.ordinal()) {
			id = "id:" + this.mId + Symbol.HYPHEN + sequenceLabelProcessed.getTerm(index) + Symbol.COLON
					+ sequenceLabelProcessed.getLabel(index - 1);
		}

		return id;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

}
