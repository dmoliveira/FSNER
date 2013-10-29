package lbd.FSNER.DataProcessor.Component;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.data.handler.ISequence;

public class PreprocessData {

	public static Map<String, ISequence> preprocessSequence(ISequence sequence, List<AbstractDataPreprocessor> activityList) {

		Map<String, ISequence> preproccessedSequenceMap = new HashMap<String, ISequence>();

		for(AbstractDataPreprocessor activity : activityList) {
			preproccessedSequenceMap.put(activity.getActivityName(), activity.preprocessingSequence(sequence));
		}

		return(preproccessedSequenceMap);
	}

	public static String preproccessTerm(String term, int label,
			ArrayList<AbstractDataPreprocessor> activityList, String activityPreprocessingName) {

		AbstractDataPreprocessor activityPreprocessing = null;

		for(AbstractDataPreprocessor activity : activityList) {
			if(activity.getActivityName().equals(activityPreprocessingName)) {
				activityPreprocessing = activity;
				break;
			}
		}

		return(activityPreprocessing.preprocessingToken(term, label));
	}

}
