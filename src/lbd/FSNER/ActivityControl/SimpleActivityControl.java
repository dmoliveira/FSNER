package lbd.FSNER.ActivityControl;

import iitb.CRF.DataSequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;
import lbd.CRF.SequenceSet;
import lbd.FSNER.ActivityControl.FilterCombination.TermComplementaryFocusFilterCombination;
import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.DataProcessor.Component.PreprocessData;
import lbd.FSNER.Model.AbstractActivityControl;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilter.FilterState;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.SimpleStopWatch;
import lbd.FSNER.Utils.SupportEntity;

public class SimpleActivityControl extends AbstractActivityControl {
	private static final long serialVersionUID = 1L;

	public SimpleActivityControl() {
		//super(new SimpleFilterCombination());
		// super(new TermFocusFilterCombination());
		// super(new CapitalizationFocusFilterCombination());
		super(new TermComplementaryFocusFilterCombination());
		//super(new ContextFocusFilterCombination());
	}

	@Override
	protected void startActivityControlSub(String contextSourceFile) {

		SimpleStopWatch stopWatch = new SimpleStopWatch();

		// -- Initialize All Activities (DataPreprocessor & Filter)
		stopWatch.start();
		initialize();
		if (Debug.ActivityControl.showElapsedTime) {
			stopWatch.show("Initialize Time:");
		}

		// -- Load (Only for Filters)
		stopWatch.start();
		load(contextSourceFile);
		if (Debug.ActivityControl.showElapsedTime) {
			stopWatch.show("Load Time:");
		}

		// -- Generate Combination of MultiFilters from 2 to 5 filters together
		stopWatch.start();
		if (Parameters.SimpleActivityControl.isToCombineFilters) {
			mFilterCombination.combineAllFilters(mFilterList,
					mFilterListPerDataPreprocessor);
		}

		if (Debug.ActivityControl.showElapsedTime) {
			stopWatch.show("Combine Filters Time:");
		}

		if (Debug.ActivityControl.showGeneratedFiltersNumber) {
			System.out.println("Filters # Generated: " + mFilterList.size());
		}

		// -- Adjustment
		stopWatch.start();
		adjust(mSequenceList);
		if (Debug.ActivityControl.showElapsedTime) {
			stopWatch.show("Adjust Time:");
		}
	}

	@Override
	protected void initialize() {

		for (AbstractDataPreprocessor activity : mDataPreprocessorList) {
			activity.initialize();
		}

		for (AbstractFilter activity : mFilterList) {
			activity.initialize();
		}
	}

	@Override
	public void load(String contextSourceFilenameAddress) {

		SequenceSet inputSequenceSet = HandlingSequenceSet
				.transformFileInSequenceSet(contextSourceFilenameAddress,
						FileType.TRAINING, false);

		DataSequence sequence;

		// -- Load Action before SequenceSet Iteration
		loadActionBeforeSequenceSetIteration();

		while (inputSequenceSet.hasNext()) {

			sequence = inputSequenceSet.next();

			// -- Create a verification process to check if similar sequences
			// was added
			mSequenceList.add(sequence);

			loadSequence(sequence);
		}

		// -- Load Action after SequenceSet Iteration
		loadActionAfterSequenceSetIteration();
	}

	@Override
	protected void loadSequence(DataSequence sequence) {

		HashMap<String, SequenceLabel> processedSequenceMap = PreprocessData
				.preprocessSequence(sequence, mDataPreprocessorList);
		SequenceLabel sequenceLabel;

		AbstractDataPreprocessor dataPreprocessor;
		boolean[] hasAddedDataProcessor;

		int dataProcessorIndex;
		int entityIndex;

		// -- Load Action Before Sequence
		loadActionBeforeSequenceIteration(processedSequenceMap);

		for (int i = 0; i < sequence.length(); i++) {

			entityIndex = SupportEntity.getEntityIndex(sequence, i);
			hasAddedDataProcessor = new boolean[mDataPreprocessorList.size()];

			synchronized (this) {
				if (LabelEncoding.isEntity(sequence.y(i))) {
					mEntityList.add((String) sequence.x(i));
				}
			}

			for (AbstractFilter filter : mFilterList) {

				sequenceLabel = processedSequenceMap.get(filter
						.getPreprocesingTypeName());
				dataProcessorIndex = filter
						.getFilterPreprocessingTypeNameIndex();

				filter.loadTermSequenceRestricted(sequenceLabel, entityIndex);

				// -- Add General Statistics for Common Terms
				if (!hasAddedDataProcessor[dataProcessorIndex]) {
					dataPreprocessor = mDataPreprocessorList
							.get(dataProcessorIndex);
					synchronized (this) {
						dataPreprocessor
						.computeCommonTermsInSequence(sequenceLabel);
					}
					hasAddedDataProcessor[dataProcessorIndex] = true;
				}
			}

			// -- shift index to optimize search for entities
			i = (entityIndex > -1) ? entityIndex : i;
		}

		// -- Load Action After Sequence
		loadActionAfterSequenceIteration(processedSequenceMap);
	}

	@Override
	protected void loadActionBeforeSequenceSetIteration() {
		for (AbstractFilter activity : mFilterList) {
			activity.loadActionBeforeSequenceSetIteration();
		}
	}

	@Override
	protected void loadActionBeforeSequenceIteration(
			HashMap<String, SequenceLabel> processedSequenceMap) {
		for (AbstractFilter activity : mFilterList) {
			activity.loadActionBeforeSequenceIteration(processedSequenceMap
					.get(activity.getPreprocesingTypeName()));
		}
	}

	@Override
	protected void loadActionAfterSequenceIteration(
			HashMap<String, SequenceLabel> processedSequenceMap) {
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

	@Override
	protected void adjust(ArrayList<DataSequence> sequenceList) {

		Iterator<Entry<String, ArrayList<AbstractFilter>>> ite = mFilterListPerDataPreprocessor
				.entrySet().iterator();
		Entry<String, ArrayList<AbstractFilter>> entry;

		SequenceLabel sequenceLabelPreprocessed;
		int dataProcessIndex;
		int firstFilterIndex = 0;

		while (ite.hasNext()) {

			entry = ite.next();
			dataProcessIndex = entry.getValue().get(firstFilterIndex)
					.getFilterPreprocessingTypeNameIndex();

			for (DataSequence sequence : sequenceList) {

				sequenceLabelPreprocessed = mDataPreprocessorList.get(
						dataProcessIndex).preprocessingSequence(sequence);

				for (AbstractFilter filter : entry.getValue()) {

					filter.adjust(sequenceLabelPreprocessed);

					// -- Add General Statistics
					addFilterStatistic(filter, sequenceLabelPreprocessed);
				}
			}
		}

		/** Print Filter Statatistics **/
		printFilterStatistics();
	}

	protected void printFilterStatistics() {
		if (!Parameters.SimpleActivityControl.isItUpdate
				&& Debug.ActivityControl.printFilterStatistics) {
			System.out.println("- Filters TRAINING statistics");
			for (AbstractFilter filter : mFilterList) {
				if (filter.getFilterState() != FilterState.Auxiliary) {
					filter.printFilterProbabilityInstanceStatistics();
				}
			}
		}
	}

	@Override
	public void update(ArrayList<DataSequence> updateSequenceList) {
		for (DataSequence sequence : updateSequenceList) {

			// -- Create a verification process to check if similar sequences
			// was added
			mSequenceList.add(sequence);
			loadSequence(sequence);
		}

		adjust(updateSequenceList);
	}
}
