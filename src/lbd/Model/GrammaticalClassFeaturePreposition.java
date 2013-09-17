package lbd.Model;

import lbd.CRF.LabelMap;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;

public class GrammaticalClassFeaturePreposition extends GrammaticalClassFeature {

	private static final long serialVersionUID = 1L;

	protected static final String [] PREPOSITION = {"ante", "após", "apos", "até", "ate", 
		"com", "contra", "de", "desde", "dês", "des", "em", "entre", "para",
		"perante", "por", "per", "sem", "sob", "sobre", "trás", "tras",
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
