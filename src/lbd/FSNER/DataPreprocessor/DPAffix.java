package lbd.FSNER.DataPreprocessor;

import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;

public class DPAffix extends AbstractDataPreprocessor{

	private static final long serialVersionUID = 1L;

	protected int startAffix;
	protected int endAffix;

	protected final int MIN_TERM_LENGHT = 5;

	public DPAffix(int startAffix, int endAffix) {
		super(ClassName.getSingleName(DPAffix.class.getName()) +
				"(" + startAffix + "," + endAffix + ")", null);

		this.startAffix = startAffix;
		this.endAffix = endAffix;

		if(startAffix > endAffix) {
			new Throwable("DPAffix: StartAffix > EndAffix (" + startAffix + ">" + endAffix + ")");
		}
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public String preprocessingToken(String term, int label) {
		return (endAffix < term.length() && term.length() > MIN_TERM_LENGHT)?
				term.substring(startAffix, endAffix) : Symbol.EMPTY;
	}

}
