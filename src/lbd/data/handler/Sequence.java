package lbd.data.handler;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lbd.FSNER.Configuration.Parameters;

public class Sequence implements ISequence {

	private static final long serialVersionUID = 1L;

	private final List<String> mTokenList;
	private final List<Integer> mLabelList;

	public Sequence() {
		mTokenList = new ArrayList<String>();
		mLabelList = new ArrayList<Integer>();
	}

	public Sequence(String [] pTokenList) {

		mTokenList =  Arrays.asList(pTokenList);
		mLabelList = new ArrayList<Integer>();

		for(@SuppressWarnings("unused") String cToken : pTokenList) {
			mLabelList.add(Parameters.DataHandler.mLabelEncoding.getOutsideLabel().ordinal());
		}
	}

	@Override
	public void add(String pToken, int pLabel) {
		mTokenList.add(pToken);
		mLabelList.add(pLabel);
	}

	@Override
	public String getToken(int pIndex) {
		return mTokenList.get(pIndex);
	}

	@Override
	public int getLabel(int pIndex) {
		return mLabelList.get(pIndex);
	}

	@Override
	public void setLabel(int pIndex, int pLabel) {
		mLabelList.set(pIndex, pLabel);
	}

	@Override
	public int length() {
		return mLabelList.size();
	}

	@Override
	public ISequence clone() {
		ISequence vClonedSequence = new Sequence();
		for(int i = 0; i < length(); i++) {
			vClonedSequence.add(getToken(i),getLabel(i));
		}

		return vClonedSequence;
	}

	@Override
	public String[] toArraySequence() {
		return(mTokenList.toArray(new String[mTokenList.size()]));
	}
}
