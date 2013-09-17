package lbd.FSNER.DataPreprocessor;

import lbd.FSNER.Component.SequenceLabelElement;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.ClassName;

public class DPLowerCase extends AbstractDataPreprocessor{

	private static final long serialVersionUID = 1L;

	public DPLowerCase() {
		super(ClassName.getSingleName(DPLowerCase.class.getName()), null);
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SequenceLabelElement preprocessingTerm(String term, int label) {
		return (new SequenceLabelElement(term.toLowerCase(), label));
	}

}
