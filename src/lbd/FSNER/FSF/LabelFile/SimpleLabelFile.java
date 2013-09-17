package lbd.FSNER.FSF.LabelFile;

import iitb.CRF.DataSequence;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;
import lbd.CRF.Sequence;
import lbd.CRF.SequenceSet;
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
import lbd.FSNER.Utils.WriterOutput;

public class SimpleLabelFile extends AbstractLabelFile {

	protected final static double SCORE_THRESHOLD = 0;

	public SimpleLabelFile() {
		super();
	}

	@Override
	protected void labelFileSub(String pFilenameAddressToLabel) {

		try {
			OutputStreamWriter vOutputFile = FileUtils.createOutputStreamWriter(pFilenameAddressToLabel, Constants.FileExtention.Tagged);
			mTaggedFilenameAddress = Parameters.generateOutputFilenameAddress(pFilenameAddressToLabel, Constants.FileExtention.Tagged);

			SequenceSet vInputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(pFilenameAddressToLabel,
					FileType.TRAINING, false);

			DataSequence vSequence;

			while(vInputSequenceSet.hasNext()) {

				//-- Label Sequence
				mSequenceNumber++;
				vSequence = vInputSequenceSet.next();
				labelSequence(vSequence);

				WriterOutput.writeSequence(vOutputFile, vSequence);
			}

			if(Debug.LabelFile.printFilterStatistics) {
				printFilterStatistics();
			}

			vOutputFile.flush();
			vOutputFile.close();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public DataSequence labelSequence(DataSequence sequence) {

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
		labelSequenceAvgProbEntityConsiderAllTweet(sequence);
		//labelSequenceAvgProbEntityConsiderAllPartialTweet(sequence);
		//labelSequenceAvgProbConsiderAllTweet(sequence);
		//labelSequenceAvgProbConsiderAllPartialTweet(sequence);

		return sequence;
	}

	/** Only for experimental propose **/

	protected void labelSequenceNoProb(DataSequence sequence) {

		HashMap<String, SequenceLabel> proccessedSequenceMap = PreprocessData.preprocessSequence(
				sequence, mActivityControl.getDataPreprocessorList());

		for(int i = 0; i < sequence.length(); i++) {
			setLabel(sequence, proccessedSequenceMap, i);
		}

		mUpdateControl.addSequence(sequence);
	}

	protected void labelSequenceNoProbOnlySeqEntities(DataSequence sequence) {

		HashMap<String, SequenceLabel> proccessedSequenceMap = PreprocessData.preprocessSequence(
				sequence, mActivityControl.getDataPreprocessorList());

		boolean hasEntity = false;
		int label;

		for(int i = 0; i < sequence.length(); i++) {
			label = setLabel(sequence, proccessedSequenceMap, i);

			if(LabelEncoding.isEntity(label)) {
				hasEntity = true;
			}
		}

		if(hasEntity) {
			mUpdateControl.addSequence(sequence);
		}
	}

	protected void labelSequenceAvgProbEntityConsiderAllTweet(DataSequence sequence) {

		HashMap<String, SequenceLabel> proccessedSequenceMap = PreprocessData.preprocessSequence(
				sequence, mActivityControl.getDataPreprocessorList());

		int vOriginalLabel;
		int label;
		double averageEntityProbability = 0;
		boolean hasEntity = false;
		int normalization = 0;

		for(int i = 0; i < sequence.length(); i++) {
			vOriginalLabel = sequence.y(i);
			sequence.set_y(i, LabelEncoding.BILOU.Outside.ordinal());
			label = setLabel(sequence, proccessedSequenceMap, i);

			if(LabelEncoding.isEntity(label)) {
				if(!LabelEncoding.isEntity(vOriginalLabel)) {
					mTermLevelStatisticsAnalysis.addWrongTermsLabeledAsEntities(sequence.x(i) + "(" + mSequenceNumber + ")");
				} else {
					mTermLevelStatisticsAnalysis.addTermLabeledAsEntity((String) sequence.x(i) + "(" + mSequenceNumber + ")");
				}

				hasEntity = true;
				averageEntityProbability += mLabelCalculator.getLabelProbabilities()[label]/mLabelCalculator.getNormalizationFactor()[label];
				normalization++;
			} else if(LabelEncoding.isEntity(vOriginalLabel)) {
				mTermLevelStatisticsAnalysis.addMissedEntityTerms((String) sequence.x(i) + "(" + mSequenceNumber + ")");
			}
		}

		if(hasEntity && averageEntityProbability/normalization > mUpdateControl.getThreshouldConfidenceSequence()) {
			mUpdateControl.addSequence(sequence);
		}
	}

	protected void labelSequenceAvgProbEntityConsiderAllPartialTweet(DataSequence sequence) {

		HashMap<String, SequenceLabel> proccessedSequenceMap = PreprocessData.preprocessSequence(
				sequence, mActivityControl.getDataPreprocessorList());

		int label;
		double probability = 0;
		double averageEntityProbability = 0;
		boolean hasEntity = false;
		int normalization = 0;

		Sequence newSequence = new Sequence();

		for(int i = 0; i < sequence.length(); i++) {
			label = setLabel(sequence, proccessedSequenceMap, i);
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

		HashMap<String, SequenceLabel> proccessedSequenceMap = PreprocessData.preprocessSequence(
				sequence, mActivityControl.getDataPreprocessorList());

		int label;
		int termNumberAnalyzed = 0;
		double averageEntityProbability = 0;

		for(int i = 0; i < sequence.length(); i++) {
			label = setLabel(sequence, proccessedSequenceMap, i);

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

		HashMap<String, SequenceLabel> proccessedSequenceMap = PreprocessData.preprocessSequence(
				sequence, mActivityControl.getDataPreprocessorList());

		int label;
		double probability = 0;

		Sequence newSequence = new Sequence();

		for(int i = 0; i < sequence.length(); i++) {
			label = setLabel(sequence, proccessedSequenceMap, i);
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
	protected int setLabel(DataSequence sequence, HashMap<String, SequenceLabel> proccessedSequenceMap, int index) {

		sequence.set_y(index, LabelEncoding.BILOU.Outside.ordinal());
		int mostProbablyLabel = mLabelCalculator.calculateMostProbablyLabel(index, proccessedSequenceMap,
				mActivityControl.getDataPreprocessorList(), mActivityControl.getFilterList());

		if(Debug.LabelFile.printTermIdentifiedAsEntity && LabelEncoding.BILOU.isEntity(
				BILOU.values()[mostProbablyLabel].name())){
			System.out.println(sequence.x(index) + "(" + LabelEncoding.BILOU.
					isEntity(BILOU.values()[sequence.y(index)].name()) + ")");
		}

		sequence.set_y(index, mostProbablyLabel);
		mEntityList.add((String)sequence.x(index));

		/*if(index == sequence.length() - 1)
			entityLinker.executeLinker(sequence, sequenceLowerCase);*/

		return(mostProbablyLabel);
	}

	@Override
	protected void labelStreamSub(ArrayList<ArrayList<String>> streamList) {

		SequenceSet inputSequenceSet =  HandlingSequenceSet.
				transformStreamInSequenceSet(streamList, BILOU.Outside.ordinal());

		DataSequence sequence;

		while(inputSequenceSet.hasNext()) {

			//-- Label Sequence
			sequence = inputSequenceSet.next();
			labelSequence(sequence);
		}
	}

	@SuppressWarnings("unused")
	@Override
	protected void printFilterStatistics() {

		System.out.println("\nFilter Statistics to Assigned Entity Labels:\n--------------------------------------------");
		String vMessageFormat = "\t{0} ({1}) {2} {3} {4}";

		for(AbstractFilter filter : mActivityControl.getFilterList()) {
			if(filter.getFilterProbability().getTotalAssignedLabelsInTest() > 0) {
				System.out.println(MessageFormat.format(vMessageFormat,
						filter.getActivityName() ,
						filter.getPreprocesingTypeName(),
						filter.getFilterProbability().getFilterStatisticForCorrectAssignedLabelsInTest(),
						filter.getFilterProbability().getFilterStatisticForAssignedLabelsInTest(),
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
