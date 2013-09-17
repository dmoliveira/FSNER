package lbd.AutoTagger;

import iitb.CRF.DataSequence;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.LabelMap;
import lbd.CRF.SequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;
import lbd.Utils.Utils;

public class SelectEntitiesByRelevantContext {
	
	protected ArrayList<ArrayList<String>> contextList;
	protected HashMap<String, Boolean> candidateEntity;
	
	protected final String ENCODE_USED = "ISO-8859-1";
	protected final double THRESHOULD = 0.5;
	
	public SelectEntitiesByRelevantContext() {
		
		contextList = new ArrayList<ArrayList<String>>();
		candidateEntity = new HashMap<String, Boolean>();
	}
	
	public void runSelection(String sourceInputFile, String fileToLabel, String dictionaryFilenameAddress) {
		
		
		//-- Load Candidates
		loadCandidatesEntities(dictionaryFilenameAddress);
		
		//-- Load Context
		loadContext(sourceInputFile);
		
		//-- Analyze file to label
	}
	
	protected void loadCandidatesEntities (String dictionaryAddress) {
	
		String line;
		
		try {
			BufferedReader in = new BufferedReader (new InputStreamReader(
					new FileInputStream(dictionaryAddress)));
			
			while((line = in.readLine()) != null)
				candidateEntity.put(line.toLowerCase(), false);
			
			in.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void loadContext(String sourceInputFile) {
		
		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(sourceInputFile,
				FileType.TRAINING, false);
		DataSequence sequence;
		
		for (inputSequenceSet.startScan(); inputSequenceSet.hasNext();) {
			
			sequence = inputSequenceSet.next();
			
			for(int i = 0; i < sequence.length(); i++) {
				if(sequence.y(i) == LabelMap.getLabelIndexPOSTagPTBR("Beginning") ||
						sequence.y(i) == LabelMap.getLabelIndexPOSTagPTBR("UnitToken")) {
					addToContext(sequence);
					break;
				}
			}
		}
	}
	
	protected void addToContext(DataSequence sequence) {
		
		ArrayList<String> context = new ArrayList<String>();
		
		for(int i = 0; i < sequence.length(); i++) {
			if(sequence.y(i) == LabelMap.getLabelIndexPOSTagPTBR("Outside"))
				context.add(((String)sequence.x(i)).toLowerCase());
		}
		
		if(context.size() > 0)
			contextList.add(context);
	}
	
	protected void analyzeCandidatesEntities(String inputFilenameAddress, String sourceInputFile) {
				
		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(sourceInputFile,
				FileType.TRAINING, false);
		
		DataSequence sequence;
		String [] sequenceList;
		
		for (inputSequenceSet.startScan(); inputSequenceSet.hasNext();) {
			
			sequence = inputSequenceSet.next();
			sequenceList = Utils.convertSequenceToLowerCase(sequence, sequence.length());
			
			for(int i = 0; i < sequence.length(); i++) {
				
				if(candidateEntity.containsKey(sequenceList[i]))
					if(!candidateEntity.get(sequenceList[i]) && isCandidateEnable(sequenceList, sequence)) {
						candidateEntity.put(sequenceList[i], true);						}					
			}
		}
	}
	
	protected boolean isCandidateEnable(String [] sequenceList, DataSequence sequence) {
		
		boolean isCandidateEnable = false;
		int correctHit;
		
		for(ArrayList<String> context : contextList) {
			
			correctHit = 0;
			
			for(String term : context) {
				for(int i = 0; i < sequenceList.length; i++) {
					
					if(sequence.y(i) == LabelMap.getLabelIndexPOSTagPTBR("Outside") &&
							sequenceList[i].equals(term)) {
						correctHit++;
						break;
					}
					
					if(((double)correctHit)/context.size() >= THRESHOULD) {
						isCandidateEnable = true;
						return(isCandidateEnable);
					}
				}
			}			
		}
		
		return(isCandidateEnable);
	}
	
	
	
	protected void writeSequence() {
		
	}
	
	protected String generateOutput(String inputFilenameAddress) {
		String outputFilenameAddress = "";
		
		return(outputFilenameAddress);
	}
}
