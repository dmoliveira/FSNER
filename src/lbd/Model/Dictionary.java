package lbd.Model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.wcohen.secondstring.JaroWinkler;
import com.wcohen.secondstring.SoftTFIDF;

import lbd.NewModels.BrownHierarquicalCluster.BrownCluster;
import lbd.SummirizedPattern.SummarizedPattern;
import lbd.Utils.RemoveStopWordsTool;
import lbd.Utils.Distance.JaroWinklerDistance;

public class Dictionary implements Serializable{

	private static final long serialVersionUID = 1501784800622286426L;
	
	protected final String ENCODE_USED = "ISO-8859-1";
	protected final String DELIMITER_SPLIT = " ";
	
	public enum DictionaryMatchingType {JaroWinkler, SOFTTFIDF, None};
	protected DictionaryMatchingType matchingType;
	
	protected HashMap<String, HashMap<String, Integer>>dictionaryMap;
	
	protected int termId;
	protected double maximumDistance; //-- 0.2
	protected int prefixMapSize; //-- 3
	
	protected transient RemoveStopWordsTool removeStopWordTool;
	protected String stopWordFile = "./samples/data/bcs2010/Dictionaries/" + "EnglishStopWords-Tweet.dic";
	
	protected transient BrownCluster brownCluster;
	protected int bitPrefixSize;
	
	protected transient SummarizedPattern summarizedPattern;
	
	public Dictionary(int prefixMapSize, SummarizedPattern summarizedPattern) {
		
		this(0, prefixMapSize, DictionaryMatchingType.None);
		this.summarizedPattern = summarizedPattern;
	}
	
	public Dictionary(int prefixMapSize, BrownCluster brownCluster, int bitPrefixSize) {
		
		this(0, prefixMapSize, DictionaryMatchingType.None);
		this.brownCluster = brownCluster;
		this.bitPrefixSize = bitPrefixSize;
	}
	
	public Dictionary(double maximumDistance, int prefixMapSize, DictionaryMatchingType matchingType) {
		
		dictionaryMap = new HashMap<String, HashMap<String, Integer>>();
		removeStopWordTool = new RemoveStopWordsTool(stopWordFile);
		
		termId = 0;
		
		this.maximumDistance = maximumDistance;
		this.prefixMapSize = prefixMapSize;
	}
	
	public void loadDictionary(String dictionaryFilenameAddress) {
		
		try {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(dictionaryFilenameAddress), ENCODE_USED));
			
			String entity;
			String entityTerm;
			String [] entityElement;
			
			while((entity = in.readLine()) != null) {
				
				entity = removeStopWordTool.removeStopWord(entity);
				entity = (summarizedPattern == null)? entity.toLowerCase() : entity;
				
				entityElement = entity.split(DELIMITER_SPLIT);
				
				for(int i = 0; i < entityElement.length; i++) {
					
					entityTerm = entityElement[i];
					
					if(!entityTerm.isEmpty()) {
						
						entityTerm = entityElement[i].trim();
						
						if(brownCluster != null && brownCluster.getClusterValue(entityTerm).length() > bitPrefixSize)
							entityTerm = brownCluster.getClusterValue(entityTerm).substring(0, bitPrefixSize);
						else if(summarizedPattern != null)
							entityTerm = summarizedPattern.getPattern(entityTerm);
						
						addTerm(entityTerm);
					}
				}
			}
			
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addTerm(String term) {
		
		String prefixDictionaryKey = (term.length() > prefixMapSize)? term.substring(0,prefixMapSize) : term;
		
		if(!dictionaryMap.containsKey(prefixDictionaryKey))
			dictionaryMap.put(prefixDictionaryKey, new HashMap<String, Integer>());
		
		HashMap<String, Integer> prefixDictionaryMap = dictionaryMap.get(prefixDictionaryKey);
		prefixDictionaryMap.put(term, ++termId);
	}
	
	public int isMatching(String term) {
		
		int termId = -1;
		
		if(brownCluster != null && brownCluster.getClusterValue(term).length() > bitPrefixSize)
			term = brownCluster.getClusterValue(term).substring(0, bitPrefixSize);
		else if(summarizedPattern != null)
			term = summarizedPattern.getPattern(term);
		
		
		String prefixDictionaryKey = (term.length() > prefixMapSize)? term.substring(0, prefixMapSize) : term;
		HashMap<String, Integer> prefixDictionaryMap = dictionaryMap.get(prefixDictionaryKey);
		
		/*double distance;
		boolean isJaroWinklerDistance = DictionaryMatchingType.JaroWinkler == matchingType;
		Iterator<Entry<String,Integer>> ite = prefixDictionaryMap.entrySet().iterator();
		Entry<String,Integer> entry;
		
		JaroWinklerDistance jwDistance = new JaroWinklerDistance();
		SoftTFIDF softTFIDF = new SoftTFIDF(new JaroWinkler());
		
		/*while(ite.hasNext()) {
			
			entry = ite.next();
			distance = (isJaroWinklerDistance)? jwDistance.distance(entry.getKey(), term) :
				softTFIDF.score(entry.getKey(), term);
			
			if((isJaroWinklerDistance && distance <= maximumDistance) || 
					(!isJaroWinklerDistance && distance >= maximumDistance)) {
				termIdMatched = entry.getValue();
				break;
			}
		}*/
		
		if(prefixDictionaryMap != null && prefixDictionaryMap.containsKey(term)) {
			if(brownCluster != null)
				termId = brownCluster.getClusterValuePrefixId(term);
			else if(summarizedPattern != null)
				termId = summarizedPattern.getSummarizedPatternId(term);
			else
				termId = prefixDictionaryMap.get(term);
		}
		
		return(termId);
	}
    
    public static void main(String [] args) {
		/** TEST **/
		Dictionary dict = new Dictionary(0.1, 3, DictionaryMatchingType.JaroWinkler);
		dict.loadDictionary("./samples/data/bcs2010/Dictionaries/Geo-loc-RUS.dic");
		System.out.println("Loaded " + dict.dictionaryMap.size() + " clusters and " + dict.termId + " terms");
	}

}
