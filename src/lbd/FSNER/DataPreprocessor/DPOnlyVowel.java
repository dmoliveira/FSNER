package lbd.FSNER.DataPreprocessor;

import java.util.HashMap;

import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.ClassName;

public class DPOnlyVowel extends AbstractDataPreprocessor{

	private static final long serialVersionUID = 1L;

	protected HashMap<Character, Object> vowelMap;
	protected Character [] vowelList = {'A', 'E', 'I', 'O', 'U'};

	public DPOnlyVowel() {
		super(ClassName.getSingleName(DPOnlyVowel.class.getName()), null);
	}

	@Override
	public void initialize() {

		vowelMap = new HashMap<Character, Object>();

		for(Character c : vowelList) {
			vowelMap.put(c, null);
		}
	}

	@Override
	public String preprocessingToken(String pTerm, int pLabel) {

		String vPreprocessedTerm = "";
		String termUpperCase = pTerm.toUpperCase();

		for(int i = 0; i < pTerm.length(); i++) {
			if(isVowel(termUpperCase, i)) {
				vPreprocessedTerm += termUpperCase.charAt(i);
			}
		}

		return vPreprocessedTerm;
	}

	public boolean isVowel(String termUpperCase, int index) {

		return(vowelMap.containsKey(termUpperCase.charAt(index)));
	}

}
