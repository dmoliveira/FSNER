package lbd.FSNER.Filter;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.DataSequence;

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
	protected String getSequenceInstanceIdSub(DataSequence pSequence,
			SequenceLabel pSequenceLabelProcessed, int pIndex) {

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
				vPreviousStates += pSequence.y(cState) + Symbol.HYPHEN;
			}

			vId = "id:"
					+ this.mId
					+ Symbol.HYPHEN
					+ ((mIsToConsiderStatePerToken) ? pSequenceLabelProcessed
							.getTerm(pIndex) + Symbol.COLON : Symbol.EMPTY)
							+ vPreviousStates;
		}

		return vId;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

}
