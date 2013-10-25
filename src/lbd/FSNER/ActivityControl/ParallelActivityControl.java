package lbd.FSNER.ActivityControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilter.FilterState;
import lbd.data.handler.DataSequence;

public class ParallelActivityControl extends SimpleActivityControl implements Runnable {

	private static final long serialVersionUID = 1L;

	protected final static int MAX_THREADS = 4;

	protected HashMap<String, SequenceLabel> processedSequenceMap;

	protected DataSequence sequence;
	protected static enum Phase{LoadSequence, Adjust};
	protected Phase phase;

	protected int startFilterList;
	protected int endFilterList;

	public ParallelActivityControl() {
		super();
	}

	/*public void load(String contextSourceFilenameAddress) {

		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(contextSourceFilenameAddress,
				FileType.TRAINING, false);

		//-- Load Action before SequenceSet Iteration
		loadActionBeforeSequenceSetIteration();

		ArrayList<Thread> threadList = new ArrayList<Thread>();
		int threadNumber = 0;

		while(inputSequenceSet.hasNext()) {

			sequence = inputSequenceSet.next();

			Runnable task = new ParallelActivityControl();
			((ParallelActivityControl)task).setPhase(Phase.LoadSequence);
			((ParallelActivityControl)task).setSequence(sequence);
			((ParallelActivityControl)task).setDataPreprocessorList(dataPreprocessorList);
			((ParallelActivityControl)task).setFilterList(filterList);
			sequenceList.add(sequence);

			Thread worker = new Thread(task);
			worker.setName(String.valueOf(++threadNumber));

			worker.start();

			threadList.add(worker);

			if(threadList.size() >= MAX_THREADS) {
				waitUntilThreadsFinish(threadList);
				threadList.clear();
			}
		}

		//-- Load Action after SequenceSet Iteration
		loadActionAfterSequenceSetIteration();
	}*/


	@Override
	public void run() {
		if(phase == Phase.LoadSequence) {
			loadSequence(sequence);
		} else if(phase == Phase.Adjust) {
			X(sequence);
		}
	}

	protected int getPosThread(ArrayList<Thread> threadsList) {

		int pos = -1;

		for (int i = 0; i < threadsList.size(); i++) {
			if(!threadsList.get(i).isAlive()) {
				pos = i;
				break;
			}
		}

		return(pos);
	}

	protected void waitUntilThreadsFinish(List<Thread> threadsList) {
		int running = 0;

		do {
			running = 0;
			for (Thread thread : threadsList) {
				if (thread.isAlive()) {
					running++;
				}
			}
		} while (running > 0);
	}

	@Override
	protected void adjust(final List<DataSequence> sequenceList) {

		int interval = 0;
		List<Thread> threadList = new ArrayList<Thread>();

		for(int i = 0; i < MAX_THREADS; i++) {

			Runnable task = new ParallelActivityControl();
			((ParallelActivityControl)task).setPhase(Phase.Adjust);
			((ParallelActivityControl)task).setDataPreprocessorList(mDataPreprocessorList);
			((ParallelActivityControl)task).setFilterList(mFilterList);
			//((ParallelActivityControl)task).sequenceList = sequenceList;
			((ParallelActivityControl)task).startFilterList = interval;

			interval += sequenceList.size()/MAX_THREADS;
			((ParallelActivityControl)task).endFilterList = interval;

			//System.out.println(((ParallelActivityControl)task).startFilterList + ":" + ((ParallelActivityControl)task).endFilterList + "(" + sequenceList.size() + ")");

			Thread worker = new Thread(task);
			worker.setName("AdjustThread#" + String.valueOf(i + 1));

			worker.start();
			threadList.add(worker);
		}

		waitUntilThreadsFinish(threadList);

		/** Print Filter Statatistics**/
		if(Debug.ActivityControl.printFilterStatistics) {
			for(AbstractFilter filter : mFilterList) {
				if(filter.getFilterState() != FilterState.Auxiliary) {
					filter.printFilterProbabilityInstanceStatistics();
				}
			}
		}

	}

	protected void X(DataSequence pSequence) {
		for(int i = startFilterList; i < endFilterList; i++) {

			//processedSequenceMap = PreprocessData.preprocessSequence(sequenceList.get(i), dataPreprocessorList);

			for(AbstractFilter filter : mFilterList)  {

				SequenceLabel sequenceLabel = processedSequenceMap.get(filter.getPreprocesingTypeName());
				filter.adjust(sequenceLabel);

				//-- Add General Statistics
				addFilterStatistic(filter, pSequence, sequenceLabel);
			}
		}
	}

	public void setSequence(DataSequence sequence) {
		this.sequence = sequence;
	}

	public void setPhase(Phase phase) {
		this.phase = phase;
	}

	public void setDataPreprocessorList(
			List<AbstractDataPreprocessor> dataPreprocessorList) {
		this.mDataPreprocessorList = dataPreprocessorList;
	}

	public void setFilterList(List<AbstractFilter> filterList) {
		this.mFilterList = filterList;
	}
}
