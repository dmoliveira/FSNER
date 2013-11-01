package lbd.Utils;

public class Frequency<T> {

	private T mId;
	private int mFrequency;

	public Frequency(T id, int pFrequency) {
		this.mId = id;
		this.mFrequency = pFrequency;
	}

	public Frequency(T pId) {
		this.mId = pId;
		mFrequency = 0;
	}

	public void addFrequency() {
		mFrequency++;
	}

	public T getId() {
		return mId;
	}

	public void setId(T pId) {
		this.mId = pId;
	}

	public int getFrequency() {
		return mFrequency;
	}

	public void setFrequency(int pFequency) {
		this.mFrequency = pFequency;
	}

}
