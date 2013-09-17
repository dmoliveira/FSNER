package lbd.NewModels.FeatureFilters;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;

public class CapitalizedTerms extends NewFeatureTypes{

	private static final long serialVersionUID = 1L;

	public CapitalizedTerms(FeatureGenImpl fgen) {
		super(fgen);
	}

	@Override
	protected int startFeature(DataSequence data, int pos) {
		return ((isCapitalized((String)data.x(pos)))? 1 : 0);
	}
	
	public static boolean isCapitalized(String term) {
		
		boolean isCapitalized = Character.isUpperCase(term.charAt(0)) &&
		!Character.isLetter(term.charAt(0));
		
		if(isCapitalized) {
			for(int i = 1; i < term.length(); i++) {
				if(!Character.isLetter(term.charAt(i)) || 
						Character.isUpperCase(term.charAt(i))) {
					isCapitalized = false;
					break;
				}
			}
		}
		
		return(isCapitalized);
	}

}
