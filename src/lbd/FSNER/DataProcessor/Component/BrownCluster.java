package lbd.FSNER.DataProcessor.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class BrownCluster {
	
	protected transient final String ENCODE_USED = "ISO-8859-1";

	protected HashMap<String, lbd.NewModels.BrownHierarquicalCluster.Cluster> brownClusterMap;
	protected HashMap<String, String> termClusterMap;
	protected HashMap<String, Integer> clusterValueMap;
	
	protected final transient String DELIMITER_SPLIT = "	";
	public static final transient String TAG_NOT_FOUND = "*NotFound*"; 
	
	protected transient int INDEX_CLUSTER_VALUE = 0;
	protected transient int INDEX_TERM = 1;
	
	protected transient int maximumBitLenght;
	private transient int clusterValueMapId;
	
	public BrownCluster() {
		
		brownClusterMap = new HashMap<String, lbd.NewModels.BrownHierarquicalCluster.Cluster>();
		termClusterMap = new HashMap<String, String>();
		clusterValueMap = new HashMap<String, Integer>();
	}
	
	public void loadBrownHierarquicalCluster(String inputFilenameAddress) {
		
		try {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilenameAddress), ENCODE_USED));
			maximumBitLenght = 0;
			clusterValueMapId = 0;
			
			String line;
			String [] lineElement;
			
			String clusterValue;
			String term;
			
			lbd.NewModels.BrownHierarquicalCluster.Cluster cluster;
			
			while((line = in.readLine()) != null) {
				
				lineElement = line.split(DELIMITER_SPLIT);
				
				if(lineElement.length > 1) {
					
					clusterValue = lineElement[INDEX_CLUSTER_VALUE];
					term = lineElement[INDEX_TERM];
					
					if(!brownClusterMap.containsKey(clusterValue)) {
						brownClusterMap.put(clusterValue, new lbd.NewModels.BrownHierarquicalCluster.Cluster(clusterValue));
						addToClusterValueMap(clusterValue);
					}
					
					cluster = brownClusterMap.get(clusterValue);
					cluster.addTerm(term);
					
					if(termClusterMap.containsKey(term))
						System.out.println("--Exist " + term + " added(" + termClusterMap.get(term) + ") new(" + clusterValue + ")");
					
					termClusterMap.put(term, clusterValue);
					
					if(maximumBitLenght < clusterValue.length())
						maximumBitLenght = clusterValue.length();
				}
			}
			
			in.close();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void addToClusterValueMap(String clusterValue) {
		
		String prefixClusterValue;
		
		for(int i = 1; i < clusterValue.length(); i++) {
			prefixClusterValue = clusterValue.substring(0, i);
			
			//System.out.println(prefixClusterValue + " (" + i + ") " + clusterValue);
			
			if(!clusterValueMap.containsKey(prefixClusterValue))
				clusterValueMap.put(prefixClusterValue, ++clusterValueMapId);
		}
	}
	
	public int getClusterValuePrefixId(String clusterValuePrefix) {
		return((clusterValueMap.containsKey(clusterValuePrefix))? clusterValueMap.get(clusterValuePrefix) : -1);
	}
	
	public String getClusterValue(String term) {
		return((termClusterMap.containsKey(term))? termClusterMap.get(term) : TAG_NOT_FOUND);
	}
	
	public int getMaximumBitLenght() {
		return maximumBitLenght;
	}
}
