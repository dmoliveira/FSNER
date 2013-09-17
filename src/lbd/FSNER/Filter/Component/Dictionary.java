package lbd.FSNER.Filter.Component;

import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Utils.Symbol;

public class Dictionary {

	protected final String DELIMITER_TERM = Symbol.SPACE;
	protected final String DELIMITER_ENCODED_TERM = Symbol.DOT;
	protected final int PREFIX_SIZE = 5;
	
	protected String dictionaryName;
	
	protected HashMap<String, Integer> termCountIdMap;
	protected HashMap<String, String> termIdMap;
	protected HashMap<String, ArrayList<DictionaryEntry>> dictionaryMap;
	
	public Dictionary(String dictionaryName) {
	
		this.dictionaryName = dictionaryName;
		
		termCountIdMap = new HashMap<String, Integer>();
		termIdMap = new HashMap<String, String>();
		dictionaryMap = new HashMap<String, ArrayList<DictionaryEntry>>();
	}
	
	public void addEntry(String [] entryValueProcessed) {
		
		//-- Add to Term Id Map to aggregate id counter
		addToTermIdMap(entryValueProcessed);
		
		//-- Get encoded Entry
		String encodedEntryValue = getEncodedEntry(entryValueProcessed);
		
		//-- Add to Dictionary Map
		addToDictionaryMap(entryValueProcessed, encodedEntryValue);
	}
	
	protected void addToTermIdMap(String [] entryValueProcessed) {
		
		String term;
		String termPrefix;
		int termPrefixId = 1;
		
		for(int i = 0; i < entryValueProcessed.length; i++) {
			
			term = entryValueProcessed[i];
			if(!term.isEmpty() && !termIdMap.containsKey(term)) {
				
				termPrefix = (term.length() > PREFIX_SIZE)? term.substring(0, PREFIX_SIZE) : term;
				
				if(termCountIdMap.containsKey(termPrefix))
					termPrefixId = termCountIdMap.get(termPrefix) + 1;
				
				termCountIdMap.put(termPrefix, termPrefixId);
				termIdMap.put(term, (termPrefix + termPrefixId));
			}
		}
	}
	
	protected String getEncodedEntry(String [] entryValueProcessed) {
		
		String encodedEntry = Symbol.EMPTY;
		int encodedTermNumber = 0;
		
		for(int i = 0; i < entryValueProcessed.length; i++) {
			if(termIdMap.containsKey(entryValueProcessed[i])) {
				encodedEntry += termIdMap.get(entryValueProcessed[i]) + DELIMITER_ENCODED_TERM;
				encodedTermNumber++;
			}
		}
		
		encodedEntry = (encodedTermNumber == entryValueProcessed.length)? encodedEntry : Symbol.EMPTY;
		
		return(encodedEntry);
	}
	
	protected void addToDictionaryMap(String [] entryValueProcessed, String encodedEntryValue) {
		
		ArrayList<DictionaryEntry> dictionaryEntryList;
		
		for(int i = 0; i < entryValueProcessed.length; i++) {
			
			if(!dictionaryMap.containsKey(entryValueProcessed[i]))
				dictionaryMap.put(entryValueProcessed[i], new ArrayList<DictionaryEntry> ());
			
			dictionaryEntryList = dictionaryMap.get(entryValueProcessed[i]);
			dictionaryEntryList.add(new DictionaryEntry(encodedEntryValue, i, entryValueProcessed.length));
		}
	}
	
	public boolean isTermPartFromEntityEntryInDictionary(SequenceLabel sequenceLabelProcessed, int index) {
		return(getDictionaryEntry(sequenceLabelProcessed, index) != null);
	}
	
	public DictionaryEntry getDictionaryEntry(SequenceLabel sequenceLabelProcessed, int index) {
		
		DictionaryEntry dictionaryEntry = null;
		ArrayList<DictionaryEntry> dictionaryEntryList = dictionaryMap.get(sequenceLabelProcessed.getTerm(index));
		
		String [] candidateEntryValue;
		String candidadeEncodedEntryValue;
		
		if(dictionaryEntryList != null) {
		
			for(DictionaryEntry candidateDictionaryEntry : dictionaryEntryList) {
				candidateEntryValue = generateCandidateEntryValue(candidateDictionaryEntry, sequenceLabelProcessed, index);
				candidadeEncodedEntryValue = getEncodedEntry(candidateEntryValue);
				
				if(candidadeEncodedEntryValue.equals(candidateDictionaryEntry.getEntryValue())) {
					dictionaryEntry = candidateDictionaryEntry;
					break;
				}
			}
		}
		
		return(dictionaryEntry);
	}
	
	protected String [] generateCandidateEntryValue(DictionaryEntry dictionaryEntry, SequenceLabel sequenceLabelProcessed, int index) {
		
		int entrySize = dictionaryEntry.getEntrySize();
		int entryIndex = dictionaryEntry.getTermIndex();
		
		int startOffSet = index - entryIndex;
		int endOffSet = entrySize - entryIndex;
		
		String [] candiadeEntryValue = new String[entrySize];
		
		for(int i = startOffSet; i >= 0 && i < sequenceLabelProcessed.size() && i < endOffSet; i++) {
			candiadeEntryValue[i - startOffSet] = sequenceLabelProcessed.getTerm(i);
		}
		
		return(candiadeEntryValue);
	}
	
	public boolean containsInDictionary(String term) {
		return(dictionaryMap.containsKey(term));
	}
	
	public String getDictionaryName() {
		return(dictionaryName);
	}
}
