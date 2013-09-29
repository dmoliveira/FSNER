package lbd.data.handler;

public interface DataIter {
	void startScan();
	boolean hasNext();
	DataSequence next();
};
