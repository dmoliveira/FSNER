package lbd.Utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class TFIDF implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public class TFIDFElement {
		
		protected String term;
		protected int frequency;
		protected double tFIDF;
		protected double iDF;
		
		public TFIDFElement(String term) {
			this.term = term;
		}
		
		public TFIDFElement(String term, double iDF) {
			this.term = term;
			this.iDF = iDF;
		}
		
		public TFIDFElement(String term, int frequency, double iDF) {
			this.term = term;
			this.frequency = frequency;
			this.iDF = iDF;
		}
		
		public TFIDFElement(String term, double iDF, double tFIDF) {
			this.term = term;
			this.iDF = iDF;
			this.tFIDF = tFIDF;
		}
		
		public TFIDFElement(String term, int frequency, double iDF, double tFIDF) {
			this.term = term;
			this.frequency = frequency;
			this.iDF = iDF;
			this.tFIDF = tFIDF;
		}

		public String getTerm() {
			return term;
		}

		public void setTerm(String term) {
			this.term = term;
		}

		public int getFrequency() {
			return frequency;
		}

		public void setFrequency(int frequency) {
			this.frequency = frequency;
		}

		public double getiDF() {
			return iDF;
		}

		public void setiDF(double iDF) {
			this.iDF = iDF;
		}

		public double gettFIDF() {
			return tFIDF;
		}

		public void settFIDF(double tFIDF) {
			this.tFIDF = tFIDF;
		}		
	}
	
	protected final String ENCODE_USED = "ISO-8859-1";
	
	protected HashMap<String, Frequency> termFrequencyMap;
	protected HashMap<String, Frequency> documentFrequencyMap;
	protected HashMap<String, TFIDFElement> iDFMap;
	
	protected ArrayList<TFIDFElement> tFIDFList;
	
	protected int totalSequenceNumber;

	public TFIDF() {
		
		termFrequencyMap = new HashMap<String, Frequency>();
		documentFrequencyMap = new HashMap<String, Frequency>();
		iDFMap = new HashMap<String, TFIDFElement>();
		
		tFIDFList = new ArrayList<TFIDFElement>();
	}

	public void analyzeStrategy(ArrayList<String> termList, String inputFilenameAddress) {
		
		addEntitiesToFrequencyMap(termList);
		calculateEntitiesFrequency(inputFilenameAddress);
		calculateIDF();
		calculateTFIDF();
		normalizeTFIDF();
		
		QuickSort.sort(getTFIDFArray(), tFIDFList);
	}
	
	private void addEntitiesToFrequencyMap(ArrayList<String> termList) {		
		
		String entity;
			
		for(String term : termList) {
			
			entity = term.toLowerCase();
			
			termFrequencyMap.put(entity, new Frequency(entity, 1));
			documentFrequencyMap.put(entity, new Frequency(entity, 1));
		}
	}
	
	private void calculateEntitiesFrequency(String inputFilenameAddress) {
		
		BufferedReader in;
		ArrayList<String> sequence = null;
		
		try {
			in = new BufferedReader (new InputStreamReader(
					new FileInputStream(inputFilenameAddress), ENCODE_USED));
			
			sequence = Utils.getSequenceInFile(in, true);
			
			while(sequence.size() > 0) {
				
				calculateFrequencyInSequence(sequence);
				totalSequenceNumber++;
				
				sequence = Utils.getSequenceInFile(in, true);
			}
			
			in.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void calculateFrequencyInSequence(ArrayList<String> sequence) {
		
		HashMap<String, Boolean> entitiesFound = new HashMap<String, Boolean> ();
		Frequency termFrequency;
		
		for(String token : sequence) {
			
			if((termFrequency = termFrequencyMap.get(token)) != null) {
				termFrequency.addFrequency();
				
				if(!entitiesFound.containsKey(token)) {
					termFrequency = documentFrequencyMap.get(token);
					termFrequency.addFrequency();
					entitiesFound.put(token, true);
				}
			}
		}		
	}
	
	private void calculateIDF() {
		
		double idf;
		Frequency frequency;
		
		Collection<Frequency> frequencyCollection = documentFrequencyMap.values();
		Iterator<Frequency> itr = frequencyCollection.iterator();
		 
		while((itr.hasNext())) {
			
			frequency = itr.next(); 
			idf = Math.log10(((double)totalSequenceNumber)/frequency.getFrequency());
			
			iDFMap.put(frequency.getId(), new TFIDFElement(frequency.getId(), frequency.getFrequency(), idf));
		}
	}
	
	private void calculateTFIDF() {
		
		String term;
		double idf;
		int tf;
		
		TFIDFElement tFIDFElement;
		Frequency frequency;
		
		Collection<TFIDFElement> frequencyCollection = iDFMap.values();
		Iterator<TFIDFElement> itr = frequencyCollection.iterator();
		
		tFIDFList = new ArrayList<TFIDFElement>();
		 
		while((itr.hasNext())) {
			
			tFIDFElement = itr.next();
			
			term = tFIDFElement.getTerm();
			frequency = termFrequencyMap.get(term);
			
			tf = frequency.getFrequency();
			idf = tFIDFElement.getiDF();
			
			tFIDFElement.settFIDF(tf * idf);
			tFIDFList.add(tFIDFElement);
		}
		
	}
	
	private void normalizeTFIDF() {
		normalizeTFIDFPhase1();
		normalizeTFIDFPhase2();
	}
	
	private void normalizeTFIDFPhase1() {
		
		double maxValue = -1;
		
		for(TFIDFElement tFIDF : tFIDFList) {
			if(maxValue < tFIDF.gettFIDF())
				maxValue = tFIDF.gettFIDF();
		}
		
		for(TFIDFElement tFIDF : tFIDFList)
			tFIDF.settFIDF(tFIDF.gettFIDF()/maxValue);
		
	}
	
	private void normalizeTFIDFPhase2() {
		
		double normalizedFactor = 0;
		
		for(TFIDFElement tFIDF : tFIDFList)
				normalizedFactor += Math.pow(tFIDF.gettFIDF(), 2);
		
		normalizedFactor = Math.sqrt(normalizedFactor);
		
		for(TFIDFElement tFIDF : tFIDFList)
			tFIDF.settFIDF(tFIDF.gettFIDF()/normalizedFactor);
	}
	
	private Double [] getTFIDFArray() {
		
		Double [] tFIDTArray = new Double [tFIDFList.size()];
		int index = -1;
		
		for(TFIDFElement tFIDF : tFIDFList)
			tFIDTArray[++index] = tFIDF.gettFIDF();
		
		return(tFIDTArray);
	}

	public HashMap<String, Frequency> getTermFrequencyMap() {
		return termFrequencyMap;
	}

	public void setTermFrequencyMap(HashMap<String, Frequency> termFrequencyMap) {
		this.termFrequencyMap = termFrequencyMap;
	}

	public HashMap<String, Frequency> getDocumentFrequencyMap() {
		return documentFrequencyMap;
	}

	public void setDocumentFrequencyMap(
			HashMap<String, Frequency> documentFrequencyMap) {
		this.documentFrequencyMap = documentFrequencyMap;
	}

	public HashMap<String, TFIDFElement> getiDFMap() {
		return iDFMap;
	}

	public void setiDFMap(HashMap<String, TFIDFElement> iDFMap) {
		this.iDFMap = iDFMap;
	}

	public ArrayList<TFIDFElement> gettFIDFList() {
		return tFIDFList;
	}

	public void settFIDFList(ArrayList<TFIDFElement> tFIDFList) {
		this.tFIDFList = tFIDFList;
	}

	public int getTotalSequenceNumber() {
		return totalSequenceNumber;
	}

	public void setTotalSequenceNumber(int totalSequenceNumber) {
		this.totalSequenceNumber = totalSequenceNumber;
	}
}
