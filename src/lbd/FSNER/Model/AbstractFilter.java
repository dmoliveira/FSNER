package lbd.FSNER.Model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.Statistic.FilterProbability;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;

public abstract class AbstractFilter extends AbstractActivity{

	private static final long serialVersionUID = 1L;

	protected transient HashMap<String, ArrayList<String>> mSequenceInstanceIdToFilterTermsMap;

	protected transient int mPreprocessingTypeNameIndex;
	protected String mPreprocessingTypeName;
	protected String mCommonFilterName;

	protected AbstractFilterScoreCalculatorModel mScoreCalculator;

	protected FilterProbability mFilterProbability;
	protected boolean mIsToConsiderFilterProbability;

	public enum FilterState {Active, Auxiliary};
	public enum FilterMode {inLoad, inLabel, inUpdate};

	protected boolean mIsToUseFilterInUnreliableSituation;

	protected FilterState mFilterState;
	protected static FilterMode mFilterMode;

	protected int mInstanceFrequencyThreshould;

	public AbstractFilter(String activityName, int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator) {

		super(activityName + Symbol.PLUS +
				ClassName.getSingleName(scoreCalculator.getClass().getName()) +
				"[id"+ (_globalId+1) +"]");

		this.mSequenceInstanceIdToFilterTermsMap = new HashMap<String, ArrayList<String>>();
		this.mCommonFilterName = this.getClass().getName();
		this.mPreprocessingTypeNameIndex = preprocessingTypeNameIndex;
		this.mScoreCalculator = scoreCalculator;
		this.mFilterState = FilterState.Active;
		this.mIsToConsiderFilterProbability = true;
		this.mIsToUseFilterInUnreliableSituation = true;
		this.mInstanceFrequencyThreshould = 0;
	}

	public abstract void adjust(SequenceLabel sequenceProcessedLabel);

	public void loadTermSequenceRestricted(SequenceLabel sequenceLabelProcessed, int index) {
		if(sequenceLabelProcessed.size() > 1 && index > -1 && !sequenceLabelProcessed.getTerm(index).isEmpty()) {
			loadTermSequence(sequenceLabelProcessed, index);
		}
	}

	public abstract void loadTermSequence(SequenceLabel sequenceLabelProcessed, int index);

	public abstract void loadActionBeforeSequenceSetIteration();

	public abstract void loadActionBeforeSequenceIteration(SequenceLabel sequenceLabelProcessed);

	public abstract void loadActionAfterSequenceIteration(SequenceLabel sequenceLabelProcessed);

	public abstract void loadActionAfterSequenceSetIteration();

	public double calculateScore(SequenceLabel sequenceLabelProcessed, int index) {
		return(mScoreCalculator.calculateScoreInLabel(sequenceLabelProcessed, index));
	}

	public FilterProbability getFilterProbability() {
		return(mFilterProbability);
	}

	public void setProbabilityFilterElement(AbstractFilterProbabilityElement filterProbabilityElement) {
		mFilterProbability = new FilterProbability(filterProbabilityElement);
	}

	public String getSequenceInstanceId(SequenceLabel sequenceLabelProcessed, int index) {

		String id = Symbol.EMPTY;

		if(!sequenceLabelProcessed.getTerm(index).isEmpty()) {
			id = getSequenceInstanceIdSub(sequenceLabelProcessed, index);
		}

		return(id);
	}

	public void printFilterProbabilityInstanceStatistics() {
		String vMessageFormat = "--- {0} --- Instances({1}) {2}";

		if(mFilterProbability.size() > 0) {
			System.out.println(MessageFormat.format(vMessageFormat, mActivityName,
					mFilterProbability.size(),
					mFilterProbability.getFilterStatisticForAssignedLabelsInTrain()));
			if(Debug.ActivityControl.printFilterInstanceStatistics) {
				mFilterProbability.printFilterInstanceStatistic(this);
			}
		}
	}

	public static int countTotalFilterPerClass(ArrayList<AbstractFilter> filterList) {

		int totalFiltersPerClass = 0;
		HashMap<String, Object> filterClassName = new HashMap<String, Object>();

		for(AbstractFilter filter : filterList) {
			if(!filterClassName.containsKey(filter.getCommonFilterName())) {
				filterClassName.put(filter.getCommonFilterName(), null);
				totalFiltersPerClass++;
			}
		}

		return(totalFiltersPerClass);
	}

	protected abstract String getSequenceInstanceIdSub(SequenceLabel sequenceLabelProcessed, int index);

	public void setFilterTerm(String pSequenceInstanceId, String pTerm) {
		if(!mSequenceInstanceIdToFilterTermsMap.containsKey(pSequenceInstanceId)) {
			mSequenceInstanceIdToFilterTermsMap.put(pSequenceInstanceId, new ArrayList<String>());
		}

		if(!mSequenceInstanceIdToFilterTermsMap.get(pSequenceInstanceId).contains(pTerm)) {
			mSequenceInstanceIdToFilterTermsMap.get(pSequenceInstanceId).add(pTerm);
		}
	}

	public String getFilterTerms(String pSequenceInstanceId) {
		String vFilterTerms = Symbol.EMPTY;

		if(mSequenceInstanceIdToFilterTermsMap.containsKey(pSequenceInstanceId))  {
			for(String cTerm : mSequenceInstanceIdToFilterTermsMap.get(pSequenceInstanceId)) {
				vFilterTerms += cTerm + Symbol.COMMA + Symbol.SPACE;
			}

			if(!vFilterTerms.isEmpty()) {
				vFilterTerms = vFilterTerms.substring(0, vFilterTerms.length()
						- (Symbol.COMMA.length() + Symbol.SPACE.length()));
			}
		}

		return vFilterTerms;
	}

	public void setCommonFilterName(String commonFilterName) {
		this.mCommonFilterName = commonFilterName;

	}

	public String getCommonFilterName() {
		return(mCommonFilterName);
	}

	public void setFilterState(FilterState filterState) {
		this.mFilterState = filterState;
	}

	public FilterState getFilterState() {
		return(mFilterState);
	}

	public static void setFilterMode(FilterMode filterMode) {
		AbstractFilter.mFilterMode = filterMode;
	}

	public static FilterMode getFilterMode() {
		return(mFilterMode);
	}

	public int getFilterPreprocessingTypeNameIndex() {
		return(mPreprocessingTypeNameIndex);
	}

	public void setPreprocessingTypeName(String preprocessingTypeName) {
		this.mPreprocessingTypeName = preprocessingTypeName;
	}

	public String getPreprocesingTypeName() {
		return(mPreprocessingTypeName);
	}

	public void setUseFilterInUnrealiableSituation(boolean useFilterInUnreliableSituation) {
		this.mIsToUseFilterInUnreliableSituation = useFilterInUnreliableSituation;
	}

	public boolean isToUseFilterInUnreliableSituation() {
		return(mIsToUseFilterInUnreliableSituation);
	}

	public void setConsiderFilterProbability(boolean considerFilterProbability) {
		this.mIsToConsiderFilterProbability = considerFilterProbability;
	}

	public boolean considerFilterProbability() {
		return(mIsToConsiderFilterProbability);
	}

	public void clearFilterProbability() {
		mFilterProbability.clear();
	}

	public int getInstanceFrequencyThreshould() {
		return(mInstanceFrequencyThreshould);
	}

}
