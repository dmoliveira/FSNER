package lbd.FSNER.Filter;

import java.util.HashSet;
import java.util.Set;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Utils.EntityUtils;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.DataSequence;

public class FtrEntityContext extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	private static final String DELIMITER_AFFIXES = Symbol.PIPE + Symbol.PIPE;
	private static final String DELIMITER_TOKEN_CONTEXT = Symbol.PIPE;

	protected Set<String> mContextSet;

	protected int mContextSizeUsed;

	protected int mMaximumContextSize = Parameters.Filter.Context.maximumContextSize;
	protected int mMaximumEntitySizeToSearch = Parameters.Filter.Context.maximumEntitySizeToSearch;

	// -- Memory for load
	private int mEndEntityIndex;

	public FtrEntityContext(int pPreprocessingTypeIndex, int pContextSizeUsed) {
		super(FtrEntityContext.class.getName() + ".CtxWnd:" + pContextSizeUsed, pPreprocessingTypeIndex);
		mContextSet = new HashSet<String>();
		mContextSizeUsed = Math.min(pContextSizeUsed, mMaximumContextSize);
	}

	@Override
	public void initialize() {
	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceIteration(SequenceLabel pSequenceLabelProcessed) {
		mEndEntityIndex = -1;
	}

	@Override
	public void loadTermSequence(SequenceLabel pSequenceLabelProcessed, int pIndex) {
		if(mEndEntityIndex == -1) {
			mEndEntityIndex = EntityUtils.getEntityEndIndex(pSequenceLabelProcessed, pIndex);

			if(mEndEntityIndex != -1) {
				mContextSet.add(getContext(pSequenceLabelProcessed, pIndex, (mEndEntityIndex - pIndex + 1)));
			}
		}

		if(mEndEntityIndex == pIndex) {
			mEndEntityIndex = -1;
		}
	}

	protected String getContext(SequenceLabel pSequenceLabelProcessed, int pIndex, int pEntitySize) {

		if(pEntitySize < 1) {
			pEntitySize = 1;
		}

		int vLowerBound = Math.max(0, pIndex - mContextSizeUsed);
		int vUpperBound = Math.min(pSequenceLabelProcessed.size(), (pIndex + pEntitySize) + mContextSizeUsed - 1);

		String vContextText = Symbol.EMPTY;

		for(int cContextIndex = vLowerBound; cContextIndex < vUpperBound; cContextIndex++) {
			if(cContextIndex != pIndex) {
				vContextText += pSequenceLabelProcessed.getTerm(cContextIndex) + DELIMITER_TOKEN_CONTEXT;
			} else {
				if(vContextText.endsWith(DELIMITER_TOKEN_CONTEXT)) {
					vContextText = vContextText.substring(0, vContextText.length() - 1);
				}
				vContextText += DELIMITER_AFFIXES;
			}
		}

		if(!vContextText.isEmpty()) {
			vContextText = vContextText.substring(0, vContextText.length() - 1).trim();
		}

		return vContextText;
	}

	@Override
	public void loadActionAfterSequenceIteration(
			SequenceLabel pSequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		mContextSet.remove(Symbol.EMPTY);
	}

	@Override
	public void adjust(SequenceLabel pSequenceProcessedLabel) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(DataSequence pSequence, SequenceLabel pSequenceLabelProcessed, int pIndex) {

		boolean vWasContextFound =false;
		String vContextText = Symbol.EMPTY;
		int vEntitySizeContext = mMaximumEntitySizeToSearch;

		//-- Search for prefix & suffix
		do {
			vContextText = getContext(pSequenceLabelProcessed, pIndex, vEntitySizeContext);
			vWasContextFound = mContextSet.contains(vContextText);
		} while(!vWasContextFound && --vEntitySizeContext > 0);

		String vId = (vWasContextFound)?  "mId:" + mId + ".Ctx." + vContextText : Symbol.EMPTY;
		return vId;
	}

}
