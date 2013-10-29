package lbd.FSNER.Filter;

import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Model.AbstractMetaFilter;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public class FtrTranslate extends AbstractMetaFilter{

	private static final long serialVersionUID = 1L;
	protected int mTranslatedPosition; // 0 is the current observation position i of xi.

	public FtrTranslate(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator,
			int pTranslatedPosition) {

		super("Translated[" + pTranslatedPosition+ "]", preprocessingTypeNameIndex, scoreCalculator);
		mTranslatedPosition = pTranslatedPosition;
	}

	@Override
	protected String getSequenceInstanceIdSub(ISequence pSequence, ISequence pPreprocessedSequence,
			int pIndex) {

		String vId = Symbol.EMPTY;
		int vTranslatedPosition = pIndex + mTranslatedPosition;

		if(vTranslatedPosition >= 0 && vTranslatedPosition < pSequence.length()) {
			vId = super.getSequenceInstanceIdSub(pSequence, pPreprocessedSequence, vTranslatedPosition);
		}

		return vId;
	}

}
