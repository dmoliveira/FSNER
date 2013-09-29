package lbd.data.handler;

import java.util.ArrayList;
import java.util.List;

// Obs: SegmentSequence is equivalent to Sequence
public class SequenceSet implements DataIter {

	private List<SegmentSequence> mSequenceList;
	private int mCurrentPosition;

	public SequenceSet() {
		mCurrentPosition = 0;
		mSequenceList = new ArrayList<SegmentSequence>();
	}

	public void addSequence(SegmentSequence sequence) {
		mSequenceList.add(sequence);
	}

	@Override
	public boolean hasNext() {
		return (mCurrentPosition < mSequenceList.size());
	}

	@Override
	public DataSequence next() {
		return (mSequenceList.get(mCurrentPosition++));
	}

	@Override
	public void startScan() {
		mCurrentPosition = 0;
	}

	public SegmentSequence get(int pIndex) {
		return(mSequenceList.get(pIndex));
	}

	public int size() {
		return (mSequenceList.size());
	}

}
