package lbd.FSNER.Component;

import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Filter.Component.Entity;
import lbd.FSNER.Filter.Component.Term;
import lbd.Utils.QuickSort;
import lbd.data.handler.ISequence;

public class ClusterHandler {

	protected HashMap<String, ArrayList<Cluster>> clusterMap;
	protected ArrayList<Cluster> generalClusterList;
	protected double coefficientOfSimilarity;
	protected double coefficientOfGeneralSimilarity;

	public ClusterHandler(double coefficientOfSimilarity, double coefficientOfGeneralSimilarity) {

		this.coefficientOfSimilarity = coefficientOfSimilarity;
		this.coefficientOfGeneralSimilarity = coefficientOfGeneralSimilarity;

		clusterMap = new HashMap<String, ArrayList<Cluster>>();
		generalClusterList = new ArrayList<Cluster>();
	}

	//-- for new AII and SequenceLabel
	public void addSequence(ArrayList<Entity> entityList, ISequence sequenceLabelProcessed) {

		String [] processedSequence = new String[sequenceLabelProcessed.length()];

		for(int i = 0; i < processedSequence.length; i++) {
			processedSequence[i] = sequenceLabelProcessed.getToken(i);
		}

		addSequence(entityList, sequenceLabelProcessed);
	}

	public void addSequence(ArrayList<Entity> entityList, String [] sequenceWithoutStopWord) {

		Sequence sequence;

		Cluster cluster;
		ArrayList<Cluster> clusterList = new ArrayList<Cluster>();

		for(Entity entity : entityList) {
			sequence = transformInSequence(sequenceWithoutStopWord);
			cluster = addSequence(sequence, entity);
			clusterList.add(cluster);
		}

		//-- Add to general cluster list
		sequence = transformInSequence(sequenceWithoutStopWord);
		if(entityList.size() > 0 && sequence.getTermList().size() >= 2) {
			addSequenceToGeneralCluster(sequence);
		}

		//-- Similarity between clusters
		addSimilarClusterToEntity(clusterList, entityList);
		addSimilarClusterToOtherClusters(clusterList);
	}

	public Cluster addSequence(Sequence sequence, Entity entity) {

		ArrayList<Cluster> clusterList = clusterMap.get(entity.getId());
		Cluster cluster = null;

		if(clusterList == null) {
			clusterMap.put(entity.getId(), new ArrayList<Cluster>());
			clusterList = clusterMap.get(entity.getId());
		} else {
			for(Cluster candidateCluster : clusterList) {
				if(candidateCluster.isSimilar(sequence)) {
					cluster = candidateCluster;
					break;
				}
			}
		}

		if(cluster != null) {
			cluster.addSequence(sequence);
		} else {
			clusterList.add(new Cluster(entity, sequence, coefficientOfSimilarity));
			cluster = clusterList.get(clusterList.size() - 1);
		}

		return(cluster);
	}

	public Cluster addSequenceToGeneralCluster(Sequence sequence) {

		Cluster cluster = null;
		int clusterNumber = 0;

		for(Cluster clusterCandidate : generalClusterList) {

			clusterNumber++;

			if(clusterCandidate.isSimilarGeneralCluster(sequence)) {
				//System.out.println("Cluster: " + clusterNumber + " sim:" + clusterCandidate.calculateSequenceSimilarity(sequence));
				cluster = clusterCandidate;
				break;
			}
		}

		if(cluster != null) {
			cluster.addSequence(sequence);
		} else {
			generalClusterList.add(new Cluster(null, sequence, coefficientOfGeneralSimilarity));
			cluster = generalClusterList.get(generalClusterList.size() - 1);
		}

		return(cluster);
	}

	public ArrayList<Cluster> getSimilarClusterList(Entity entity, String [] candidateTermList) {

		Sequence sequence = null;

		if(entity != null) {
			sequence = transformInSequence(candidateTermList);
		}

		return((sequence != null && sequence.getTermList().size() > 0)? getSimilarClusterList(entity, sequence) : null);
	}

	public ArrayList<Cluster> getSimilarClusterList(Entity entity, Sequence sequence) {

		ArrayList<Cluster> clusterList = clusterMap.get(entity.getId());
		ArrayList<Cluster> similarClusterList = new ArrayList<Cluster>();
		ArrayList<Double> invertedSimilarityList = new ArrayList<Double>();

		double similarityValue = 0;
		double currentSimilarityValue = 0;

		if(clusterList != null) {
			for(Cluster candidateCluster : clusterList) {

				currentSimilarityValue = candidateCluster.calculateSequenceSimilarity(sequence);

				if(currentSimilarityValue > similarityValue) {
					similarClusterList.add(candidateCluster);
					invertedSimilarityList.add(-1 * currentSimilarityValue);//((currentSimilarityValue != 0)?currentSimilarityValue:-1));
					similarityValue = currentSimilarityValue;
				}
			}
		}

		QuickSort.sort((invertedSimilarityList.toArray(new Double [invertedSimilarityList.size()])), similarClusterList);

		return(similarClusterList);
	}

	public ArrayList<Cluster> getSimilarGeneralClusterList(String [] candidateTermList) {

		Sequence sequence = transformInSequence(candidateTermList);

		return((sequence != null && sequence.getTermList().size() > 0)? getSimilarGeneralClusterList(sequence) : null);
	}

	public ArrayList<Cluster> getSimilarGeneralClusterList(Sequence sequence) {

		ArrayList<Cluster> similarGeneralClusterList = new ArrayList<Cluster>();
		ArrayList<Double> invertedSimilarityList = new ArrayList<Double>();

		double similarityValue = 0;
		double currentSimilarityValue = 0;

		for(Cluster candidateCluster : generalClusterList) {

			currentSimilarityValue = candidateCluster.calculateSequenceSimilarity(sequence);

			if(currentSimilarityValue > similarityValue) {
				similarGeneralClusterList.add(candidateCluster);
				invertedSimilarityList.add(-1 * currentSimilarityValue);
				similarityValue = currentSimilarityValue;
			}
		}

		QuickSort.sort((invertedSimilarityList.toArray(new Double [invertedSimilarityList.size()])), similarGeneralClusterList);

		return(similarGeneralClusterList);
	}

	public void updateClusterScorePerEntity(ArrayList<Entity> entityList) {

		ArrayList<Cluster> clusterList;

		for(Entity entity : entityList) {

			clusterList = clusterMap.get(entity.getId());

			for(Cluster cluster : clusterList) {
				cluster.updateClusterScore(cluster.getEntity());
			}
		}
	}

	public void addSimilarClusterToEntity(ArrayList<Cluster> clusterList, ArrayList<Entity> entityList) {
		for(Cluster cluster : clusterList) {
			for(Entity entity : entityList) {
				if(cluster.getEntity() != entity) {
					entity.setSimilarCluster(cluster);
				}
			}
		}
	}

	public void addSimilarClusterToOtherClusters(ArrayList<Cluster> clusterList) {
		for(Cluster mainCluster : clusterList) {
			for(Cluster subCluster : clusterList) {
				if(mainCluster != subCluster) {
					mainCluster.setSimilarCluster(subCluster);
				}
			}
		}
	}

	public static Sequence transformInSequence(String [] candidateTermList) {

		Sequence sequence = new Sequence();

		for(int i = 0; i < candidateTermList.length; i++) {
			sequence.setTerm(new Term(candidateTermList[i]));
		}

		return(sequence);
	}

	public void addClusterList(String entityName, ArrayList<Cluster> clusterList) {
		clusterMap.put(entityName, clusterList);
	}

	public HashMap<String, ArrayList<Cluster>> getClusterMap() {
		return(clusterMap);
	}

	public ArrayList<Cluster> getGeneralClusterList() {
		return(generalClusterList);
	}

	public double getCoefficientOfSimilarity() {
		return(coefficientOfSimilarity);
	}

	@Override
	public String toString() {
		return("Coefficient: " + coefficientOfSimilarity + " MapSize: " + clusterMap.size());
	}

}
