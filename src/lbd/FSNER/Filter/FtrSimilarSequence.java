package lbd.FSNER.Filter;

import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.LabelEncoding;

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
	public void loadActionBeforeSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProcessed, int index) {
		
		if(LabelEncoding.isEntity(sequenceLabelProcessed.getLabel(index))) {
			
			sequenceList.add(new HashMap<String, Object>());
			HashMap<String, Object> termSequenceMap = sequenceList.get(sequenceList.size()-1);
			
			for(int i = 0; i < sequenceLabelProcessed.size(); i++)
				if(i != index)
					termSequenceMap.put(sequenceLabelProcessed.getTerm(i), null);
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
		
		String id = "";
		
		int indexId = 0;
		double sequenceSimilarity;
		
		for(HashMap<String, Object> termSequenceMap : sequenceList) {
			
			sequenceSimilarity = calculateSequenceSimilarity(sequenceLabelProcessed, index, termSequenceMap)/((double) sequenceLabelProcessed.size()-1);
			indexId++;
			
			if(sequenceSimilarity > similarSequenceThreshold) {
				id = "id:" + this.mId + "-" + indexId;
				break;
			}
		}
		
		return (id);
	}
	
	protected int calculateSequenceSimilarity(SequenceLabel sequenceLabelProcessed, int index, HashMap<String, Object> termSequenceMap) {
		
		int numberTermSequenceSimilar = 0;
		
		for(int i = 0; i < sequenceLabelProcessed.size(); i++) {
			if(i != index && termSequenceMap.containsKey(sequenceLabelProcessed.getTerm(i))) {
				numberTermSequenceSimilar++;
			}
		}
		
		return(numberTermSequenceSimilar);
	}

}
