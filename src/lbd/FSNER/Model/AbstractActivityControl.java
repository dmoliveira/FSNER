package lbd.FSNER.Model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lbd.FSNER.Model.AbstractFilter.FilterStage;
import lbd.data.handler.ISequence;

public abstract class AbstractActivityControl implements Serializable {

	private static final long serialVersionUID = 1L;

	protected List<AbstractDataPreprocessor> mDataPreprocessorList;
	protected List<AbstractFilter> mFilterList;

	protected Map<String, List<AbstractFilter>> mFilterListPerDataPreprocessor;
	protected Map<String, Set<AbstractFilter>> mClassNameSingleFilterMap;

	protected List<ISequence> mSequenceList;
	protected Set<String> mEntitySet;

	protected Map<String, Map<String, Integer>> mPredictSubSequenceLabel;

	protected AbstractCombineFiltersInActiveControl mFilterCombination;

	public AbstractActivityControl(AbstractCombineFiltersInActiveControl pFilterCombination) {

		mDataPreprocessorList = new ArrayList<AbstractDataPreprocessor>();
		mFilterList = new ArrayList<AbstractFilter>();

		mFilterListPerDataPreprocessor = new HashMap<String, List<AbstractFilter>>();
		mClassNameSingleFilterMap = new HashMap<String, Set<AbstractFilter>>();

		mSequenceList = new ArrayList<ISequence>();
		mEntitySet = new HashSet<String>();

		mFilterCombination = pFilterCombination;

		mPredictSubSequenceLabel = new HashMap<String, Map<String, Integer>>();
	}

	public void addActivity(List<AbstractActivity> pActivityList) {
		for(AbstractActivity cActivity : pActivityList) {
			addActivity(cActivity);
		}
	}

	public void addActivity(AbstractActivity pActivity) {
		if(pActivity instanceof AbstractDataPreprocessor) {
			mDataPreprocessorList.add((AbstractDataPreprocessor) pActivity);
		} else if(pActivity instanceof AbstractFilter) {
			addAbstractFilter(pActivity);
		}
	}

	private void addAbstractFilter(AbstractActivity pActivity) {

		AbstractFilter vFilter = (AbstractFilter) pActivity;
		int vDataProcessorIndex = vFilter.getFilterPreprocessingTypeIndex();
		String vDataProcessorName = mDataPreprocessorList.get(vDataProcessorIndex).getActivityName();

		//-- Adding data preprocessor name to filter
		vFilter.setPreprocessingTypeName(vDataProcessorName);

		//-- Initiating filter to map of data preprocessor
		if(!mFilterListPerDataPreprocessor.containsKey(vDataProcessorName)) {
			mFilterListPerDataPreprocessor.put(vDataProcessorName, new ArrayList<AbstractFilter>());
		}

		mFilterList.add(vFilter);
		mFilterListPerDataPreprocessor.get(vDataProcessorName).add(vFilter);
		addAbstractFilterToSingleFilterMap(vFilter, vDataProcessorName);
	}

	private void addAbstractFilterToSingleFilterMap(AbstractFilter vFilter,	String vDataProcessorName) {
		if(!(vFilter instanceof AbstractMetaFilter)) {
			if(!mClassNameSingleFilterMap.containsKey(vFilter.getClass().getName())) {
				mClassNameSingleFilterMap.put(vFilter.getClass().getName(), new HashSet<AbstractFilter>());
			}
			mClassNameSingleFilterMap.get(vFilter.getClass().getName()).add(vFilter);
		}
	}

	public void startActivityControl(String pContextSourceFile) {

		//-- Set FilterMode
		AbstractFilter.setFilterStage(FilterStage.Train);

		//-- Restarts entity list
		mEntitySet.clear();

		//-- Start the activity control.
		startActivityControlSub(pContextSourceFile);
	}

	protected abstract void startActivityControlSub(String pContextSourceFile);

	protected abstract void initialize();

	public abstract void load(String pTrainingFilenameAddress);

	protected abstract void loadSequence(ISequence pSequence);

	protected abstract void loadActionBeforeSequenceSetIteration();

	protected abstract void loadActionBeforeSequenceIteration(Map<String, ISequence> pProcessedSequenceMap);

	protected abstract void loadActionAfterSequenceIteration(Map<String, ISequence> pProcessedSequenceMap);

	protected abstract void loadActionAfterSequenceSetIteration();

	protected abstract void adjust(List<ISequence> pSequenceList);

	public abstract void update(List<ISequence> pSequenceList);

	protected void addFilterStatistic(AbstractFilter pFilter, ISequence pSequence, ISequence pPreprocessedSequence) {

		for(int i = 0; i < pPreprocessedSequence.length(); i++) {

			String vSequenceInstanceId = pFilter.getSequenceInstanceId(pSequence, pPreprocessedSequence, i);

			if(!vSequenceInstanceId.isEmpty()) {
				synchronized (this) {
					pFilter.getFilterProbability().addStatistic(vSequenceInstanceId,
							(pPreprocessedSequence.getToken(i)),
							pPreprocessedSequence.getLabel(i));
				}
			}
		}
	}

	public List<AbstractDataPreprocessor> getDataPreprocessorList() {
		return(mDataPreprocessorList);
	}

	public List<AbstractFilter> getFilterList() {
		return(mFilterList);
	}

	public Set<String> getEntitySet() {
		return(mEntitySet);
	}

	public List<ISequence> getSequenceList() {
		return mSequenceList;
	}

	public Set<AbstractFilter> getFiltersByClassName(String pFilterClassName) {
		return mClassNameSingleFilterMap.get(pFilterClassName);
	}

	public Map<String, Set<AbstractFilter>> getClassNameSingleFilterMap() {
		return mClassNameSingleFilterMap;
	}
}
