package lbd.FSNER.Filter;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Utils.ClassName;
import lbd.data.handler.ISequence;

public class FtrTokenLength extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	public FtrTokenLength(int preprocessingTypeNameIndex) {
		super(ClassName.getSingleName(FtrTokenLength.class.getName()),
				preprocessingTypeNameIndex);

		this.mFilterClassName = "Ort" + preprocessingTypeNameIndex;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceIteration(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(ISequence pPreprocessedSequence, int pIndex) {
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
	public void adjust(ISequence pPreprocessedSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(ISequence pSequence,
			ISequence pPreprocessedSequence, int pIndex) {
		return ("id:" + this.mId + ".len:" + pPreprocessedSequence.getToken(pIndex).length());
	}
}
