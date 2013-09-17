package lbd.Model;

import lbd.FSNER.ArtificialIntelligenceInterpreter;
import lbd.FSNER.Filter.Component.Entity;
import lbd.Utils.Utils;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

public class AIIFeature extends FeatureTypes{

	protected ArtificialIntelligenceInterpreter aII;
	
	protected boolean hasFound;
	protected boolean hasAdded;
	
	protected float weight;
	protected int currentState;
	protected int idFeature;
	
	protected String featureName;
	protected String entityValue;
	
	protected DataSequence sequence;
	protected String [] sequenceLowerCase;
	protected String [] termList;
	
	public AIIFeature(FeatureGenImpl fgen, ArtificialIntelligenceInterpreter aII, float weight) {
		super(fgen);
		this.aII = aII;
		this.weight = weight;
		
		featureName = "AII";
	}
	
	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
		
		hasFound = false;
		hasAdded = false;
		
		currentState = -1;
		
		entityValue = (sequence == data)? sequenceLowerCase[pos] : ((String)data.x(pos)).toLowerCase();
		
		Entity entity = aII.getEntity(entityValue);
		
		if(entity != null) {
			
			hasFound = true;
			
			if(sequence != data) {
				sequence = data;
				sequenceLowerCase = Utils.convertSequenceToLowerCase(data, data.length());
				termList = aII.removeStopWordFromSequence(sequenceLowerCase);
			}
			
			currentState = aII.getCandidateLabel(sequenceLowerCase, pos, termList);
			
			//advance();
			
			idFeature = entity.getEntityNumber();
		}
		
		return hasFound;
	}
	
	public void advance() {
		currentState++;
	}

	@Override
	public boolean hasNext() {
		return (hasFound && !hasAdded);
	}

	@Override
	public void next(FeatureImpl f) {
		
		setFeatureIdentifier(idFeature + currentState, currentState, featureName + "(" + entityValue + ")",f);
		
		f.yend = currentState;
		f.ystart = -1;
		f.val = 1 * weight;
		
		hasAdded = true;
		//advance();
	}
}
