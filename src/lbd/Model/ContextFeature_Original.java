package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

public class ContextFeature_Original extends FeatureTypes {

	private static final long serialVersionUID = -4097231555056110415L;
	
	private SupportContext supportContext;
	private boolean hasFoundContext;
	
	private int idContextFeature;
	private int currentState;
	private int previousState;
	
	/** Test + Features **/
	private ContextToken contextToken;
	
	boolean isPrefixTime;
	boolean isSuffixTime;
	boolean isEntityTime;
	boolean isOutside;
	
	int prefixIndex;
	int suffixIndex;
	
	private String featureName;
	
	public ContextFeature_Original(FeatureGenImpl fgen, SupportContext supportContext) {
		super(fgen);
		featureName = "Cxt.";
		idContextFeature = 1;
		currentState = -1;
		this.supportContext = supportContext;
	}
	
	public ContextFeature_Original(FeatureGenImpl fgen, 
			SupportContext supportContext, String featureName) {
		super(fgen);
		this.featureName = featureName;
		idContextFeature = 1;
		currentState = -1;
		this.supportContext = supportContext;
	}

	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
		
		if(currentState < 0) {
			
			//idContextFeature = supportContext.existContextInSequence(data, pos).getContextTokenID();
			hasFoundContext = (idContextFeature >= 0);
			
			if(hasFoundContext) {
				featureName = "Cxt." + ((String)data.x(pos));
				currentState = data.y(pos);
				previousState = (pos > 0)?data.y(pos-1):-1;
			}
			
		} else {			
			hasFoundContext = false;
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
		f.ystart = -1;
		f.val = 1;
		
		//Restart to startScanFeaturesAt again
		currentState = -1;
	}
	

}
