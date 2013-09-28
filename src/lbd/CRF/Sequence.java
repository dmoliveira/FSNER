package lbd.CRF;

import iitb.CRF.DataSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lbd.FSNER.Utils.LabelEncoding;

public class Sequence implements DataSequence {

	private static final long serialVersionUID = 1L;

	private List<String> tokenList;
	private List<Integer> labelList;

	@SuppressWarnings({"unchecked"})
	public Sequence(ArrayList<String> tokenList, ArrayList<Integer> labelList) {
		this.tokenList = (List<String>) tokenList.clone();
		this.labelList = (List<Integer>) labelList.clone();
	}

	@SuppressWarnings("unused")
	public Sequence(String [] tokenList) {
		this.tokenList =  Arrays.asList(tokenList);

		List<Integer> labelList = new ArrayList<Integer>();
		for(String cToken : tokenList) {
			labelList.add(LabelEncoding.getOutsideLabel());
		}
		this.labelList = labelList;
	}

	public Sequence() {
		tokenList = new ArrayList<String>();
		labelList = new ArrayList<Integer>();
	}

	public void addElement(String token, int label) {
		tokenList.add(token);
		labelList.add(label);
	}

	@Override //Quantidade de r�tulos ou tokens
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
