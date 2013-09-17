package lbd.FSNER.Component.Statistic;

import java.io.Serializable;

import lbd.FSNER.Utils.LabelEncoding;

public class LabelProbabilityElement implements Serializable{

	private static final long serialVersionUID = 1L;

	protected int numberLabelAsEntity;
	protected int numberTotalLabel;

	protected int [] numberLabelAsEntityArray;

	public LabelProbabilityElement() {
		numberLabelAsEntityArray = new int [LabelEncoding.getAlphabetSize()] ;
	}

	public void add(boolean isEntityLabel, int label) {

		numberTotalLabel++;
		numberLabelAsEntityArray[label]++;

		if(isEntityLabel) {
			numberLabelAsEntity++;
		}
	}

	public double getEntityLabelProbability() {
		return(((double)numberLabelAsEntity)/numberTotalLabel);
	}

	public double getEntityLabelProbability(int label) {
		return(((double)numberLabelAsEntityArray[label])/numberTotalLabel);
	}
}
