package lbd.FSNER.Filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.TermSequence;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;

public class FtrSimpleDictionary extends AbstractFilter{

	private static final long serialVersionUID = 1L;
	
	protected final String DICTIONARY_UNIVERSAL_DIRECTORY = "./samples/data/bcs2010/AutoTagger/Dictionary/";
	
	protected static ArrayList<HashMap<String, LinkedList<TermSequence>>> dictionaryList;
	protected static ArrayList<String> dictionaryNameList;
	protected static HashMap<String, Boolean> dictionaryLoadMap;
	
	protected AbstractDataPreprocessor dataProcessor;

	public FtrSimpleDictionary(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator, AbstractDataPreprocessor dataProcessor) {
		
		super(ClassName.getSingleName(FtrSimpleDictionary.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);
		
		if(dictionaryList == null) {
			dictionaryList = new ArrayList<HashMap<String,LinkedList<TermSequence>>>();
			dictionaryNameList = new ArrayList<String>();
			dictionaryLoadMap = new HashMap<String, Boolean>();
		}
		
		this.dataProcessor = dataProcessor;
	}
	
	@Override
	public void initialize() {
		
		//-- Load Dictionary Name List
		loadDictionaryNameList();
		
		//-- Load all dictionaries from the universal directory
		loadAllDictionary();
	}
	
	protected void loadDictionaryNameList() {
		
		File folder = new File(DICTIONARY_UNIVERSAL_DIRECTORY);
		File[] listOfFiles = folder.listFiles();
		
		String dictionaryFilenameAddress;
		 
		for (int i = 0; i < listOfFiles.length; i++) {
		 
			dictionaryFilenameAddress = DICTIONARY_UNIVERSAL_DIRECTORY + listOfFiles[i].getName();
			
			if (listOfFiles[i].isFile() && !dictionaryLoadMap.containsKey(dictionaryFilenameAddress)) {
				dictionaryNameList.add(dictionaryFilenameAddress);
				dictionaryLoadMap.put(dictionaryFilenameAddress, false);
		        //System.out.println("--" + listOfFiles[i].getName());
		    }
		}		
	}
	
	protected void loadAllDictionary() {
		
		for(String dictionaryFilenameAddress : dictionaryNameList)
			if(!dictionaryLoadMap.get(dictionaryFilenameAddress)) {
				loadDictionary(dictionaryFilenameAddress);
				dictionaryLoadMap.put(dictionaryFilenameAddress, true);
			}
	}
	
	protected void loadDictionary(String dictionaryFilenameAddress) {
		
		try {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(dictionaryFilenameAddress), Parameters.dataEncoding));
			
			dictionaryList.add(new HashMap<String, LinkedList<TermSequence>> ());
			HashMap<String, LinkedList<TermSequence>> dictionary = dictionaryList.get(dictionaryList.size() - 1);
			
			String entry;
			TermSequence entryElement;
			
			while((entry = in.readLine()) != null) {
				
				entryElement = new TermSequence(entry, Symbol.SPACE_CHAR);
				
				for(int i = 0; i < entryElement.size(); i++)
					entryElement.set(dataProcessor.preprocessingTerm(entryElement.get(i), -1).getTerm(), i);
				
				if(!entryElement.toString().isEmpty()) addEntry(dictionary, entryElement, 0);
			}
			
			in.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void addEntry(HashMap<String, LinkedList<TermSequence>> dictionary, TermSequence entry, int index) {
		
		LinkedList<TermSequence> entryList;
		String term = entry.get(index);
		
		if(!dictionary.containsKey(term)) {
			dictionary.put(term, new LinkedList<TermSequence> ());
			dictionary.get(term).add(entry);
		} else {
		
			entryList = dictionary.get(term);
			entryList.add(entry);
		
			/*for(int i = 0; i < entryList.size(); i++) {
				if(entryList.get(i).size() <= entry.size()) {
					entryList.add(i, entry);
					System.out.println(entry);
					break;
				}
			}*/
		}
	}
	
	@Override
	public void loadActionBeforeSequenceSetIteration() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadActionBeforeSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProcessed, int index) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void loadActionAfterSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void adjust(SequenceLabel sequenceProcessedLabel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getSequenceInstanceIdSub(
			SequenceLabel sequenceLabelProcessed, int index) {
		
		String id = Symbol.EMPTY;
		LinkedList<TermSequence> entryList;
		TermSequence sequence = new TermSequence(sequenceLabelProcessed.toArraySequence());
		
		for(int i = 0; i < dictionaryNameList.size(); i++) {

			entryList = dictionaryList.get(i).get(sequenceLabelProcessed.getTerm(index));
			
			if(entryList != null && isTermEntityPart(entryList, sequence, index)) {
				id = "id:" + this.mId + ".dic:" + dictionaryNameList.get(i);
				break;
			}
		}
		
		return (id);
	}
	
	protected boolean isTermEntityPart(LinkedList<TermSequence> entryList,
			TermSequence sequence, int index) {
		
		boolean isTermEntityPart = false;
		
		for(TermSequence entry : entryList) {
			if(isMatching(sequence, entry, index)) {
				isTermEntityPart = true;
				break;
			}
		}
		
		return(isTermEntityPart);
	}
	
	protected boolean isMatching(TermSequence sequence, TermSequence entry, int index) {
		
		boolean isMatching = false;
		
		String sequenceStr = sequence.toString();
		String entryStr = entry.toString();
		
		int indexOfEntryInSequence = -1;
		int convertedIndex = convertTermIndexToStringIndex(sequence, index);
		
		while((indexOfEntryInSequence = sequenceStr.indexOf(entryStr)) != -1) {
			if(indexOfEntryInSequence <= convertedIndex && convertedIndex < (indexOfEntryInSequence + entryStr.length())) {
				isMatching = true;
				break;
			} else {
				sequenceStr.substring(indexOfEntryInSequence + entryStr.length());
				convertedIndex -= indexOfEntryInSequence + entryStr.length();
			}
		}
		
		return(isMatching);
	}
	
	protected int convertTermIndexToStringIndex(TermSequence sequence, int index) {
		
		String sequenceLoaded = Symbol.EMPTY;
		
		for(int i = 0; i < sequence.size() && i < index; i++)
			sequenceLoaded += sequence.get(i) + Symbol.SPACE;
		
		return(sequenceLoaded.length() - 1);
	}

}
