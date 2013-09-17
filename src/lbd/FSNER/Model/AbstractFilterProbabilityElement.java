package lbd.FSNER.Model;

import java.io.Serializable;

import lbd.FSNER.Utils.LabelEncoding;

public abstract class AbstractFilterProbabilityElement implements Serializable {

	private static final long serialVersionUID = 1L;
	protected int totalAssignedEntityLabel;
	protected int totalAssignedLabel;

	protected int [] totalAssignedEntityPerLabel;

	public AbstractFilterProbabilityElement() {
		totalAssignedEntityPerLabel = new int [LabelEncoding.getAlphabetSize()];
	}

	public abstract void addLabel(int label);

	public double getProbability() {
		return(((double)totalAssignedEntityLabel)/totalAssignedLabel);
	}

	public double getProbability(int label) {
		return(((double)totalAssignedEntityPerLabel[label])/totalAssignedLabel);
	}

	public int getTotalAssignedEntityLabel() {
		return(totalAssignedEntityLabel);
	}

	public int getTotalAssignedEntityPerLabel(int label) {
		return(totalAssignedEntityPerLabel[label]);
	}

	public int getAlphabetSize() {
		return(totalAssignedEntityPerLabel.length);
	}

	public int getTotalNumberLabel() {
		return(totalAssignedLabel);
	}
}
