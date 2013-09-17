package lbd.Model;

import java.util.ArrayList;

import lbd.Utils.ExtendsAccentVariabilityInTweet;
import lbd.Utils.Utils;
import lbd.Utils.ExtendsAccentVariabilityInTweet.AccentCharacter;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;

public class ContextMostValuableTokensFeature extends ContextFeature{

	protected double threshould;
	
	public ContextMostValuableTokensFeature(FeatureGenImpl fgen,
			SupportContext supportContext, float weight, double threshould) {
		super(fgen, supportContext, weight);
		this.threshould = threshould;
	}
	
	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {

		isFeatureAdded = false;
		currentState = -1;
		
		if(sequence != data) {
			sequence = data;
			sequenceList = Utils.convertSequenceToLowerCase(data, data.length());
		}
		
		idContextFeature = supportContext.isContextMostValuableWords(sequenceList, pos, threshould);
		
		hasFoundContext = (idContextFeature != -1);
		
		if(hasFoundContext) {
			
			//idContextFeature = contextToken.getContextTokenID();
			currentState = 4;//contextToken.getToken().getState();
			previousState = -1;
			featureName = "CxtMVW_"+data.x(pos);
		}
		
		return(hasFoundContext);
	}

}
