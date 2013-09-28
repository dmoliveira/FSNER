package lbd.NewModels.Context;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class ContextAnalysisRelaxedFeature extends NewFeatureTypes{

	private static final long serialVersionUID = 1L;
	protected ContextAnalysisRelaxed contextAnalysisRelaxed;

	public ContextAnalysisRelaxedFeature(FeatureGenImpl fgen, ContextAnalysisRelaxed contextAnalysisRelax) {
		super(fgen);

		this.contextAnalysisRelaxed = contextAnalysisRelax;

		featureName = "ContextAnalysisRelaxed";
		featureType = FeatureType.ContextAnalysis;
		proccessSequenceType = ProccessSequenceType.Plain;
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {

		return contextAnalysisRelaxed.getContextId(proccessedSequence, pos);
	}

}
