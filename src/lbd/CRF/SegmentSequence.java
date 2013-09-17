package lbd.CRF;

import java.util.ArrayList;

import iitb.CRF.SegmentDataSequence;

public class SegmentSequence extends Sequence implements SegmentDataSequence {

	private ArrayList<Integer> endSegmentList;
	
	public SegmentSequence() {
		super();
		endSegmentList = new ArrayList<Integer>();
	}
	
	@Override
	public void addElement(String token, int label) {
		super.addElement(token, label);
		endSegmentList.add(-1);
	}
	
	@Override
	public int getSegmentEnd(int segmentStart) {		
		return (endSegmentList.get(segmentStart));
	}

	@Override
	public void setSegment(int segmentStart, int segmentEnd, int y) {
		
		for(int i = segmentStart; i <= segmentEnd; i++) {
			set_y(i,y);
			endSegmentList.set(i, segmentEnd);
		}
		
	}

}
