package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

public class OutsideFeature extends FeatureTypes{

	private static final long serialVersionUID = 1L;

	private SupportContext supportContext;
	private boolean hasFoundOutsideToken;

	private ContextToken outsideToken;

	private int idOutsideFeature;
	private int currentState;
	private int previousState = -1;

	private String featureName;

	public OutsideFeature(FeatureGenImpl fgen, SupportContext supportContext) {
		super(fgen);
		featureName = "Out.";
		idOutsideFeature = 1;
		currentState = -1;
		this.supportContext = supportContext;
	}

	public OutsideFeature(FeatureGenImpl fgen,
			SupportContext supportContext, String featureName) {
		super(fgen);
		this.featureName = featureName;
		idOutsideFeature = 1;
		currentState = -1;
		this.supportContext = supportContext;
	}

	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {

		hasFoundOutsideToken = false;

		if(currentState < 0) {

			//outsideToken = supportContext.getOutsideToken((String)data.x(pos));
			hasFoundOutsideToken = (outsideToken != null);

			if(hasFoundOutsideToken) {

				featureName = "Out." + ((String)data.x(pos));

				idOutsideFeature = outsideToken.getContextTokenID();
				currentState = outsideToken.getToken().getState();

				if(outsideToken.getPrefixSize() > 0) {
					previousState = outsideToken.getPrefix(0).getState();
				} else {
					previousState = -1;
				}
			}

		}

		return(hasFoundOutsideToken);
	}

	@Override
	public boolean hasNext() {

		return (currentState >= 0);
	}

	@Override
	public void next(FeatureImpl f) {

		setFeatureIdentifier(idOutsideFeature, currentState, featureName + "(" + idOutsideFeature + ")", f);

		f.yend = currentState;
		f.ystart = previousState;
		f.val = 1;

		currentState = -1;
	}
}
