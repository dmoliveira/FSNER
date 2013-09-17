package lbd.FSNER.Component;

import java.io.Serializable;
import java.util.ArrayList;

public class SequenceLabel implements Serializable{

	private static final long serialVersionUID = 1L;

	protected ArrayList<SequenceLabelElement> sequenceLabelElementList;

	public SequenceLabel() {
		sequenceLabelElementList = new ArrayList<SequenceLabelElement>();
	}

	public void add(SequenceLabelElement sequenceLabelElement) {
		sequenceLabelElementList.add(sequenceLabelElement);
	}

	public void addTerm(String term, int label) {
		sequenceLabelElementList.add(new SequenceLabelElement (term, label));
	}

	public String getTerm(int index) {
		return(sequenceLabelElementList.get(index).getTerm());
	}

	public int getLabel(int index) {
		return(sequenceLabelElementList.get(index).getLabel());
	}

	public void setLabel(int index, int label) {
		sequenceLabelElementList.get(index).setLabel(label);
	}

	public int size() {
		return(sequenceLabelElementList.size());
	}

	public String[] toArraySequence() {
		String [] sequence = new String[sequenceLabelElementList.size()];

		for(int i = 0; i < sequenceLabelElementList.size(); i++) {
			sequence[i] = sequenceLabelElementList.get(i).getTerm();
		}

		return(sequence);
	}

	@Override
	public String toString() {

		String objectValues = "";

		for(int i = 0; i < sequenceLabelElementList.size(); i++) {
			objectValues += " {" + sequenceLabelElementList.get(i).getTerm() + "," +
					sequenceLabelElementList.get(i).getLabel() + "}";
		}

		objectValues += " ";

		return(objectValues);
	}

}
