package lbd.FSNER.Component;

import java.io.Serializable;

public class SequenceLabelElement implements Serializable{

	protected String term;
	protected int label;

	public SequenceLabelElement(String term, int label) {
		this.term = term;
		this.label = label;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

}
