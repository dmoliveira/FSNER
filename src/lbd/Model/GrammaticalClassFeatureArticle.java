package lbd.Model;

import lbd.CRF.LabelMap;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;

public class GrammaticalClassFeatureArticle extends GrammaticalClassFeature {
	
	private static final long serialVersionUID = 1L;

	protected static final String [] ARTICLE = {"o", "a", "os", "as", 
		"um", "uma", "uns", "umas",
		"ao", "aos", "do", "dos", "no", "nos",
		"pelo", "pelos", "num", "nuns",
		"à", "às", "da", "das", "na", "nas", 
		"pela", "numa", "numas"};

	public GrammaticalClassFeatureArticle(FeatureGenImpl fgen, float weight) {
		super(fgen, weight, ARTICLE);
		
		featureName = "Article";
		grammaticalClassState = LabelMap.getLabelIndexPOSTagPTBR("Article");
	}
	
	public GrammaticalClassFeatureArticle(FeatureGenImpl fgen) {
		super(fgen, ARTICLE);
		
		featureName = "Article";
		grammaticalClassState = LabelMap.getLabelIndexPOSTagPTBR("Article");
	}

	@Override
	protected void additionalStartScanFeaturesAt(DataSequence data,
			int prevPos, int pos, boolean isBelongsToThisGrammaticalClass) {}
}
