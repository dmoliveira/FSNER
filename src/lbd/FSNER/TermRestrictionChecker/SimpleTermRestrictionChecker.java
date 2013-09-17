package lbd.FSNER.TermRestrictionChecker;

import lbd.FSNER.Model.AbstractTermRestrictionChecker;
import lbd.FSNER.Utils.Symbol;
import lbd.Utils.RemoveStopWordsTool;

public class SimpleTermRestrictionChecker extends AbstractTermRestrictionChecker{
	
	protected RemoveStopWordsTool stopWordTool;
	
	public SimpleTermRestrictionChecker(String stopWordFilenameAddress) {
		stopWordTool = new RemoveStopWordsTool(stopWordFilenameAddress);
	}

	@Override
	public boolean isTermRestricted(String term) {
		
		boolean isTermRestricted = false;
		term = term.toLowerCase();
		
		if(!isTermRestricted && (term.startsWith("#") || term.startsWith("@") 
				|| term.startsWith("http") || term.startsWith("www"))) {
			isTermRestricted = true;
		}
		
		//-- If the first character is number
		if(!isTermRestricted && !term.isEmpty()) {
			try {
				Double.parseDouble("" + term.charAt(0));
				isTermRestricted = true;
			} catch(NumberFormatException e) {
			}
		}
		
		//-- If the term is number
		if(!isTermRestricted) {
			try {
				Double.parseDouble(term);
				isTermRestricted = true;
			} catch(NumberFormatException e) {
			}
		}
		
		//-- if term is less than 2 or above 10 length
		if(!isTermRestricted && (term.length() < 4 || term.length() > 10))
			isTermRestricted = true;
		
		if(!isTermRestricted && stopWordTool.isStopWord(term))
			isTermRestricted = true;
		
		return (isTermRestricted);
	}

}
