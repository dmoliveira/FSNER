package lbd.FSNER.Component.Statistic;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterProbability;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.Collections.CollectionsUtils;

public class FilterProbabilityHandler implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Map<String, AbstractFilterProbability> mFilterProbabilityMap;

	//-- For Training
	protected int mTotalAssignedLabelsInTrain;
	protected List<String> mEntityTermsAssignedInTrainList;
	protected List<Integer> mLabelFrequencyInTrainList;

	//-- For Test/Validation/Operation
	protected int mTotalAssignedEntityLabelsInTest;
	protected List<String> mEntityTermsCorrectAssignedInTestList;
	protected List<String> mEntityTermsMissedAssignedInTestList;
	protected List<String> mEntityTermsWrongAssignedInTestList;

	protected Class<AbstractFilterProbability> mFilterProbabilityElementClass;

	@SuppressWarnings("unchecked")
	public FilterProbabilityHandler(AbstractFilterProbability pFPEObject) {

		mFilterProbabilityElementClass = (Class<AbstractFilterProbability>) pFPEObject.getClass();
		mFilterProbabilityMap = new HashMap<String, AbstractFilterProbability>();

		mTotalAssignedLabelsInTrain = 0;
		mEntityTermsAssignedInTrainList = new ArrayList<String>();

		mTotalAssignedEntityLabelsInTest = 0;
		mEntityTermsCorrectAssignedInTestList = new ArrayList<String>();
		mEntityTermsMissedAssignedInTestList = new ArrayList<String>();
		mEntityTermsWrongAssignedInTestList = new ArrayList<String>();

		mLabelFrequencyInTrainList = new ArrayList<Integer>();
		for(int i = 0; i < LabelEncoding.getAlphabetSize(); i++) {
			mLabelFrequencyInTrainList.add(0);
		}
	}

	public void addStatistic(String pId, String pTerm, int pLabel) {

		if(!mFilterProbabilityMap.containsKey(pId)) {
			try {
				mFilterProbabilityMap.put(pId, mFilterProbabilityElementClass.newInstance());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		mFilterProbabilityMap.get(pId).addLabel(pLabel);

		mTotalAssignedLabelsInTrain++;
		mLabelFrequencyInTrainList.set(pLabel, mLabelFrequencyInTrainList.get(pLabel) + 1);

		if(LabelEncoding.isEntity(pLabel)) {
			mEntityTermsAssignedInTrainList.add(pTerm);
		}
	}

	public double getProbability(String pId) {
		return(getProbability(pId, -1));
	}

	public double getProbability(String pId, int pLabel) {

		double vFilterProbability = 0;

		if(mFilterProbabilityMap.containsKey(pId)) {
			vFilterProbability = (pLabel > -1)?
					mFilterProbabilityMap.get(pId).getProbability(pLabel) :
						mFilterProbabilityMap.get(pId).getProbability();
		}

		return(vFilterProbability);
	}

	public int getInstanceFrequency(String pId) {

		int vInstanceFrequency = 0;

		if(mFilterProbabilityMap.containsKey(pId)) {
			vInstanceFrequency = mFilterProbabilityMap.get(pId).getTotalNumberLabel();
		}

		return(vInstanceFrequency);
	}

	public void addToFilterStatisticForAssignedLabels(String pTerm, boolean pIsEntity, boolean pWasFilterAssignedEntityLabel) {
		if(pIsEntity) {
			mTotalAssignedEntityLabelsInTest++;

			if(pWasFilterAssignedEntityLabel) {
				mEntityTermsCorrectAssignedInTestList.add(pTerm);
			} else {
				mEntityTermsMissedAssignedInTestList.add(pTerm);
			}
		} else {
			if(pWasFilterAssignedEntityLabel) {
				mEntityTermsWrongAssignedInTestList.add(pTerm);
			}
		}
	}

	public void printFilterInstanceStatistic(AbstractFilter pFilter) {

		Iterator<Entry<String, AbstractFilterProbability>> vIterator = mFilterProbabilityMap.entrySet().iterator();
		Entry<String, AbstractFilterProbability> vFilterProbability;
		String vMessageFormat = "\t{0} P:{1}% ({2}/{3})";

		while(vIterator.hasNext()) {

			vFilterProbability = vIterator.next();

			System.out.println(MessageFormat.format(vMessageFormat,
					vFilterProbability.getKey(),
					100 * vFilterProbability.getValue().getProbability(),
					vFilterProbability.getValue().getTotalAssignedEntityLabel(),
					vFilterProbability.getValue().getTotalNumberLabel()));
		}

	}

	public String getFilterStatisticForAssignedLabelsInTrain() {

		double vEntityTermPercentage = ((100.0 * mEntityTermsAssignedInTrainList.size()) / mTotalAssignedLabelsInTrain);
		String vEntityTermsAssignedInTrainList = Integer.toString(mEntityTermsAssignedInTrainList.size());
		String vEntityTermsAssignedLabelsInTrain = Integer.toString(mTotalAssignedLabelsInTrain);

		String vStatisticForAssignedLabels = MessageFormat.format("- Ent.Term:{0,number,#.##}% ({1}/{2})",
				vEntityTermPercentage, vEntityTermsAssignedInTrainList, vEntityTermsAssignedLabelsInTrain);

		return vStatisticForAssignedLabels;
	}

	public String getFilterPrecisionStatisticInTest() {

		double vDivisor = mEntityTermsCorrectAssignedInTestList.size() + mEntityTermsWrongAssignedInTestList.size();
		vDivisor = (vDivisor == 0)? 1 : vDivisor;

		double vFilterPrecisionInTest = getFilterPrecisionInTest();
		String vEntityTermsCorrectAssignedInTest = Integer.toString(mEntityTermsCorrectAssignedInTestList.size());
		String vEntityTermsCorectAndWrongAssignedInTest = Integer.toString((int)vDivisor);

		return MessageFormat.format("- P:{0,number,#.##}% ({1}/{2})", vFilterPrecisionInTest,
				vEntityTermsCorrectAssignedInTest, vEntityTermsCorectAndWrongAssignedInTest);
	}

	public String getFilterRecallStatisticInTest() {

		double vFilterRecallInTest = getFilterRecallInTest();
		String vEntityTermsCorrectAssignedInTestList = Integer.toString(mEntityTermsCorrectAssignedInTestList.size());
		String vTotalAssignedEntityLabelsInTest = Integer.toString(mTotalAssignedEntityLabelsInTest);

		return MessageFormat.format("- R:{0,number,#.##}% ({1}/{2})", vFilterRecallInTest,
				vEntityTermsCorrectAssignedInTestList,vTotalAssignedEntityLabelsInTest);
	}

	public String getFilterF1StatisticInTest() {

		double vFilterPrecisionInTest = getFilterPrecisionInTest();
		double vFilterRecallInTest = getFilterRecallInTest();

		if(vFilterPrecisionInTest + vFilterRecallInTest == 0) {
			return "- F1:0.00%";
		}

		return MessageFormat.format("- F1:{0,number,#.##}%",  getFilterF1InTest());
	}

	public int getMostProbablyLabel(String pInstanceId) {

		int vLabel = LabelEncoding.getOutsideLabel();
		int vMaxNumber = -1;

		AbstractFilterProbability vFilterProbability = mFilterProbabilityMap.get(pInstanceId);

		for(int i = 0; i < vFilterProbability.getAlphabetSize(); i++) {
			if(vMaxNumber < vFilterProbability.getTotalAssignedEntityPerLabel(i)) {
				vMaxNumber = vFilterProbability.getTotalAssignedEntityPerLabel(i);
				vLabel = i;
			}
		}

		return(vLabel);
	}

	public void clear() {
		mTotalAssignedEntityLabelsInTest = 0;
	}

	public int getNumberEntityLabel(String pInstanceId) {
		return(mFilterProbabilityMap.get(pInstanceId).getTotalAssignedEntityLabel());
	}

	public int getNumberEntityLabel(String pInstanceId, int pLabel) {
		return(mFilterProbabilityMap.get(pInstanceId).getTotalAssignedEntityPerLabel(pLabel));
	}

	public int getTotalNumberLabel(String pInstanceId) {
		return(mFilterProbabilityMap.get(pInstanceId).getTotalNumberLabel());
	}

	public int getTotalCorrectAssignedEntityLabelsInTrain() {
		return(mEntityTermsAssignedInTrainList.size());
	}

	public int getTotalAssignedLabelsInTrain() {
		return(mTotalAssignedLabelsInTrain);
	}

	public String getEntityTermsCorrectAssignedInTrain() {
		return CollectionsUtils.GetTerms(mEntityTermsAssignedInTrainList);
	}

	public int getTotalCorrectAssignedEntityLabelsInTest() {
		return(mEntityTermsCorrectAssignedInTestList.size());
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

	public List<String> getEntityTermsCorrectAssignedInTestList() {
		return mEntityTermsCorrectAssignedInTestList;
	}

	public List<String> getEntityTermsMissedAssignedInTestList() {
		return mEntityTermsMissedAssignedInTestList;
	}

	public List<String> getEntityTermsWrongAssignedInTestList() {
		return mEntityTermsWrongAssignedInTestList;
	}

	public double getFilterPrecisionInTest() {
		double vDivisor = mEntityTermsCorrectAssignedInTestList.size() + mEntityTermsWrongAssignedInTestList.size();
		vDivisor = (vDivisor == 0)? 1 : vDivisor;

		return (100.0 * mEntityTermsCorrectAssignedInTestList.size()) / vDivisor;
	}

	public double getFilterRecallInTest() {
		return (100.0 * mEntityTermsCorrectAssignedInTestList.size()) / mTotalAssignedEntityLabelsInTest;
	}

	public double getFilterF1InTest() {
		double vFilterPrecisionInTest = getFilterPrecisionInTest();
		double vFilterRecallInTest = getFilterRecallInTest();
		return (100.0 * 2 * vFilterPrecisionInTest * vFilterRecallInTest)/(vFilterPrecisionInTest + vFilterRecallInTest);
	}

	public int size() {
		return(mFilterProbabilityMap.size());
	}
}
