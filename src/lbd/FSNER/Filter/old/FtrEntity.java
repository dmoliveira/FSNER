package lbd.FSNER.Filter.old;

import java.util.ArrayList;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.data.handler.ISequence;

public class FtrEntity extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected ArrayList<String> entityList;

	public FtrEntity(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator) {

		super(ClassName.getSingleName(FtrEntity.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);

		entityList = new ArrayList<String>();
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
		entityList.add(pPreprocessedSequence.getToken(pIndex));
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
	public String getSequenceInstanceIdSub(ISequence pSequence, ISequence pPreprocessedSequence, int pIndex) {
		return ("id:" + mId + "-" + pPreprocessedSequence.getToken(pIndex));
	}

	public ArrayList<String> getEntityList() {
		return(entityList);
	}
}
