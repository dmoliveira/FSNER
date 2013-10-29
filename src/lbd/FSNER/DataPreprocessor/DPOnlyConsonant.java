package lbd.FSNER.DataPreprocessor;

import java.util.HashMap;

import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.ClassName;

public class DPOnlyConsonant extends AbstractDataPreprocessor{

	private static final long serialVersionUID = 1L;

	protected HashMap<Character, Object> consonantMap;
	protected Character [] consonantList = {'B', 'C', 'D', 'F', 'G', 'H', 'J',
			'K', 'L', 'M', 'N', 'P', 'Q', 'R',
			'S', 'T', 'V', 'W', 'Y', 'X', 'Z'};

	public DPOnlyConsonant() {
		super(ClassName.getSingleName(DPOnlyConsonant.class.getName()), null);
	}

	@Override
	public void initialize() {

		consonantMap = new HashMap<Character, Object>();

		for(Character c : consonantList) {
			consonantMap.put(c, null);
		}
	}

	@Override
	public String preprocessingToken(String pToken, int pLabel) {

		String vPreprocessedTerm = "";
		String vTermUpperCase = pToken.toUpperCase();

		for(int i = 0; i < pToken.length(); i++) {
			if(isConsonant(vTermUpperCase, i)) {
				vPreprocessedTerm += vTermUpperCase.charAt(i);
			}
		}

		return vPreprocessedTerm;
	}

	public boolean isConsonant(String termUpperCase, int index) {

		return(consonantMap.containsKey(termUpperCase.charAt(index)));
	}

}
