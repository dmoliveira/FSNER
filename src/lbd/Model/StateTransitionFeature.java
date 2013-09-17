package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

public class StateTransitionFeature extends FeatureTypes {
	
	private static final long serialVersionUID = 1L;

	public StateTransitionFeature(FeatureGenImpl fgen, boolean isTest) {
		super(fgen);
		this.isTest = isTest;
	}
	
	protected int previousState;
	protected int currentState;
	protected int featureId;
	
	protected boolean isTest;
	
	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
		
		featureId = -1;
		currentState = -1;
		previousState = -1;
		
		//-- O Beginning, 1 Inside, 2 Last and 4 UnitToken
		if(!isTest || !(data.y(pos) == 0 || data.y(pos) == 1 || data.y(pos) == 2 || data.y(pos) == 4)) {
			advanceFeature();
		
			return true;
		} else {
			return false;
		}
	}
	
	private void advanceFeature() {
		featureId++;
		if(featureId % (model.numStates()+1) == 0) {
			currentState++;
			previousState = -1;
		} else
			previousState++;
	}
	
	@Override
	public boolean hasNext() {
		
		return (featureId < model.numStates() * model.numStates() + model.numStates());
	}
	
	@Override
	public void next(FeatureImpl f) {
		
		setFeatureIdentifier(featureId, featureId, "StateTrans(" + previousState + "->" + currentState + ")", f);
		//System.out.println(featureId + " StateTrans_" + previousState + "->" + currentState);
		
		f.yend = currentState;
		f.ystart = previousState;
		f.val = 1;
		
		advanceFeature();
	}

}
