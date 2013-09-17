package lbd.FSNER.Component.Statistic;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterProbabilityElement;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Collections.CollectionsUtils;

public class FilterProbability implements Serializable {

	private static final long serialVersionUID = 1L;

	protected HashMap<String, AbstractFilterProbabilityElement> mFilterProbabilityMap;

	protected int mTotalAssignedEntityLabelsInTrain;
	protected int mTotalCorrectAssignedEntityLabelsInTrain;
	protected ArrayList<String> mEntityTermsCorrectAssignedInTrainList;
	protected ArrayList<String> mEntityTermsMissedAssignedInTrainList;

	protected int mTotalAssignedEntityLabelsInTest;
	protected int mTotalCorrectAssignedEntityLabelsInTest;
	protected int mTotalWrongAssignedEntityLabelsInTest;
	protected ArrayList<String> mEntityTermsCorrectAssignedInTestList;
	protected ArrayList<String> mEntityTermsMissedAssignedInTestList;
	protected ArrayList<String> mEntityTermsWrongAssignedInTestList;

	@SuppressWarnings("rawtypes")
	protected Class mFilterProbabilityElementClass;

	public FilterProbability(AbstractFilterProbabilityElement fpeObject) {

		mFilterProbabilityElementClass = fpeObject.getClass();
		mFilterProbabilityMap = new HashMap<String, AbstractFilterProbabilityElement>();

		mTotalAssignedEntityLabelsInTrain = 0;
		mTotalCorrectAssignedEntityLabelsInTrain = 0;
		mEntityTermsCorrectAssignedInTrainList = new ArrayList<String>();
		mEntityTermsMissedAssignedInTrainList = new ArrayList<String>();

		mTotalAssignedEntityLabelsInTest = 0;
		mTotalCorrectAssignedEntityLabelsInTest = 0;
		mEntityTermsCorrectAssignedInTestList = new ArrayList<String>();
		mEntityTermsMissedAssignedInTestList = new ArrayList<String>();
		mEntityTermsWrongAssignedInTestList = new ArrayList<String>();
	}

	public void addStatistic(String pId, String pTerm, int pLabel) {

		if(!mFilterProbabilityMap.containsKey(pId)) {
			try {
				mFilterProbabilityMap.put(pId, (AbstractFilterProbabilityElement) mFilterProbabilityElementClass.newInstance());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		mFilterProbabilityMap.get(pId).addLabel(pLabel);

		//-- Average Assigned Label in Train
		mTotalAssignedEntityLabelsInTrain++;

		if(LabelEncoding.isEntity(pLabel)) {
			mTotalCorrectAssignedEntityLabelsInTrain++;
			mEntityTermsCorrectAssignedInTrainList.add(pTerm);
		} else {
			mEntityTermsMissedAssignedInTrainList.add(pTerm);
		}
	}

	public double getProbability(String id) {
		return(getProbability(id, -1));
	}

	public double getProbability(String id, int label) {

		double filterProbability = 0;

		if(mFilterProbabilityMap.containsKey(id)) {
			filterProbability = (label > -1)?
					mFilterProbabilityMap.get(id).getProbability(label) :
						mFilterProbabilityMap.get(id).getProbability();
		}

		return(filterProbability);
	}

	public int getInstanceFrequency(String id) {

		int instanceFrequency = 0;

		if(mFilterProbabilityMap.containsKey(id)) {
			instanceFrequency = mFilterProbabilityMap.get(id).getTotalNumberLabel();
		}

		return(instanceFrequency);

	}

	public void addToFilterStatisticForAssignedLabels(String pTerm, boolean isEntity, boolean wasFilterAssignedEntityLabel) {
		if(isEntity) {
			mTotalAssignedEntityLabelsInTest++;

			if(wasFilterAssignedEntityLabel) {
				mTotalCorrectAssignedEntityLabelsInTest++;
				mEntityTermsCorrectAssignedInTestList.add(pTerm);
			} else {
				mEntityTermsMissedAssignedInTestList.add(pTerm);
			}
		} else {
			if(wasFilterAssignedEntityLabel) {
				mTotalWrongAssignedEntityLabelsInTest++;
				mEntityTermsWrongAssignedInTestList.add(pTerm);
			}
		}
	}

	public void printFilterInstanceStatistic(AbstractFilter pFilter) {

		Iterator<Entry<String, AbstractFilterProbabilityElement>> vIterator = mFilterProbabilityMap.entrySet().iterator();
		Entry<String, AbstractFilterProbabilityElement> vFilterProbability;
		String vMessageFormat = "\t{0} [{1}] P:{2}% ({3}/{4})";

		while(vIterator.hasNext()) {

			vFilterProbability = vIterator.next();

			System.out.println(MessageFormat.format(vMessageFormat,
					vFilterProbability.getKey(), pFilter.getFilterTerms(vFilterProbability.getKey()),
					new DecimalFormat("#.##").format(vFilterProbability.getValue().getProbability()*100),
					vFilterProbability.getValue().getTotalAssignedEntityLabel(),
					vFilterProbability.getValue().getTotalNumberLabel()));
		}

	}

	public String getFilterStatisticForAssignedLabelsInTrain() {
		return("- P: " +
				(new DecimalFormat("#.##").format(
						(100.0*mTotalCorrectAssignedEntityLabelsInTrain)/mTotalAssignedEntityLabelsInTrain)))
						+ "% ("+mTotalCorrectAssignedEntityLabelsInTrain+"/" + mTotalAssignedEntityLabelsInTrain + ")";
	}

	public String getFilterStatisticForCorrectAssignedLabelsInTest() {
		double vDivisor = (mTotalCorrectAssignedEntityLabelsInTest+mTotalWrongAssignedEntityLabelsInTest == 0)?
				1 : mTotalCorrectAssignedEntityLabelsInTest+mTotalWrongAssignedEntityLabelsInTest;

		return("- P: " +
				(new DecimalFormat("#.##").format(
						(100.0*mTotalCorrectAssignedEntityLabelsInTest)/
						(vDivisor)))) + "% ("+mTotalCorrectAssignedEntityLabelsInTest+"/" +
						(mTotalCorrectAssignedEntityLabelsInTest+mTotalWrongAssignedEntityLabelsInTest) + ")";
	}

	public String getFilterStatisticForAssignedLabelsInTest() {
		return("- R: " +
				(new DecimalFormat("#.##").format(
						(100.0*mTotalCorrectAssignedEntityLabelsInTest)/mTotalAssignedEntityLabelsInTest)))
						+ "% ("+mTotalCorrectAssignedEntityLabelsInTest+"/" + mTotalAssignedEntityLabelsInTest + ")";
	}

	public String getFilterF1StatisticInTest() {
		double vPrecision = (mTotalCorrectAssignedEntityLabelsInTest+mTotalWrongAssignedEntityLabelsInTest == 0)? 0 : mTotalCorrectAssignedEntityLabelsInTest/((double)mTotalCorrectAssignedEntityLabelsInTest+mTotalWrongAssignedEntityLabelsInTest);
		double vRecall = (mTotalAssignedEntityLabelsInTest == 0)? 0 : ((double)mTotalCorrectAssignedEntityLabelsInTest)/mTotalAssignedEntityLabelsInTest;

		if(vPrecision + vRecall == 0) {
			return "- F1: 0.0%";
		}

		return("- F1: " +
				(new DecimalFormat("#.##").format(
						(100.0 * 2 * vPrecision * vRecall)/(vPrecision + vRecall)))) + "%";
	}

	public int getMostProbablyLabel(String instanceId) {

		int vLabel = -1;
		int vMaxNumber = -1;

		AbstractFilterProbabilityElement filterProbabilityElement = mFilterProbabilityMap.get(instanceId);

		for(int i = 0; i < filterProbabilityElement.getAlphabetSize(); i++) {
			if(vMaxNumber < filterProbabilityElement.getTotalAssignedEntityPerLabel(i)) {
				vMaxNumber = filterProbabilityElement.getTotalAssignedEntityPerLabel(i);
				vLabel = i;
			}
		}

		return(vLabel);
	}

	public void clear() {
		mTotalCorrectAssignedEntityLabelsInTest = 0;
		mTotalAssignedEntityLabelsInTest = 0;
	}

	public int getNumberEntityLabel(String instanceId) {
		return(mFilterProbabilityMap.get(instanceId).getTotalAssignedEntityLabel());
	}

	public int getNumberEntityLabel(String instanceId, int label) {
		return(mFilterProbabilityMap.get(instanceId).getTotalAssignedEntityPerLabel(label));
	}

	public int getTotalNumberLabel(String instanceId) {
		return(mFilterProbabilityMap.get(instanceId).getTotalNumberLabel());
	}

	public int getTotalCorrectAssignedEntityLabelsInTrain() {
		return(mTotalCorrectAssignedEntityLabelsInTrain);
	}

	public int getTotalAssignedLabelsInTrain() {
		return(mTotalAssignedEntityLabelsInTrain);
	}

	public String getEntityTermsCorrectAssignedInTrain() {
		return CollectionsUtils.GetTerms(mEntityTermsCorrectAssignedInTrainList);
	}

	public String getEntityTermsMissedAssignedInTrain() {
		return CollectionsUtils.GetTerms(mEntityTermsMissedAssignedInTrainList);
	}

	public int getTotalCorrectAssignedEntityLabelsInTest() {
		return(mTotalCorrectAssignedEntityLabelsInTest);
	}

	public int getTotalAssignedLabelsInTest() {
		return(mTotalAssignedEntityLabelsInTest);
	}

	public String getEntityTermsCorrectAssignedInTest() {
		return CollectionsUtils.GetTerms(mEntityTermsCorrectAssignedInTestList);
	}

	public String getEntityTermsMissedAssignedInTest() {
		return CollectionsUtils.GetTerms(mEntityTermsMissedAssignedInTestList);
	}

	public String getEntityTermsWrongAssignedInTest() {
		return CollectionsUtils.GetTerms(mEntityTermsWrongAssignedInTestList);
	}

	public ArrayList<String> getEntityTermsCorrectAssignedInTestList() {
		return mEntityTermsCorrectAssignedInTestList;
	}

	public ArrayList<String> getEntityTermsMissedAssignedInTestList() {
		return mEntityTermsMissedAssignedInTestList;
	}

	public ArrayList<String> getEntityTermsWrongAssignedInTestList() {
		return mEntityTermsWrongAssignedInTestList;
	}

	public int size() {
		return(mFilterProbabilityMap.size());
	}
}
