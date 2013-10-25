package lbd.FSNER.Filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.FSNER.Component.Cluster;
import lbd.FSNER.Component.ClusterHandler;
import lbd.FSNER.Component.Sequence;
import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Filter.Component.Entity;
import lbd.FSNER.Filter.ScoreCalculatorModel.FSCMClusterHandler;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.EntityUtils;
import lbd.data.handler.DataSequence;

public class FtrClusterHandler extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected final double COEFFICIENT_OF_SIMILARITY = 0;
	protected final double COEFFICIENT_OF_GENERAL_SIMILARY = 0.5;

	protected ClusterHandler clusterHandler;
	protected ArrayList<Entity> entityList;

	protected ArrayList<String> entityValueListInSequence;

	public FtrClusterHandler(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator,
			ArrayList<Entity> entityList) {

		super(ClassName.getSingleName(FtrClusterHandler.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);

		this.entityList = entityList;

		clusterHandler = new ClusterHandler(COEFFICIENT_OF_SIMILARITY, COEFFICIENT_OF_GENERAL_SIMILARY);

		if(scoreCalculator instanceof FSCMClusterHandler) {
			((FSCMClusterHandler) scoreCalculator).setEntityList(entityList);
			((FSCMClusterHandler) scoreCalculator).setClusterHandler(clusterHandler);
		}
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
	}

	@Override
	public void loadActionBeforeSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceIteration(SequenceLabel sequenceLabelProcessed) {
		entityValueListInSequence = new ArrayList<String>();
	}

	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProcessed,
			int index) {

		int label = sequenceLabelProcessed.getLabel(index);

		if(label != LabelEncoding.BILOU.Outside.ordinal() && label < LabelEncoding.BILOU.values().length) {
			entityValueListInSequence.add(sequenceLabelProcessed.getTerm(index));
		}
	}

	@Override
	public void loadActionAfterSequenceIteration(SequenceLabel sequenceLabelProcessed) {

		ArrayList<Entity> entityListInSequence = new ArrayList<Entity>();
		Entity entity;

		for(String entityValue : entityValueListInSequence) {
			entity = EntityUtils.getEntity(entityValue, entityList);
			if(entity != null) {
				entityListInSequence.add(entity);
			}
		}


		clusterHandler.addSequence(entityListInSequence, sequenceLabelProcessed.toArraySequence());
	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjust(SequenceLabel sequenceProcessedLabel) {

		Entity entity;
		double clusterScore;

		Iterator <Entry<String, ArrayList<Cluster>>> clusterListIterator = clusterHandler.getClusterMap().entrySet().iterator();
		ArrayList<Cluster> clusterList;

		while(clusterListIterator.hasNext()) {

			clusterList = clusterListIterator.next().getValue();

			entity = clusterList.get(0).getEntity();
			entity.calculateProbabilityToBeEntity();

			for(Cluster cluster : clusterList) {

				//-- Calculate the term Score
				cluster.calculateTermScore();

				//-- Start score measures
				cluster.startScoreMeasures();

				for(Sequence sequence : cluster.getSequenceList()) {

					//-- calculate sequence score (in cluster)
					clusterScore = sequence.calculateScore(entity);
					cluster.addScore(clusterScore);
				}

				//-- Calculate Average Cluster Score
				cluster.calculateAverageScore();
			}
		}

		//-- Calculate the scores of general clusters
		calculateGeneralClusterScorePerSequence();
	}

	protected void calculateGeneralClusterScorePerSequence() {

		double clusterScore;

		for(Cluster cluster : clusterHandler.getGeneralClusterList()) {

			//-- Calculate the term Score
			cluster.calculateTermScore();

			//-- Start score measures
			cluster.startScoreMeasures();

			for(Sequence sequence : cluster.getSequenceList()) {

				//-- calculate sequence score (in cluster)
				clusterScore = sequence.calculateGeneralScore();
				cluster.addScore(clusterScore);
			}

			//-- Calculate Average Cluster Score
			cluster.calculateAverageScore();
		}
	}

	@Override
	public String getSequenceInstanceIdSub(DataSequence pSequence, SequenceLabel sequenceLabelProcessed,
			int index) {

		Entity entity = EntityUtils.getEntity(sequenceLabelProcessed.getTerm(index), entityList);

		double threshold;
		double maximumSequenceScore;

		String id = "";

		if(entity != null) {
			ArrayList<Cluster> clusterList = clusterHandler.getSimilarClusterList(entity, sequenceLabelProcessed.toArraySequence());

			if(clusterList != null) {
				for(Cluster cluster : clusterList) {
					threshold = cluster.getAverageScore();
					maximumSequenceScore = ((FSCMClusterHandler)mScoreCalculator).calculateClusterScore(cluster, entity, sequenceLabelProcessed.toArraySequence());

					if(maximumSequenceScore > threshold) {
						id = "id:"+id+"-clusterId:"+cluster.getId();
						break;
					}
				}
			}
		}

		return (id);
	}
}
