package lbd.FSNER.Component;

import java.util.ArrayList;

import lbd.FSNER.Utils.Symbol;

public class TermSequence {
	
	protected ArrayList<String> termList;
	protected String sequence;
	
	public TermSequence(String termSequence, char delimiter) {
		this();
		add(termSequence, delimiter);
	}
	
	public TermSequence(ArrayList<String> termList) {
		this();
		add(termList);
	}
	
	public TermSequence(String [] termList) {
		this();
		add(termList);
	}
	
	public TermSequence(String term) {
		this();
		add(term);
	}
	
	public TermSequence() {
		termList = new ArrayList<String> ();
		sequence = Symbol.EMPTY;
	}
	
	public void add(ArrayList<String> termList) {
		for(String term : termList) {
			this.termList.add(term);
			sequence += term + Symbol.SPACE;
		}
	}
	
	public void add(String [] termList) {
		
		for(int i = 0; i < termList.length; i++) {
			this.termList.add(termList[i]);
			sequence += termList[i] + Symbol.SPACE;
		}
	}
	
	public void add(String termSequence, char delimiter) {
		
		String term = Symbol.EMPTY;
		
		for(int i = 0; i < termSequence.length(); i++) {
			if(termSequence.charAt(i)  != delimiter) {
				term += termSequence.charAt(i); 
			} else {
				termList.add(term);
				sequence += term + Symbol.SPACE;
				term = Symbol.EMPTY;
			}
		}
			
	}
	
	public void add(String term) {
		termList.add(term);
		sequence += term + Symbol.SPACE;
	}
	
	public String get(int i) {
		return(termList.get(i));
	}
	
	public void set(String term, int index) {
		termList.set(index, term);
	}
	
	public int size() {
		return(termList.size());
	}
	
	public ArrayList<String> getTermList() {
		return(termList);
	}
	
	public String[] toArray() {
		return((String[])termList.toArray(new String[termList.size()]));
	}
	
	public void clear() {
		termList.clear();
	}
	
	public int sequenceSize() {
		return(sequence.length());
	}
	
	public String getSequence() {
		return(sequence);
	}
	
	public String toString() {		
		return(sequence);
	}

}
