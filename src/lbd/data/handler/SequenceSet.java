package lbd.data.handler;

import java.text.MessageFormat;
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

	public static void checkSequenceSetAreSameSize(SequenceSet pSequenceSetA, SequenceSet pSequenceSetB) {
		if(pSequenceSetA.size() != pSequenceSetB.size()) {
			throw new ArrayIndexOutOfBoundsException(MessageFormat.format("Error: Size of SequenceSet A and B are different. A: {0} and B:{1}.",
					pSequenceSetA.size(), pSequenceSetB.size()));
		}

		while(pSequenceSetA.hasNext()) {
			checkSequenceAreSameTokens(pSequenceSetA.next(), pSequenceSetB.next());

		}
	}

	public static void checkSequenceAreSameTokens(ISequence pSequenceA, ISequence pSequenceB) {
		if(pSequenceA.length() != pSequenceB.length()) {
			throw new ArrayIndexOutOfBoundsException(MessageFormat.format("Error: Length of Sequence A and B are different. A: {0} and B:{1}.",
					pSequenceA.length(), pSequenceB.length()));
		}

		for(int i = 0; i < pSequenceA.length(); i++) {
			if(!pSequenceA.getToken(i).equals(pSequenceB.getToken(i))) {
				throw new ArrayIndexOutOfBoundsException(MessageFormat.format("Error: Token of Sequence A and B are different. A: {0} and B:{1}.",
						pSequenceA.getToken(i), pSequenceB.getToken(i)));
			}
		}
	}

}
