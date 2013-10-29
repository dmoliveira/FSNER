package lbd.FSNER.Filter;

import java.util.HashMap;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public class FtrPosition extends AbstractFilter{

	private static final long serialVersionUID = 1L;
	protected HashMap<Integer, Object> positionMap;

	//-- The range of position analyzed (0.. MAX POSITION[
	protected final int MAX_POSITION = 0;

	public FtrPosition(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator) {
		super(ClassName.getSingleName(FtrPosition.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);

		this.mFilterClassName = "Pos" + preprocessingTypeNameIndex;
		positionMap = new HashMap<Integer, Object>();
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}


	@Override
	public void loadTermSequence(ISequence pPreprocessedSequence, int pIndex) {

		if(pIndex <= MAX_POSITION) {
			positionMap.put(pIndex, null);
		}
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
	public void adjust(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(ISequence pSequence,
			ISequence pPreprocessedSequence, int index) {

		return (((positionMap.containsKey(index))? "id:" + mId + Symbol.DOT + index : Symbol.EMPTY));
	}

}
