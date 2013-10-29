package lbd.FSNER.DataPreprocessor;

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
	public String preprocessingToken(String pToken, int pLabel) {
		return pToken;
	}

}
