package lbd.FSNER.DataPreprocessor;

import lbd.FSNER.Component.SequenceLabelElement;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.ClassName;

public class DPPlainSequence extends AbstractDataPreprocessor{

	private static final long serialVersionUID = 1L;

	public DPPlainSequence() {
		super(ClassName.getSingleName(DPPlainSequence.class.getName()), null);
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SequenceLabelElement preprocessingTerm(String term, int label) {
		return (new SequenceLabelElement(term, label));
	}

}
