package lbd.FSNER.DataProcessor.Component;

import iitb.CRF.DataSequence;

import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.SequenceLabelElement;
import lbd.FSNER.Model.AbstractDataPreprocessor;

public class PreprocessData {
	
	public static HashMap<String, SequenceLabel> preprocessSequence(DataSequence sequence, ArrayList<AbstractDataPreprocessor> activityList) {
		
		HashMap<String, SequenceLabel> preproccessedSequenceMap = new HashMap<String, SequenceLabel>();
			
		for(AbstractDataPreprocessor activity : activityList)
			preproccessedSequenceMap.put(activity.getActivityName(), activity.preprocessingSequence(sequence));
		
		return(preproccessedSequenceMap);
	}
	
	public static SequenceLabelElement preproccessTerm(String term, int label,
			ArrayList<AbstractDataPreprocessor> activityList, String activityPreprocessingName) {
		
		AbstractDataPreprocessor activityPreprocessing = null;
		
		for(AbstractDataPreprocessor activity : activityList) {
			if(activity.getActivityName().equals(activityPreprocessingName)) {
				activityPreprocessing = activity;
				break;
			}
		}
		
		return(activityPreprocessing.preprocessingTerm(term, label));
	}

}
