package lbd.FSNER.DataPreprocessor;

import lbd.FSNER.Component.SequenceLabelElement;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.Utils.RemoveStopWordsTool;

public class DPStopWord extends AbstractDataPreprocessor {

	private static final long serialVersionUID = 1L;
	
	protected RemoveStopWordsTool stopWordTool;
	protected final int TERM_MINIMUM_SIZE = 1;
	
	public DPStopWord(String initializeFile) {
		super(ClassName.getSingleName(DPStopWord.class.getName())+(initializeFile).substring(
				initializeFile.lastIndexOf(Symbol.SLASH)), initializeFile);
	}

	@Override
	public void initialize() {
		stopWordTool = new RemoveStopWordsTool(mInitializeFile);
	}

	@Override
	public SequenceLabelElement preprocessingTerm(String term, int label) {
		return (new SequenceLabelElement(((stopWordTool.isStopWord(term))? Symbol.EMPTY : term), label));
	}
}
