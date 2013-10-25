package lbd.FSNER.Filter;

import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.DataSequence;

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
	public void loadTermSequence(SequenceLabel sequenceLabelProcessed, int index) {

		if(LabelEncoding.isEntity((sequenceLabelProcessed.getLabel(index))) && index <= MAX_POSITION) {
			positionMap.put(index, null);
		}
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
	public void adjust(SequenceLabel sequenceProcessedLabel) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(DataSequence pSequence,
			SequenceLabel sequenceLabelProcessed, int index) {

		return (((positionMap.containsKey(index))? "id:" + mId + Symbol.DOT + index : Symbol.EMPTY));
	}

}
