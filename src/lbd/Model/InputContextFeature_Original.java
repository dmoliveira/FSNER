package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

public class InputContextFeature_Original extends FeatureTypes {

	private static final long serialVersionUID = 9029957088656523828L;
	
	private SupportContext supportContext;
	private boolean hasFoundContext;
	
	private ContextToken contextToken;
	
	private int idContextFeature;
	private int currentState;
	private int previousState;
	
	private String featureName;
	
	public InputContextFeature_Original(FeatureGenImpl fgen, SupportContext supportContext) {
		super(fgen);
		featureName = "ICxt.";
		idContextFeature = 1;
		currentState = -1;
		this.supportContext = supportContext;
	}
	
	public InputContextFeature_Original(FeatureGenImpl fgen, 
			SupportContext supportContext, String featureName) {
		super(fgen);
		this.featureName = featureName;
		idContextFeature = 1;
		currentState = -1;
		this.supportContext = supportContext;
	}

	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
		
		hasFoundContext = false;
		
		if(currentState < 0) {
			
			//idContextFeature = supportContext.existInputContextInSequence(data, pos);
			hasFoundContext = (idContextFeature >= 0);
			
			if(hasFoundContext) {
				
				contextToken = supportContext.getContextTokenByID(idContextFeature);
				featureName = "ICxt." + ((String)data.x(pos));
				
				currentState = contextToken.getToken().getState();
				previousState = (pos > 0 && contextToken.getPrefixSize() > 0)?contextToken.getPrefix(0).getState():-1;				
			}
			
		}
		
		return(hasFoundContext);
	}
	
	@Override
	public boolean hasNext() {
		
		return (currentState >= 0);
	}

	@Override
	public void next(FeatureImpl f) {
			
		setFeatureIdentifier(idContextFeature, currentState, featureName + "(" + idContextFeature + ")", f);
		
		f.yend = currentState;
		f.ystart = previousState;
		f.ystart = -1;
		f.val = 1;
		
		currentState = -1;
	}
	
}
