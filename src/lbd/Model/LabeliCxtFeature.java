package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class LabeliCxtFeature extends NewFeatureTypes{

	private static final long serialVersionUID = 1L;

	public LabeliCxtFeature(FeatureGenImpl fgen) {
		super(fgen);
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {
		
		return ((pos > 0 && data.y(pos - 1) > 4)? 1 : -1);
	}

}
