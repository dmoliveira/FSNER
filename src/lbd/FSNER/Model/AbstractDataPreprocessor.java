package lbd.FSNER.Model;

import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.SequenceLabelElement;
import lbd.FSNER.Component.Statistic.LabelProbabilityElement;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.data.handler.DataSequence;

public abstract class AbstractDataPreprocessor extends AbstractActivity{

	private static final long serialVersionUID = 1L;
	
	protected HashMap<String, LabelProbabilityElement> commonTermMap;

	public AbstractDataPreprocessor(String activityName, String initializeFile) {
		super(activityName);
		
		this.mInitializeFile = initializeFile;
		
		commonTermMap = new HashMap<String, LabelProbabilityElement>();
	}
	
	public SequenceLabel preprocessingSequence(DataSequence sequence) {
		
		SequenceLabel sequenceLabel = new SequenceLabel();
		
		for(int i = 0; i < sequence.length(); i++)
			sequenceLabel.add(preprocessingTerm((String)sequence.x(i), sequence.y(i)));
		
		return (sequenceLabel);
	}
	
	public abstract SequenceLabelElement preprocessingTerm(String term, int label);
	
	public void computeCommonTermsInSequence(SequenceLabel sequenceLabel) {
		
		LabelProbabilityElement labelProbabilityElement;
		String term;
		int label;
		
		for(int i = 0; i < sequenceLabel.size(); i++) {
			
			term = sequenceLabel.getTerm(i).toLowerCase();
			
			if(!commonTermMap.containsKey(term))
				commonTermMap.put(term, new LabelProbabilityElement());
			
			label = sequenceLabel.getLabel(i);
			labelProbabilityElement = commonTermMap.get(term);
			labelProbabilityElement.add(LabelEncoding.isEntity(label), label);
		}
		
	}
	
	public double getCommonTermProbability(String term) {
		
		double commonTermPercent = 0;
		
		if(commonTermMap.containsKey(term.toLowerCase())) {
			commonTermPercent = 1 - commonTermMap.get(term.toLowerCase()).getEntityLabelProbability();
			//System.out.println(commonTermPercent + " "  + term);
		}
		
		return(commonTermPercent);
	}
}
