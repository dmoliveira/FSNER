package lbd.FSNER.DataPreprocessor;

import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabelElement;
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
		
		for(Character c : consonantList)
			consonantMap.put(c, null);
	}

	@Override
	public SequenceLabelElement preprocessingTerm(String term, int label) {
		
		String preprocessedTerm = "";
		String termUpperCase = term.toUpperCase();
		
		for(int i = 0; i < term.length(); i++) {
			if(isConsonant(termUpperCase, i)) {
				preprocessedTerm += termUpperCase.charAt(i);
			}
		}
		
		return (new SequenceLabelElement(preprocessedTerm, label));
	}
	
	public boolean isConsonant(String termUpperCase, int index) {			
		
		return(consonantMap.containsKey(termUpperCase.charAt(index)));
	}

}
