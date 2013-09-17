package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

public class TwitterSymbolsFeature extends FeatureTypes {
	
	private static final long serialVersionUID = 1L;
	protected final String [] TWEET_SYMBOL = {"rt", "http", "www", "@", "#"};	
	protected final int STATE_OUTSIDE = 3;
	
	protected boolean isTweetSymbol;
	protected boolean hasAnalyzed;
	protected int featureId;
	protected float weight;

	public TwitterSymbolsFeature(FeatureGenImpl fgen, float weight) {
		super(fgen);
		this.weight = weight;
	}
	
	public TwitterSymbolsFeature(FeatureGenImpl fgen) {
		super(fgen);
		weight = 1;
	}
	
	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
		
		isTweetSymbol = false;
		hasAnalyzed = false;
		featureId = -1;
		
		for(int i = 0; i < TWEET_SYMBOL.length; i++)
			if(((String)data.x(pos)).toLowerCase().indexOf(TWEET_SYMBOL[i]) != -1) {
				isTweetSymbol = true;
				featureId = i;
				break;
			}
		
		return isTweetSymbol;
	}

	@Override
	public boolean hasNext() {
		return (isTweetSymbol && !hasAnalyzed);
	}

	@Override
	public void next(FeatureImpl f) {
		
		setFeatureIdentifier(featureId, STATE_OUTSIDE, "TweFeat_" + "(" + TWEET_SYMBOL[featureId] + ")",f);
		
		f.yend = STATE_OUTSIDE;
		f.ystart = -1;
		f.val = 1 * weight;
		
		hasAnalyzed = true;
	}
}
