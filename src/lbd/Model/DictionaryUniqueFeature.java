package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class DictionaryUniqueFeature extends NewFeatureTypes {

	private static final long serialVersionUID = 1L;

	protected transient Dictionary dictionary;

	public DictionaryUniqueFeature(FeatureGenImpl fgen, Dictionary dictionary) {
		super(fgen);

		this.dictionary = dictionary;

		featureName = "Dictionary";
		featureType = FeatureType.Dictionary;
		skipOutsideState = false;
		proccessSequenceType = ProccessSequenceType.Plain;
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {

		int dictionaryId = dictionary.isMatching(proccessedSequence[pos]);

		return ((dictionaryId > -1)? 1 : -1);
	}
}
