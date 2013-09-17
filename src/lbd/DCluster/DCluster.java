package lbd.DCluster;

import java.io.BufferedReader;
import java.io.FileInputStream;
/**
 * DCluster was made by Oliveira, D. M. in 2012 (dmztheone@gmail.com)
 * ------------------------------------------------------------------ */
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.Utils.Utils;

public class DCluster implements Serializable {

	private static final long serialVersionUID = -5340953915398622004L;
	
	protected final String ENCODE_USED = "ISO-8859-1";
	protected final String TAG_SPACE = " ";
	protected final String TAG_DOT = ".";
	
	protected final String ACRONYM = "-DCluster";
	protected final String EXTENSION_DCLUSTER = ".dcl";
	protected final String EXTENSION_DEBUG = ".dbg";
	
	protected int maxEditDistance = 3; //-- Default is 3
	protected int minTermSize = 1; //-- Default is 1
	
	protected int termNumber;
	protected transient int termModificationNumber;
	protected transient int termOutOfVocabulary;
	
	protected HashMap<String, ArrayList<Cluster>> clusterMap;
	protected HashMap<String, Term> termInModelMap;
	
	public DCluster() {
		clusterMap = new HashMap<String, ArrayList<Cluster>> ();
		termInModelMap = new HashMap<String, Term>();
		termModificationNumber = 0;
	}
	
	public DCluster(int maxEditDistance) {
		clusterMap = new HashMap<String, ArrayList<Cluster>> ();
		termInModelMap = new HashMap<String, Term>();
		this.maxEditDistance = maxEditDistance;
		termModificationNumber = 0;
	}
	
	/******************************************************************************************************************
	 * 
	 * -- Phase I: Generate DCluster
	 * 
	 ******************************************************************************************************************/
	
	public void generateDCluster(String wfreqInputFilenameAddress) {
		
		try {
			
			System.out.println("-- Start");
			System.out.println(" Analyzing File: \"" + wfreqInputFilenameAddress + "\"");
			
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(wfreqInputFilenameAddress), ENCODE_USED));
			
			String line;
			String [] lineElement;
			
			ArrayList<Cluster> clusterList;
			
			String termValue;
			String clusterKey;
			
			int termFrequency;
			int indexTerm = 0;
			int indexFrequency = 1;
			termNumber = 0;
			
			while((line = in.readLine()) != null) {
				
				lineElement = line.split(TAG_SPACE);
				termNumber++;
				
				termValue = lineElement[indexTerm].toLowerCase();
				termFrequency = Integer.parseInt(lineElement[indexFrequency]);
				
				clusterKey = generateClusterKey(termValue);
				clusterList = clusterMap.get(clusterKey);
				
				if(clusterList == null) {
					clusterMap.put(clusterKey, new ArrayList<Cluster> ());
					clusterList = clusterMap.get(clusterKey);
				}
				
				addTermInClusterList(clusterList, termValue, termFrequency);
			}
			
			in.close();
			
			System.out.println("   ClusterMap #: " + clusterMap.size()); 
			System.out.println("   Clusters #: " + Cluster.getGlobalClusterId());
			System.out.println("   Terms #: " + termNumber);
			
			writeDCluster(generateDClusterOutputFilenameAddress(wfreqInputFilenameAddress, EXTENSION_DCLUSTER));
			writeDClusterDebugFile(generateDClusterOutputFilenameAddress(wfreqInputFilenameAddress, EXTENSION_DEBUG));
			
			System.out.println("--Finished");
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void addTerm(String termValue, int termFrequency) {
		
		String clusterKey = generateClusterKey(termValue);
		ArrayList<Cluster> clusterList = clusterMap.get(clusterKey);
		
		if(clusterList == null) {
			clusterMap.put(clusterKey, new ArrayList<Cluster> ());
			clusterList = clusterMap.get(clusterKey);
		}
		
		addTermInClusterList(clusterList, termValue, termFrequency);
	}
	
	protected void addTermInClusterList(ArrayList<Cluster> clusterList, String termValue, int termFrequency) {
		
		Cluster cluster = null;
		int lowerEditDistance = maxEditDistance + 1;
		int editDistance;
		
		for(Cluster clusterItem : clusterList) {
			
			editDistance = Utils.getLevenshteinDistance(clusterItem.getMainTermValue(), termValue);
			
			if(editDistance <= maxEditDistance && editDistance < lowerEditDistance) {
				cluster = clusterItem;
				lowerEditDistance = editDistance;
				
				if(editDistance == 1)
					break;
			}
		}
		
		if(cluster == null) {
			clusterList.add(new Cluster(termValue));
			cluster = clusterList.get(clusterList.size()-1);
		}
		
		cluster.addTerm(new Term(termValue, termFrequency, cluster.getClusterId()));
	}
	
	protected String generateClusterKey(String value) {
		return(value.substring(0, (int) Math.ceil(value.length()/2.0)));
	}
	
	/******************************************************************************************************************
	 * 
	 * -- Phase II: Analyze Training Model Sequence
	 * 
	 ******************************************************************************************************************/
	
	public void analyzeTrainingModelSequence(String [] sequenceLowerCase) {
		
		String termValue;
		
		Term term;
		
		for(int i = 0; i < sequenceLowerCase.length; i++) {
			termValue = sequenceLowerCase[i];
			term = getTerm(termValue);
			
			if(term == null) {
				
				addTerm(termValue, -1);
				term = getTerm(termValue);
				
				termNumber++;
				//System.out.print(sequenceLowerCase[i] + ",");
			}
			
			termInModelMap.put(termValue, term);
			
			term.setExistInModel(true);
			term.addModelFrequency();
		}
	}
	
	/******************************************************************************************************************
	 * 
	 * -- Phase III: Normalize Sequence
	 * 
	 ******************************************************************************************************************/
	
	public String[] normalizeSequence(String [] sequenceLowerCase) {
		
		String [] normalizedSequence = new String [sequenceLowerCase.length];
		
		for(int i = 0; i < sequenceLowerCase.length; i++)
			normalizedSequence[i] = getMostApproximatedTerm(sequenceLowerCase[i]);
		
		return(normalizedSequence);
	}
	
	protected String getMostApproximatedTerm(String termValue) {
		
		String candidateTermValue = termValue;
		Term term = getTerm(termValue);
		
		if((term != null && !term.existInModel) || term == null)
			candidateTermValue = selectedMostApproximatedTerm(termValue);
		
		return(candidateTermValue);
	}
	
	protected String selectedMostApproximatedTerm(String termValue) {
		
		String termValueSelected = termValue;
		
		int editDistance;
		int lowerEditDistance = maxEditDistance;
		int maxModelFrequency = -1;
		
		ArrayList<Cluster> clusterList = clusterMap.get(generateClusterKey(termValue));
		
		if(clusterList != null) {
			for(Cluster cluster : clusterList) {
				if(Utils.isTermMatching(cluster.getMainTermValue(), termValue, minTermSize, maxEditDistance)) {
					for(Term term : cluster.getTermList()) {
						
						editDistance = Utils.getLevenshteinDistance(termValue, term.getValue());
						
						if(term.existInModel && editDistance <= lowerEditDistance &&
								term.getModelFrequency() > maxModelFrequency) {
							
							termValueSelected = term.getValue();
							lowerEditDistance = editDistance;
							maxModelFrequency = term.getModelFrequency();
						}
					}
				}
			}
		}
		
		if(!termValueSelected.equals(termValue))
			termModificationNumber++;
		else 
			termOutOfVocabulary++;
		
		return(termValueSelected);
	}
	
	/******************************************************************************************************************
	 * 
	 * -- Auxiliary Methods
	 * 
	 ******************************************************************************************************************/
	
	public Term getTerm(String termValue) {
		
		Term term = null;
		
		ArrayList<Cluster> clusterList = clusterMap.get(generateClusterKey(termValue));
		
		if(clusterList != null) {
			for(Cluster cluster : clusterList) {
				if(Utils.isTermMatching(cluster.getMainTermValue(), termValue, minTermSize, maxEditDistance)) {
					term = cluster.getTerm(termValue);
					
					if(term != null)
						break;
				}
			}
		}
		
		return(term);
	}
	
	public int getTermModificationNumber() {
		return(termModificationNumber);
	}
	
	protected String generateDClusterOutputFilenameAddress(String inputFilenameAddress, String extension) {
		
		int endIndexInputFilename = inputFilenameAddress.lastIndexOf(TAG_DOT);
		
		String dClusterOutputFilenameAddress = inputFilenameAddress.substring(0, endIndexInputFilename);
		dClusterOutputFilenameAddress += ACRONYM + extension;
		
		return(dClusterOutputFilenameAddress);
	}

	public void readDCluster(String dClusterFilenameAddress, DCluster target) throws IOException, ClassNotFoundException {
    	
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(dClusterFilenameAddress));
		DCluster dClusterInstance = (DCluster) in.readObject();
		cloneDCluster(target, dClusterInstance);
		
		in.close();
    }
	
	private void cloneDCluster(DCluster target, DCluster clone) {
		
		target.clusterMap = clone.clusterMap;
		target.maxEditDistance = clone.maxEditDistance;
		target.minTermSize = clone.minTermSize;
		target.termNumber = clone.termNumber;
	}
    
    public void writeDCluster(String dClusterOutputFilenameAddress) throws IOException {
    	
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dClusterOutputFilenameAddress));

		out.writeObject(this);
		out.flush();
		out.close();
    }
    
    public void writeDClusterDebugFile(String dClusterOutputFilenameAddress) {
    	
    	try {
    		
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dClusterOutputFilenameAddress), ENCODE_USED);
			
			Iterator<Entry<String, ArrayList<Cluster>>> ite = clusterMap.entrySet().iterator();
			Entry<String, ArrayList<Cluster>> entry;
			
			String key;
			ArrayList<Cluster> clusterList;
			
			while(ite.hasNext()) {
				
				entry = ite.next();
				
				key = entry.getKey();
				clusterList = entry.getValue();
				
				out.write("[" + key  + "]\n");
				
				for(Cluster cluster : clusterList) {
					out.write("   " + cluster.getMainTermValue() + "{ ");
					
					for(Term term : cluster.getTermList())
						out.write(term.getValue() + " ");
					
					out.write("}\n");
				}
				
			}
			
			out.flush();
			out.close();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void resetTermsInModelStatus() {
    	
    	Iterator<Entry<String, Term>> ite = termInModelMap.entrySet().iterator();
    	termModificationNumber = 0;
    	termOutOfVocabulary = 0;
    	
    	while(ite.hasNext())
    		ite.next().getValue().setExistInModel(false);
    }

	public int getTermNumber() {
		return termNumber;
	}

	public void setTermNumber(int termNumber) {
		this.termNumber = termNumber;
	}

	public int getTermInModelNumber() {
		return termInModelMap.size();
	}

	public void setTermModificationNumber(int termModificationNumber) {
		this.termModificationNumber = termModificationNumber;
	}

	public int getTermOutOfVocabulary() {
		return termOutOfVocabulary;
	}

	public void setTermOutOfVocabulary(int termOutOfVocabulary) {
		this.termOutOfVocabulary = termOutOfVocabulary;
	}
	
}
