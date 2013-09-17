package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

public class StatePositionFeature extends FeatureTypes {

	private static final long serialVersionUID = 1L;
	private int currentState;
	private int positionId;
	private boolean hasPosition;
	private float weight;
	
	public StatePositionFeature(FeatureGenImpl fgen, float weight) {
		super(fgen);
		this.weight = weight;
	}

	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
		
		hasPosition = false;
		
		if(pos > 0 && pos < data.length()) {
			hasPosition = true;
			currentState = 0;
			positionId = pos;
		}
		
		return hasPosition;
	}
	
	@Override
	public boolean hasNext() {
		return (hasPosition && currentState < model.numStates());
	}
	
	@Override
	public void next(FeatureImpl f) {
		
		setFeatureIdentifier(positionId * model.numStates() + currentState, currentState, "StatePos" + 
				"(" + positionId + "," + currentState + ")",f);
		
		f.yend = currentState;
		f.ystart = -1;
		f.val = 1 * weight;
		
		currentState++;
	}
}
