package lbd.FSNER.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lbd.FSNER.Configuration.Parameters;
import lbd.data.handler.ISequence;


public abstract class AbstractLabelFileLabelCalculatorModel implements Serializable {

	private static final long serialVersionUID = 1L;

	protected boolean mIsUnrealibleSituation;
	protected Map<String, Object> mUnknownTermMap;
	protected List<String> mUnknownTermList;
	protected AbstractTermRestrictionChecker mTermRestrictionChecker;

	protected double[]  mLabelProbability;
	protected int [] mNormalizationFactor;

	public AbstractLabelFileLabelCalculatorModel(AbstractTermRestrictionChecker pTermRestrictionChecker) {
		mUnknownTermMap = new HashMap<String, Object>();
		mUnknownTermList = new ArrayList<String>();
		mTermRestrictionChecker = pTermRestrictionChecker;
	}

	public int calculateMostProbablyLabel(int pIndex, ISequence pSequence,
			Map<String, ISequence> pPreProccessedSequenceMap, List<AbstractDataPreprocessor> pDataProcessorList,
			List<AbstractFilter> pFilterList) {

		mLabelProbability = new double [Parameters.DataHandler.mLabelEncoding.getAlphabetSize()];
		mNormalizationFactor  = new int[Parameters.DataHandler.mLabelEncoding.getAlphabetSize()];

		int vAssignedLabel = calculateMostProbablyLabelSub(pIndex, pSequence, pPreProccessedSequenceMap,
				pDataProcessorList, pFilterList);

		return vAssignedLabel;
	}

	public abstract int calculateMostProbablyLabelSub(int pIndex,
			ISequence pSequence,
			Map<String, ISequence> pPreProccessedSequenceMap,
			List<AbstractDataPreprocessor> pDataProcessorList,
			List<AbstractFilter> pFilterList);

	public void setIsUnrealibleSituation(boolean pIsUnrealibleSituation) {
		this.mIsUnrealibleSituation = pIsUnrealibleSituation;
	}

	public boolean isUnrealibleSituation() {
		return(mIsUnrealibleSituation);
	}

	public void addAsUnknownTerm(String pTerm) {

		pTerm = pTerm.toLowerCase();

		if(!mUnknownTermMap.containsKey(pTerm)) {
			mUnknownTermMap.put(pTerm, null);
			mUnknownTermList.add(pTerm);
		}
	}

	public List<String> getUnknownTermList() {
		return(mUnknownTermList);
	}

	public void removeRestrictedTermFromUnknownTermList() {

		List<String> termListToRemove = new ArrayList<String>();

		for(String cTerm : mUnknownTermList) {
			if(mTermRestrictionChecker.isTermRestricted(cTerm)) {
				termListToRemove.add(cTerm);
			}
		}

		for(String cTerm : termListToRemove) {
			mUnknownTermList.remove(cTerm);
		}
	}

	public void cleanUnknownTermLists() {
		mUnknownTermMap.clear();
		mUnknownTermList.clear();
	}

	public void printUnknownTermList() {

		System.out.println("-- Has a total of (" + mUnknownTermList.size() + ") unknown terms.");

		for(String term : mUnknownTermList) {
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
