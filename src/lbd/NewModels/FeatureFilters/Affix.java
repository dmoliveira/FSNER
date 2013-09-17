package lbd.NewModels.FeatureFilters;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class Affix extends NewFeatureTypes{

	private static final long serialVersionUID = 1L;
	protected AffixComponent affixComponent;

	public Affix(FeatureGenImpl fgen, AffixComponent affixComponent) {
		super(fgen);
		this.affixComponent = affixComponent;
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {
		return (affixComponent.getSequenceInstanceIdSub(data, pos));
	}

}
