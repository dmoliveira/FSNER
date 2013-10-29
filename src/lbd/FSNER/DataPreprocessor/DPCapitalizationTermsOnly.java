package lbd.FSNER.DataPreprocessor;

import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;

public class DPCapitalizationTermsOnly extends AbstractDataPreprocessor{

	private static final long serialVersionUID = 1L;

	public DPCapitalizationTermsOnly() {
		super(ClassName.getSingleName(DPCapitalizationTermsOnly.class.getName()), null);
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public String preprocessingToken(String pToken, int pLabel) {
		return (isCapitalized(pToken))? pToken : Symbol.EMPTY;
	}

	public static boolean isCapitalized(String term) {

		boolean isCapitalized = Character.isUpperCase(term.charAt(0)) &&
				Character.isLetter(term.charAt(0));

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
