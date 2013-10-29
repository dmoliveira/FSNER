package lbd.FSNER.DataPreprocessor;

import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.Utils.RemoveStopWordsTool;

public class DPStopWord extends AbstractDataPreprocessor {

	private static final long serialVersionUID = 1L;

	protected RemoveStopWordsTool mStopWordTool;
	protected final int TERM_MINIMUM_SIZE = 1;

	public DPStopWord(String initializeFile) {
		super(ClassName.getSingleName(DPStopWord.class.getName())+(initializeFile).substring(
				initializeFile.lastIndexOf(Symbol.SLASH)), initializeFile);
	}

	@Override
	public void initialize() {
		mStopWordTool = new RemoveStopWordsTool(mInitializeFile);
	}

	@Override
	public String preprocessingToken(String pToken, int pLabel) {
		return (mStopWordTool.isStopWord(pToken))? Symbol.EMPTY : pToken;
	}
}
