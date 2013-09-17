package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

import java.util.HashMap;

public abstract class GrammaticalClassFeature extends FeatureTypes {

	private static final long serialVersionUID = 1L;

	protected HashMap<String, Integer> termMap;
	protected String [] termArray;
	
	protected String featureName;
	
	protected int previousState;
	protected int currentState;
	protected int grammaticalClassState;
	protected int featureId;
	protected float weight;
	protected int shiftPos;
	
	protected int termIndex;
	protected boolean isBelongsToThisGrammaticalClass;
	protected boolean hasAddedFeature;
	
	public GrammaticalClassFeature(FeatureGenImpl fgen, float weight, String[] termArray, int shiftPos) {
		super(fgen);
		
		this.weight = weight;
		this.termArray = termArray;
		this.shiftPos = shiftPos;

		loadMap();
	}
	
	public GrammaticalClassFeature(FeatureGenImpl fgen, float weight, String[] termArray) {
		super(fgen);
		
		this.weight = weight;
		this.termArray = termArray;

		loadMap();
	}
	
	public GrammaticalClassFeature(FeatureGenImpl fgen, String [] termArray) {
		super(fgen);
		
		weight = 1f;
		this.termArray = termArray;
		
		loadMap();
	}
	
	protected void loadMap() {
		if(termMap == null) {
			
			termMap = new HashMap<String, Integer>();
			
			for(int i = 0; i < termArray.length; i++)
				termMap.put(termArray[i], i);
		}
	}
	
	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
		
		featureId = -1;
		currentState = -1;
		previousState = -1;
		isBelongsToThisGrammaticalClass = false;
		hasAddedFeature = false;
		
		/*String token = ((String)data.x(pos)).toLowerCase();
		
		if(termMap.containsKey(token)) {
			
			isBelongsToThisGrammaticalClass = true;
			termIndex = termMap.get(token);
			
			advanceFeature();
		}
		
		//-- For the subclasses 
		additionalStartScanFeaturesAt(data, prevPos, pos, isBelongsToThisGrammaticalClass);
		
		*/
		
		//if((pos + shiftPos > 0 && pos + shiftPos < data.length()) || shiftPos == 0) {
			
			//-- Previous State
			//String token = ((String)data.x(pos-1)).toLowerCase();
			
			//-- Current State
			String token = ((String)data.x(pos)).toLowerCase();
			
			if(termMap.containsKey(token)) {
				
				isBelongsToThisGrammaticalClass = true;
				termIndex = termMap.get(token);
				
				//advanceFeature();
			//}
		
			//-- For the subclasses 
			additionalStartScanFeaturesAt(data, prevPos, pos, isBelongsToThisGrammaticalClass);
			
		}
		
		return isBelongsToThisGrammaticalClass;
	}
	
	protected abstract void additionalStartScanFeaturesAt(DataSequence data, int prevPos,
			int pos, boolean isBelongsToThisGrammaticalClass);
	
	private void advanceFeature() {
		
		//-- All States and Previous States
		featureId++;
		if(featureId % (model.numStates()+1) == 0) {
			currentState++;
			previousState = -1;
		} else
			previousState++;
		
		//-- All States
		//currentState++;
	}
	
	@Override
	public boolean hasNext() {
		
		//-- All States and Previous States
		/*return (isBelongsToThisGrammaticalClass && 
				featureId < model.numStates() * model.numStates() + model.numStates());*/
		
		//-- All States
		//return (isBelongsToThisGrammaticalClass && currentState < model.numStates());
		
		//-- All Previous States
		//return (isBelongsToThisGrammaticalClass && previousState < model.numStates());
		
		//-- Only the termIndex
		return (isBelongsToThisGrammaticalClass && !hasAddedFeature);
	}
	
	@Override
	public void next(FeatureImpl f) {
		
		//-- All States and Previous States
		/*setFeatureIdentifier(termIndex * (model.numStates() * model.numStates() + model.numStates())  + featureId,
				currentState, featureName + "(" + previousState + "->" + currentState + ")", f);*/
		
		//--All States
		/*setFeatureIdentifier(termIndex * model.numStates() + currentState,
				currentState, featureName + "(" + previousState + "->" + currentState + ")", f);*/
		
		//-- Only Term Index
		setFeatureIdentifier(termIndex,
				grammaticalClassState, featureName + "(" + previousState + "->" + currentState + ")", f);
		
		//-- All Previous State
		/*setFeatureIdentifier(termIndex * (model.numStates()+1) + grammaticalClassState + featureId,
				grammaticalClassState, featureName + "(" + previousState + "->" + currentState + ")", f);*/
		
		//System.out.println(featureName + " id " + featureId + " " + previousState + "->" + currentState);
		
		f.yend = grammaticalClassState;
		f.ystart = previousState;
		//f.ystart = grammaticalClassState;
		f.val = 1 * weight;
		
		//advanceFeature();
		
		//-- Only term index
		hasAddedFeature = true;
	}
}
