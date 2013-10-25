package lbd.FSNER.LabelFile;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Configuration.Constants;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.DataProcessor.Component.PreprocessData;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractLabelFile;
import lbd.FSNER.Utils.FileUtils;
import lbd.FSNER.Utils.LabelEncoding;
import lbd.FSNER.Utils.LabelEncoding.BILOU;
import lbd.data.handler.DataSequence;
import lbd.data.handler.Sequence;
import lbd.data.handler.SequenceSet;
import lbd.data.handler.SequenceSetHandler;
import lbd.data.handler.SequenceSetHandler.FileType;

public class SimpleLabelFile extends AbstractLabelFile {

	private static final long serialVersionUID = 1L;
	protected final static double SCORE_THRESHOLD = 0;

	public SimpleLabelFile() {
		super();
	}

	@Override
	protected void labelFileSub(String pFilenameAddressToLabel) {

		try {
			OutputStreamWriter vOutputFile = null;

			vOutputFile = FileUtils.createOutputStreamWriter(pFilenameAddressToLabel, Constants.FileExtention.Tagged);
			mTaggedFilenameAddress = Parameters.generateOutputFilenameAddress(pFilenameAddressToLabel, Constants.FileExtention.Tagged);

			SequenceSet vInputSequenceSet =  SequenceSetHandler.getSequenceSetFromFile(pFilenameAddressToLabel,
					FileType.TOLABEL, false);

			DataSequence vSequence = null;

			while(vInputSequenceSet.hasNext()) {

				//-- Label Sequence
				mSequenceNumber++;
				vSequence = vInputSequenceSet.next();
				labelSequence(vSequence);

				SequenceSetHandler.writeSequenceToFile(vOutputFile, vSequence);

				if(Debug.LabelFile.printNumberedLabelSequence) {
					printNumberedSequence(vSequence);
				}
			}

			if(Debug.LabelFile.printFilterStatistics) {
				printFilterStatistics();
			}

			vOutputFile.flush();
			vOutputFile.close();

		} catch (UnsupportedEncodingException pException) {
			pException.printStackTrace();
		} catch (FileNotFoundException pException) {
			pException.printStackTrace();
		} catch (IOException pException) {
			pException.printStackTrace();
		}
	}

	@Override
	public DataSequence labelSequence(DataSequence pSequence) {

		/*HashMap<String, SequenceLabel> proccessedSequenceMap = PreprocessData.preprocessSequence(
				sequence, activityControl.getDataPreprocessorList());

		for(int i = 0; i < sequence.length(); i++)
			setLabel(sequence, proccessedSequenceMap, i);*/

		//-- Add sequence for future update of the NER model
		//if(CRITERIO)
		//updateControl.addSequence(sequence);

		//-- For experiment
		//labelSequenceNoProb(sequence);
		//labelSequenceNoProbOnlySeqEntities(sequence);
		labelSequenceAvgProbEntityConsiderAllTweet(pSequence);
		//labelSequenceAvgProbEntityConsiderAllPartialTweet(sequence);
		//labelSequenceAvgProbConsiderAllTweet(sequence);
		//labelSequenceAvgProbConsiderAllPartialTweet(sequence);

		return pSequence;
	}

	/** Only for experimental propose **/
	protected void labelSequenceNoProb(DataSequence sequence) {

		Map<String, SequenceLabel> proccessedSequenceMap = PreprocessData.preprocessSequence(
				sequence, mActivityControl.getDataPreprocessorList());

		for(int i = 0; i < sequence.length(); i++) {
			getLabel(sequence, proccessedSequenceMap, i);
		}

		mUpdateControl.addSequence(sequence);
	}

	protected void labelSequenceNoProbOnlySeqEntities(DataSequence sequence) {

		Map<String, SequenceLabel> proccessedSequenceMap = PreprocessData.preprocessSequence(
				sequence, mActivityControl.getDataPreprocessorList());

		boolean hasEntity = false;
		int label;

		for(int i = 0; i < sequence.length(); i++) {
			label = getLabel(sequence, proccessedSequenceMap, i);

			if(LabelEncoding.isEntity(label)) {
				hasEntity = true;
			}
		}

		if(hasEntity) {
			mUpdateControl.addSequence(sequence);
		}
	}

	protected void labelSequenceAvgProbEntityConsiderAllTweet(DataSequence pSequence) {

		Map<String, SequenceLabel> vProccessedSequenceMap = PreprocessData.preprocessSequence(
				pSequence, mActivityControl.getDataPreprocessorList());

		int vOriginalLabel;
		int vLabel;

		double vAverageEntityProbability = 0;
		boolean vHasEntity = false;
		int vNormalization = 0;

		for(int i = 0; i < pSequence.length(); i++) {
			vOriginalLabel = pSequence.y(i);
			vLabel = getLabel(pSequence, vProccessedSequenceMap, i);

			//-- Used the original term label only for statistics.
			if(LabelEncoding.isEntity(vLabel)) {
				if(!LabelEncoding.isEntity(vOriginalLabel)) {
					mTermLevelStatisticsAnalysis.addWrongTermsLabeledAsEntities(pSequence.x(i) + "(" + mSequenceNumber + ")");
				} else {
					mTermLevelStatisticsAnalysis.addTermLabeledAsEntity((String) pSequence.x(i) + "(" + mSequenceNumber + ")");
				}

				vHasEntity |= true;
				vAverageEntityProbability += mLabelCalculator.getLabelProbabilities()[vLabel]/mLabelCalculator.getNormalizationFactor()[vLabel];
				vNormalization++;
			} else if(LabelEncoding.isEntity(vOriginalLabel)) {
				mTermLevelStatisticsAnalysis.addMissedEntityTerms((String) pSequence.x(i) + "(" + mSequenceNumber + ")");
			}
		}

		if(vHasEntity && vAverageEntityProbability/vNormalization > mUpdateControl.getThreshouldConfidenceSequence()) {
			mUpdateControl.addSequence(pSequence);
		}
	}

	protected void labelSequenceAvgProbEntityConsiderAllPartialTweet(DataSequence sequence) {

		Map<String, SequenceLabel> proccessedSequenceMap = PreprocessData.preprocessSequence(
				sequence, mActivityControl.getDataPreprocessorList());

		int label;
		double probability = 0;
		double averageEntityProbability = 0;
		boolean hasEntity = false;
		int normalization = 0;

		Sequence newSequence = new Sequence();

		for(int i = 0; i < sequence.length(); i++) {
			label = getLabel(sequence, proccessedSequenceMap, i);
			probability = mLabelCalculator.getLabelProbabilities()[label]/mLabelCalculator.getNormalizationFactor()[label];

			if(probability > mUpdateControl.getThreshouldConfidenceSequence()) {
				newSequence.addElement((String)sequence.x(i), label);
			}

			if(LabelEncoding.isEntity(label)) {
				hasEntity = true;
				averageEntityProbability += probability;
				normalization++;
			}
		}

		if(hasEntity && averageEntityProbability/normalization > mUpdateControl.getThreshouldConfidenceSequence()) {
			mUpdateControl.addSequence(newSequence);
		}
	}

	protected void labelSequenceAvgProbConsiderAllTweet(DataSequence sequence) {

		Map<String, SequenceLabel> proccessedSequenceMap = PreprocessData.preprocessSequence(
				sequence, mActivityControl.getDataPreprocessorList());

		int label;
		int termNumberAnalyzed = 0;
		double averageEntityProbability = 0;

		for(int i = 0; i < sequence.length(); i++) {
			label = getLabel(sequence, proccessedSequenceMap, i);

			if(mLabelCalculator.getNormalizationFactor()[label] != 0) {
				averageEntityProbability += mLabelCalculator.getLabelProbabilities()[label]/mLabelCalculator.getNormalizationFactor()[label];
				termNumberAnalyzed++;
			}
		}

		if(averageEntityProbability/termNumberAnalyzed >= mUpdateControl.getThreshouldConfidenceSequence()) {
			mUpdateControl.addSequence(sequence);
		}
	}

	protected void labelSequenceAvgProbConsiderAllPartialTweet(DataSequence sequence) {

		Map<String, SequenceLabel> proccessedSequenceMap = PreprocessData.preprocessSequence(
				sequence, mActivityControl.getDataPreprocessorList());

		int label;
		double probability = 0;

		Sequence newSequence = new Sequence();

		for(int i = 0; i < sequence.length(); i++) {
			label = getLabel(sequence, proccessedSequenceMap, i);
			probability = mLabelCalculator.getLabelProbabilities()[label]/mLabelCalculator.getNormalizationFactor()[label];

			if(probability > mUpdateControl.getThreshouldConfidenceSequence()) {
				newSequence.addElement((String)sequence.x(i), label);
			}
		}

		if(newSequence.size() > 1) {
			mUpdateControl.addSequence(newSequence);
		}
	}


	/** Only for experimental propose **/

	@SuppressWarnings("unused")
	@Override
	protected int getLabel(DataSequence pSequence, Map<String, SequenceLabel> pProccessedSequenceMap, int pIndex) {

		//-- Force Y(i) To be equals an outside label
		pSequence.set_y(pIndex, LabelEncoding.getOutsideLabel());

		int vMostProbablyLabel = mLabelCalculator.calculateMostProbablyLabel(pIndex, pSequence, pProccessedSequenceMap,
				mActivityControl.getDataPreprocessorList(), mActivityControl.getFilterList());

		if(Debug.LabelFile.printTermIdentifiedAsEntity && LabelEncoding.BILOU.isEntity(
				BILOU.values()[vMostProbablyLabel].name())){
			System.out.println(pSequence.x(pIndex) + "(" + LabelEncoding.BILOU.
					isEntity(BILOU.values()[pSequence.y(pIndex)].name()) + ")");
		}

		//-- Force set Y(i) equals most probably label
		pSequence.set_y(pIndex, vMostProbablyLabel);

		if(LabelEncoding.isEntity(vMostProbablyLabel)) {
			mEntityList.add((String)pSequence.x(pIndex));
		}

		return(vMostProbablyLabel);
	}

	@Override
	protected void labelStreamSub(List<List<String>> streamList) {

		SequenceSet inputSequenceSet =  SequenceSetHandler.
				getSequenceSetFromStream(streamList, BILOU.Outside.ordinal());

		DataSequence sequence;

		while(inputSequenceSet.hasNext()) {

			//-- Label Sequence
			sequence = inputSequenceSet.next();
			labelSequence(sequence);
		}
	}

	@Override
	protected void printFilterStatistics() {

		System.out.println("\nFilter Statistics to Assigned Entity Labels:\n--------------------------------------------");
		String vMessageFormat = "\t{0} ({1}) {2} {3} {4}";

		for(AbstractFilter filter : mActivityControl.getFilterList()) {
			if(filter.getFilterProbability().getTotalAssignedLabelsInTest() > 0
					&& Debug.Filter.printFilterStatisticsWherePrecisionLessEqual
					>= filter.getFilterProbability().getFilterPrecisionInTest()) {
				System.out.println(MessageFormat.format(vMessageFormat,
						filter.getActivityName() ,
						filter.getPreprocesingTypeName(),
						filter.getFilterProbability().getFilterPrecisionStatisticInTest(),
						filter.getFilterProbability().getFilterRecallStatisticInTest(),
						filter.getFilterProbability().getFilterF1StatisticInTest()));

				printTermStatisticsByFilter(filter);
			}
			filter.clearFilterProbability();
		}
		System.out.println("--------------------------------------------");

	}

	protected void printTermStatisticsByFilter(AbstractFilter filter) {
		if(Debug.LabelFile.printCorrectEntityTermIdentifiedByFilter &&
				filter.getFilterProbability().getEntityTermsCorrectAssignedInTest().length() > 0) {
			System.out.println(MessageFormat.format("\t- Entity terms correct assigned({0}): {1}", filter.getFilterProbability().getEntityTermsCorrectAssignedInTestList().size(),
					filter.getFilterProbability().getEntityTermsCorrectAssignedInTest()));
		}

		if(Debug.LabelFile.printMissedEntityTermIdentifiedByFilter &&
				filter.getFilterProbability().getEntityTermsMissedAssignedInTest().length() > 0) {
			System.out.println(MessageFormat.format("\t- Entity terms missed({0}): {1}", filter.getFilterProbability().getEntityTermsMissedAssignedInTestList().size(),
					filter.getFilterProbability().getEntityTermsMissedAssignedInTest()));
		}

		if(Debug.LabelFile.printWrongEntityTermIdentifiedByFilter &&
				filter.getFilterProbability().getEntityTermsWrongAssignedInTest().length() > 0) {
			System.out.println(MessageFormat.format("\t- Entity terms wrong({0}): {1}", filter.getFilterProbability().getEntityTermsWrongAssignedInTestList().size(),
					filter.getFilterProbability().getEntityTermsWrongAssignedInTest()));
		}
	}
}
