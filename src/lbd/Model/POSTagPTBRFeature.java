package lbd.Model;

import lbd.AutoTagger.SelectByPOSTag;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

public class POSTagPTBRFeature extends FeatureTypes{
	
	private static final long serialVersionUID = 1L;
	protected SelectByPOSTag selByPOSTag;
	protected SupportContext supportContext;
	protected String posTagKey;
	protected float weight;
	
	protected boolean hasFoundPOSTag;
	protected boolean isFeatureAdded;
	protected int currentState;	
	
	protected int featureNumberTest;
	protected int featureNotFound;
	
	public POSTagPTBRFeature(FeatureGenImpl fgen, SelectByPOSTag selByPOSTag, float weight) {
		super(fgen);
		
		this.selByPOSTag = selByPOSTag;
		this.weight = weight;	
	}
	
	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
		
		posTagKey = selByPOSTag.generatePOSTagKey(data, pos);
		hasFoundPOSTag = false;
		isFeatureAdded = false;
		currentState = -1;
		
		if(selByPOSTag.isPOSTagAboveOrEqualsThreshould(posTagKey)) {
			hasFoundPOSTag = true;
			advance();
		}
		
		//@DMZDebug
		if(selByPOSTag.getSupportContext().isTest && data.y(pos) > -1 && data.y(pos) < 5 && data.y(pos) != 3 && !hasFoundPOSTag) {
			int start = (pos - 3 > 0)? pos - 3 : 0;
			System.out.print("Not Found #" + ++featureNotFound + " ");
			for(int i = start; i <= pos + 3 && i < data.length(); i++)
				System.out.print(((i == pos)?"[":"") + data.x(i) + "(" + data.y(i) + ")" + ((i == pos)?"] ":" "));
			
			System.out.println();
		}
		
		//@DMZDebug
		if(selByPOSTag.getSupportContext().isTest && data.y(pos) > -1 && data.y(pos) < 5 && data.y(pos) != 3 && !hasFoundPOSTag) {
			int start = (pos - 3 > 0)? pos - 3 : 0;
			System.out.print("Not Found #" + ++featureNotFound + " ");
			for(int i = start; i <= pos + 3 && i < data.length(); i++)
				System.out.print(((i == pos)?"[":"") + data.x(i) + ((i == pos)?"] ":" "));
			
			System.out.println();
		}
		
		return hasFoundPOSTag;
	}

	@Override
	public boolean hasNext() {
		return (hasFoundPOSTag && currentState < model.numStates());//!isFeatureAdded);
		//return (hasFoundPOSTag && !isFeatureAdded);
	}
	
	protected void advance() {
		currentState++;
	}

	@Override
	public void next(FeatureImpl f) {
		
		ContextToken context = selByPOSTag.getContextAssociatedWithPOSTag(posTagKey);
		
		//setFeatureIdentifier(context.getContextTokenID(), context.getToken().getState(), "POSTag" + "(" + posTagKey + ")",f);
		setFeatureIdentifier(context.getContextTokenID() * model.numStates() + currentState, currentState, "POSTag" + "(" + posTagKey + ")",f);
		
		f.yend = currentState;//context.getToken().getState();
		//f.yend = context.getToken().getState();
		f.ystart = context.getPrefix(0).getState();
		f.val = 1 * weight;
		
		//-- @DMZDebug
		/*if(selByPOSTag.getSupportContext().isTest && currentState == 4) {
			featureNumberTest++;
			System.out.println("# " + featureNumberTest + " " + "POSTag (" + (context.getContextTokenID() * model.numStates() + currentState) + ")" + "cS: " + currentState + ", pS: " + context.getPrefix(0).getState());
		}*/
		
		isFeatureAdded = true;
		advance();
	}

}
