package lbd.data.handler;

public interface ISequenceSegment extends ISequence {

	/** get the end position of the segment starting at segmentStart */
	int getSegmentEnd(int pSegmentStart);

	/** set segment boundary and label */
	void setSegment(int pSegmentStart, int pSegmentEnd, int pSegmentLabel);
};
