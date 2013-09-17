package lbd.Model;

import lbd.Utils.Utils;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

public class VerbTenseFeature extends FeatureTypes{
	
	protected SupportContext supportContext;
	
	/*protected String [] verbTenseSuffix = {"as", "os", "amos", "ais", "am", 
			"emos", "ai", "em", "es", "eis", "i", "este", "eu", "estes", 
			"eram", "era", "eras", "eramos", "ereis", "er", "eres", "ermos",
			"erdes", "erem", "esse", "esses", "essemos", "esseis", "essem",
			"ei", "Ã¡s", "Ã¡", "Ã£o", "ao", "ia", "ias", "Ã­amos", "iamos", 
			"Ã­eis", "ieis", "iam", "es", "mos", "des"};*/
	
	protected String [] verbTenseSuffix = {"as", "os", "amos", "ais", "am", 
			"emos", "ai", "em", "es", "eis", "i", "este", "eu", "estes", "rá", "ou",
			"eram", "era", "eras", "eramos", "ereis", "er", "eres", "ermos",
			"erdes", "erem", "esse", "esses", "essemos", "esseis", "essem",
			"ei", "é¡s", "e¡s", "é¡", "ão", "ao", "ia", "ias", "í­amos", "iamos", 
			"í­eis", "ieis", "iam", "es", "mos", "des", "ar", "ando", "indo", "endo", "ado", "ido"};
	
	protected int currentState;
	protected int idVerbTenseToken;
	protected float weight;
	
	protected String featureName;
	protected final String FEATURE_ACRONYM = "VrbTns_";
	
	protected DataSequence sequence;
	protected String [] sequenceList;
	
	boolean wasAdded;
	boolean hasFeature;
	
	public VerbTenseFeature(FeatureGenImpl fgen, SupportContext supportContext, float weight) {
		super(fgen);
		this.supportContext = supportContext;
		this.weight = weight;
	}
	
	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
				
		ContextToken context = null;
		int state = -1;
		hasFeature = false;
		wasAdded = false;
		currentState = -1;
		
		if(sequence != data) {
			sequence = data;
			sequenceList = Utils.convertSequenceToLowerCase(data, data.length());
		}
		
		/** Original **/
		/*context = supportContext.existContextZeroInSequenceContextZeroHashMap(sequenceList[pos]);
		
		if(context != null) {
			
			state = context.getToken().getState();
			
			if(data.length() > pos + 1)
				context = supportContext.existContextZeroInSequenceContextZeroHashMap(sequenceList[pos+1]);
			
			if(((context != null && isVerbTense(context.getTokenValue()) == -1) || context == null) &&
					data.length() > pos + 2)
				context = supportContext.existContextZeroInSequenceContextZeroHashMap(sequenceList[pos+2]);
		}
		
		hasFeature = (context != null &&  isVerbTense(context.getTokenValue()) != -1 && state != 3);*/
		
		/** New approach **/
			
		if(sequenceList.length > pos + 1)
			hasFeature = (idVerbTenseToken = isVerbTense(sequenceList[pos+1])) != -1;
		
		if(!hasFeature && sequenceList.length > pos + 2)
			hasFeature = (idVerbTenseToken = isVerbTense(sequenceList[pos+2])) != -1;
		
		if(hasFeature) {
			
			//idVerbTenseToken = context.getContextTokenID();
			featureName = FEATURE_ACRONYM + data.x(pos); //+ "," + context.getTokenValue();
			advance();
			//currentState = state;
		}
		
		return (hasFeature);
	}
	
	@Override
	public boolean hasNext() {
		return (hasFeature && currentState < model.numStates());
	}
	
	protected void advance() {
		currentState++;
	}

	@Override
	public void next(FeatureImpl f) {
		
		setFeatureIdentifier(idVerbTenseToken * model.numStates() + currentState , currentState, featureName + "(" + idVerbTenseToken + ")",f);
		
		f.yend = currentState;
		f.ystart = -1;
		f.val = 1 * weight;
				
		wasAdded = true;
		advance();
	}
	
	protected int isVerbTense(String token) {
		
		int isVerbTense = -1;
		String suffix;
		
		token = token.toLowerCase();
		
		for(int i = 0; i < verbTenseSuffix.length; i++) {
			
			suffix = verbTenseSuffix[i];
			
			if(token.length() - suffix.length() >= 3 &&
					token.substring(token.length() - suffix.length()).equals(suffix)) {
				isVerbTense = i;
				break;
			}
		}
		
		return(isVerbTense);
	}
}
