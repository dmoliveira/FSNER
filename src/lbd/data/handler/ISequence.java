package lbd.data.handler;

import java.io.Serializable;

public interface ISequence extends Serializable {
	public Object getToken(int pIndex);
	public int getLabel(int pIndex);
	public void setLabel(int pIndex, int pLabel);
	public int length();
}
