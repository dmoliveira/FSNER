package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.Utils.Utils;

public class OutputContextFeature extends ContextFeature {

	private static final long serialVersionUID = 1L;

	public OutputContextFeature(FeatureGenImpl fgen, SupportContext supportContext, float weight) {
		super(fgen, supportContext);
		this.weight = weight;
	}

	public OutputContextFeature(FeatureGenImpl fgen, SupportContext supportContext) {
		super(fgen, supportContext);
		weight = 1f;
	}

	public OutputContextFeature(FeatureGenImpl fgen, SupportContext supportContext, String featureName) {
		super(fgen, supportContext, featureName);
	}

	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {

		isFeatureAdded = false;
		ContextToken contextToken = null;
		currentState = -1;

		if(sequence != data) {
			sequence = data;
			sequenceList = Utils.convertSequenceToLowerCase(data, data.length());
		}

		//contextToken = supportContext.existSuffixInSequence(sequenceList, pos);
		/*if(supportContext.isTest) {
			contextToken = supportContext.existContextZeroInSequenceContextZeroHashMap(sequenceList[pos]);

			if(contextToken == null)
				contextToken = supportContext.existContextInSequenceContextHashMap(sequenceList, pos);

		} if(!supportContext.isTest || contextToken == null)*/
		if(!supportContext.isTest || supportContext.existPrefixInSequenceRestrictedPrefixContextHashMap(sequenceList, pos) == null) {
			contextToken = supportContext.existSuffixInSequenceRestrictedSuffixContextHashMap(sequenceList, pos);
			/*else
				contextToken = null;*/
		}

		/*if(supportContext.isTest && contextToken == null) {
			ExtendsAccentVariabilityInTweet eXAVIT = new ExtendsAccentVariabilityInTweet();
			String[][]sequenceTokenAccentVariability = eXAVIT.generateSequenceAccentVariation(supportContext, sequenceList, pos);

			if(sequenceTokenAccentVariability != null) {
				for(int i = 0; i < sequenceTokenAccentVariability.length; i++) {
					contextToken = supportContext.existSuffixInSequenceRestrictedSuffixContextHashMap(sequenceList, pos);

					if(contextToken != null)
						break;
				}
			}
		}*/

		//contextToken = supportContext.existSuffixInSequenceRestrictedSuffixContextHashMap(sequenceList, pos);
		hasFoundContext = (contextToken != null);

		if(hasFoundContext) {

			idContextFeature = contextToken.getContextTokenID();
			//currentState = contextToken.getToken().getState();
			previousState = -1;
			featureName = "oCxt("+supportContext.getWindowSize()+"){";

			featureName += contextToken.getTokenValue();

			if(contextToken.getSuffixSize() > 0) {
				featureName += "," + contextToken.getSuffix(0).getValue().toUpperCase();
			}

			featureName += "}";

			advance();
		}

		return(hasFoundContext);
	}
}
