package lbd.FSNER.ActivityControl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.DataProcessor.Component.PreprocessData;
import lbd.FSNER.Model.AbstractActivityControl;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilter.FilterState;
import lbd.FSNER.Utils.EntityUtils;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.SimpleStopWatch;
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
		//super(new CustomFilterCombination());
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

		// -- Pos Processament

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

		if(pSequence == null || pSequence.length() == 0) {
			return;
		}

		Map<String, SequenceLabel> vProcessedSequenceMap = PreprocessData.preprocessSequence(pSequence, mDataPreprocessorList);
		Set<SequenceLabel> vSequenceLabelPreprocessedSet = new HashSet<SequenceLabel>();

		int vEntityIndex;

		// -- Load Action Before Sequence
		loadActionBeforeSequenceIteration(vProcessedSequenceMap);

		for (int i = 0; i < pSequence.length(); i++) {

			vEntityIndex = EntityUtils.getEntityIndex(pSequence, i);

			if(vEntityIndex == -1) {
				return;
			}

			synchronized (this) {
				if (LabelEncoding.isEntity(pSequence.y(i))) {
					mEntitySet.add((String) pSequence.x(i));
				}
			}

			for (AbstractFilter cFilter : mFilterList) {

				SequenceLabel vSequenceLabel = vProcessedSequenceMap.get(cFilter.getPreprocesingTypeName());

				int vDataProcessorIndex = cFilter.getFilterPreprocessingTypeIndex();

				cFilter.loadTermSequence(vSequenceLabel, vEntityIndex);

				// -- Calculate term commonness
				calculateTermCommonness(vSequenceLabelPreprocessedSet, vSequenceLabel, vDataProcessorIndex);
			}

			// -- shift index to optimize search for entities
			i = vEntityIndex;
		}

		// -- Load Action After Sequence
		loadActionAfterSequenceIteration(vProcessedSequenceMap);
	}

	private void calculateTermCommonness(Set<SequenceLabel> vSequenceLabelPreprocessedSet,
			SequenceLabel vSequenceLabel, int vDataProcessorIndex) {

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

		Entry<String, List<AbstractFilter>> vEntry;
		List<AbstractFilter> vFilterList;

		int dataProcessIndex;
		int firstFilterIndex = 0;

		while (ite.hasNext()) {

			vEntry = ite.next();
			vFilterList = vEntry.getValue();

			dataProcessIndex = vEntry.getValue().get(firstFilterIndex).getFilterPreprocessingTypeIndex();

			for (DataSequence sequence : sequenceList) {
				addSequenceToFilters(vFilterList, dataProcessIndex, sequence);
			}
		}

		/** Print Filter Statatistics **/
		printFilterStatistics();
	}

	private void addSequenceToFilters(List<AbstractFilter> pFilterList, int pDataProcessIndex,
			DataSequence pSequence) {

		SequenceLabel vSequenceLabelPreprocessed;
		vSequenceLabelPreprocessed = mDataPreprocessorList.get(pDataProcessIndex).preprocessingSequence(pSequence);

		for (AbstractFilter cFilter : pFilterList) {

			cFilter.adjust(vSequenceLabelPreprocessed);

			// -- Add General Statistics
			addFilterStatistic(cFilter, pSequence, vSequenceLabelPreprocessed);
		}
	}

	protected void printFilterStatistics() {
		if (!Parameters.SimpleActivityControl.isItUpdate
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
		}
		System.out.println();
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
