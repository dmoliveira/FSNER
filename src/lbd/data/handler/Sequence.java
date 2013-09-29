package lbd.data.handler;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lbd.FSNER.Utils.LabelEncoding;

public class Sequence implements DataSequence {

	private static final long serialVersionUID = 1L;

	// Token and Label list are Final- Same references
	private final List<String> mTokenList;
	private final List<Integer> mLabelList;

	@SuppressWarnings({"unchecked"})
	public Sequence(ArrayList<String> pTokenList, ArrayList<Integer> pLabelList) {
		this.mTokenList = pTokenList;
		this.mLabelList = (List<Integer>) pLabelList.clone();
	}

	@SuppressWarnings("unused")
	public Sequence(String [] tokenList) {
		this.mTokenList =  Arrays.asList(tokenList);

		List<Integer> labelList = new ArrayList<Integer>();
		for(String cToken : tokenList) {
			labelList.add(LabelEncoding.getOutsideLabel());
		}
		this.mLabelList = labelList;
	}

	public Sequence() {
		mTokenList = new ArrayList<String>();
		mLabelList = new ArrayList<Integer>();
	}

	public void addElement(String token, int label) {
		mTokenList.add(token);
		mLabelList.add(label);
	}

	@Override
	public int length() {
		return mLabelList.size();
	}

	@Override
	public void set_y(int i, int label) {
		mLabelList.set(i, label);
	}

	public void set_x(int i, String token) {
		mTokenList.set(i, token);
	}

	@Override
	public Object x(int i) {
		return mTokenList.get(i);
	}

	@Override
	public int y(int i) {
		return mLabelList.get(i);
	}

	public int size() {return(mTokenList.size());}

}
