package lbd.FSNER.Filter.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Component.Cluster;
import lbd.Utils.QuickSort;
import lbd.Utils.Utils;

public class Entity implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public static final int MINIMUM_RELAX_MATCHING_LENGTH = 4;//5
	public static int EDIT_DISTANCE_ACCEPTABLE = 1;//1
	
	protected static int globalEntityNumber;
	protected int entityNumber;
	
	protected String id;
	protected int frequency;
	protected int frequencyNonEntity;
	protected double probabilityToBeEntity;
	protected ArrayList<Term> termList;
	protected HashMap<String, Entity> similarEntityMap;
	protected HashMap<Cluster, Boolean> similarClusterMap;
	protected Double [] termScore;
	
	protected ArrayList<Double> scoreSequenceList;
	
	protected double minScore;
	protected double maxScore;
	protected double averageScore;
	protected double stardardDeviationScore;
	
	public Entity(String id) {
		this.id = id;//id.toLowerCase(); (Original)
		termList = new ArrayList<Term>();
		similarEntityMap = new HashMap<String, Entity>();
		similarClusterMap = new HashMap<Cluster, Boolean>();
		scoreSequenceList = new ArrayList<Double>();
		
		minScore = Double.MAX_VALUE;
		maxScore = Double.MIN_VALUE;
		
		entityNumber = globalEntityNumber++;
	}
	
	public Term addFrequency(String id) {
		
		Term term = getTerm(id);
		
		if(term != null) {
			term.addFrequency();
		} else {			
			termList.add(new Term(id));
			term = termList.get(termList.size() - 1);
			term.addFrequency();
		}
		
		return(term);
	}
	
	public void addFrequencyPerSequence() {
		frequency++;
	}
	
	public void addToFrequencyAsNonEntity() {
		frequencyNonEntity++;
	}
	
	public Term getTerm(String id) {
		
		Term term = null;
		
		for(Term candidateTerm : termList) {
			if(candidateTerm.isTermMatching(id)) {
				term = candidateTerm;
				break;
			}
		}
		
		return(term);
	}
	
	//-- to relax matching (2nd Step: LevenshteinDistance)
	//-- Param String id need to be in lowerCase
	public boolean isTermMatching(String id) {
		
		boolean isMatching = false;
		
		if(id.length() >= MINIMUM_RELAX_MATCHING_LENGTH) {
			if(Utils.getLevenshteinDistance(this.id, id) <= EDIT_DISTANCE_ACCEPTABLE) {
				isMatching = true;
			}
		} else if((this.id).equals(id)) {
			isMatching = true;
		}
		
		return(isMatching);
	}
	
	public void calculateTermScore() {
		
		termScore = new Double[termList.size()];
		int scoreIndex = 0;
		
		for(Term term : termList) {
			term.calculateScore(frequency);
			termScore[scoreIndex++] = term.getScore();
		}
	}
	
	public double calculateProbabilityToBeEntity() {
		probabilityToBeEntity = ((double)frequency)/(frequency + frequencyNonEntity);
		
		return(probabilityToBeEntity);
	}
	
	public void sortByAscendentTermScore() {
		QuickSort.sort(termScore, termList);
	}
	
	public void addSequenceScore(double score) {
		
		scoreSequenceList.add(score);
		
		if(score > maxScore)
			maxScore = score;
		
		if(score < minScore)
			minScore = score;
		
		averageScore += score;
	}
	
	public double getSequenceScore(int index) {
		return(scoreSequenceList.get(index));
	}

	public ArrayList<Double> getScoreSequenceList() {
		return scoreSequenceList;
	}
	
	public void linkEntity(Entity entity) {
		
		frequency = entity.getFrequency();
		frequencyNonEntity = entity.getFrequencyNonEntity();
		probabilityToBeEntity = entity.getProbabilityToBeEntity();
		
		termList = entity.getTermList();
		similarEntityMap = entity.getSimilarEntityMap();
		similarClusterMap = entity.getSimilarClusterMap();
		termScore = entity.getTermScore();
		scoreSequenceList = entity.getScoreSequenceList();
	}

	public static int getGlobalEntityNumber() {
		return globalEntityNumber;
	}

	public int getEntityNumber() {
		return entityNumber;
	}

	public String getId() {
		return id;
	}

	public int getFrequency() {
		return frequency;
	}

	public int getFrequencyNonEntity() {
		return frequencyNonEntity;
	}

	public double getProbabilityToBeEntity() {
		return probabilityToBeEntity;
	}

	public ArrayList<Term> getTermList() {
		return termList;
	}
	
	public void setTermList(ArrayList<Term> newTermList) {
		termList = newTermList;
	}
	
	public Entity getSimilarEntity(String entityName) {
		return(similarEntityMap.get(entityName));
	}
	
	public void setSimilarEntity(Entity entity) {
		similarEntityMap.put(entity.getId(), entity);
	}
	
	public HashMap<Cluster, Boolean> getSimilarClusterMap() {
		return(similarClusterMap);
	}
	
	public void setSimilarCluster(Cluster cluster) {
		similarClusterMap.put(cluster, false);
	}
	
	public HashMap<String, Entity> getSimilarEntityMap() {
		return(similarEntityMap);
	}

	public Double[] getTermScore() {
		return termScore;
	}

	public double getMinScore() {
		return minScore;
	}

	public void setMinScore(double minScore) {
		this.minScore = minScore;
	}

	public double getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(double maxScore) {
		this.maxScore = maxScore;
	}

	public double getAverageScore() {
		return averageScore;
	}

	public void setAverageScore(double averageScore) {
		this.averageScore = averageScore;
	}

	public double getStardardDeviationScore() {
		return stardardDeviationScore;
	}

	public void setStardardDeviationScore(double stardardDesviationScore) {
		this.stardardDeviationScore = stardardDesviationScore;
	}

	public static int getMinimumRelaxMatchingLength() {
		return MINIMUM_RELAX_MATCHING_LENGTH;
	}
	
	public String toString() {
		return("Entity: " + id + " freq: " + frequency + " Prob: " + 
				probabilityToBeEntity + " TermListSize: " + termList.size());
	}
}
