package lbd.FSNER.DataProcessor.Component;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.SequenceLabelElement;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.data.handler.DataSequence;

public class PreprocessData {

	public static Map<String, SequenceLabel> preprocessSequence(DataSequence sequence, List<AbstractDataPreprocessor> activityList) {

		Map<String, SequenceLabel> preproccessedSequenceMap = new HashMap<String, SequenceLabel>();

		for(AbstractDataPreprocessor activity : activityList) {
			preproccessedSequenceMap.put(activity.getActivityName(), activity.preprocessingSequence(sequence));
		}

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
