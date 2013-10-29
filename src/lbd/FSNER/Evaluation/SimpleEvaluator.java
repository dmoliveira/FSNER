package lbd.FSNER.Evaluation;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lbd.FSNER.Configuration.Constants.FileType;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Evaluation.Component.EvaluationLevel;
import lbd.FSNER.Evaluation.Component.Statistics;
import lbd.data.handler.ISequence;
import lbd.data.handler.SequenceSet;
import lbd.data.handler.sequenceSetHandler.SequenceSetHandlerLine;
import lbd.fsner.entity.Entity;
import lbd.fsner.entity.EntityType;
import lbd.fsner.label.encoding.Label;

public class SimpleEvaluator {

	protected Map<Integer, Statistics> mStatisticsMap;

	private void initializeVariables() {
		mStatisticsMap = new TreeMap<Integer, Statistics>();
	}

	public void evaluate(String pTaggedFilenameAddress, String pGoldStandardFilenameAddress) {

		for(EvaluationLevel cEvaluationLevel : EvaluationLevel.values()) {
			if(cEvaluationLevel.isToEvaluate()) {
				initializeVariables();

				SequenceSetHandlerLine vSequeneSetHandler = new SequenceSetHandlerLine();
				SequenceSet vTaggedSequenceSet = vSequeneSetHandler.getSequenceSetFromFile(pTaggedFilenameAddress, FileType.TAGGED, false);
				SequenceSet vGoldStandardSequenceSet = vSequeneSetHandler.getSequenceSetFromFile(pGoldStandardFilenameAddress, FileType.TEST, false);

				if(vTaggedSequenceSet.size() != vGoldStandardSequenceSet.size()) {
					throw new ArrayIndexOutOfBoundsException("Error in set size; Tagged Set:" + vTaggedSequenceSet.size()
							+ "  Gold Set:" + vGoldStandardSequenceSet.size() + ".");
				}

				int vSequenceNumber = 0;

				while(vTaggedSequenceSet.hasNext()) {
					ISequence vTaggedSequence = vTaggedSequenceSet.next();
					ISequence vGoldStandardSequence = vGoldStandardSequenceSet.next();
					evaluateSequences(vTaggedSequence, vGoldStandardSequence, ++vSequenceNumber, cEvaluationLevel);
				}

				printEvaluation(cEvaluationLevel);
			}
		}
	}

	protected void evaluateSequences(ISequence pTaggedSequence, ISequence pGoldStandardSequence, int pSequenceNumber, EvaluationLevel pEvaluationLevel) {
		if(pTaggedSequence.length() != pGoldStandardSequence.length()) {
			throw new ArrayIndexOutOfBoundsException("Error in sequence no. " + pSequenceNumber +  "; sizes of sequences are not equals.");
		}

		if(EvaluationLevel.TokenLv1 == pEvaluationLevel) {
			evaluateSequencesLevel1(pTaggedSequence, pGoldStandardSequence);
		} else if(EvaluationLevel.LabelLv2 == pEvaluationLevel) {
			evaluateSequencesLevel2(pTaggedSequence, pGoldStandardSequence);
		} else if(EvaluationLevel.EntityLv3 == pEvaluationLevel) {
			evaluateSequencesLevel3(pTaggedSequence, pGoldStandardSequence);
		}
	}

	protected void evaluateSequencesLevel1(ISequence pTaggedSequence, ISequence pGoldStandardSequence) {
		for(int i = 0; i < pTaggedSequence.length(); i++) {
			if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(pGoldStandardSequence.getLabel(i)))) {
				if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(pTaggedSequence.getLabel(i)))) {
					calculateTP(EntityType.getEntityType(pTaggedSequence.getLabel(i)));
				} else {
					calculateFN(EntityType.getEntityType(pGoldStandardSequence.getLabel(i)));
				}
			} else if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(pTaggedSequence.getLabel(i)))) {
				calculateFP(EntityType.getEntityType(pTaggedSequence.getLabel(i)));
			}
		}
	}

	protected void evaluateSequencesLevel2(ISequence pTaggedSequence, ISequence pGoldStandardSequence)  {
		for(int i = 0; i < pTaggedSequence.length(); i++) {
			if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(pGoldStandardSequence.getLabel(i)))) {
				if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(pTaggedSequence.getLabel(i))) &&
						Label.getCanonicalLabel(pTaggedSequence.getLabel(i)) == Label.getCanonicalLabel(pGoldStandardSequence.getLabel(i))) {
					calculateTP(EntityType.getEntityType(pTaggedSequence.getLabel(i)));
				} else if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(pTaggedSequence.getLabel(i)))){
					calculateFN(EntityType.getEntityType(pGoldStandardSequence.getLabel(i)));
					calculateFP(EntityType.getEntityType(pTaggedSequence.getLabel(i)));
				} else {
					calculateFN(EntityType.getEntityType(pGoldStandardSequence.getLabel(i)));
				}
			} else if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getCanonicalLabel(pTaggedSequence.getLabel(i)))) {
				calculateFP(EntityType.getEntityType(pTaggedSequence.getLabel(i)));
			}
		}
	}

	protected void evaluateSequencesLevel3(ISequence pTaggedSequence, ISequence pGoldStandardSequence) {


		List<Entity> vTaggedEntityList = Parameters.DataHandler.mLabelEncoding.getEntities(pTaggedSequence);
		List<Entity> vGoldStandardEntityList = Parameters.DataHandler.mLabelEncoding.getEntities(pGoldStandardSequence);

		for(Entity cGoldEntity : vGoldStandardEntityList) {
			calculateTPAndFN(vTaggedEntityList, cGoldEntity);
		}

		calculateFP(vTaggedEntityList);
	}

	//-- Calculate entity found and missed frequency
	private void calculateTPAndFN(List<Entity> pTaggedEntityList, Entity pGoldEntity) {

		boolean vWasEntityNotFound = true;

		for(Entity cTaggedEntity : pTaggedEntityList) {
			if(isEntityMatch(pGoldEntity, cTaggedEntity)) {
				calculateTP(cTaggedEntity.getEntityType());
				pTaggedEntityList.remove(cTaggedEntity);
				vWasEntityNotFound = false;
				break;
			}
		}

		if(vWasEntityNotFound) {
			calculateFN(pGoldEntity.getEntityType());
		}
	}

	private boolean isEntityMatch(Entity pGoldEntity, Entity cTaggedEntity) {
		return pGoldEntity.getValue().equals(cTaggedEntity.getValue())
				&& pGoldEntity.getIndex() == cTaggedEntity.getIndex()
				&& (!Parameters.Evaluator.mIsToConsiderType || pGoldEntity.getEntityType() == cTaggedEntity.getEntityType());
	}

	private void calculateTP(EntityType pTaggedEntityType) {
		addEntryToStatisticsMap(pTaggedEntityType);
		mStatisticsMap.get(pTaggedEntityType.ordinal()).addTP();
		mStatisticsMap.get(EntityType.All.ordinal()).addTP();
	}

	private void calculateFN(EntityType pGoldEntityType) {
		addEntryToStatisticsMap(pGoldEntityType);
		mStatisticsMap.get(pGoldEntityType.ordinal()).addFN();
		mStatisticsMap.get(EntityType.All.ordinal()).addFN();
	}

	//-- Calculate entity wrong labeled frequency
	private void calculateFP(List<Entity> vTaggedEntityList) {
		for(Entity cTaggedEntity : vTaggedEntityList) {
			addEntryToStatisticsMap(cTaggedEntity.getEntityType());
			mStatisticsMap.get(cTaggedEntity.getEntityType().ordinal()).addFP();
			mStatisticsMap.get(EntityType.All.ordinal()).addFP();
		}
	}

	private void calculateFP(EntityType pTaggedEntityType) {
		addEntryToStatisticsMap(pTaggedEntityType);
		mStatisticsMap.get(pTaggedEntityType.ordinal()).addFP();
		mStatisticsMap.get(EntityType.All.ordinal()).addFP();
	}

	private void addEntryToStatisticsMap(EntityType pEntityType) {
		if(!mStatisticsMap.containsKey(pEntityType.ordinal())) {
			mStatisticsMap.put(pEntityType.ordinal(), new Statistics());
		}

		if(!mStatisticsMap.containsKey(EntityType.All.ordinal())) {
			mStatisticsMap.put(EntityType.All.ordinal(), new Statistics());
		}
	}

	public void printEvaluation(EvaluationLevel pEvaluationLevel) {
		System.out.println("\n--- Evaluation Results - " + pEvaluationLevel.name());
		for(Integer cEntityTypeOrdinal : mStatisticsMap.keySet()) {
			Statistics vStatistics = mStatisticsMap.get(cEntityTypeOrdinal);
			vStatistics.calculateStatistics();
			System.out.println(MessageFormat.format("\t{0}\tP:{1,number,0.00000} R:{2,number,0.00000}, F1:{3,number,0.00000}",
					EntityType.values()[cEntityTypeOrdinal].getValue(),  vStatistics.getPrecision(),
					vStatistics.getRecall(), vStatistics.getF1()));
		}
	}

	public Statistics getStatistics(EntityType pEntityType) {
		return mStatisticsMap.get(pEntityType.ordinal());
	}

}
