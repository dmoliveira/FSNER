package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

public class GrammaticalNumeralNounFeature extends FeatureTypes {

	private static final long serialVersionUID = 1L;
	protected float weight;
	protected final String [] NUMERAL_NOUN_SUFFIX = {"es", "eis", "is", "ns", "ões", "oes", "ães", "aes", "s"};
	
	protected boolean isNumeralNounSuffix;
	protected int featureId;
	protected int currentState;
	protected double THRESHOULD = 3;
	
	public GrammaticalNumeralNounFeature(FeatureGenImpl fgen, float weight) {
		super(fgen);
		this.weight = weight;
	}
	
	public GrammaticalNumeralNounFeature(FeatureGenImpl fgen) {
		super(fgen);
	}
	
	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
		
		int startSuffixPosition;
		
		isNumeralNounSuffix = false;
		featureId = -1;
		currentState = -1;
		
		for(int i = 0; i < NUMERAL_NOUN_SUFFIX.length; i++) {
			
			startSuffixPosition = ((String)data.x(pos)).toLowerCase().indexOf(NUMERAL_NOUN_SUFFIX[i]);
			
			if( startSuffixPosition != -1 && 
					((String)data.x(pos)).length()  >=  THRESHOULD + NUMERAL_NOUN_SUFFIX[i].length() &&
					startSuffixPosition + NUMERAL_NOUN_SUFFIX[i].length() == ((String)data.x(pos)).length()) {
				isNumeralNounSuffix = true;
				featureId = i;
				break;
			}
		}
		
		return isNumeralNounSuffix;
	}

	@Override
	public boolean hasNext() {
		
		currentState++;
		return (isNumeralNounSuffix && currentState < model.numStates());
	}

	@Override
	public void next(FeatureImpl f) {
		
		setFeatureIdentifier(featureId * model.numStates() + currentState, currentState,
				"NumeralNoun_" + "(" + NUMERAL_NOUN_SUFFIX[featureId] + ")",f);
		
		f.yend = currentState;
		f.ystart = -1;
		f.val = 1 * weight;
	}

}
