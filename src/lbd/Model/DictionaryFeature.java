package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class DictionaryFeature extends NewFeatureTypes {

	private static final long serialVersionUID = 1L;

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
