package lbd.FSNER.Filter;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;

public class FtrTermLength extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	public FtrTermLength(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator) {
		super(ClassName.getSingleName(FtrTermLength.class.getName()),
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
		return ("id:" + this.mId + ".len:" + sequenceLabelProcessed.getTerm(index));
	}
}
