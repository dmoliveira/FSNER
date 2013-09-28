package lbd.NewModels.Context;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.Affix.Affix.AffixType;
import lbd.NewModels.Context.ContextManager.ContextType;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class UniqueContextAnalysisFeature extends NewFeatureTypes{

	private static final long serialVersionUID = 1L;

	protected ContextAnalysis contextAnalysis;

	protected ContextType contextType;
	protected final int NUMBER_CONTEXT_TYPE = 3;

	protected int windowSize;
	protected AffixType affixType;

	public UniqueContextAnalysisFeature(FeatureGenImpl fgen, ContextAnalysis contextAnalysis, ContextType contextType,
			int windowSize, AffixType affixType) {

		super(fgen);

		this.contextType = contextType;
		this.contextAnalysis = contextAnalysis;
		this.windowSize = windowSize;
		this.affixType = affixType;
		featureName = "ContextUnique";
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {

		int contextId = ((featureMode == FeatureMode.InTrain  && data.y(pos) == 3))? -1 :
			contextAnalysis.getContextId(data, pos, contextType, windowSize);

		contextId = (contextId > -1)? 0 : -1;

		return contextId;
	}
}
