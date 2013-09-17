package lbd.FSNER.Filter;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;

public class FtrShiftFilterPosition extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	protected AbstractFilter filter;
	protected int shiftPosition;

	public FtrShiftFilterPosition(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator, AbstractFilter filter, int shiftPosition) {
		super(ClassName.getSingleName(FtrShiftFilterPosition.class.getName()) +
				".SP:" + shiftPosition + Symbol.PARENTHESE_LEFT +
				filter.getActivityName() + Symbol.PARENTHESE_RIGHT,
				preprocessingTypeNameIndex, scoreCalculator);

		this.filter = filter;
		this.shiftPosition = shiftPosition;
		//this.commonFilterName = "Shf.SP:" + filter.getCommonFilterName();//+ shiftPosition + CommonTag.DOT
		this.mCommonFilterName = filter.getCommonFilterName();//+ shiftPosition + CommonTag.DOT
	}

	@Override
	public void initialize() {
		// TODO Auto-generated constructor stub

	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
		// TODO Auto-generated constructor stub

	}

	@Override
	public void loadActionBeforeSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated constructor stub

	}

	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProcessed, int index) {
		// TODO Auto-generated constructor stub

	}

	@Override
	public void loadActionAfterSequenceIteration(SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated constructor stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated constructor stub

	}

	@Override
	public void adjust(SequenceLabel sequenceProcessedLabel) {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getSequenceInstanceIdSub(SequenceLabel sequenceLabelProcessed, int index) {

		int newIndex = index + shiftPosition;
		String id = ((newIndex > -1 && newIndex < sequenceLabelProcessed.size())?
				"id:" + this.mId + Symbol.DOT + filter.getSequenceInstanceId(
						sequenceLabelProcessed, newIndex) : Symbol.EMPTY);

		return (id);
	}

}
