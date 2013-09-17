package lbd.Model;

import lbd.CRF.LabelMap;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;

public class GrammaticalClassFeatureConjunction extends GrammaticalClassFeature {

	private static final long serialVersionUID = 1L;

	protected static final String [] CONJUNCTION = {"e", "nem", "mas", "tamb�m", "tambem", 
		"ainda", "sen�o", "senao", "como", "por�m", "porem", "todavia", "contudo",
		"entretanto", "antes", "ou", "ora", "j�", "ja", "quer", "logo", "portanto",
		"pois", "que", "porque", "porquanto", "feito", "embora", "conquanto", "se",
		"caso", "contanto", "conforme", "segundo", "consoante", "quando", "enquanto",
		"mal", "se"};
	
	public GrammaticalClassFeatureConjunction(FeatureGenImpl fgen, float weight) {
		super(fgen, weight, CONJUNCTION);
		
		featureName = "Conjunction";
		grammaticalClassState = LabelMap.getLabelIndexPOSTagPTBR("Conjunction");
	}
	
	public GrammaticalClassFeatureConjunction(FeatureGenImpl fgen) {
		super(fgen, CONJUNCTION);
		
		featureName = "Conjunction";
		grammaticalClassState = LabelMap.getLabelIndexPOSTagPTBR("Conjunction");
	}

	@Override
	protected void additionalStartScanFeaturesAt(DataSequence data,
			int prevPos, int pos, boolean isBelongsToThisGrammaticalClass) {}
}
