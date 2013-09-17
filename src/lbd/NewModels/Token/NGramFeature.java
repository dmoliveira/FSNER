package lbd.NewModels.Token;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.Context.ContextManager.ContextType;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class NGramFeature extends NewFeatureTypes{

	private static final long serialVersionUID = -4127762658988563430L;
	
	protected transient NGram nGram;
	protected transient int startPositionNGram;
	protected transient int endPositionNGram;

	public NGramFeature(FeatureGenImpl fgen, NGram nGram, int startPositionNGram, int endPositionNGram) {
		super(fgen);
		
		featureName = "NGram";
		proccessSequenceType = ProccessSequenceType.Plain;
		featureType = FeatureType.NGram;
		
		this.nGram = nGram; 
		
		this.startPositionNGram = startPositionNGram;
		this.endPositionNGram = endPositionNGram;
		
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {
		
		int nGramId = nGram.getNGramId(proccessedSequence[pos], startPositionNGram, endPositionNGram);
		
		return nGramId;
	}

}
