package lbd.FSNER.Filter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Model.AbstractMetaFilter;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.DataSequence;

public class FtrWindow extends AbstractMetaFilter{

	private static final long serialVersionUID = 1L;

	public enum WindowType {Prefix, Suffix, Both};

	protected int mWindowSize;
	protected WindowType mWindowType;

	public FtrWindow(int pPreprocessingTypeNameIndex, AbstractFilterScoreCalculatorModel pScoreCalculator,
			int pWindowSize, WindowType pWindowType, List<AbstractFilter> pFilterList) {

		super("Window[" + pWindowType.name() + ":" + pWindowSize + "](" + getFilterClassNames(pFilterList) + ")",
				pPreprocessingTypeNameIndex, pScoreCalculator);

		mWindowSize = pWindowSize;
		mWindowType = pWindowType;
		mFilterList = pFilterList;
	}

	public FtrWindow(int pPreprocessingTypeNameIndex, AbstractFilterScoreCalculatorModel pScoreCalculator,
			int pWindowSize) {

		super("Window[" + pWindowSize + "]", pPreprocessingTypeNameIndex, pScoreCalculator);

		mWindowSize = pWindowSize;
	}

	@Override
	protected String getSequenceInstanceIdSub(DataSequence pSequence,
			SequenceLabel pSequenceLabelProcessed, int pIndex) {

		List<Integer> vOffsetList = createOffsetList();
		int vLowerBound = vOffsetList.get(0);
		int vUpperBound = vOffsetList.get(vOffsetList.size() - 1);

		if(pIndex + vLowerBound < 0 || pIndex + vUpperBound > pSequence.length() - 1) {
			return Symbol.EMPTY;
		}

		String vId = Symbol.EMPTY;
		String vPartialId;

		for(int cOffset : vOffsetList) {

			vPartialId = super.getSequenceInstanceIdSub(pSequence, pSequenceLabelProcessed, pIndex + cOffset);

			if(!vPartialId.isEmpty()) {
				vId += MessageFormat.format("idx({0}):{1}+", pIndex + cOffset, vPartialId);
			}
		}

		return (vId.isEmpty())? vId : mIdTag + ".id:" + this.mId + "(" + vId.substring(0, vId.length() - 1) + ")";
	}

	private List<Integer> createOffsetList() {

		List<Integer> vOffsetList = new ArrayList<Integer>();

		if(mWindowType == null) {
			vOffsetList.add(((mWindowSize > 0)? 1 : -1));
		} else if(mWindowType == WindowType.Prefix) {
			for(int i = 1; i <= mWindowSize; i++) {
				vOffsetList.add(-i);
			}
		} else if(mWindowType == WindowType.Suffix) {
			for(int i = 1; i <= mWindowSize; i++) {
				vOffsetList.add(i);
			}
		} else if(mWindowType == WindowType.Both) {
			for(int i = 1; i <= mWindowSize; i++) {
				vOffsetList.add(-i);
				vOffsetList.add(i);
			}
		}
		Collections.sort(vOffsetList);

		//Special case, when you want to utilize the same position
		if(vOffsetList.size() == 0) {
			vOffsetList.add(0);
		}

		return vOffsetList;
	}
}
