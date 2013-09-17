package lbd.NewModels.Context;

import iitb.CRF.DataSequence;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.Utils.Utils;

public class ContextAnalysisRelaxed {
	
	protected static int id;
	
	protected HashMap<String, String> contextMap;
	protected HashMap<String, Integer> contextIdMap;
	
	protected transient final String DELIMITER_TERM = "|";
	protected transient final String DELIMITER_ELEMENT = " ";
	protected transient final int WINDOW_SIZE = 3;
	
	protected transient DataSequence sequence;
	protected transient String [] sequenceList;
	
	public ContextAnalysisRelaxed() {
		
		contextMap = new HashMap<String, String>();
		contextIdMap = new HashMap<String, Integer>();
		
		id = 0;
	}
	
	public void addContext(DataSequence data) {
		
		proccessSequence(data);
		
		for(int i = 0; i < data.length(); i++) {
			addAllPrefixContexts(sequenceList, i);
			addAllSuffixContexts(sequenceList, i);
		}
	}
	
	public void addAllPrefixContexts(String [] sequence, int pos) {
		
		String [] termList = getPrefixContextTermList(sequence, pos);
		
		if(termList != null)
			addToContextMap(termList);
	}
	
	public void addAllSuffixContexts(String [] sequence, int pos) {
		
		String [] termList = getSuffixContextTermList(sequence, pos);
		
		if(termList != null)
			addToContextMap(termList);
	}
	
	public String [] getPrefixContextTermList(String [] sequence, int pos) {
		
		String [] termList = null;
		
		if(pos >= WINDOW_SIZE) {
			termList = new String[WINDOW_SIZE];
			
			for(int i = pos - WINDOW_SIZE; i > -1 && i < pos; i++) {
				termList[i - (pos - WINDOW_SIZE)] = sequence[i];
				//System.out.print(termList[i - (pos - WINDOW_SIZE)] + " ");
			}
			//System.out.println();
		}
		
		return(termList);
	}
	
	public String [] getSuffixContextTermList(String [] sequence, int pos) {
		
		String [] termList = null;
		
		if(pos + WINDOW_SIZE < sequence.length) {
			termList = new String[WINDOW_SIZE];
			
			for(int i = pos + 1; i < sequence.length && i <= pos + WINDOW_SIZE; i++) {
				termList[i - (pos + 1)] = sequence[i];
				//System.out.print(termList[i - (pos + 1)] + " ");
			}
			//System.out.println();
		}
		
		return(termList);
	}
	
	protected void addToContextMap(String [] termList) {
		
		contextMap.put(termList[0] + DELIMITER_TERM + termList[1], termList[2]);
		contextMap.put(termList[1] + DELIMITER_TERM + termList[2], termList[0]);
		contextMap.put(termList[0] + DELIMITER_TERM + termList[2], termList[1]);
		contextMap.put(termList[0] + DELIMITER_TERM + termList[1] + DELIMITER_TERM + termList[2], "");
		
		contextIdMap.put(termList[0] + DELIMITER_TERM + termList[1], ++id);
		contextIdMap.put(termList[1] + DELIMITER_TERM + termList[2], ++id);
		contextIdMap.put(termList[0] + DELIMITER_TERM + termList[2], ++id);
		contextIdMap.put(termList[0] + DELIMITER_TERM + termList[1] + DELIMITER_TERM + termList[2], ++id);
	}
	
	public int getContextId(String [] sequence, int pos) {
		
		int contextId = -1;
		
		String [] prefixTermList = getPrefixContextTermList(sequence, pos);
		String [] suffixTermList = getSuffixContextTermList(sequence, pos);
		
		ArrayList<String> contextList = new ArrayList<String>();
		
		if(prefixTermList != null) {
		
			contextList.add(prefixTermList[0] + DELIMITER_TERM + prefixTermList[1] + prefixTermList[2]);
			
			contextList.add(prefixTermList[1] + DELIMITER_TERM + prefixTermList[2]);
			contextList.add(prefixTermList[0] + DELIMITER_TERM + prefixTermList[2]);
			contextList.add(prefixTermList[0] + DELIMITER_TERM + prefixTermList[1]);
		}
		
		if(suffixTermList != null) {
		
			contextList.add(suffixTermList[0] + DELIMITER_TERM + suffixTermList[1] + suffixTermList[2]);
			
			contextList.add(suffixTermList[1] + DELIMITER_TERM + suffixTermList[2]);
			contextList.add(suffixTermList[0] + DELIMITER_TERM + suffixTermList[2]);
			contextList.add(suffixTermList[0] + DELIMITER_TERM + suffixTermList[1]);
		}
		
		for(String context : contextList) {
			if(contextIdMap.containsKey(context)) {
				contextId = contextIdMap.get(context);
				break;
			}
		}
		
		return(contextId);
	}
	
	protected void proccessSequence(DataSequence data) {
		
		if(data != sequence) {
			sequence = data;
			sequenceList = Utils.convertSequenceToLowerCase(data, data.length());
		}
	}
	
	/**************************************************************************
	 * 
	 * Auxiliary Methods
	 * 
	 **************************************************************************/
	
	public void readContextAnalysisRelaxedObject(String filename) throws IOException, ClassNotFoundException {
    	
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "ISO-8859-1"));
		
		String line;
		String [] lineElement;
		
		int indexKey = 0;;
		int indexValue = 1;
		
		contextIdMap = new HashMap<String, Integer>();
		
		while((line = in.readLine()) != null && !line.isEmpty()) {
		
			lineElement = line.split(DELIMITER_ELEMENT);
			
			contextIdMap.put(lineElement[indexKey], Integer.parseInt(lineElement[indexValue]));
		}
		
		in.close();
    }
    
    public void writeContextAnalysisRelaxedObject(String filename) throws IOException {
    	
		Writer out = new OutputStreamWriter(new FileOutputStream(filename), "ISO-8859-1");
		
		
		Iterator<Entry<String,Integer>> iteCxtMap = contextIdMap.entrySet().iterator();
		Entry<String,Integer> entry;
		
		while(iteCxtMap.hasNext()) {
			
			entry = iteCxtMap.next();
			out.write(entry.getKey() + DELIMITER_ELEMENT + entry.getValue());
			
			out.write("\n");
		}
		
		out.flush();
		out.close();
    }
	

}
