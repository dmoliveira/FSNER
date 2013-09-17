package lbd.FSNER.DataPreprocessor;

import lbd.FSNER.Component.SequenceLabelElement;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;

public class DPSemiCapitalizationTermsOnly extends AbstractDataPreprocessor{

	private static final long serialVersionUID = 1L;

	public DPSemiCapitalizationTermsOnly() {
		super(ClassName.getSingleName(DPSemiCapitalizationTermsOnly.class.getName()),
				null);
	}
	
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SequenceLabelElement preprocessingTerm(String term, int label) {
		
		String preprocessedTerm = (isSemiCapitalized(term))? term : Symbol.EMPTY;
		
		return (new SequenceLabelElement(preprocessedTerm, label));
	}

	public static boolean isSemiCapitalized(String term) {
		
		boolean isSemiCapitalized = Character.isUpperCase(term.charAt(0));
		
		if(isSemiCapitalized) {
			for(int i = 1; i < term.length(); i++) {
				if(Character.isUpperCase(term.charAt(i))) {
					isSemiCapitalized = false;
					break;
				}
			}
		}
		
		return(isSemiCapitalized);
	}

}
