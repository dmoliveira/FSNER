package lbd.FSNER.Filter;

import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Symbol;

public class FtrEntityProbability extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected HashMap<String, Object> entityList;

	public FtrEntityProbability(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator) {

		super(ClassName.getSingleName(FtrEntityProbability.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);

		entityList = new HashMap<String, Object>();
		//this.commonFilterName = "EP" + preprocessingTypeNameIndex;
		//this.commonFilterName = "Wrd" + preprocessingTypeNameIndex;
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
	public void loadActionBeforeSequenceIteration(SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProccessed, int index) {

		if(LabelEncoding.isEntity(sequenceLabelProccessed.getLabel(index))) {
			entityList.put(sequenceLabelProccessed.getTerm(index), null);
		}
	}

	@Override
	public void loadActionAfterSequenceIteration(SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjust(SequenceLabel sequenceLabel) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSequenceInstanceIdSub(SequenceLabel sequenceLabelProcessed,
			int index) {

		String id = Symbol.EMPTY;

		if(entityList.containsKey(sequenceLabelProcessed.getTerm(index))) {
			id = "id:" + this.mId + Symbol.HYPHEN + sequenceLabelProcessed.getTerm(index);
		}

		return (id);
	}

}
