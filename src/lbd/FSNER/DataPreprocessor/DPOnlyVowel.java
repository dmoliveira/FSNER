package lbd.FSNER.DataPreprocessor;

import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabelElement;
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
		
		for(Character c : vowelList)
			vowelMap.put(c, null);
	}

	@Override
	public SequenceLabelElement preprocessingTerm(String term, int label) {
		
		String preprocessedTerm = "";
		String termUpperCase = term.toUpperCase();
		
		for(int i = 0; i < term.length(); i++) {
			if(isVowel(termUpperCase, i)) {
				preprocessedTerm += termUpperCase.charAt(i);
			}
		}
		
		return (new SequenceLabelElement(preprocessedTerm, label));
	}
	
	public boolean isVowel(String termUpperCase, int index) {			
		
		return(vowelMap.containsKey(termUpperCase.charAt(index)));
	}

}
