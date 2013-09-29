package lbd.FSNER.Model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Model.AbstractFilter.FilterMode;
import lbd.data.handler.DataSequence;

public abstract class AbstractActivityControl implements Serializable {

	private static final long serialVersionUID = 1L;

	protected List<AbstractDataPreprocessor> mDataPreprocessorList;
	protected List<AbstractFilter> mFilterList;
	protected Map<String, List<AbstractFilter>> mFilterListPerDataPreprocessor;

	protected List<DataSequence> mSequenceList;
	protected List<String> mEntityList;

	protected AbstractCombineFiltersInActiveControl mFilterCombination;

	protected final double COMMON_TERM_PROBABILTY = 1.1;

	public AbstractActivityControl(AbstractCombineFiltersInActiveControl pFilterCombination) {

		mDataPreprocessorList = new ArrayList<AbstractDataPreprocessor>();
		mFilterList = new ArrayList<AbstractFilter>();
		mSequenceList = new ArrayList<DataSequence>();
		mEntityList = new ArrayList<String>();
		mFilterListPerDataPreprocessor = new HashMap<String, List<AbstractFilter>>();

		mFilterCombination = pFilterCombination;
	}

	public void addActivity(AbstractActivity pActivity) {

		if(pActivity instanceof AbstractDataPreprocessor) {
			mDataPreprocessorList.add((AbstractDataPreprocessor) pActivity);
		} else if(pActivity instanceof AbstractFilter) {

			mFilterList.add((AbstractFilter) pActivity);

			//-- Adding data preprocessor name to filter
			mFilterList.get(mFilterList.size()-1).setPreprocessingTypeName(
					mDataPreprocessorList.get(mFilterList.get(mFilterList.size()-1).
							getFilterPreprocessingTypeNameIndex()).getActivityName());

			//-- Adding filter to data preprocessor list
			int dpIndex = ((AbstractFilter) pActivity).getFilterPreprocessingTypeNameIndex();
			if(!mFilterListPerDataPreprocessor.containsKey(mDataPreprocessorList.get(dpIndex).getActivityName())) {
				mFilterListPerDataPreprocessor.put(mDataPreprocessorList.get(dpIndex).getActivityName(), new ArrayList<AbstractFilter>());
			}
			mFilterListPerDataPreprocessor.get(mDataPreprocessorList.get(dpIndex).getActivityName()).add(((AbstractFilter) pActivity));
		}
	}

	public void startActivityControl(String pContextSourceFile) {

		//-- Set FilterMode
		AbstractFilter.setFilterMode(FilterMode.inLoad);

		//-- Restarts entity list
		mEntityList.clear();

		//-- Start the activity control.
		startActivityControlSub(pContextSourceFile);
	}

	protected abstract void startActivityControlSub(String pContextSourceFile);

	protected abstract void initialize();

	public abstract void load(String pContextSourceFilenameAddress);

	protected abstract void loadSequence(DataSequence pSequence);

	protected abstract void loadActionBeforeSequenceSetIteration();

	protected abstract void loadActionBeforeSequenceIteration(Map<String, SequenceLabel> pProcessedSequenceMap);

	protected abstract void loadActionAfterSequenceIteration(Map<String, SequenceLabel> pProcessedSequenceMap);

	protected abstract void loadActionAfterSequenceSetIteration();

	protected abstract void adjust(List<DataSequence> pSequenceList);

	public abstract void update(List<DataSequence> pSequenceList);

	protected void addFilterStatistic(AbstractFilter pFilter, SequenceLabel pSequenceLabelProcessed) {

		String pSequenceInstanceId;

		for(int i = 0; i < pSequenceLabelProcessed.size(); i++) {
			if(mDataPreprocessorList.get(pFilter.getFilterPreprocessingTypeNameIndex()).
					getCommonTermProbability(pSequenceLabelProcessed.getTerm(i)) < COMMON_TERM_PROBABILTY) {

				pSequenceInstanceId = pFilter.getSequenceInstanceId(pSequenceLabelProcessed, i);

				if(!pSequenceInstanceId.isEmpty()) {
					synchronized (this) {
						pFilter.getFilterProbability().addStatistic(pSequenceInstanceId,
								pSequenceLabelProcessed.getTerm(i),
								pSequenceLabelProcessed.getLabel(i));

						if(Debug.ActivityControl.printFilterInstanceStatistics) {
							pFilter.setFilterTerm(pSequenceInstanceId, pSequenceLabelProcessed.getTerm(i));
						}
					}
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

	public List<String> getEntityList() {
		return(mEntityList);
	}
}
