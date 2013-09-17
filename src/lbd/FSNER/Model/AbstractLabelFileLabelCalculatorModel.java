package lbd.FSNER.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;


public abstract class AbstractLabelFileLabelCalculatorModel implements Serializable {

	private static final long serialVersionUID = 1L;
	protected boolean isUnrealibleSituation;
	protected HashMap<String, Object> unknownTermMap;
	protected ArrayList<String> unknownTermList;
	protected AbstractTermRestrictionChecker termRestrictionChecker;

	protected double[]  mLabelProbability;
	protected int [] mNormalizationFactor;

	public AbstractLabelFileLabelCalculatorModel(AbstractTermRestrictionChecker termRestrictionChecker) {
		unknownTermMap = new HashMap<String, Object>();
		unknownTermList = new ArrayList<String>();
		this.termRestrictionChecker = termRestrictionChecker;
	}

	public abstract int calculateMostProbablyLabel(int index,
			HashMap<String, SequenceLabel> proccessedSequenceMap,
			ArrayList<AbstractDataPreprocessor> dataProcessorList,
			ArrayList<AbstractFilter> filterList);

	public void setIsUnrealibleSituation(boolean isUnrealibleSituation) {
		this.isUnrealibleSituation = isUnrealibleSituation;
	}

	public boolean isUnrealibleSituation() {
		return(isUnrealibleSituation);
	}

	public void addAsUnknownTerm(String term) {

		term = term.toLowerCase();

		if(!unknownTermMap.containsKey(term)) {
			unknownTermMap.put(term, null);
			unknownTermList.add(term);
		}
	}

	public ArrayList<String> getUnknownTermList() {
		return(unknownTermList);
	}

	public void removeRestrictedTermFromUnknownTermList() {

		ArrayList<String> termListToRemove = new ArrayList<String>();

		for(String term : unknownTermList) {
			if(termRestrictionChecker.isTermRestricted(term)) {
				termListToRemove.add(term);
			}
		}

		for(String term : termListToRemove) {
			unknownTermList.remove(term);
		}
	}

	public void cleanUnknownTermLists() {
		unknownTermMap.clear();
		unknownTermList.clear();
	}

	public void printUnknownTermList() {

		System.out.println("-- Has a total of (" + unknownTermList.size() + ") unknown terms.");

		for(String term : unknownTermList) {
			System.out.println(term);
		}
	}

	public double [] getLabelProbabilities() {
		return(mLabelProbability);
	}

	public int [] getNormalizationFactor() {
		return(mNormalizationFactor);
	}
}
