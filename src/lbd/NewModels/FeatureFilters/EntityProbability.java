package lbd.NewModels.FeatureFilters;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class EntityProbability extends NewFeatureTypes{

	private static final long serialVersionUID = 1L;
	protected EntityProbabilityComponent entityProbabilityComponent;

	public EntityProbability(FeatureGenImpl fgen, EntityProbabilityComponent entityProbabilityComponent) {
		super(fgen);
		this.entityProbabilityComponent = entityProbabilityComponent;
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {
		return (entityProbabilityComponent.getId((String)data.x(pos)));
	}

}
