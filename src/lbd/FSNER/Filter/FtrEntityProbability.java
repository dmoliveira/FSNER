package lbd.FSNER.Filter;

import java.util.HashMap;
import java.util.Map;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Symbol;

public class FtrEntityProbability extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected Map<String, Object> mEntityList;

	public FtrEntityProbability(int pPreprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel vScoreCalculator) {

		super(ClassName.getSingleName(FtrEntityProbability.class.getName()),
				pPreprocessingTypeNameIndex, vScoreCalculator);

		mEntityList = new HashMap<String, Object>();
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
	public void loadActionBeforeSequenceIteration(SequenceLabel pSequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(SequenceLabel pSequenceLabelProccessed, int pIndex) {

		if(LabelEncoding.isEntity(pSequenceLabelProccessed.getLabel(pIndex))) {
			mEntityList.put(pSequenceLabelProccessed.getTerm(pIndex), null);
		}
	}

	@Override
	public void loadActionAfterSequenceIteration(SequenceLabel pSequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjust(SequenceLabel pSequenceLabel) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSequenceInstanceIdSub(SequenceLabel pSequenceLabelProcessed,
			int pIndex) {

		String vId = Symbol.EMPTY;

		if(mEntityList.containsKey(pSequenceLabelProcessed.getTerm(pIndex))) {
			vId = "id:" + mId + Symbol.HYPHEN + pSequenceLabelProcessed.getTerm(pIndex);
		}

		return (vId);
	}

}
