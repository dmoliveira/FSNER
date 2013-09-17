package lbd.FSNER.Filter.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Utils.MapId;
import lbd.FSNER.Utils.CommonEnum.Flexibility;

public class BagOfWords {

	protected int id; 
	
	//-- String=Entity, ArrayList=Sequence, MapId=Terms of the Sequence
	protected HashMap<String, ArrayList<MapId<String, Object>>> entityBagOfWordsMap;
	protected HashMap<String, MapId<String, Object>> generalEntityBagOfWordsMap;
	
	public BagOfWords() {
		
		id = 0;
		
		entityBagOfWordsMap = new HashMap<String, ArrayList<MapId<String, Object>>>();
		generalEntityBagOfWordsMap = new HashMap<String, MapId<String, Object>>();
	}
	
	public void addSequence(SequenceLabel sequenceLabelProcessed, int index) {
		
		MapId<String, Object> bagOfWordsMap;
		MapId<String, Object> generalBagOfWordsMap;
		String mainTerm = sequenceLabelProcessed.getTerm(index);
		
		if(!generalEntityBagOfWordsMap.containsKey(mainTerm)) {
			generalEntityBagOfWordsMap.put(mainTerm, new MapId<String, Object>(++id));
			entityBagOfWordsMap.put(mainTerm, new ArrayList<MapId<String, Object>>());
		}
		
		generalBagOfWordsMap = generalEntityBagOfWordsMap.get(mainTerm);
		
		entityBagOfWordsMap.get(mainTerm).add(new MapId<String, Object>(++id));
		bagOfWordsMap = entityBagOfWordsMap.get(mainTerm).get(entityBagOfWordsMap.get(mainTerm).size()-1);
		
		for(int i = 0; i < sequenceLabelProcessed.size(); i++) {
			if(i != index) {
				generalBagOfWordsMap.put(sequenceLabelProcessed.getTerm(i), null);
				bagOfWordsMap.put(sequenceLabelProcessed.getTerm(i), null);
			}
		}
	}
	
	public int getBagOfWordsId(SequenceLabel sequenceLabelProcessed, int index,
			Flexibility flexibility, boolean isGeneralUse, double threshold) {
		
		int id = -1;
		
		if(isGeneralUse) {
			if(flexibility == Flexibility.Total) {
				id = getGeneralIdTotalFlexibility(sequenceLabelProcessed, index, threshold);
			} else if(flexibility == Flexibility.Restrict) {
				id = getGeneralIdRestrictFlexibility(sequenceLabelProcessed, index, threshold);
			}
		} else {
			if(flexibility == Flexibility.Total) {
				id = getSpecificIdTotalFlexibility(sequenceLabelProcessed, index, threshold);
			} else if(flexibility == Flexibility.Restrict) {
				id = getSpecificIdRestrictedFlexibility(sequenceLabelProcessed, index, threshold);
			}
		}

		id = (id > -1)? generateAdjustedId(id, flexibility, isGeneralUse) : id;
		
		return(id);
	}
	
	protected int getGeneralIdTotalFlexibility(SequenceLabel sequenceLabelProcessed,
			int index, double threshold) {
		
		int id = -1;
		int termsInBagOfWordsNumber = 0;
		
		Iterator<Entry<String, MapId<String, Object>>> ite = generalEntityBagOfWordsMap.entrySet().iterator();
		Entry<String, MapId<String, Object>> bagOfWords;
		
		while(ite.hasNext() && id < 0) {
			
			bagOfWords = ite.next();
			
			for(int i = 0; i < sequenceLabelProcessed.size(); i++) {
				if(i != index && bagOfWords.getValue().containsKey(sequenceLabelProcessed.getTerm(i)))
					termsInBagOfWordsNumber++;
			}
			
			if((termsInBagOfWordsNumber/((double)sequenceLabelProcessed.size())) > threshold) {
				id = bagOfWords.getValue().getId();
				break;
			} else {
				termsInBagOfWordsNumber = 0;
			}
		}
		
		return(id);
	}
	
	protected int getGeneralIdRestrictFlexibility(SequenceLabel sequenceLabelProcessed,
			int index, double threshold) {
		
		int id = -1;
		int termsInBagOfWordsNumber = 0;
		
		MapId<String, Object> bagOfWords = generalEntityBagOfWordsMap.get(sequenceLabelProcessed.getTerm(index));
		
		if(bagOfWords != null) {
			for(int i = 0; i < sequenceLabelProcessed.size(); i++) {
				if(i != index && bagOfWords.containsKey(sequenceLabelProcessed.getTerm(i)))
					termsInBagOfWordsNumber++;
			}
			
			if((termsInBagOfWordsNumber/((double)sequenceLabelProcessed.size())) > threshold)
				id = bagOfWords.getId();
		}
		
		return(id);
	}
	
	protected int getSpecificIdTotalFlexibility(SequenceLabel sequenceLabelProcessed,
			int index, double threshold) {
		
		int id = -1;
		int termsInBagOfWordsNumber = 0;
		
		Iterator<Entry<String, ArrayList<MapId<String, Object>>>> ite = entityBagOfWordsMap.entrySet().iterator();
		Entry<String, ArrayList<MapId<String, Object>>> entry;
		
		while(ite.hasNext() && id < 0) {
			
			entry = ite.next();
			
			for(MapId<String, Object> bagOfWords : entry.getValue()) {
				for(int i = 0; i < sequenceLabelProcessed.size(); i++) {
					if(i != index && bagOfWords.containsKey(sequenceLabelProcessed.getTerm(i)))
						termsInBagOfWordsNumber++;
				}
				
				if((termsInBagOfWordsNumber/((double)sequenceLabelProcessed.size())) > threshold) {
					id = bagOfWords.getId();
					break;
				} else {
					termsInBagOfWordsNumber = 0;
				}
			}
		}
		
		return(id);
	}
	
	protected int getSpecificIdRestrictedFlexibility(SequenceLabel sequenceLabelProcessed,
			int index, double threshold) {
		
		int id = -1;
		int termsInBagOfWordsNumber = 0;
		
		ArrayList<MapId<String, Object>> bagOfWordsList = entityBagOfWordsMap.get(sequenceLabelProcessed.getTerm(index));
		
		if(bagOfWordsList != null) {
			
			for(MapId<String, Object> bagOfWords : bagOfWordsList) {
				for(int i = 0; i < sequenceLabelProcessed.size(); i++) {
					if(i != index && bagOfWords.containsKey(sequenceLabelProcessed.getTerm(i)))
						termsInBagOfWordsNumber++;
				}
				
				if((termsInBagOfWordsNumber/((double)sequenceLabelProcessed.size())) > threshold) {
					id = bagOfWords.getId();
					break;
				} else {
					termsInBagOfWordsNumber = 0;
				}
			}
		}
		
		return(id);
	}
	
	protected int generateAdjustedId(int id, Flexibility flexibility, boolean isGeneralUse) {
		return(((id * Flexibility.values().length) + flexibility.ordinal()) * 2 + ((isGeneralUse)? 1 : 0));
	}
}
