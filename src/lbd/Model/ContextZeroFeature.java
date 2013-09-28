package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.Utils.Utils;

public class ContextZeroFeature extends ContextFeature {

	private static final long serialVersionUID = 1L;

	public ContextZeroFeature(FeatureGenImpl fgen, SupportContext supportContext, float weight) {
		super(fgen, supportContext);
		this.weight = weight;
	}

	public ContextZeroFeature(FeatureGenImpl fgen, SupportContext supportContext) {
		super(fgen, supportContext);
		weight = 1f;
	}

	public ContextZeroFeature(FeatureGenImpl fgen, SupportContext supportContext, String featureName) {
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

		//Date date = new Date();
		//if(!supportContext.isTest || supportContext.existSuffixInSequenceRestrictedSuffixContextHashMap(sequenceList, pos) == null)
		contextToken = supportContext.existContextZeroInSequenceContextZeroHashMap(sequenceList[pos]);

		/* Prototype */
		/*if(supportContext.isTest && contextToken == null) {

			ExtendsAccentVariabilityInTweet eXAVIT = new ExtendsAccentVariabilityInTweet();

			ArrayList<AccentCharacter> accentList = eXAVIT.getTokenAccents(sequenceList[pos]);

			if(accentList.size() > 0) {
				ArrayList<String> tokenVariation = eXAVIT.generateVariationAccentToken(sequenceList[pos], accentList);

				for(int i = 1; i < tokenVariation.size(); i++) {
					contextToken = supportContext.existContextZeroInSequenceContextZeroHashMap(tokenVariation.get(i));

					if(contextToken != null)
						break;
				}
			}
		}*/

		//System.out.println("CxtZeroTime: " + ((new Date()).getTime() - date.getTime()));
		hasFoundContext = (contextToken != null);

		if(hasFoundContext) {
			idContextFeature = contextToken.getContextTokenID();
			//currentState = contextToken.getToken().getState();
			previousState = -1;

			featureName = "CxtZero{" + contextToken.getTokenValue() + "}";

			advance();
		}

		/*if(supportContext.isTest && sequence.y(pos) > -1 && sequence.y(pos) < 5 && sequence.y(pos) != 3 && !hasFoundContext) {
			int start = (pos - 3 > 0)? pos - 3 : 0;
			System.out.print("Not Found #" + ++featureNotFound + " ");
			for(int i = start; i <= pos + 3 && i < sequence.length(); i++)
				System.out.print(((i == pos)?"[":"") + sequence.x(i) + ((i == pos)?"] ":" "));

			System.out.println();
		}*/

		return(hasFoundContext);
	}
}
