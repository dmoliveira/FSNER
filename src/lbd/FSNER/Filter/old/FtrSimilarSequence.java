package lbd.FSNER.Filter.old;

import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.data.handler.ISequence;

public class FtrSimilarSequence extends AbstractFilter{

	private static final long serialVersionUID = 1L;
	protected double similarSequenceThreshold;

	protected ArrayList<HashMap<String, Object>> sequenceList;

	public FtrSimilarSequence(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator, double similarSequenceThreshold) {

		super(ClassName.getSingleName(FtrSimilarSequence.class.getName()) +
				".Ts:" + similarSequenceThreshold,
				preprocessingTypeNameIndex, scoreCalculator);

		sequenceList = new ArrayList<HashMap<String,Object>>();

		this.similarSequenceThreshold = similarSequenceThreshold;
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

		sequenceList.add(new HashMap<String, Object>());
		HashMap<String, Object> termSequenceMap = sequenceList.get(sequenceList.size()-1);

		for(int i = 0; i < pPreprocessedSequence.length(); i++) {
			if(i != pIndex) {
				termSequenceMap.put(pPreprocessedSequence.getToken(i), null);
			}
		}

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
	protected String getSequenceInstanceIdSub(ISequence pSequence, ISequence pPreprocessedSequence, int pIndex) {

		String id = "";

		int indexId = 0;
		double sequenceSimilarity;

		for(HashMap<String, Object> termSequenceMap : sequenceList) {

			sequenceSimilarity = calculateSequenceSimilarity(pPreprocessedSequence, pIndex, termSequenceMap)/((double) pPreprocessedSequence.length()-1);
			indexId++;

			if(sequenceSimilarity > similarSequenceThreshold) {
				id = "id:" + this.mId + "-" + indexId;
				break;
			}
		}

		return id;
	}

	protected int calculateSequenceSimilarity(ISequence pPreprocessedSequence, int pIndex, HashMap<String, Object> pTermSequenceMap) {

		int vNumberTermSequenceSimilar = 0;

		for(int i = 0; i < pPreprocessedSequence.length(); i++) {
			if(i != pIndex && pTermSequenceMap.containsKey(pPreprocessedSequence.getToken(i))) {
				vNumberTermSequenceSimilar++;
			}
		}

		return vNumberTermSequenceSimilar;
	}

}
