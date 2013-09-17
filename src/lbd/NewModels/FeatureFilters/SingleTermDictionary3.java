package lbd.NewModels.FeatureFilters;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class SingleTermDictionary3 extends NewFeatureTypes{

	private static final long serialVersionUID = 1L;
	
	protected int dictionaryNameIndex;
	protected SingleTermDictionary3Component dictionary;
	

	public SingleTermDictionary3(FeatureGenImpl fgen, int dictionaryNameIndex, 
			SingleTermDictionary3Component dictionary) {
		
		super(fgen);
		this.dictionaryNameIndex = dictionaryNameIndex;
		this.dictionary = dictionary;
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {
		return (dictionary.getSequenceInstanceIdSub(dictionaryNameIndex, data, pos));
	}

}
