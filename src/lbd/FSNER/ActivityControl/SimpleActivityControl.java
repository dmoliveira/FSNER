package lbd.FSNER.ActivityControl;


import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import lbd.data.handler.DataSequence;
import lbd.data.handler.SequenceSet;
import lbd.data.handler.SequenceSetHandler;
import lbd.data.handler.SequenceSetHandler.FileType;

public class SimpleActivityControl extends AbstractActivityControl {
	private static final long serialVersionUID = 1L;

	public SimpleActivityControl() {
		super(new SimpleFilterCombination());
		// super(new TermFocusFilterCombination());
		// super(new CapitalizationFocusFilterCombination());
		//super(new TermComplementaryFocusFilterCombination()); //USE State Filter - Caution [!]
		//super(new ContextFocusFilterCombination());
	}

	@Override
	protected void startActivityControlSub(String pContextSourceFile) {

		SimpleStopWatch vStopWatch = new SimpleStopWatch();

		// -- Initialize All Activities (DataPreprocessor & Filter)
		vStopWatch.start();
		initialize();
		if (Debug.ActivityControl.showElapsedTime) {
			vStopWatch.show("Initialize Time:");
		}

		// -- Load (Only for Filters)
		vStopWatch.start();
		load(pContextSourceFile);
		if (Debug.ActivityControl.showElapsedTime) {
			vStopWatch.show("Load Time:");
		}

		// -- Generate Combination of MultiFilters from 2 to 5 filters together
		vStopWatch.start();
		if (Parameters.SimpleActivityControl.isToCombineFilters) {
			mFilterCombination.combineAllFilters(mFilterList,
					mFilterListPerDataPreprocessor);
		}

		if (Debug.ActivityControl.showElapsedTime) {
			vStopWatch.show("Combine Filters Time:");
		}

		if (Debug.ActivityControl.showGeneratedFiltersNumber) {
			System.out.println("Filters # Generated: " + mFilterList.size());
		}

		// -- Adjustment
		vStopWatch.start();
		adjust(mSequenceList);
		if (Debug.ActivityControl.showElapsedTime) {
			vStopWatch.show("Adjust Time:");
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
	public void load(String pContextSourceFilenameAddress) {

		SequenceSet vInputSequenceSet = SequenceSetHandler
				.getSequenceSetFromFile(pContextSourceFilenameAddress,
						FileType.TRAINING, false);

		DataSequence vSequence;

		// -- Load Action before SequenceSet Iteration
		loadActionBeforeSequenceSetIteration();

		while (vInputSequenceSet.hasNext()) {

			vSequence = vInputSequenceSet.next();

			//TODO: Create a verification process to check if similar sequences was added
			mSequenceList.add(vSequence);

			loadSequence(vSequence);
		}

		// -- Load Action after SequenceSet Iteration
		loadActionAfterSequenceSetIteration();
	}

	@Override
	protected void loadSequence(DataSequence pSequence) {

		Map<String, SequenceLabel> vProcessedSequenceMap = PreprocessData
				.preprocessSequence(pSequence, mDataPreprocessorList);
		SequenceLabel sequenceLabel;

		AbstractDataPreprocessor vDataPreprocessor;
		boolean[] vHasAddedDataProcessor;

		int dataProcessorIndex;
		int vEntityIndex;

		// -- Load Action Before Sequence
		loadActionBeforeSequenceIteration(vProcessedSequenceMap);

		for (int i = 0; i < pSequence.length(); i++) {

			vEntityIndex = SupportEntity.getEntityIndex(pSequence, i);
			vHasAddedDataProcessor = new boolean[mDataPreprocessorList.size()];

			synchronized (this) {
				if (LabelEncoding.isEntity(pSequence.y(i))) {
					mEntityList.add((String) pSequence.x(i));
				}
			}

			for (AbstractFilter filter : mFilterList) {

				sequenceLabel = vProcessedSequenceMap.get(filter
						.getPreprocesingTypeName());
				dataProcessorIndex = filter
						.getFilterPreprocessingTypeNameIndex();

				filter.loadTermSequenceRestricted(sequenceLabel, vEntityIndex);

				// -- Add General Statistics for Common Terms
				if (!vHasAddedDataProcessor[dataProcessorIndex]) {
					vDataPreprocessor = mDataPreprocessorList
							.get(dataProcessorIndex);
					synchronized (this) {
						vDataPreprocessor
						.computeCommonTermsInSequence(sequenceLabel);
					}
					vHasAddedDataProcessor[dataProcessorIndex] = true;
				}
			}

			// -- shift index to optimize search for entities
			i = (vEntityIndex > -1) ? vEntityIndex : i;
		}

		// -- Load Action After Sequence
		loadActionAfterSequenceIteration(vProcessedSequenceMap);
	}

	@Override
	protected void loadActionBeforeSequenceSetIteration() {
		for (AbstractFilter activity : mFilterList) {
			activity.loadActionBeforeSequenceSetIteration();
		}
	}

	@Override
	protected void loadActionBeforeSequenceIteration(
			Map<String, SequenceLabel> processedSequenceMap) {
		for (AbstractFilter activity : mFilterList) {
			activity.loadActionBeforeSequenceIteration(processedSequenceMap
					.get(activity.getPreprocesingTypeName()));
		}
	}

	@Override
	protected void loadActionAfterSequenceIteration(
			Map<String, SequenceLabel> processedSequenceMap) {
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
	protected void adjust(List<DataSequence> sequenceList) {

		Iterator<Entry<String, List<AbstractFilter>>> ite = mFilterListPerDataPreprocessor
				.entrySet().iterator();
		Entry<String, List<AbstractFilter>> entry;

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

	@SuppressWarnings("unused")
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
	public void update(List<DataSequence> updateSequenceList) {
		for (DataSequence sequence : updateSequenceList) {

			// -- Create a verification process to check if similar sequences
			// was added
			mSequenceList.add(sequence);
			loadSequence(sequence);
		}

		adjust(updateSequenceList);
	}
}
