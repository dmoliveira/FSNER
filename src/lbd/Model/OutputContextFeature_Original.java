package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

public class OutputContextFeature_Original extends FeatureTypes {

	private static final long serialVersionUID = 1L;
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

	boolean oneTime = false;
	boolean isEntity = false;
	int prefixIndex;
	int suffixIndex;

	private String featureName;

	public OutputContextFeature_Original(FeatureGenImpl fgen, SupportContext supportContext) {
		super(fgen);
		featureName = "Cxt.";
		idContextFeature = 1;
		currentState = -1;
		this.supportContext = supportContext;
	}

	public OutputContextFeature_Original(FeatureGenImpl fgen,
			SupportContext supportContext, String featureName) {
		super(fgen);
		this.featureName = featureName;
		idContextFeature = 1;
		currentState = -1;
		this.supportContext = supportContext;
	}

	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {

		oneTime = false;
		isEntity = false;

		if(currentState < 0) {

			//idContextFeature = supportContext.existContextInSequenceExperiment(data, pos);
			hasFoundContext = (idContextFeature >= 0);

			if(hasFoundContext) {
				isEntity = true;
				featureName = "OCxt." + ((String)data.x(pos));
				currentState = supportContext.getContextTokenByID(idContextFeature).getToken().getState();
				previousState = (pos > 0)?data.y(pos-1):-1;
			} else {
				featureName = "nonOCxt." + ((String)data.x(pos));
				ContextToken t = null;//supportContext.getOutsideToken((String)data.x(pos));

				if(t != null) {
					idContextFeature = t.getContextTokenID();
					currentState = t.getToken().getState();
					hasFoundContext = true;
				}

			}

		} else {
			hasFoundContext = false;
		}

		return(hasFoundContext);
	}

	public void nextState() {
		currentState = supportContext.getContextTokenByID(idContextFeature).getSuffix(0).getState();
		idContextFeature++;

		if(!oneTime) {
			oneTime = true;
		}
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
		//currentState = -1;
		if(isEntity && oneTime) {
			nextState();
		} else {
			currentState = -1;
		}
	}

}
