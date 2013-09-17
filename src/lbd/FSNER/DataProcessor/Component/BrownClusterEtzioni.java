package lbd.FSNER.DataProcessor.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import lbd.FSNER.Utils.Symbol;

public class BrownClusterEtzioni {
	
	protected final String ENCODE_USED = "ISO-8859-1";
	protected final String DELIMITER_SPLIT = " ";
	
	protected HashMap<String, String> brownClusterMap;
	protected HashMap<String, Integer> brownClusterIdMap;
	
	protected final int BINARY_SIZE = 20;
	protected final String BIT_ONE = "1";
	protected final String BIT_ZERO = "0";
	
	protected int clusterValueMapId;
	
	public BrownClusterEtzioni() {
		
		brownClusterMap = new HashMap<String, String>();
		brownClusterIdMap = new HashMap<String, Integer>();
		
		clusterValueMapId = 0;
	}
	
	public void loadBrownCluster(String inputFilenameAddress) {
		
		String line;
		String [] lineElement;
		
		int clusterId;
		
		int indexTerm = 0;
		int indexClusterId = 1;
		
		String term;
		String binaryClusterId;
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilenameAddress), ENCODE_USED));
			
			while((line = in.readLine()) != null) {
				
				lineElement = line.split(DELIMITER_SPLIT);
				
				term = lineElement[indexTerm];
				clusterId = Integer.parseInt(lineElement[indexClusterId]);
				
				binaryClusterId = transformClusterIdToClusterBinaryId(clusterId);
				
				//System.out.println("** " + term + " >> " + clusterId + " >> " + binaryClusterId);
				
				addAllPrefixes(term, binaryClusterId);
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
	
	protected String transformClusterIdToClusterBinaryId(int clusterId) {
		
		String clusterBinaryId = "";
		
		for(int i = 0; i < BINARY_SIZE; i++) {
			if((clusterId & (1 << i)) > 0)
				clusterBinaryId += BIT_ONE;
			else
				clusterBinaryId += BIT_ZERO;
		}		
		
		return(clusterBinaryId);
	}
	
	protected void addAllPrefixes(String term, String clusterValue) {
		
		//System.out.println();
		for(int i = 1; i <= clusterValue.length(); i++) {
			brownClusterMap.put(term, clusterValue.substring(0, i));
			brownClusterIdMap.put(clusterValue.substring(0, i), ++clusterValueMapId);
			//System.out.print(clusterValue.substring(0, i) + ", ");
		}
		//System.out.println();
	}
	
	public int getClusterId(String term, int bitPrefixSize) {

		String clusterBinaryId = (brownClusterMap.containsKey(term))? brownClusterMap.get(term).substring(0, bitPrefixSize) : Symbol.EMPTY;
		int clusterId = (!clusterBinaryId.isEmpty() && brownClusterIdMap.containsKey(clusterBinaryId))? brownClusterIdMap.get(clusterBinaryId) : -1;
		
		//if(!clusterBinaryId.isEmpty())System.out.println("getClusterId(): " + term + " (" + bitPrefixSize + ") " + clusterBinaryId + " >> " + brownClusterMap.get(term));
		
		return(clusterId);
	}
}
