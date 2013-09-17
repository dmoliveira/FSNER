package lbd.FSNER.DataPreprocessor;

import lbd.FSNER.Component.SequenceLabelElement;
import lbd.FSNER.DataProcessor.Component.SummarizedPattern;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.ClassName;

public class DPSummarizedPattern extends AbstractDataPreprocessor{

	private static final long serialVersionUID = 1L;
	
	protected SummarizedPattern summarizedPattern;

	public DPSummarizedPattern() {
		super(ClassName.getSingleName(DPSummarizedPattern.class.getName()), null);
	}
	
	@Override
	public void initialize() {
		summarizedPattern = new SummarizedPattern();
	}

	@Override
	public SequenceLabelElement preprocessingTerm(String term, int label) {
		return (new SequenceLabelElement(summarizedPattern.getPattern(term), label));
	}

}
