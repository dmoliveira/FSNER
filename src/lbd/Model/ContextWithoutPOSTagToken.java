package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.Utils.Utils;

public class ContextWithoutPOSTagToken extends ContextFeature {

	private static final long serialVersionUID = 1L;
	
	public ContextWithoutPOSTagToken(FeatureGenImpl fgen,
			SupportContext supportContext, float weight) {
		super(fgen, supportContext, weight);
	}
	
	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {

		isFeatureAdded = false;
		currentState = -1;
		previousState = -1;
		ContextToken context;
		String key;
		
		if(sequence != data) {
			sequence = data;
			sequenceList = Utils.convertSequenceToLowerCase(data, data.length());
		}
		
		key = supportContext.generateSequenceWithoutPOSTagTokenKey(sequenceList, pos);
		context = supportContext.getFastAccessContextWithoutPOSTagToken().get(key);
		
		hasFoundContext = (context != null);
		
		if(hasFoundContext) {
			
			idContextFeature = context.getContextTokenID();
			currentState = context.getToken().getState();
			previousState = (context.getPrefixSize() > 0)?context.getPrefix(0).getState() : -1;
			featureName = "CxtWPTT_" + key;
		}
		
		return(hasFoundContext);
	}

}
