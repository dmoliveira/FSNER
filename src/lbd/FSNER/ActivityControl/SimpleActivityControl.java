package lbd.FSNER.ActivityControl;


import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lbd.FSNER.Configuration.Constants;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.DataProcessor.Component.PreprocessData;
import lbd.FSNER.Model.AbstractActivityControl;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilter.FilterState;
import lbd.FSNER.Utils.EntityUtils;
import lbd.FSNER.Utils.SimpleStopWatch;
import lbd.data.handler.ISequence;
import lbd.data.handler.SequenceSet;
import lbd.fsner.label.encoding.Label;

public class SimpleActivityControl extends AbstractActivityControl {
	private static final long serialVersionUID = 1L;

	public SimpleActivityControl() {
		super(new SimpleFilterCombination());
		// super(new TermFocusFilterCombination());
		// super(new CapitalizationFocusFilterCombination());
		//super(new TermComplementaryFocusFilterCombination()); //USE State Filter - Caution [!]
		//super(new ContextFocusFilterCombination());
		//super(new CustomFilterCombination());
	}

	@Override
	protected void startActivityControlSub(String pTrainingFilenameAddress) {

		SimpleStopWatch vStopWatch = new SimpleStopWatch();

		// -- Initialize All Activities (DataPreprocessor & Filter)
		vStopWatch.start();
		initialize();
		if (Debug.ActivityControl.showElapsedTime) {
			vStopWatch.show("Initialization Time:");
		}

		// -- Load (Only for Filters)
		vStopWatch.start();
		load(pTrainingFilenameAddress);
		if (Debug.ActivityControl.showElapsedTime) {
			vStopWatch.show("Loading Time:");
		}

		// -- Generate Combination of MultiFilters from 2 to 5 filters together
		vStopWatch.start();
		if (Parameters.SimpleActivityControl.isToCombineFilters) {
			mFilterCombination.combineAllFilters(mFilterList,
					mFilterListPerDataPreprocessor);
		}

		if (Debug.ActivityControl.showElapsedTime) {
			vStopWatch.show("Combination Filters Time:");
		}

		if (Debug.ActivityControl.showGeneratedFiltersNumber) {
			System.out.println("Filters # Generated: " + mFilterList.size());
		}

		// -- Adjustment
		vStopWatch.start();
		adjust(mSequenceList);
		if (Debug.ActivityControl.showElapsedTime) {
			vStopWatch.show("Adjusting Time:");
		}
	}

	@Override
	protected void initialize() {

		for (AbstractDataPreprocessor cActivity : mDataPreprocessorList) {
			cActivity.initialize();
		}

		for (AbstractFilter cActivity : mFilterList) {
			cActivity.initialize();
		}
	}

	@Override
	public void load(String pTrainingFilenameAddress) {

		SequenceSet vInputSequenceSet = Parameters.DataHandler.mSequenceSetHandler
				.getSequenceSetFromFile(pTrainingFilenameAddress, Constants.FileType.TRAIN, false);

		// -- Load Action before SequenceSet Iteration
		loadActionBeforeSequenceSetIteration();

		while (vInputSequenceSet.hasNext()) {

			ISequence vSequence = vInputSequenceSet.next();

			//TODO: Create a verification process to check if similar sequences was added
			mSequenceList.add(vSequence);

			loadSequence(vSequence);
		}

		// -- Load Action after SequenceSet Iteration
		loadActionAfterSequenceSetIteration();
	}

	@Override
	protected void loadSequence(ISequence pSequence) {

		if(pSequence == null || pSequence.length() == 0) {
			return;
		}

		Map<String, ISequence> vProcessedSequenceMap = PreprocessData.preprocessSequence(pSequence, mDataPreprocessorList);
		Set<ISequence> vSequenceLabelPreprocessedSet = new HashSet<ISequence>();

		int vEntityIndex;

		// -- Load Action Before Sequence
		loadActionBeforeSequenceIteration(vProcessedSequenceMap);

		for (int i = 0; i < pSequence.length(); i++) {

			vEntityIndex = EntityUtils.getEntityIndex(pSequence, i);

			if(vEntityIndex == -1) {
				return;
			}

			synchronized (this) {
				if (Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(pSequence.getLabel(i)))) {
					mEntitySet.add(pSequence.getToken(i));
				}
			}

			for (AbstractFilter cFilter : mFilterList) {

				ISequence vSequenceLabel = vProcessedSequenceMap.get(cFilter.getPreprocesingTypeName());

				int vDataProcessorIndex = cFilter.getFilterPreprocessingTypeIndex();

				if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(vSequenceLabel.getLabel(vEntityIndex)))) {
					cFilter.loadTermSequence(vSequenceLabel, vEntityIndex);
				}

				// -- Calculate term commonness
				calculateTermCommonness(vSequenceLabelPreprocessedSet, vSequenceLabel, vDataProcessorIndex);
			}

			// -- shift index to optimize search for entities
			i = vEntityIndex;
		}

		// -- Load Action After Sequence
		loadActionAfterSequenceIteration(vProcessedSequenceMap);
	}

	private void calculateTermCommonness(Set<ISequence> vSequenceLabelPreprocessedSet,
			ISequence vSequenceLabel, int vDataProcessorIndex) {

		if (!vSequenceLabelPreprocessedSet.contains(vSequenceLabel)) {

			vSequenceLabelPreprocessedSet.add(vSequenceLabel);
			AbstractDataPreprocessor vDataPreprocessor = mDataPreprocessorList.get(vDataProcessorIndex);

			synchronized (this) {
				vDataPreprocessor.computeCommonTermsInSequence(vSequenceLabel);
			}
		}
	}

	@Override
	protected void loadActionBeforeSequenceSetIteration() {
		for (AbstractFilter activity : mFilterList) {
			activity.loadActionBeforeSequenceSetIteration();
		}
	}

	@Override
	protected void loadActionBeforeSequenceIteration(Map<String, ISequence> processedSequenceMap) {
		for (AbstractFilter cFilter : mFilterList) {
			cFilter.loadActionBeforeSequenceIteration(processedSequenceMap
					.get(cFilter.getPreprocesingTypeName()));
		}
	}

	@Override
	protected void loadActionAfterSequenceIteration(
			Map<String, ISequence> processedSequenceMap) {
		for (AbstractFilter activity : mFilterList) {
			activity.loadActionAfterSequenceIteration(processedSequenceMap
					.get(activity.getPreprocesingTypeName()));
		}
	}

	@Override
	protected void loadActionAfterSequenceSetIteration() {
		for (AbstractFilter activity : mFilterList) {
			activity.loadActionAfterSequenceSetIteration();
		}
	}

	//TODO: Improve this posprocessement. Its consume a lot of time when are a large number of filters.
	@Override
	protected void adjust(List<ISequence> pSequenceList) {

		final int FIRST_FILTER_INDEX = 0;

		for(List<AbstractFilter> cFilterList : mFilterListPerDataPreprocessor.values()) {

			int vDataProcessIndex = cFilterList.get(FIRST_FILTER_INDEX).getFilterPreprocessingTypeIndex();

			for (ISequence cSequence : pSequenceList) {
				addSequenceToFilters(cFilterList, vDataProcessIndex, cSequence);
			}
		}

		/** Print Filter Statatistics **/
		printFilterStatistics();
	}

	private void addSequenceToFilters(List<AbstractFilter> pFilterList, int pDataProcessIndex, ISequence pSequence) {

		ISequence vSequenceLabelPreprocessed = mDataPreprocessorList.get(pDataProcessIndex).preprocessingSequence(pSequence);

		for (AbstractFilter cFilter : pFilterList) {

			cFilter.adjust(vSequenceLabelPreprocessed);

			// -- Add General Statistics
			addFilterStatistic(cFilter, pSequence, vSequenceLabelPreprocessed);
		}
	}

	protected void printFilterStatistics() {
		if (!Parameters.SimpleActivityControl.mIsItUpdate
				&& Debug.ActivityControl.printFilterStatistics) {
			System.out.println("\n- Filters TRAINING statistics");
			for (AbstractFilter filter : mFilterList) {
				if (filter.getFilterState() != FilterState.Auxiliary) {
					filter.printFilterProbabilityInstanceStatistics();
				}
			}
		}

		if(Debug.ActivityControl.printFilterStatistics || Debug.ActivityControl.printFilterInstanceStatistics) {
			System.out.print("\n--- Total Filters Number: " + mFilterList.size());
			System.out.println();
		}
	}

	@Override
	public void update(List<ISequence> updateSequenceList) {
		for (ISequence sequence : updateSequenceList) {

			// -- Create a verification process to check if similar sequences
			// was added
			mSequenceList.add(sequence);
			loadSequence(sequence);
		}

		adjust(updateSequenceList);
	}
}
