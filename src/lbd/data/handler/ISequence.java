package lbd.data.handler;

import java.io.Serializable;

public interface ISequence extends Serializable {
	public String getToken(int pIndex);
	public int getLabel(int pIndex);
	public void setLabel(int pIndex, int pLabel);
	public int length();
	public void add(String pToken, int pLabel);
	public ISequence clone();
	public String[] toArraySequence();
}
