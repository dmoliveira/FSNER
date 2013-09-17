package lbd.NewModels.Context;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class ContextPrefixSizeFeature extends NewFeatureTypes {

	private static final long serialVersionUID = 3795318182107069713L;
	
	public ContextPrefixSizeFeature(FeatureGenImpl fgen) {
		super(fgen);
		
		featureName = "CxtPfxSze";
		
		proccessSequenceType = ProccessSequenceType.Plain;
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {
		
		int prefixSizeId = (pos > 0)? (((String)data.x(pos-1)).length()) : -1;
		
		return prefixSizeId;
	}
}
