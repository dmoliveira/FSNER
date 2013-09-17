package lbd.AutoTagger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;

import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.SequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;
import lbd.Model.ContextToken;
import lbd.Model.SupportContext;

public class SelectEntity {
	
	protected HashMap<String, Boolean> dictionaryMap;
	protected final String ENCODE_USED = "ISO-8859-1";
	
	protected SupportContext supportContext;
	protected SupportContext supportContext2;
	
	protected final String DELIMITER_SPLIT = "\\|";
	protected final String DELIMITER = "|";
	protected enum BILOU {Beginning, Inside, Last, Outside, UnitToken};
	
	protected final String DOT = ".";
	protected final String BLANK = "";
	protected final String SPACE = " ";
	
	protected final String ACRONYM = "-SECxt(New)";
	
	protected int windowSize;
	protected final boolean CONSIDER_ONLY_ENTITIES = true;
	protected int numerEntityFound;
	
	protected String typeCxt = "";
	
	public SelectEntity(int windowSize) {
		dictionaryMap = new HashMap<String, Boolean>();
		supportContext = new SupportContext("BILOU");
		
		this.windowSize = windowSize;
	}
	
	protected void loadDictionary(String inputFilenameAddress) {
		
		try {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilenameAddress)));
			
			String line;
			
			while((line = in.readLine()) != null)
				dictionaryMap.put(line.toLowerCase(), true);
			
			in.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void loadSupportContext(String contextSource) {
		
		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(contextSource,
				FileType.TRAINING, false);
		
		supportContext = new SupportContext(windowSize, "BILOU", CONSIDER_ONLY_ENTITIES);
		
		supportContext.generateContext(inputSequenceSet);
	}
	
	public void labelFile(String dictionary, String contextSource, String fileToLabel) {
		
		String [] sequence;
		String [] labelSequence;
		int endPos;
		
		loadDictionary(dictionary);
		loadSupportContext(contextSource);
		
		System.out.println("-- Select Entity (NEW)\n");
		System.out.println("   Dictionary: " + dictionary);
		System.out.println("   Context Source: " + contextSource);
		System.out.println("   File to Label: " + fileToLabel);
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileToLabel)));
			Writer out = new OutputStreamWriter(new FileOutputStream(generateOutputFilenameAddress(fileToLabel)), ENCODE_USED);
			
			numerEntityFound = 0;
			boolean hasSentenceEntity = false;
			
			do {
				
				sequence = getSequence(in);
				labelSequence = new String[sequence.length];
				typeCxt = "";
				
				for(int i = 0; i < sequence.length; i++) {
					
					endPos = getEntityOffset(sequence, i, dictionaryMap);
					
					if(endPos < i || canAddAsEntity(sequence, i, endPos)) {
						setLabel(sequence, labelSequence, i, endPos);
						/*if(endPos >= i && canAddAsEntity(sequence, i, endPos)) {
							hasSentenceEntity = true;
						}*/
					} else {
						addAsOutside(labelSequence, i, endPos);
					}
					
					//-- if it is outside add 1 (because of getEntityOffset flag for not found entity)
					i = endPos + ((endPos < i)?1:0);
				}
				
				/*if(hasSentenceEntity) {
					printSequence(sequence);
					hasSentenceEntity = false;
				}*/
				
				if(sequence.length != 0 && sequence[0].length() > 0)
					writeSequence(out, sequence, labelSequence);
				
			}while(sequence.length != 0 && sequence[0].length() > 0);
			
			System.out.println("-- Number of Entities Found: " + numerEntityFound + "\n");
			
			in.close();
			
			out.flush();
			out.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	protected void setLabel(String [] sequence, String [] labelSequence, int startPos, int endPos) {
		
		//-- is outside
		if(endPos < startPos) {
			labelSequence[startPos] = BILOU.Outside.name();
		} else if(endPos > startPos){
			
			for(int i = startPos; i <= endPos; i++) {
				if(i == startPos) {
					labelSequence[i] = BILOU.Beginning.name();
				} else if(i > startPos && i < endPos) {
					labelSequence[i] = BILOU.Inside.name();
				} else if(i == endPos) {
					labelSequence[i] = BILOU.Last.name();
					numerEntityFound++;
				}
			}
		} else if(startPos == endPos) {
			labelSequence[startPos] = BILOU.UnitToken.name();
			numerEntityFound++;
		}
	}
	
	protected <E> int getEntityOffset(String [] sequence, int pos, HashMap<String, E> entityMap) {
		
		String key;
		String [] entity = null;
		
		int maxEntityPartFound = 0;
		int entityPartFound = 0;
		
		for (Iterator i = entityMap.keySet().iterator(); i.hasNext();) { 
			
			key = (String) i.next();
			entity = key.split(SPACE);
			entityPartFound = 0;
			
			for(int j = pos; j-pos < entity.length && j < sequence.length; j++) {
				if(entity[j-pos].equals(sequence[j])) {
					entityPartFound++;
				}
			}
			
			if(entityPartFound == entity.length && entityPartFound > maxEntityPartFound)
				maxEntityPartFound = entityPartFound;
		}
		
		return(pos + maxEntityPartFound - 1);
	}
	
	protected boolean canAddAsEntity(String [] sequence, int startPos, int endPos) {
		
		boolean canAddAsEntity = false;
		
		String [] sequenceLowerCase = lbd.Utils.Utils.convertSequenceToLowerCase(sequence, sequence.length);
		
		ContextToken context = supportContext.existContextInSequenceContextHashMap(sequenceLowerCase, startPos);
		
		if(context != null)
			canAddAsEntity = true;
		
		return(canAddAsEntity);
	}
	
	protected void addAsOutside(String [] labelSequence, int startPos, int endPos) {
		for(int i = startPos; i <= endPos; i++)
			labelSequence[i] = BILOU.Outside.name();
	}
	
	protected String [] getSequence(BufferedReader in) {
		
		String line;
		String sequence = "";
		
		try {
			while((line = in.readLine()) != null && !line.equals(BLANK))
				sequence += line.split(DELIMITER_SPLIT)[0] + SPACE;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(sequence.length() > 0)
			sequence = sequence.substring(0, sequence.length()-1);
		
		return(sequence.split(SPACE));
	}
	
	protected void writeSequence(Writer out, String [] sequence, String [] labelSequence) {
		
			try {
				
				for(int i = 0; i < sequence.length; i++)
					out.write(sequence[i] + DELIMITER + labelSequence[i] + "\n");
				
				out.write("\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	protected void printSequence(String [] sequence) {
		
		System.out.print(typeCxt + ": ");
		
		for(int i = 0; i < sequence.length; i++) {
			System.out.print(sequence[i] + SPACE);
		}
		
		System.out.println();
	}
	
	protected String generateOutputFilenameAddress(String inputFilenameAddress) {
		
		int endPos = inputFilenameAddress.lastIndexOf(DOT);
		String outputFilenameAddress = inputFilenameAddress.substring(0, endPos);
		
		outputFilenameAddress += ACRONYM + inputFilenameAddress.substring(endPos);
		outputFilenameAddress = outputFilenameAddress.replace("/Input/", "/Output/");
		
		return(outputFilenameAddress);
	}

}
