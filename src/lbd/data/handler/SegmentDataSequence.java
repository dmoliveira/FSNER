package lbd.data.handler;

public interface SegmentDataSequence extends DataSequence {
	/** get the end position of the segment starting at segmentStart */
	int getSegmentEnd(int segmentStart);
	/** set segment boundary and label */
	void setSegment(int segmentStart, int segmentEnd, int y);
};
