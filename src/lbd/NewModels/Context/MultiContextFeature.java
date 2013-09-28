package lbd.NewModels.Context;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.Context.ContextManager.ContextType;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class MultiContextFeature extends NewFeatureTypes {

	private static final long serialVersionUID = 1L;

	protected ContextAnalysis contextAnalysis;

	protected ContextType contextType;
	protected final int NUMBER_CONTEXT_TYPE = 3;

	protected int windowSize;

	public MultiContextFeature(FeatureGenImpl fgen, ContextAnalysis contextAnalysis) {

		super(fgen);

		this.contextAnalysis = contextAnalysis;
		featureName = "MultiContext";
	}


	@Override
	protected int startFeature(DataSequence data, int pos) {

		int contextId = ((featureMode == FeatureMode.InTrain && data.y(pos) == 3))? -1 :
			contextAnalysis.getContextId(data, pos, contextType, windowSize);

		return contextId;
	}

	public int getSimilarContext(DataSequence data, int pos) {

		String encodedContext = ContextManager.getEncodedContext(proccessedSequence, pos);
		int contextId = -1;

		/*String similarContext = contextAnalysis.getSimilarContext(encodedContext, ContextType.Prefix);
		contextType = ContextType.Prefix;

		if(similarContext.equals("")) {
			similarContext = contextAnalysis.getSimilarContext(encodedContext, ContextType.PrefixSuffix);
			contextType = ContextType.PrefixSuffix;
		} if(similarContext.equals("")) {
			similarContext = contextAnalysis.getSimilarContext(encodedContext, ContextType.Suffix);
			contextType = ContextType.Suffix;
		}

		if(!similarContext.equals(""))
			contextId = contextAnalysis.getContextId(similarContext, contextType);*/

		return(contextId);
	}
}
