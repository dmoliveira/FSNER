package lbd.Model;

import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes.FeatureType;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;

public class DictionaryFeature extends NewFeatureTypes {

	private static final long serialVersionUID = -5732070986046854120L;

	protected transient Dictionary dictionary;
	
	public DictionaryFeature(FeatureGenImpl fgen, Dictionary dictionary) {
		super(fgen);
		
		featureName = "Dictionary";
		featureType = FeatureType.Dictionary;
		this.dictionary = dictionary;
		
		proccessSequenceType = ProccessSequenceType.Plain;
		skipOutsideState = true;
	}
	
	@Override
	protected int startFeature(DataSequence data, int pos) {
		
		return dictionary.isMatching(proccessedSequence[pos]);
	}

}
