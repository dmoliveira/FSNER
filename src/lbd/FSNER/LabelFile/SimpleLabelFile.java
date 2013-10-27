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
import lbd.data.handler.ISequence;
import lbd.data.handler.Sequence;
import lbd.data.handler.SequenceSet;
import lbd.fsner.label.encoding.Label;

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

			SequenceSet vInputSequenceSet =  Parameters.DataHandler.mSequenceSetHandler.getSequenceSetFromFile(pFilenameAddressToLabel,
					Constants.FileType.LABEL, false);

			ISequence vSequence = null;

			while(vInputSequenceSet.hasNext()) {

				//-- Label Sequence
				mSequenceNumber++;
				vSequence = vInputSequenceSet.next();
				labelSequence(vSequence);

				Parameters.DataHandler.mSequenceSetHandler.writeSequenceToFile(vOutputFile, vSequence);

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
	public ISequence labelSequence(ISequence pSequence) {

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
	protected void labelSequenceNoProb(ISequence sequence) {

		Map<String, SequenceLabel> proccessedSequenceMap = PreprocessData.preprocessSequence(
				sequence, mActivityControl.getDataPreprocessorList());

		for(int i = 0; i < sequence.length(); i++) {
			getLabel(sequence, proccessedSequenceMap, i);
		}

		mUpdateControl.addSequence(sequence);
	}

	protected void labelSequenceNoProbOnlySeqEntities(ISequence sequence) {

		Map<String, SequenceLabel> proccessedSequenceMap = PreprocessData.preprocessSequence(
				sequence, mActivityControl.getDataPreprocessorList());

		boolean hasEntity = false;
		int label;

		for(int i = 0; i < sequence.length(); i++) {
			label = getLabel(sequence, proccessedSequenceMap, i);

			if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getLabel(label))) {
				hasEntity = true;
			}
		}

		if(hasEntity) {
			mUpdateControl.addSequence(sequence);
		}
	}

	protected void labelSequenceAvgProbEntityConsiderAllTweet(ISequence pSequence) {

		Map<String, SequenceLabel> vProccessedSequenceMap = PreprocessData.preprocessSequence(
				pSequence, mActivityControl.getDataPreprocessorList());

		int vOriginalLabel;
		int vLabel;

		double vAverageEntityProbability = 0;
		boolean vHasEntity = false;
		int vNormalization = 0;

		for(int i = 0; i < pSequence.length(); i++) {
			vOriginalLabel = pSequence.getLabel(i);
			vLabel = getLabel(pSequence, vProccessedSequenceMap, i);

			//-- Used the original term label only for statistics.
			if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getLabel(vLabel))) {
				if(!Parameters.DataHandler.mLabelEncoding.isEntity(Label.getLabel(vOriginalLabel))) {
					mTermLevelStatisticsAnalysis.addWrongTermsLabeledAsEntities(pSequence.getToken(i) + "(" + mSequenceNumber + ")");
				} else {
					mTermLevelStatisticsAnalysis.addTermLabeledAsEntity((String) pSequence.getToken(i) + "(" + mSequenceNumber + ")");
				}

				vHasEntity |= true;
				vAverageEntityProbability += mLabelCalculator.getLabelProbabilities()[vLabel]/mLabelCalculator.getNormalizationFactor()[vLabel];
				vNormalization++;
			} else if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getLabel(vOriginalLabel))) {
				mTermLevelStatisticsAnalysis.addMissedEntityTerms((String) pSequence.getToken(i) + "(" + mSequenceNumber + ")");
			}
		}

		if(vHasEntity && vAverageEntityProbability/vNormalization > mUpdateControl.getThreshouldConfidenceSequence()) {
			mUpdateControl.addSequence(pSequence);
		}
	}

	protected void labelSequenceAvgProbEntityConsiderAllPartialTweet(ISequence sequence) {

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
				newSequence.add((String)sequence.getToken(i), label);
			}

			if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getLabel(label))) {
				hasEntity = true;
				averageEntityProbability += probability;
				normalization++;
			}
		}

		if(hasEntity && averageEntityProbability/normalization > mUpdateControl.getThreshouldConfidenceSequence()) {
			mUpdateControl.addSequence(newSequence);
		}
	}

	protected void labelSequenceAvgProbConsiderAllTweet(ISequence sequence) {

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

	protected void labelSequenceAvgProbConsiderAllPartialTweet(ISequence sequence) {

		Map<String, SequenceLabel> proccessedSequenceMap = PreprocessData.preprocessSequence(
				sequence, mActivityControl.getDataPreprocessorList());

		int label;
		double probability = 0;

		Sequence newSequence = new Sequence();

		for(int i = 0; i < sequence.length(); i++) {
			label = getLabel(sequence, proccessedSequenceMap, i);
			probability = mLabelCalculator.getLabelProbabilities()[label]/mLabelCalculator.getNormalizationFactor()[label];

			if(probability > mUpdateControl.getThreshouldConfidenceSequence()) {
				newSequence.add((String)sequence.getToken(i), label);
			}
		}

		if(newSequence.length() > 1) {
			mUpdateControl.addSequence(newSequence);
		}
	}


	/** Only for experimental propose **/

	@SuppressWarnings("unused")
	@Override
	protected int getLabel(ISequence pSequence, Map<String, SequenceLabel> pProccessedSequenceMap, int pIndex) {

		//-- Force Y(i) To be equals an outside label
		pSequence.setLabel(pIndex, Parameters.DataHandler.mLabelEncoding.getOutsideLabel().ordinal());

		int vMostProbablyLabel = mLabelCalculator.calculateMostProbablyLabel(pIndex, pSequence, pProccessedSequenceMap,
				mActivityControl.getDataPreprocessorList(), mActivityControl.getFilterList());

		if(Debug.LabelFile.printTermIdentifiedAsEntity && Parameters.DataHandler.mLabelEncoding.isEntity(
				Label.getLabel(vMostProbablyLabel))){
			System.out.println(pSequence.getToken(pIndex) + "(" +
					Parameters.DataHandler.mLabelEncoding.isEntity(Label.getLabel(pSequence.getLabel(pIndex))) + ")");
		}

		//-- Force set Y(i) equals most probably label
		pSequence.setLabel(pIndex, vMostProbablyLabel);

		if(Parameters.DataHandler.mLabelEncoding.isEntity(Label.getLabel(vMostProbablyLabel))) {
			mEntityList.add((String)pSequence.getToken(pIndex));
		}

		return(vMostProbablyLabel);
	}

	@Override
	protected void labelStreamSub(List<List<String>> streamList) {

		SequenceSet inputSequenceSet =  Parameters.DataHandler.mSequenceSetHandler.
				getSequenceSetFromStream(streamList, Parameters.DataHandler.mLabelEncoding.getOutsideLabel().ordinal());

		ISequence sequence;

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
