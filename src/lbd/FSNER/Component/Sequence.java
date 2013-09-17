package lbd.FSNER.Component;

import java.util.ArrayList;

import lbd.FSNER.Filter.Component.Entity;
import lbd.FSNER.Filter.Component.Term;

public class Sequence {
	
	protected ArrayList<Term> termList;
	
	protected static int globalId;
	protected int id;
	
	protected double score;
	protected double similarity;
	
	public Sequence() {
		
		globalId++;
		id = globalId;
		
		termList = new ArrayList<Term>();
	}
	
	public void startNewTermList() {
		this.termList = new ArrayList<Term>();
	}
	
	public int getId() {
		return(id);
	}
	
	public double getScore() {
		return(score);
	}
	
	public void setScore(double score) {
		this.score = score;
	}
	
	public double calculateScore(Entity entity) {
		
		score = 0;
		
		for(Term term : termList) {
			if(!term.getId().equals(entity.getId()))
				score += term.getScore();
		}
		
		return(score);
	}
	
	public double calculateGeneralScore() {
		
		score = 0;
		
		for(Term term : termList)
			score += term.getScore();
		
		return(score);
	}
	
	public Term getTerm(int index) {
		return(termList.get(index));
	}
	
	public void setTerm(Term term, int index) {
		termList.set(index, term);
	}
	
	public void setTerm(Term term) {
		termList.add(term);
	}
	
	public void setAllTerms(ArrayList<Term> allTerms) {
		termList.addAll(allTerms);
	}
	
	public ArrayList<Term> getTermList() {
		return(termList);
	}
	
	public double getSimilarity() {
		return(similarity);
	}
	
	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}
	
	public String[] getTermNameList() {
		
		String [] termNameList = new String[termList.size()];
		
		for(int i = 0; i < termList.size(); i++)
			termNameList[i] = termList.get(i).getId();
		
		return(termNameList);
	}
	
	public String toString() {
		
		String sequence = "";
		
		for(int i = 0; i < termList.size(); i++)
			sequence += termList.get(i) + " ";
		
		return("Seq: " + sequence + " sc(" + score + ")");
	}
}
