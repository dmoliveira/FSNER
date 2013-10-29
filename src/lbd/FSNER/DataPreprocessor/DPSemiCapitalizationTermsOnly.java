package lbd.FSNER.DataPreprocessor;

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
	public String preprocessingToken(String pToken, int pLabel) {
		return (isSemiCapitalized(pToken))? pToken : Symbol.EMPTY;
	}

	public static boolean isSemiCapitalized(String pToken) {

		boolean vIsSemiCapitalized = Character.isUpperCase(pToken.charAt(0));

		if(vIsSemiCapitalized) {
			for(int i = 1; i < pToken.length(); i++) {
				if(Character.isUpperCase(pToken.charAt(i))) {
					vIsSemiCapitalized = false;
					break;
				}
			}
		}

		return(vIsSemiCapitalized);
	}

}
