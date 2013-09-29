package lbd.FSNER.Filter.Component;

import java.io.Serializable;
import java.util.HashMap;

public class DictionaryFtr4 implements Serializable{

	private static final long serialVersionUID = 1L;
	protected HashMap<String, Object> mEntryMap;

	public DictionaryFtr4() {
		mEntryMap = new HashMap<String, Object>();
	}

	public void addEntry(String pEntry) {
		if(pEntry != null && !pEntry.isEmpty()) {
			mEntryMap.put(pEntry, null);
		}
	}

	public boolean hasEntry(String pCandidateEntry) {
		return mEntryMap.containsKey(pCandidateEntry);
	}
}
