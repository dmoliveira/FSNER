package iitb.Model;
import java.util.*;
import java.io.*;

import lbd.Model.ContextToken;
import iitb.CRF.*;

/**
 *
 * This is created by FeatureGenTypes and is available for any
 * featureTypes class to use. What it does is provide you counts of
 * the number of times a word occurs in a state.
 * 
 * @author Sunita Sarawagi
 * @author Diego Marinho de Oliveira [@DMZ] (Edited to enable context support'11)
 * */


public class WordsInTrainDMZVersion implements Serializable {
	
	public class HEntry implements Serializable {
		
		private int index; //which is the index of the word in above arrays -- cntsArray, cntsOverAllWords
		private int cnt; //which is the count of the word
		private int stateArray[]; //which stores the count of the word in each state
		
		public HEntry(int v) {
		    index = v;
		    cnt = 0;
		}
		
		public HEntry(int v, int numStates) {
		    index = v;
		    cnt = 0;
		    stateArray = new int[numStates];
		}
    };
    private Hashtable dictionary; //This is a HashMap indexed by word to HEntry
    private int cntsArray[][]; //This is a HashMap indexed by word to HEntry
    private int cntsOverAllWords[]; //This array stores for each state a total
    								//count of all the words appearing in that state as observed during training
    private int allTotal; //This is the total count of all words

    transient TokenGenerator tokenGenerator;
    public WordsInTrainDMZVersion() {
	this(new TokenGenerator());
    }
    public WordsInTrainDMZVersion(TokenGenerator tokenGen) {
	tokenGenerator = tokenGen;
	dictionary = new Hashtable();
    }
    int[] getStateArray(int pos) {
	return cntsArray[pos];
    }
    public int getIndex(Object w) {
	return ((HEntry)(dictionary.get(((String)w).toLowerCase()))).index; //@DMZDebug
    }
    boolean inDictionary(Object w) {
	return (dictionary.get(((String)w).toLowerCase()) != null);//@DMZDebug
    }
    public int count(Object w) {
	HEntry entry = (HEntry)dictionary.get(((String)w).toLowerCase()); //@DMZDebug
	return ((entry != null)?entry.cnt:0);
    }
    public int count(int wordPos, int state) {
	return getStateArray(wordPos)[state];
    }
    public int count(int state) {
	return cntsOverAllWords[state];
    }
    public int totalCount() {return allTotal;}

    public int dictionaryLength() {return dictionary.size();}

    public int nextStateWithWord(Object w, int prev) {
	if (!inDictionary(((String)w).toLowerCase()))//@DMZDebug
	    return -1;
	int pos = getIndex(((String)w).toLowerCase());//@DMZDebug
	return nextStateWithWord(pos,prev);
    }
    public int nextStateWithWord(int pos, int prev) {
	int k = 0;
	if (prev >= 0)
	    k = prev + 1;
	for (; k < getStateArray(pos).length; k++) {
	    if (getStateArray(pos)[k] > 0)
		return k;
	}
	return -1;
    }
    public Enumeration allWords() {return dictionary.keys();}
    private void addDictElem(Object x, int y) {
	HEntry index = (HEntry)dictionary.get(((String)x).toLowerCase());//@DMZDebug
	if (index == null) {
	    index = new HEntry(dictionary.size());
	    dictionary.put(((String)x).toLowerCase(), index);//@DMZDebug
	}
	index.cnt++;
    }
    
    private void addDictElem(Object x, int y, int nelems) {
	HEntry index = (HEntry)dictionary.get(((String)x).toLowerCase());//@DMZDebug
	if (index == null) {
	    index = new HEntry(dictionary.size(),nelems);
	    dictionary.put(((String)x).toLowerCase(), index);//@DMZDebug
	}
	index.cnt++;
	index.stateArray[y]++;
    }
    
    void setAggregateCnts(int numStates) {
	cntsOverAllWords = new int[numStates];
	for (int i = 0; i < numStates; i++) {
	    cntsOverAllWords[i] = 0;
	    for (int m = 0; m < cntsArray.length; m++)
		cntsOverAllWords[i] += getStateArray(m)[i];
	    allTotal += cntsOverAllWords[i];
	}
    }
    public void train(DataIter trainData, int numStates) {
	for (trainData.startScan(); trainData.hasNext();) {
	    DataSequence seq = trainData.next();
	    for (int l = 0; l < seq.length(); l++) {
		for (tokenGenerator.startScan(seq.x(l)); tokenGenerator.hasNext();) {
		    addDictElem(tokenGenerator.next(),seq.y(l),numStates);
		}
	    }
	}
	cntsArray = new int[dictionary.size()][0];
	for (Enumeration e = dictionary.keys() ; e.hasMoreElements() ;) {
	    Object key = e.nextElement();
	    HEntry entry = (HEntry)dictionary.get(((String)key).toLowerCase());//@DMZDebug
	    cntsArray[entry.index] = entry.stateArray;
	}	
	setAggregateCnts(numStates);
    }
    public void read(BufferedReader in, int numStates) throws IOException {
	int dictLen = Integer.parseInt(in.readLine());
	cntsArray = new int[dictLen][numStates];
	String line;
	for(int l = 0; (l < dictLen) && ((line=in.readLine())!=null); l++) {
	    StringTokenizer entry = new StringTokenizer(line," ");
	    String key = entry.nextToken();
	    int pos = Integer.parseInt(entry.nextToken());
	    HEntry hEntry = new HEntry(pos);
	    dictionary.put(((String)key).toLowerCase(),hEntry);//@DMZDebug
	    while (entry.hasMoreTokens()) {
		StringTokenizer scp = new StringTokenizer(entry.nextToken(),":");
		int state = Integer.parseInt(scp.nextToken());
		int cnt = Integer.parseInt(scp.nextToken());
		getStateArray(pos)[state] = cnt;
		hEntry.cnt += cnt;
	    }
	}
	setAggregateCnts(numStates);
    }
    public void write(PrintWriter out) throws IOException {
	out.println(dictionary.size());
	for (Enumeration e = dictionary.keys() ; e.hasMoreElements() ;) {
	    Object key = e.nextElement();
	    int pos = getIndex(((String)key).toLowerCase());//@DMZDebug
	    out.print(key + " " + pos);
	    for (int s = nextStateWithWord(pos,-1); s != -1; 
		 s = nextStateWithWord(pos,s)) {
		out.print(" " + s + ":" + getStateArray(pos)[s]);
	    }
	    out.println("");
	}	
    }
};
