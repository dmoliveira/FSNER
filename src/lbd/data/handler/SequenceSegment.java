package lbd.data.handler;


import java.util.ArrayList;
import java.util.List;

public class SequenceSegment extends Sequence implements ISequenceSegment {

	private static final long serialVersionUID = 1L;

	private List<Integer> mEndSegmentList;

	public SequenceSegment() {
		super();
		mEndSegmentList = new ArrayList<Integer>();
	}

	@Override
	public void add(String pToken, int pLabel) {
		super.add(pToken, pLabel);
		mEndSegmentList.add(-1);
	}

	@Override
	public int getSegmentEnd(int pSegmentStart) {
		return (mEndSegmentList.get(pSegmentStart));
	}

	@Override
	public void setSegment(int pSegmentStart, int pSegmentEnd, int pSegmentLabel) {
		for(int i = pSegmentStart; i <= pSegmentEnd; i++) {
			setLabel(i,pSegmentLabel);
			mEndSegmentList.set(i, pSegmentEnd);
		}

	}

	@Override
	public ISequence clone() {
		ISequence vClonedSequence = new SequenceSegment();
		for(int i = 0; i < length(); i++) {
			vClonedSequence.add(getToken(i),getLabel(i));
			((SequenceSegment)vClonedSequence).mEndSegmentList.add(mEndSegmentList.get(i));
		}

		return vClonedSequence;
	}

}
