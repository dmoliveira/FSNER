package lbd.NewModels.BrownHierarquicalCluster;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class JCluster {
	
	protected final String ENCODE_USED = "ISO-8859-1";
	protected final char DELIMITER_SPLIT = ' ';
	
	protected HashMap<String, String> jClusterMap;
	protected HashMap<String, Integer> jClusterIdMap;
	
	protected static final int INDEX_CLUSTER_BINARY_ID = 0;
	protected static final int INDEX_TERM = 1;
	
	protected int clusterValueMapId;
	
	public void loadJCluster(String inputFilenameAddress) {
		
		try {
			
			jClusterMap = new HashMap<String, String>();
			jClusterIdMap = new HashMap<String, Integer>();
			
			clusterValueMapId = 0;
			
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilenameAddress), ENCODE_USED));
			
			String line;
			String[] element;
			
			while((line = in.readLine()) != null) {
				
				element = getElements(line);
				
				if(element.length > 0) {
					jClusterMap.put(element[INDEX_TERM], element[INDEX_CLUSTER_BINARY_ID]);
					jClusterIdMap.put(element[INDEX_CLUSTER_BINARY_ID], ++clusterValueMapId);
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
	
	protected String [] getElements(String line) {
		
		String [] element = new String[2];
		
		int indexElement = 0;
		int endElement = 0;
			
		while(line.charAt(endElement) != DELIMITER_SPLIT) ++endElement;
		element[indexElement] = line.substring(0, endElement);
		
		while(line.charAt(endElement) == DELIMITER_SPLIT) ++endElement;
		line = line.substring(endElement);
		
		element[++indexElement] = line.trim();
		
		return((!element[++indexElement].isEmpty())? element : new String[0]);
	}
	
	protected void addForAllPrefixesSizes(String [] element) {
		
		for(int i = 0; i <= element[INDEX_CLUSTER_BINARY_ID].length(); i++) {
			jClusterMap.put(element[INDEX_TERM], element[INDEX_CLUSTER_BINARY_ID].substring(0, i));
			jClusterIdMap.put(element[INDEX_CLUSTER_BINARY_ID].substring(0, i), ++clusterValueMapId);
		}
	}
	
	public int getJClusterId(String term, int bitPrefixSize) {
		
		String clusterBinaryId = jClusterMap.get(term).substring(0, bitPrefixSize);
		int jClusterId = (!clusterBinaryId.isEmpty() && jClusterIdMap.containsKey(clusterBinaryId))? jClusterIdMap.get(clusterBinaryId) : -1;
		
		return(jClusterId);
	}

}
