package lbd.FSNER.Filter.Component;

import java.io.Serializable;

import lbd.Utils.Utils;

public class Term implements Serializable{
		
	private static final long serialVersionUID = 1L;

	protected String id;
	
	protected int frequency;
	protected int frequencyPerSequence;
	protected double score;
	protected int sequenceTotalNumber;
	
	protected int frequencyLeftPosition;
	protected int frequencyRightPosition;
	
	protected int averageLeftPosition;
	protected int averageRightPosition;
	protected double [] leftPositionScore;
	protected double [] rightPositionScore;
	
	public static final int MINIMUM_RELAX_MATCHING_LENGTH = 4;//4-3
	public static int EDIT_DISTANCE_ACCEPTABLE = 1;//2
	
	public Term(String id) {
		
		//-- to relax the matching (1st Step: id lower case)
		this.id = id;//id.toLowerCase(); (Original)
		
		leftPositionScore = new double [8];//0 is empty, [1..7]
		rightPositionScore = new double [8];//0 is empty, [1..7]
		
		frequency = 0;
		frequencyPerSequence = 0;
		score = 0;
	}
	
	public void addFrequency() {
		frequency++;
	}
	
	public void addFrequencyPerSequence() {
		frequencyPerSequence++;
	}
	
	public double calculateScore(int sequenceTotalNumber) {
		
		this.sequenceTotalNumber = sequenceTotalNumber;
		
		score = ((double)frequencyPerSequence)/sequenceTotalNumber;
		
		return(score);
	}
	
	public double calculateScore() {
		
		score = ((double)frequencyPerSequence)/sequenceTotalNumber;
		
		return(score);
	}
	
	//-- to relax matching (2nd Step: LevenshteinDistance)
	//-- Param String id need to be in lowerCase
	public boolean isTermMatching(String id) {
		
		boolean isMatching = false;
		
		if(id.length() >= MINIMUM_RELAX_MATCHING_LENGTH) {
			
			if(id.length() == 4) {
				if(Utils.getLevenshteinDistance(this.id, id) <= EDIT_DISTANCE_ACCEPTABLE) {
					isMatching = true;
				}
			} else {
				if(Utils.getLevenshteinDistance(this.id, id) <= EDIT_DISTANCE_ACCEPTABLE+1) {
					isMatching = true;
				}
			}
			
		} else if((this.id).equals(id)) {
			isMatching = true;
		}
		
		return(isMatching);
	}
	
	public Term clone(Term target) {
		
		Term clone = new Term(target.getId());
		
		clone.setFrequency(target.getFrequency());
		clone.setFrequencyPerSequence(target.getFrequencyPerSequence());
		clone.setScore(target.getScore());
		clone.setSequenceTotalNumber(target.getSequenceTotalNumber());
		
		return(clone);
	}

	public String getId() {
		return id;
	}

	public int getFrequency() {
		return frequency;
	}

	public int getFrequencyPerSequence() {
		return frequencyPerSequence;
	}

	public double getScore() {
		return score;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public void setFrequencyPerSequence(int frequencyPerSequence) {
		this.frequencyPerSequence = frequencyPerSequence;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public int getSequenceTotalNumber() {
		return sequenceTotalNumber;
	}

	public void setSequenceTotalNumber(int sequenceTotalNumber) {
		this.sequenceTotalNumber = sequenceTotalNumber;
	}
	
	public int getFrequencyLeftPosition() {
		return frequencyLeftPosition;
	}

	public void setFrequencyLeftPosition(int frequencyLeftPosition) {
		this.frequencyLeftPosition = frequencyLeftPosition;
	}
	
	public void addFrequencyLeftPosition() {
		frequencyLeftPosition++;
	}

	public int getFrequencyRightPosition() {
		return frequencyRightPosition;
	}

	public void setFrequencyRightPosition(int frequencyRightPosition) {
		this.frequencyRightPosition = frequencyRightPosition;
	}
	
	public void addFrequencyRightPosition() {
		frequencyRightPosition++;
	}

	public int getAverageLeftPosition() {
		return(averageLeftPosition);
	}
	
	public void setAverageLeftPosition(int averageLeftPosition) {
		this.averageLeftPosition = averageLeftPosition;
	}
	
	public int getAverageRightPosition() {
		return(averageRightPosition);
	}
	
	public void setAverageRightPosition(int averageRightPosition) {
		this.averageRightPosition = averageRightPosition;
	}
	
	public double getLeftPositionScore(int index) {
		return(leftPositionScore[index]);
	}
	
	public double getRightPositionScore(int index) {
		return(rightPositionScore[index]);
	}
	
	public void addLeftPosition(int leftPosition) {
		averageLeftPosition += leftPosition;
		if(leftPosition < leftPositionScore.length)leftPositionScore[leftPosition]++;
		addFrequencyLeftPosition();
	}
	
	public void addRightPosition(int rightPosition) {
		averageRightPosition += rightPosition;
		if(rightPosition < rightPositionScore.length)rightPositionScore[rightPosition]++;
		addFrequencyRightPosition();
	}
	
	public void calculateAveragePosition() {
		averageLeftPosition = (int) Math.ceil(((double)averageLeftPosition)/frequencyLeftPosition);
		averageRightPosition = (int) Math.ceil(((double)averageRightPosition)/frequencyRightPosition);
		
		for(int i = 0; i < leftPositionScore.length; i++) {
			leftPositionScore[i] /= (double)frequencyLeftPosition;
			rightPositionScore[i] /= (double)frequencyRightPosition;
		}
	}

	public static int getEDIT_DISTANCE_ACCEPTABLE() {
		return EDIT_DISTANCE_ACCEPTABLE;
	}

	public static void setEDIT_DISTANCE_ACCEPTABLE(int editDistanceAcceptable) {
		EDIT_DISTANCE_ACCEPTABLE = editDistanceAcceptable;
	}	
	
	public String toString() {
		return("Term: " + id + " Freq: " + frequency + " FreqPSeq: " + sequenceTotalNumber + " sc(" + score + ")");
	}
}
