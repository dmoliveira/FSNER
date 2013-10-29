package lbd.FSNER.Filter;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public class FtrState extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	// -- Parameterization
	protected int mNumberPreviousStatesToConsider;
	protected boolean mIsToConsiderStatePerToken;

	public FtrState(int preprocessingTypeNameIndex,
			int pNumberPreviousStatesToConsider,
			boolean pIsToConsiderStatePerToken,
			AbstractFilterScoreCalculatorModel scoreCalculator) {

		super(ClassName.getSingleName(FtrState.class.getName()) + ".prevStr:"
				+ pNumberPreviousStatesToConsider + ".StrTkn:"
				+ pIsToConsiderStatePerToken, preprocessingTypeNameIndex,
				scoreCalculator);
		mNumberPreviousStatesToConsider = pNumberPreviousStatesToConsider;
		mIsToConsiderStatePerToken = pIsToConsiderStatePerToken;
	}

	@Override
	public void adjust(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(ISequence pPreprocessedSequence, int index) {
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
	public void loadActionAfterSequenceIteration(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(ISequence pSequence,
			ISequence pPreprocessedSequence, int pIndex) {

		String vId = Symbol.EMPTY;

		if (mNumberPreviousStatesToConsider <= 0) {
			return vId;
		}

		if (pIndex - mNumberPreviousStatesToConsider >= 0) {// &&
			// sequenceLabelProcessed.getLabel(index
			// - 1) !=
			// LabelEncoding.BILOU.Outside.ordinal())
			// {
			String vPreviousStates = Symbol.EMPTY;
			for (int cState = pIndex - mNumberPreviousStatesToConsider; cState < pIndex; cState++) {
				vPreviousStates += pSequence.getLabel(cState) + Symbol.HYPHEN;
			}

			vId = "id:"
					+ this.mId
					+ Symbol.HYPHEN
					+ ((mIsToConsiderStatePerToken) ? pPreprocessedSequence
							.getToken(pIndex) + Symbol.COLON : Symbol.EMPTY)
							+ vPreviousStates;
		}

		return vId;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

}
