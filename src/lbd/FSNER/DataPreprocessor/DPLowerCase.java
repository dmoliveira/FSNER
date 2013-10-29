package lbd.FSNER.DataPreprocessor;

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
	public String preprocessingToken(String pTerm, int pLabel) {
		return pTerm.toLowerCase();
	}

}
