package lbd.FSNER.Filter.Component;

public class DictionaryEntry {
	
	protected String entryValue;
	protected int termIndex;
	protected int entrySize;
	
	public DictionaryEntry(String entryValue, int termIndex, int entrySize) {
		
		this.entryValue = entryValue;
		this.termIndex = termIndex;
		this.entrySize = entrySize;
	}

	public String getEntryValue() {
		return entryValue;
	}

	public void setEntryValue(String entryValue) {
		this.entryValue = entryValue;
	}

	public int getTermIndex() {
		return termIndex;
	}

	public void setTermIndex(int termIndex) {
		this.termIndex = termIndex;
	}

	public int getEntrySize() {
		return entrySize;
	}

	public void setEntrySize(int entrySize) {
		this.entrySize = entrySize;
	}

}
