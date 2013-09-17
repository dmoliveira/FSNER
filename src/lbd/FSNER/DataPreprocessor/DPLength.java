package lbd.FSNER.DataPreprocessor;

import lbd.FSNER.Component.SequenceLabelElement;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;

public class DPLength extends AbstractDataPreprocessor{

	private static final long serialVersionUID = 1L;
	protected int prefixTermLength;

	public DPLength(int prefixTermLength) {
		super(ClassName.getSingleName(DPLength.class.getName()) + 
				".Len:" + prefixTermLength, null);
		
		this.prefixTermLength = prefixTermLength;
	}
	
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SequenceLabelElement preprocessingTerm(String term, int label) {
		
		String processedTerm = (prefixTermLength > -1)? 
				((prefixTermLength < term.length())?
						term.substring(0, prefixTermLength) : term) : Symbol.EMPTY;
		
		processedTerm += Symbol.HYPHEN + term.length();
						
		return (new SequenceLabelElement(processedTerm, label));
	}

}
