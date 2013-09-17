package lbd.FSNER.Component;

import iitb.CRF.DataSequence;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.ArtificialIntelligenceInterpreter;

public class EntityLinker {
	
	protected final String ENCODE_USED = "ISO-8859-1";
	protected HashMap<String, ArrayList<ArrayList<String>>> entityMap;
	protected final String DELIMITER_SPLIT = " ";
	
	public void loadEntityMap(String inputEntityListFilenameAddress) {
		
		entityMap = new HashMap<String, ArrayList<ArrayList<String>>>();
		ArrayList<ArrayList<String>> entityList;
		ArrayList<String> entityNameList;
		
		try {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputEntityListFilenameAddress), ENCODE_USED));
			
			String line;
			String [] lineElement;
			
			while((line = in.readLine()) != null) {
				
				lineElement = line.split(DELIMITER_SPLIT);
				entityNameList = new ArrayList<String>();
				
				for(int i = 0; i < lineElement.length; i++)
					entityNameList.add(lineElement[i].toLowerCase());
				
				entityList = entityMap.get(entityNameList.get(0));
				
				if(entityList == null) {
					entityList = new ArrayList<ArrayList<String>>();
					entityMap.put(entityNameList.get(0), entityList);
				}
				
				entityList.add(entityNameList);
			}
			
			in.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void executeLinker(DataSequence sequence, String [] sequenceLowerCase) {
		
		ArrayList<ArrayList<String>> entityList;
		
		for(int i = 0; i < sequence.length(); i++) {
			
			entityList = entityMap.get(sequenceLowerCase[i]);
			
			if(entityList != null)
				i = labelWholeEntity(sequence, sequenceLowerCase, i, entityList);
		}
	}
	
	protected int labelWholeEntity(DataSequence sequence, 
			String [] sequenceLowerCase, int index, ArrayList<ArrayList<String>> entityList) {
		
		boolean isCorrectEntity;
		int entityPartNameNumber;
		
		for(ArrayList<String> entityNameList : entityList) {
			
			entityPartNameNumber = 0;
			isCorrectEntity = false;
			
			for(int i = 0; i < entityNameList.size() && index + i < sequence.length(); i++) {
				if(!sequenceLowerCase[index + i].equals(entityNameList.get(i))) {
					break;
				} else { 
					entityPartNameNumber++;
					
					if(!isCorrectEntity && ArtificialIntelligenceInterpreter.isEntity(sequence.y(index + i)))
						isCorrectEntity = true;
				}
			}
			
			if(isCorrectEntity && entityNameList.size() == entityPartNameNumber) {
				labelSubSequence(sequence, index, index + entityNameList.size());
				index += entityNameList.size() - 1;
				break;
			}
		}
		
		return(index);
	}
	
	protected void labelSubSequence(DataSequence sequence, int startIndex, int endIndex) {
		
		for(int i = startIndex; i < endIndex; i++)
			sequence.set_y(i, 4);
		
	}	
}
