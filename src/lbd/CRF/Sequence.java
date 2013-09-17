package lbd.CRF;

import java.util.ArrayList;

import iitb.CRF.DataSequence;

public class Sequence implements DataSequence {

	private ArrayList<String> tokenList;
	private ArrayList<Integer> labelList;
	
	public Sequence(ArrayList<String> tokenList, ArrayList<Integer> labelList) {
		this.tokenList = (ArrayList<String>) tokenList.clone();
		this.labelList = (ArrayList<Integer>) labelList.clone();
	}
	
	public Sequence() {
		tokenList = new ArrayList<String>();
		labelList = new ArrayList<Integer>();
	}
	
	public void addElement(String token, int label) {
		tokenList.add(token);
		labelList.add(label);
	}
	
	@Override //Quantidade de rótulos ou tokens
	public int length() { 
		return labelList.size();
	}

	@Override
	public void set_y(int i, int label) {
		labelList.set(i, label);
	}
	
	//-- Add by Oliveira, D. M.
	public void set_x(int i, String token) {
		tokenList.set(i, token);
	}

	@Override
	public Object x(int i) {
		return tokenList.get(i);
	}

	@Override
	public int y(int i) {
		return labelList.get(i);
	}
	
	public int size() {return(tokenList.size());}

}
