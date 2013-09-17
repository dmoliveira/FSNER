package lbd.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class Token implements Serializable {

	private String value;
	private double weight;
	private ArrayList<Integer> stateList;
	private int state;
	
	public Token(String value, double weight, int state) {
		this.value = value;
		this.weight = weight;
		this.state = state;
		 stateList = new ArrayList<Integer> ();
	}
	
	public Token(String value, int state) {
		this.value = value;
		this.weight = 1;
		this.state = state;
		stateList = new ArrayList<Integer> ();
	}
	
	public Token(String value, double weight) {
		this.value = value;
		this.weight = weight;
		stateList = new ArrayList<Integer> ();
	}
	
	public Token(String value) {
		this.value = value;
		weight = 1;
		stateList = new ArrayList<Integer> ();
	}
	
	public Token() {
		weight = 1;
		stateList = new ArrayList<Integer> ();
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public int getStateList(int index) {
		return(stateList.get(index));
	}
	
	public void setStateList(int state) {
		stateList.add(state);
	}
	
	public boolean hasState(int state) {
		return(stateList.contains(state));
	}
	
	public int sizeStateList() {
		return(stateList.size());
	}
	
	@Override
	public String toString() {
		String tokenStatus = "Token: " + value;
		tokenStatus += ", Weight: " + weight + " State: " + state;
		
		return(tokenStatus);
	}
	
}
