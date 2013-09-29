package lbd.data.handler;

import java.util.ArrayList;

// Obs: SegmentSequence is equivalent to Sequence
public class SequenceSet implements DataIter {

	private ArrayList<SegmentSequence> sequenceList;
	private int currentPosition;

	public SequenceSet() {
		currentPosition = 0;
		sequenceList = new ArrayList<SegmentSequence>();
	}

	public void addSequence(SegmentSequence sequence) {
		sequenceList.add(sequence);
	}

	@Override
	public boolean hasNext() {
		return (currentPosition < sequenceList.size());
	}

	@Override
	public DataSequence next() {
		return (sequenceList.get(currentPosition++));
	}

	@Override
	public void startScan() {
		currentPosition = 0;
	}

	public SegmentSequence get(int index) {
		return(sequenceList.get(index));
	}

	public int size() {
		return (sequenceList.size());
	}

}
