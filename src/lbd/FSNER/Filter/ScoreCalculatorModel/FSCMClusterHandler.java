package lbd.FSNER.Filter.ScoreCalculatorModel;

import java.util.ArrayList;

import lbd.FSNER.Component.Cluster;
import lbd.FSNER.Component.ClusterHandler;
import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Filter.Component.Entity;
import lbd.FSNER.Filter.Component.Term;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.EntityUtils;

public class FSCMClusterHandler extends AbstractFilterScoreCalculatorModel{

	private static final long serialVersionUID = 1L;
	protected ClusterHandler clusterHandler;
	protected ArrayList<Entity> entityList;

	@Override
	public double calculateScore(SequenceLabel sequenceLabelProcessed,
			int index) {

		double score = 0;
		Entity entity = EntityUtils.getEntity(sequenceLabelProcessed.getTerm(index), entityList);

		ArrayList<Cluster> clusterList = clusterHandler.getClusterMap().get(sequenceLabelProcessed.getTerm(index));

		if(entity != null) {
			score = getClusterScore(clusterList, entity, sequenceLabelProcessed.toArraySequence());
		}

		return score;
	}

	public double getClusterScore(ArrayList<Cluster> clusterList, Entity entity, String [] candidateTermList) {

		double maximumSequenceScore = -1;//-1
		double threshould = 0;//0

		if(clusterList != null) {
			for(Cluster cluster : clusterList) {
				threshould = cluster.getAverageScore();
				maximumSequenceScore = calculateClusterScore(cluster, entity, candidateTermList);

				if(maximumSequenceScore > threshould) {
					/*System.out.println("ClusterID:" + cluster.getId() +
							" sim:" + cluster.calculateSequenceGeneralSimilarity(candidateTermList) + " " +
							entity.getId() + " P:" + entity.getProbabilityToBeEntity() + " Sc:"
							+ maximumSequenceScore + " Thres:" + threshould +
							" candTermSize:" + candidateTermList.length);*/

					break;
				}
			}
		}

		return(maximumSequenceScore - threshould);
	}

	public double calculateClusterScore(Cluster cluster, Entity entity, String [] candidateTermList) {

		double termsScore = 0;
		Term term;

		for(int i = 0; i < candidateTermList.length; i++) {

			term = cluster.getTerm(candidateTermList[i]);

			if(term != null  && !term.getId().equals(entity.getId())) {
				termsScore += term.getScore();
			}
		}

		return(termsScore);
	}

	public void setClusterHandler(ClusterHandler clusterHandler) {
		this.clusterHandler = clusterHandler;
	}

	public void setEntityList(ArrayList<Entity> entityList) {
		this.entityList = entityList;
	}

}
