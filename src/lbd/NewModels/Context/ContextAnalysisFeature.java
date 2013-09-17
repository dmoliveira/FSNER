package lbd.NewModels.Context;

import lbd.NewModels.Affix.Affix.AffixType;
import lbd.NewModels.Context.ContextManager.ContextType;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;

public class ContextAnalysisFeature extends NewFeatureTypes {

	private static final long serialVersionUID = 3897269660419400622L;

	protected transient ContextAnalysis contextAnalysis;
	protected transient ContextType contextType;
	protected transient AffixType affixType;

	protected int windowSize;
	
	public ContextAnalysisFeature(FeatureGenImpl fgen, ContextAnalysis contextAnalysis, ContextType contextType,
			int windowSize, AffixType affixType) {
		
		super(fgen);
		
		this.featureName = "CxtAnalysis(ct"+contextType.name()+",ws"+windowSize+",at"+affixType.name()+")";
		
		this.contextType = contextType;
		this.contextAnalysis = contextAnalysis;
		this.windowSize = windowSize;
		this.affixType = affixType;
		this.featureType = FeatureType.ContextAnalysis;
		this.skipOutsideState = false;
		//this.useOnlyWhenAllOtherFeaturesInactives = true;
		//featureToNotRunInParallel = new FeatureType [] {FeatureType.BrownHierarquicalCluster, FeatureType.Dictionary, FeatureType.SummarizedPattern};
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {
		
		/*featureId = ((featureMode == FeatureMode.InTrain && data.y(pos) == 3))? -1 :
			contextAnalysis.getContextId(data, pos, contextType, windowSize);*/
		
		featureId = contextAnalysis.getContextId(data, pos, contextType, windowSize);
		//if(featureId > -1)System.out.println(data.x(pos));
		
		return featureId;
	}
	
	protected boolean wasReachedCriteria() {
		return (contextType == ContextType.PrefixSuffix &&  sequence.length() > windowSize * 2) ||
		(contextType != ContextType.PrefixSuffix && sequence.length() > windowSize);
	}
}
