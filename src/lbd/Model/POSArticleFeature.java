package lbd.Model;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

public class POSArticleFeature extends FeatureTypes{

	private static final long serialVersionUID = 1L;
	protected float weight;
	
	protected final String [] ARTICLE = {"o", "a", "os", "as", "um", "uma", "uns", "umas",
			"ao", "aos", "do", "dos", "no", "nos", "pelo"
			, "pelos", "num", "nuns", "à", "às", "da", "das",
			"na", "nas", "pela", "pelas", "numa", "numas"};
	
	protected boolean isArticle;
	protected int featureId;
	protected int currentState;
	protected int currentPreviousState;
	protected int currentId;
	
	public POSArticleFeature(FeatureGenImpl fgen, float weight) {
		super(fgen);
		this.weight = weight;
	}
	
	public POSArticleFeature(FeatureGenImpl fgen) {
		super(fgen);
	}
	
	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
		
		isArticle = false;
		featureId = -1;
		currentState = -1;
		currentPreviousState = -1;
		currentId = -1;
		
		for(int i = 0; i < ARTICLE.length; i++) {
			
			if(((String)data.x(pos)).toLowerCase().equals(ARTICLE[i])) {
				isArticle = true;
				featureId = i;
				break;
			}
		}
		
		return isArticle;
	}

	@Override
	public boolean hasNext() {
		
		currentId++;
		
		if(currentId % (model.numStartStates() + 1) == 0) {
			currentState++;
			currentPreviousState = -1;
		} else
			currentPreviousState++;
		
		return (isArticle && currentId < model.numStates() * model.numStates() + model.numStates());
	}

	@Override
	public void next(FeatureImpl f) {
		
		setFeatureIdentifier(featureId * model.numStates() * model.numStates() + currentId, currentState,
				"POSArticle_" + "(" + ARTICLE[featureId] + ") " + currentPreviousState + " -> " + currentState,f);
		
		System.out.println("POSArticle_" + "(" + ARTICLE[featureId] + ") currentId " + currentId  + " " + currentPreviousState + " -> " + currentState);
		
		f.yend = currentState;
		f.ystart = currentPreviousState;
		f.val = 1 * weight;
	}
	
}
