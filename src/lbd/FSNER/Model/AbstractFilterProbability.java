package lbd.FSNER.Model;

import java.io.Serializable;

import lbd.FSNER.Utils.LabelEncoding;

public abstract class AbstractFilterProbability implements Serializable {

	private static final long serialVersionUID = 1L;

	protected int mTotalAssignedEntityLabel;
	protected int mTotalAssignedLabel;

	protected int [] mTotalAssignedTermPerLabel;

	public AbstractFilterProbability() {
		mTotalAssignedTermPerLabel = new int [LabelEncoding.getAlphabetSize()];
	}

	public abstract void addLabel(int pLabel);

	public double getProbability() {
		return(((double)mTotalAssignedEntityLabel)/mTotalAssignedLabel);
	}

	public double getProbability(int pLabel) {
		return(((double)mTotalAssignedTermPerLabel[pLabel])/mTotalAssignedLabel);
	}

	public int getTotalAssignedEntityLabel() {
		return(mTotalAssignedEntityLabel);
	}

	public int getTotalAssignedEntityPerLabel(int pLabel) {
		return(mTotalAssignedTermPerLabel[pLabel]);
	}

	public int getAlphabetSize() {
		return(mTotalAssignedTermPerLabel.length);
	}

	public int getTotalNumberLabel() {
		return(mTotalAssignedLabel);
	}
}
