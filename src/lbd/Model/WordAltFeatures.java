package lbd.Model;

import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;
import iitb.CRF.*;
import iitb.Model.FeatureGenImpl;
import iitb.Model.WordsInTrain;

public class WordAltFeatures extends NewFeatureTypes {
	
	private static final long serialVersionUID = 873915428237029440L;
	
	WordsInTrain dict;
	
	public static int RARE_THRESHOLD = 0;
	
	public WordAltFeatures(FeatureGenImpl m, WordsInTrain d, float weight) {
		super(m);
		dict = d;
		this.weight = weight;
		featureType = FeatureType.Dictionary;
		
		proccessSequenceType = ProccessSequenceType.AllLowerCase;
		skipOutsideState = false;
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {
		
		return (dict.count(data.x(pos)) > RARE_THRESHOLD)? dict.getIndex(data.x(pos)) : -1;
	}
};
