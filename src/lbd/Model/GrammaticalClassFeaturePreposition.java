package lbd.Model;

import lbd.CRF.LabelMap;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;

public class GrammaticalClassFeaturePreposition extends GrammaticalClassFeature {

	private static final long serialVersionUID = 1L;

	protected static final String [] PREPOSITION = {"ante", "ap�s", "apos", "at�", "ate", 
		"com", "contra", "de", "desde", "d�s", "des", "em", "entre", "para",
		"perante", "por", "per", "sem", "sob", "sobre", "tr�s", "tras",
		"conforme", "consoante", "segundo", "durante", "mediante", "visto", "como"};
	
	public GrammaticalClassFeaturePreposition(FeatureGenImpl fgen, float weight) {
		super(fgen, weight, PREPOSITION);
		
		featureName = "Preposition";
		grammaticalClassState = LabelMap.getLabelIndexPOSTagPTBR("Preposition");
	}
	
	public GrammaticalClassFeaturePreposition(FeatureGenImpl fgen) {
		super(fgen, PREPOSITION);
		
		featureName = "Preposition";
		grammaticalClassState = LabelMap.getLabelIndexPOSTagPTBR("Preposition");
	}

	@Override
	protected void additionalStartScanFeaturesAt(DataSequence data,
			int prevPos, int pos, boolean isBelongsToThisGrammaticalClass) {}
}
