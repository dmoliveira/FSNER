package lbd.FSNER.Filter;

import java.util.HashSet;
import java.util.Set;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.DataSequence;

public class FtrToken extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	//Token: Revisit - Entity must be more than one token.
	protected Set<String> mEntitySet;

	public FtrToken(int pPreprocessingTypeNameIndex) {
		super(ClassName.getSingleName(FtrToken.class.getName()), pPreprocessingTypeNameIndex);
		mEntitySet = new HashSet<String>();
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
			mEntitySet.add(pSequenceLabelProccessed.getTerm(pIndex));
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
	public String getSequenceInstanceIdSub(DataSequence pSequence, SequenceLabel pSequenceLabelProcessed,
			int pIndex) {

		String vId = Symbol.EMPTY;

		if(mEntitySet.contains(pSequenceLabelProcessed.getTerm(pIndex))) {
			vId = "id:" + mId + Symbol.HYPHEN + pSequenceLabelProcessed.getTerm(pIndex);
		}

		return (vId);
	}

}
