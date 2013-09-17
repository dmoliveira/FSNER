package lbd.FSNER.Filter;

import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Symbol;

public class FtrCombineTermsInSequence extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected final String DELIMITER_TERM = "|";

	protected ArrayList<HashMap<String, Object>> combinedSequenceList;

	protected int blockSize;
	protected double sequenceSimilarityThreshold;

	public FtrCombineTermsInSequence(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator, int blockSize, double sequenceSimilarityThreshold) {

		super(ClassName.getSingleName(FtrCombineTermsInSequence.class.getName()) +
				".BckSz:" + blockSize + ".Sim:" + sequenceSimilarityThreshold,
				preprocessingTypeNameIndex, scoreCalculator);

		combinedSequenceList = new ArrayList<HashMap<String, Object>>();

		this.blockSize = blockSize;
		this.sequenceSimilarityThreshold = sequenceSimilarityThreshold;

		this.mIsToConsiderFilterProbability = false;
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
	public void loadActionBeforeSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProcessed, int index) {

		String combinedTerm = Symbol.EMPTY;
		HashMap<String, Object> combinedTermsMap;

		if(LabelEncoding.isEntity(sequenceLabelProcessed.getLabel(index))) {

			combinedSequenceList.add(new HashMap<String, Object> ());
			combinedTermsMap = combinedSequenceList.get(combinedSequenceList.size() - 1);

			for(int i = 0; i < sequenceLabelProcessed.size(); i += blockSize) {

				combinedTerm = generateCombinedTerm(sequenceLabelProcessed, i);

				if(!combinedTerm.isEmpty()) {
					combinedTermsMap.put(combinedTerm, null);
				}
			}
		}
	}

	@Override
	public void loadActionAfterSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjust(SequenceLabel sequenceProcessedLabel) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(SequenceLabel sequenceLabelProcessed, int index) {

		String id = Symbol.EMPTY;
		String combinedTerm;

		int sequenceCombinedTermNumber;
		int combinedSequenceNumber = 0;

		for(HashMap<String, Object> combinedSequenceMap : combinedSequenceList) {

			sequenceCombinedTermNumber = 0;
			combinedSequenceNumber++;

			for(int i = 0; i < sequenceLabelProcessed.size(); i += blockSize) {

				combinedTerm = generateCombinedTerm(sequenceLabelProcessed, i);

				if(combinedSequenceMap.containsKey(combinedTerm)) {
					sequenceCombinedTermNumber++;
				}
			}

			id = generateFilterId(sequenceCombinedTermNumber, sequenceLabelProcessed.size()/blockSize, combinedSequenceNumber);

			if(!id.isEmpty()) {
				break;
			}
		}

		return (id);
	}

	protected String generateFilterId(int combinedSequenceTermNumber, int totalCombinedSequence, int combinedSequenceNumber) {

		String id = Symbol.EMPTY;

		if(((double)combinedSequenceTermNumber)/totalCombinedSequence >= sequenceSimilarityThreshold) {
			id = "id:" + this.mId + "seq#:" + combinedSequenceNumber;
		}

		return(id);
	}

	protected String generateCombinedTerm(SequenceLabel sequenceLabelProcessed, int index) {

		String termCombined = Symbol.EMPTY;

		for(int i = index; i < index + blockSize && i < sequenceLabelProcessed.size(); i++) {
			if(!sequenceLabelProcessed.getTerm(i).isEmpty()) {
				termCombined += sequenceLabelProcessed.getTerm(i) + DELIMITER_TERM;
			}
		}

		return(termCombined);
	}

}
