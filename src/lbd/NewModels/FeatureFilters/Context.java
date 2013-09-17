package lbd.NewModels.FeatureFilters;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class Context extends NewFeatureTypes{

	private static final long serialVersionUID = 1L;
	protected ContextComponent context;

	public Context(FeatureGenImpl fgen, ContextComponent context) {
		super(fgen);
		this.context = context;
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {
		// TODO Auto-generated method stub
		return 0;
	}

}
