package lbd.Utils;

import java.util.ArrayList;
import java.util.HashMap;

class EntrySequenceProbability<T, K> {

	protected T sequenceNumber;
	protected K probability;

	public EntrySequenceProbability(T sequenceNumber, K probability) {
		this.sequenceNumber = sequenceNumber;
		this.probability = probability;
	}

	public T getSequenceNumber() {
		return(sequenceNumber);
	}

	public K getSequenceProbability() {
		return(probability);
	}
}

public class ProbabilityInformationSet {

	protected HashMap<Integer, Double> probabilityInformationSetMap;
	protected ArrayList<EntrySequenceProbability<Integer, Double>> probabilityInformationSetList;
	protected ArrayList<Boolean> isSequenceReliable;
	protected double threshould;

	public ProbabilityInformationSet(double threshould) {
		probabilityInformationSetMap = new HashMap<Integer, Double>();
		probabilityInformationSetList = new ArrayList<EntrySequenceProbability<Integer,Double>>();
		isSequenceReliable = new ArrayList<Boolean>();
		this.threshould = threshould;
	}

	public void add(int sequenceNumber, double probability) {
		probabilityInformationSetMap.put(sequenceNumber, probability);
		probabilityInformationSetList.add(new EntrySequenceProbability<Integer, Double>(sequenceNumber, probability));
	}

	public double getProbabilitySequenceLabelByIndex(int index) {
		return(probabilityInformationSetList.get(index).probability);
	}

	public double getProbabilitySequenceLabel(int sequenceNumber) {
		return((probabilityInformationSetMap.containsKey(sequenceNumber))? probabilityInformationSetMap.get(sequenceNumber) : 0);
	}

	public boolean existProbabilitySequence(int sequenceNumber) {
		return(probabilityInformationSetMap.containsKey(sequenceNumber));
	}

	public boolean isSequenceReliable(int sequenceNumber) {
		return(getProbabilitySequenceLabel(sequenceNumber) >= threshould);
	}

	public double getThreshould() {
		return(threshould);
	}

	public void addRealibility(Boolean isReliable) {
		isSequenceReliable.add(isReliable);
	}

	public boolean isReliable(int sequenceIndex) {
		return(isSequenceReliable.get(sequenceIndex));
	}

}
