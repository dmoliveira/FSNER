package lbd.FSNER.Filter;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public class FtrShiftFilterPosition extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	protected AbstractFilter filter;
	protected int mShiftPosition;

	public FtrShiftFilterPosition(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator, AbstractFilter filter, int shiftPosition) {
		super(ClassName.getSingleName(FtrShiftFilterPosition.class.getName()) +
				".SP:" + shiftPosition + Symbol.PARENTHESE_LEFT +
				filter.getActivityName() + Symbol.PARENTHESE_RIGHT,
				preprocessingTypeNameIndex, scoreCalculator);

		this.filter = filter;
		this.mShiftPosition = shiftPosition;
		//this.commonFilterName = "Shf.SP:" + filter.getCommonFilterName();//+ shiftPosition + CommonTag.DOT
		this.mFilterClassName = filter.getFilterClassName();//+ shiftPosition + CommonTag.DOT
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
	public void loadActionBeforeSequenceIteration(ISequence pPreprocessedSequence) {
		// TODO Auto-generated constructor stub

	}

	@Override
	public void loadTermSequence(ISequence pPreprocessedSequence, int pIndex) {
		// TODO Auto-generated constructor stub

	}

	@Override
	public void loadActionAfterSequenceIteration(ISequence pPreprocessedSequence) {
		// TODO Auto-generated constructor stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated constructor stub

	}

	@Override
	public void adjust(ISequence pPreprocessedSequence) {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getSequenceInstanceIdSub(ISequence pSequence, ISequence pPreprocessedSequence, int vIndex) {

		int vNewIndex = vIndex + mShiftPosition;
		String vId = ((vNewIndex > -1 && vNewIndex < pPreprocessedSequence.length())?
				"id:" + this.mId + Symbol.DOT + filter.getSequenceInstanceId(pSequence,
						pPreprocessedSequence, vNewIndex) : Symbol.EMPTY);

		return vId;
	}

}
