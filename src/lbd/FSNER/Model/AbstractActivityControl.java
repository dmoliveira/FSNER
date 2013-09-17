package lbd.FSNER.Model;

import iitb.CRF.DataSequence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Model.AbstractFilter.FilterMode;

public abstract class AbstractActivityControl implements Serializable {

	private static final long serialVersionUID = 1L;
	protected ArrayList<AbstractDataPreprocessor> mDataPreprocessorList;
	protected ArrayList<AbstractFilter> mFilterList;
	protected HashMap<String, ArrayList<AbstractFilter>> mFilterListPerDataPreprocessor;

	protected ArrayList<DataSequence> mSequenceList;
	protected ArrayList<String> mEntityList;

	protected AbstractCombineFiltersInActiveControl mFilterCombination;

	protected final double COMMON_TERM_PROBABILTY = 1.1;

	public AbstractActivityControl(AbstractCombineFiltersInActiveControl pFilterCombination) {

		mDataPreprocessorList = new ArrayList<AbstractDataPreprocessor>();
		mFilterList = new ArrayList<AbstractFilter>();
		mSequenceList = new ArrayList<DataSequence>();
		mEntityList = new ArrayList<String>();
		mFilterListPerDataPreprocessor = new HashMap<String, ArrayList<AbstractFilter>>();

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

	protected abstract void loadActionBeforeSequenceIteration(HashMap<String, SequenceLabel> pProcessedSequenceMap);

	protected abstract void loadActionAfterSequenceIteration(HashMap<String, SequenceLabel> pProcessedSequenceMap);

	protected abstract void loadActionAfterSequenceSetIteration();

	protected abstract void adjust(ArrayList<DataSequence> pSequenceList);

	public abstract void update(ArrayList<DataSequence> pSequenceList);

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

	public ArrayList<AbstractDataPreprocessor> getDataPreprocessorList() {
		return(mDataPreprocessorList);
	}

	public ArrayList<AbstractFilter> getFilterList() {
		return(mFilterList);
	}

	public ArrayList<String> getEntityList() {
		return(mEntityList);
	}
}
