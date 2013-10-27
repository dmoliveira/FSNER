package lbd.FSNER.Model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.data.handler.ISequence;

public abstract class AbstractUpdateControl implements Serializable {

	private static final long serialVersionUID = 1L;
	protected ArrayList<ISequence> sequenceListToUpdate;
	protected HashMap<String, Object> sequenceAdded;
	protected double threshouldConfidenceSequence;

	public AbstractUpdateControl(double threshouldConfidenceSequence) {
		sequenceListToUpdate = new ArrayList<ISequence>();
		sequenceAdded = new HashMap<String, Object>();

		this.threshouldConfidenceSequence = threshouldConfidenceSequence;
	}

	public abstract boolean addSequence(ISequence dataSequence);

	public ArrayList<ISequence> getSequenceListToUpdate() {
		return(sequenceListToUpdate);
	}

	public ISequence getSequence(int index) {
		return(sequenceListToUpdate.get(index));
	}

	public void removeSequence(ISequence sequence) {
		sequenceListToUpdate.remove(sequence);
	}

	public int getUpdateListSize() {
		return(sequenceListToUpdate.size());
	}

	public boolean isSequenceAdded(String sequence) {
		return(sequenceAdded.containsKey(sequence));
	}

	public void restartForNextUpdate() {
		sequenceListToUpdate.clear();
	}

	public double getThreshouldConfidenceSequence() {
		return(threshouldConfidenceSequence);
	}

}
