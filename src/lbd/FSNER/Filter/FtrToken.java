package lbd.FSNER.Filter;

import java.util.HashSet;
import java.util.Set;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

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
	public void loadActionBeforeSequenceIteration(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(ISequence pPreprocessedSequence, int pIndex) {
		mEntitySet.add(pPreprocessedSequence.getToken(pIndex));
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
	public void adjust(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSequenceInstanceIdSub(ISequence pSequence, ISequence pPreprocessedSequence,
			int pIndex) {

		String vId = Symbol.EMPTY;

		if(mEntitySet.contains(pPreprocessedSequence.getToken(pIndex))) {
			vId = "id:" + mId + Symbol.HYPHEN + pPreprocessedSequence.getToken(pIndex);
		}

		return (vId);
	}

}
