package lbd.FSNER.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.FSNER.Filter.Component.Entity;
import lbd.FSNER.Filter.Component.Term;

public class Cluster {
	
	protected double coefficientOfSimilarity; //-- Standard is 0.5
	
	protected static int globalId;
	protected int id;
	
	protected Entity entity;
	protected HashMap<String, Term> mainPhrase;
	protected HashMap<String, Term> termMap;
	protected ArrayList<Sequence> sequenceList;
	protected ArrayList<Cluster> clusterSimilarList;
	
	protected double maximumScore;
	protected double averageScore;
	protected double minimumScore;
	
	public Cluster(Entity entity, Sequence sequence, double coefficientOfSimilarity) {
		
		globalId++;
		id = globalId;
		
		this.entity = entity;
		this.coefficientOfSimilarity = coefficientOfSimilarity;
		
		mainPhrase = new HashMap<String, Term>();
		termMap = new HashMap<String, Term>();
		clusterSimilarList = new ArrayList<Cluster>();
		
		sequenceList = new ArrayList<Sequence>();
		sequenceList.add(sequence);
		sequence.setSimilarity(1);
		
		assemblyMainPhrase(sequence);
		
		startScoreMeasures();
	}
	
	public void startScoreMeasures() {
		maximumScore = -1;
		minimumScore = Double.MAX_VALUE;
		averageScore = 0;
	}
	
	protected void assemblyMainPhrase(Sequence sequence) {
		
		HashMap<String, Boolean> sequenceMap = new HashMap<String, Boolean>();
		
		String termId = "";
		int termIndex = 0;
		
		for(Term term : sequence.getTermList()) {
			
			termId = term.getId();
			
			if(!mainPhrase.containsKey(termId)) {
				mainPhrase.put(termId, term);
				termMap.put(termId, term);
				
				term.addFrequencyPerSequence();
			} else {
				sequence.setTerm(mainPhrase.get(termId), termIndex);
				term = sequence.getTerm(termIndex);
			}
			
			sequenceMap.put(termId, false);
			term.addFrequency();			
			termIndex++;
		}		
	}
	
	protected void assemblyMainPhraseOld(Sequence sequence) {
		
		mainPhrase = new HashMap<String, Term>();
		HashMap<String, Boolean> sequenceMap = new HashMap<String, Boolean>();
		
		String termName;
		Term newTermReference;
		
		int termNumber = 0;
		
		for(Term term : sequence.getTermList()) {
			
			termName = term.getId();
			
			if(!sequenceMap.containsKey(termName)) {
				mainPhrase.put(termName, new Term(termName));
				
				newTermReference = mainPhrase.get(termName);
				newTermReference.addFrequencyPerSequence();
				termMap.put(termName, newTermReference);
			} else {
				newTermReference = mainPhrase.get(termName);
			}
			
			newTermReference.addFrequency();
			sequenceMap.put(termName, false);
			
			sequence.setTerm(newTermReference, termNumber++);
		}		
	}
	
	public void addSequence(Sequence sequence) {
		sequenceList.add(sequence);
		addToTermMap(sequence);
	}
	
	protected void addToTermMap(Sequence sequence) {
		
		HashMap<String, Boolean> sequenceMap = new HashMap<String, Boolean>();
		
		String termId = "";
		int termIndex = 0;
		
		for(Term term : sequence.getTermList()) {
			
			termId = term.getId();
			
			if(!termMap.containsKey(termId)) {
				termMap.put(termId, term);
				term.addFrequencyPerSequence();
			} else {
				sequence.setTerm(termMap.get(termId), termIndex);
				term = sequence.getTerm(termIndex);
				
				if(!sequenceMap.containsKey(termId))
					term.addFrequencyPerSequence();
			}
			
			sequenceMap.put(termId, true);
			term.addFrequency();
			termIndex++;
		}
	}
	
	public boolean isSimilarGeneralCluster(Sequence sequence) {
		return(calculateSequenceGeneralSimilarity(sequence) >= coefficientOfSimilarity);
	}
	
	public boolean isSimilar(Sequence sequence) {
		return(calculateSequenceSimilarity(sequence) >= coefficientOfSimilarity);
	}
	
	public boolean isSimilar(double similarityValue) {
		
		return(similarityValue >= coefficientOfSimilarity);
		
	}
	
	public double calculateSequenceSimilarity(String [] candidateTermList) {
		
		double similarity = 0;
		
		Sequence sequence = ClusterHandler.transformInSequence(candidateTermList);
		
		if(sequence.getTermList().size() > 0)
			similarity = calculateSequenceSimilarity(sequence);
			
		return(similarity);
	}
	
	public double calculateSequenceSimilarity(Sequence sequence) {
		
		double similarityValue = 0;
		double termNumber = 0;
		
		for(Term term : sequence.getTermList()) {
			if(termMap.containsKey(term.getId())) {//mainPhrase, termMap
				termNumber++;
			}
		}
		
		similarityValue = termNumber/mainPhrase.size();
		
		return(similarityValue);
	}
	
	public double calculateSequenceGeneralSimilarity(String [] candidateTermList) {
		
		double similarityValue = 0;
		Sequence sequence = ClusterHandler.transformInSequence(candidateTermList);
		
		if(sequence.getTermList().size() > 0)
			similarityValue = calculateSequenceGeneralSimilarity(sequence);
		
		return(similarityValue);
	}
	
	public double calculateSequenceGeneralSimilarity(Sequence sequence) {
		
		double similarityValue = 0;
		double termNumber = 0;
		
		for(Term term : sequence.getTermList()) {
			if(termMap.containsKey(term.getId())) {//mainPhrase, termMap
				termNumber++;
			}
		}
		
		similarityValue = termNumber/sequence.getTermList().size();
		
		return(similarityValue);
	}
	
	/** Not calculate Sequence Score - Calculated in previous step **/	
	public void addScore(double score) {
		if(score > maximumScore)
			maximumScore = score;
		if(score < minimumScore)
			minimumScore = score;
		
		averageScore += score;
	}
	
	public void calculateAverageScore() {
		averageScore /= sequenceList.size();
	}
	
	public void updateClusterScore(Entity entity) {
		
		Sequence firstSequence = sequenceList.get(0); 
		
		maximumScore = firstSequence.calculateScore(entity);
		minimumScore = maximumScore;
		averageScore = 0;
		
		double score;
		
		for(Sequence sequence : sequenceList) {
			
			score = sequence.calculateScore(entity);
			
			if(score > maximumScore)
				maximumScore = score;
			else if(score < minimumScore)
				minimumScore = score;
			
			averageScore += score;
		}
		
		averageScore /= sequenceList.size();
	}
	
	public void calculateTermScore() {
		
		Iterator<Entry<String, Term>> ite = termMap.entrySet().iterator();
		Term term;
		
		while(ite.hasNext()) {
			term = ite.next().getValue();
			term.calculateScore(sequenceList.size());
		}
	}
	
	public double getMinimumScore() {
		return(minimumScore);
	}
	
	public double getAverageScore() {
		return(averageScore);
	}
	
	public double getMaximumScore() {
		return(maximumScore);
	}

	public double getCoefficientOfSimilarity() {
		return coefficientOfSimilarity;
	}

	public void setCoefficientOfSimilarity(double coefficientOfSimilarity) {
		this.coefficientOfSimilarity = coefficientOfSimilarity;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public HashMap<String, Term> getMainPhrase() {
		return mainPhrase;
	}
	
	public HashMap<String, Term> getTermMap() {
		return termMap;
	}
	
	public Term getTerm(String termName) {
		return(termMap.get(termName));
	}

	public void setMainPhrase(HashMap<String, Term> mainPhrase) {
		this.mainPhrase = mainPhrase;
	}

	public ArrayList<Sequence> getSequenceList() {
		return sequenceList;
	}

	public void setSequenceList(ArrayList<Sequence> sequenceList) {
		this.sequenceList = sequenceList;
	}
	
	public void setSimilarCluster(Cluster similarCluster) {
		clusterSimilarList.add(similarCluster);
	}
	
	public ArrayList<Cluster> getSimilarClusterList() {
		return(clusterSimilarList);
	}
	
	public String toString() {
		return("Entity: " + entity.getId() + " [Min:" + minimumScore + " Avg: " + 
				averageScore + " Max: " + maximumScore + "] TermMapSize: " + termMap.size());
	}
}
