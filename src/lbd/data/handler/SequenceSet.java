package lbd.data.handler;

import java.util.ArrayList;
import java.util.List;

public class SequenceSet implements IDataIterator {

	private List<SequenceSegment> mSequenceList;
	private int mCurrentIndex;

	public SequenceSet() {
		mSequenceList = new ArrayList<SequenceSegment>();
		mCurrentIndex = 0;
	}

	public void add(SequenceSegment pSequence) {
		mSequenceList.add(pSequence);
	}

	@Override
	public boolean hasNext() {
		return (mCurrentIndex < mSequenceList.size());
	}

	@Override
	public SequenceSegment next() {
		return (mSequenceList.get(mCurrentIndex++));
	}

	@Override
	public void startScan() {
		mCurrentIndex = 0;
	}

	public SequenceSegment get(int pIndex) {
		return(mSequenceList.get(pIndex));
	}

	public int size() {
		return (mSequenceList.size());
	}

}
