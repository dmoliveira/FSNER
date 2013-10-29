package lbd.FSNER.DataPreprocessor;

import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;

public class DPLength extends AbstractDataPreprocessor{

	private static final long serialVersionUID = 1L;
	protected int mPrefixTermLength;

	public DPLength(int pPrefixTermLength) {
		super(ClassName.getSingleName(DPLength.class.getName()) +
				".Len:" + pPrefixTermLength, null);

		mPrefixTermLength = pPrefixTermLength;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public String preprocessingToken(String pToken, int pLabel) {

		String vProcessedTerm = (mPrefixTermLength > -1)?
				((mPrefixTermLength < pToken.length())?	pToken.substring(0, mPrefixTermLength) : pToken) : Symbol.EMPTY;

				vProcessedTerm += Symbol.HYPHEN + pToken.length();

				return vProcessedTerm;
	}

}
