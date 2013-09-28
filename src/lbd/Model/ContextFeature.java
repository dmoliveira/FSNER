package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

import java.io.Writer;

import lbd.AutoTagger.SelectByContext;
import lbd.Utils.ExtendsAccentVariabilityInTweet;
import lbd.Utils.Utils;

public class ContextFeature extends FeatureTypes {

	private static final long serialVersionUID = 1L;
	protected final String ENCODE_USED = "ISO-8859-1";

	protected SupportContext supportContext;
	protected boolean hasFoundContext;
	protected boolean isFeatureAdded;

	protected int idContextFeature;
	protected int currentId;
	protected int currentState;
	protected int previousState;

	protected String featureName;
	protected DataSequence sequence;
	protected String [] sequenceList;

	protected int featureNumberTest;
	protected int featureNotFound;

	protected float weight = 1;

	protected transient Writer outputUnknownContext;

	protected SelectByContext selByCxt;

	public ContextFeature(FeatureGenImpl fgen, SelectByContext selByCxt, float weight) {
		super(fgen);

		featureName = "Cxt.";
		idContextFeature = 1;
		currentState = -1;
		this.selByCxt = selByCxt;
		this.supportContext = selByCxt.getSupportContext();

		this.weight = weight;
	}

	public ContextFeature(FeatureGenImpl fgen, SupportContext supportContext, float weight) {
		super(fgen);

		featureName = "Cxt.";
		idContextFeature = 1;
		currentState = -1;
		this.supportContext = supportContext;

		this.weight = weight;
	}

	public ContextFeature(FeatureGenImpl fgen, SupportContext supportContext) {
		super(fgen);
		featureName = "Cxt.";
		idContextFeature = 1;
		currentState = -1;
		this.supportContext = supportContext;
	}

	public ContextFeature(FeatureGenImpl fgen,
			SupportContext supportContext, String featureName) {
		super(fgen);
		this.featureName = featureName;
		idContextFeature = 1;
		currentState = -1;
		this.supportContext = supportContext;
	}

	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {

		isFeatureAdded = false;
		ContextToken contextToken = null;
		currentState = -1;
		currentId = -1;

		if(sequence != data) {
			sequence = data;
			sequenceList = Utils.convertSequenceToLowerCase(data, data.length());
		}

		//Date date = new Date();
		/*if(supportContext.isTest)
			contextToken = supportContext.existContextZeroInSequenceContextZeroHashMap(sequenceList[pos]);
		if(!supportContext.isTest || contextToken == null)*/

		//if(!supportContext.isTest || (supportContext.isTest && supportContext.existContextZeroInSequenceContextZeroHashMap(sequenceList[pos]) == null)) {
		//if(supportContext.existContextZeroInSequenceContextZeroHashMap(sequenceList[pos]) == null) {
		//-- Traditional
		//contextToken = supportContext.existContextInSequenceContextHashMap(sequenceList, pos);
		//}

		//-- Modern
		//String key = selByCxt.getSupportContext().generateLeftPrefixKeyFromSequence(sequenceList, pos, false);
		//contextToken = selByCxt.getSupportContext().getFastAccessPrefixContextList().get(key);

		//else
		//contextToken = null;
		//System.out.println("CxtTime: " + ((new Date()).getTime() - date.getTime()));
		//contextToken = supportContext.existContextInSequenceRestricted(sequenceList, pos);

		//if(!supportContext.isTest || supportContext.existContextZeroInSequenceContextZeroHashMap(sequenceList[pos]) != null) {
		contextToken = supportContext.existContextInSequenceContextHashMap(sequenceList, pos);
		//contextToken = getExVAITContext(contextToken, pos);
		//}

		hasFoundContext = (contextToken != null);// && selByCxt.isContextAboveOrEqualsThreshould(contextToken));

		if(hasFoundContext) {

			idContextFeature = contextToken.getContextTokenID();
			//currentState = contextToken.getToken().getState();
			previousState = -1;
			featureName = "Cxt("+supportContext.getWindowSize()+"){";

			if(contextToken.getPrefixSize() > 0) {
				previousState = contextToken.getPrefix(0).getState();
				featureName += contextToken.getPrefix(0).getValue().toUpperCase() + ",";
			}

			featureName += contextToken.getTokenValue();

			if(contextToken.getSuffixSize() > 0) {
				featureName += "," + contextToken.getSuffix(0).getValue().toUpperCase();
			}

			featureName += "}";

			advance();
		}

		//@DMZDebug
		/*if(supportContext.isTest && sequence.y(pos) > -1 && sequence.y(pos) < 5 && sequence.y(pos) != 3 && !hasFoundContext) {
			int start = (pos - 3 > 0)? pos - 3 : 0;
			System.out.print("Not Found #" + ++featureNotFound + " ");
			for(int i = start; i <= pos + 3 && i < sequence.length(); i++)
				System.out.print(((i == pos)?"[":"") + sequence.x(i) + ((i == pos)?"] ":" "));

			System.out.println();
		}*/

		return(hasFoundContext);
	}

	@Override
	public boolean hasNext() {
		return (hasFoundContext && currentState < model.numStates());
		//return(hasFoundContext &&  maxState > currentState);
		//return (hasFoundContext && !isFeatureAdded);
	}

	protected void advance() {
		currentState++;
	}

	@Override
	public void next(FeatureImpl f) {

		//setFeatureIdentifier(idContextFeature, currentState, featureName + "(" + idContextFeature + ")",f);
		setFeatureIdentifier((idContextFeature * model.numStates() + currentState), currentState, featureName + "(" + idContextFeature + ")",f);

		//-- @DMZDebug
		/*if(supportContext.isTest && currentState == 4) {
			featureNumberTest++;
			System.out.println("# " + featureNumberTest + " " + featureName + "(" + (idContextFeature * model.numStates() + currentState) + ")" + "cS: " + currentState + ", pS: " + previousState);
		}*/

		f.yend = currentState;
		f.ystart = previousState;
		f.val = 1 * weight;

		//-- Restart to startScanFeaturesAt again
		isFeatureAdded = true;
		advance();
	}

	protected ContextToken getExVAITContext(ContextToken contextToken, int pos) {
		if(supportContext.isTest && contextToken == null) {
			ExtendsAccentVariabilityInTweet eXAVIT = new ExtendsAccentVariabilityInTweet();
			String[][]sequenceTokenAccentVariability = eXAVIT.generateSequenceAccentVariation(supportContext, sequenceList, pos);

			if(sequenceTokenAccentVariability != null) {
				for(int i = 0; i < sequenceTokenAccentVariability.length; i++) {
					contextToken = supportContext.existContextInSequenceContextHashMap(sequenceTokenAccentVariability[i], pos);

					if(contextToken != null) {
						return(contextToken);
					}
				}
			}
		}

		return(contextToken);
	}
}
