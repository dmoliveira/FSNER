package lbd.data.handler;

public interface IDataIterator {
	void startScan();
	boolean hasNext();
	ISequence next();
};
