package lbd.Model;

import lbd.NewModels.BrownHierarquicalCluster.BrownCluster;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;
import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;

public class CapitalizationFeature extends NewFeatureTypes{

	private static final long serialVersionUID = -6027572659290546557L;
	
	public CapitalizationFeature(FeatureGenImpl fgen) {
		super(fgen);
		
		featureName = "CapsFeat";
		featureType = FeatureType.Capitalization;
		
		proccessSequenceType = proccessSequenceType.Plain;
	}
	
	public static boolean isCapitalized(String term) {
		
		boolean isCaptalized = !term.isEmpty() && Character.isUpperCase(
				term.charAt(0)) && Character.isLetter(term.charAt(0));
		
		if(isCaptalized) {
			for(int i = 1; i < term.length(); i++) {
				if(Character.isUpperCase(term.charAt(i)) || !Character.isLetter(term.charAt(i))) {
					isCaptalized = false;
					break;
				}
			}
		}
		
		return(isCaptalized);
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {
		
		String term = proccessedSequence[pos];
						
		return ((isCapitalized(term))? 1 : 0);
	}
}
