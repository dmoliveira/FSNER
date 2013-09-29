package lbd.FSNER.Model;


import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.Statistic.TermLevelStatisticsAnalysis;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Model.AbstractFilter.FilterMode;
import lbd.FSNER.Utils.SimpleStopWatch;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.DataSequence;
import lbd.data.handler.HandlingSequenceSet;
import lbd.data.handler.SequenceSet;
import lbd.data.handler.HandlingSequenceSet.FileType;

public abstract class AbstractLabelFile implements Serializable {

	private static final long serialVersionUID = 1L;
	protected AbstractActivityControl mActivityControl;
	protected AbstractUpdateControl mUpdateControl;
	protected AbstractLabelFileLabelCalculatorModel mLabelCalculator;

	protected SimpleStopWatch mStopWatch;

	protected String mTaggedFilenameAddress;
	protected String mFilenameAddressToLabel;
	protected ArrayList<String> mEntityList;

	protected int mSequenceNumber;

	protected TermLevelStatisticsAnalysis mTermLevelStatisticsAnalysis;

	public AbstractLabelFile() {
		mEntityList = new ArrayList<String>();
		mTermLevelStatisticsAnalysis = new TermLevelStatisticsAnalysis();
	}

	public void addActivityControl(AbstractActivityControl activityControl) {
		this.mActivityControl = activityControl;
	}

	public void addSequenceScoreCalculatorModel(AbstractLabelFileLabelCalculatorModel labelCalculator) {
		this.mLabelCalculator = labelCalculator;
	}

	public void labelFile(String filenameAddressToLabel, boolean isUnrealibleSituation) {

		//-- File to label and set the reliability of the file to label
		this.mFilenameAddressToLabel = filenameAddressToLabel;
		mLabelCalculator.setIsUnrealibleSituation(isUnrealibleSituation);

		//-- Set Filter Mode
		AbstractFilter.setFilterMode(FilterMode.inLabel);

		mSequenceNumber = 0;

		//-- Clear some objects
		mEntityList.clear();
		mLabelCalculator.cleanUnknownTermLists();
		cleanFilterStatisticsInLabelProcess();

		mStopWatch = new SimpleStopWatch();
		mStopWatch.start();
		labelFileSub(filenameAddressToLabel);
		mTermLevelStatisticsAnalysis.printAllStatistics();
		mLabelCalculator.removeRestrictedTermFromUnknownTermList();

		if(Debug.LabelFile.showUnknownTerms) {
			mLabelCalculator.printUnknownTermList();
		}

		if(Debug.LabelFile.showElapsedTime){
			mStopWatch.show("Label File Time:");
			System.out.println();
		}
	}

	public void labelStream(ArrayList<ArrayList<String>> streamList, boolean isUnrealibleSituation) {

		//-- set the reliability of the file to label
		mLabelCalculator.setIsUnrealibleSituation(isUnrealibleSituation);

		//-- Set Filter Mode
		AbstractFilter.setFilterMode(FilterMode.inLabel);

		//-- Clear some objects
		mEntityList.clear();
		mLabelCalculator.cleanUnknownTermLists();
		cleanFilterStatisticsInLabelProcess();

		mStopWatch = new SimpleStopWatch();
		mStopWatch.start();
		labelStreamSub(streamList);
		mLabelCalculator.removeRestrictedTermFromUnknownTermList();
		if(Debug.LabelFile.showUnknownTerms) {
			mLabelCalculator.printUnknownTermList();
		}

		if(Debug.LabelFile.showElapsedTime){
			mStopWatch.show("\nLabel Stream Time:");
			System.out.println();
		}
	}

	public void updateWithLabeledFile(String filenameAddressToLabel) {

		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(filenameAddressToLabel,
				FileType.TRAINING, false);

		DataSequence sequence;

		//-- Clear some objects
		mEntityList.clear();

		cleanFilterStatisticsInLabelProcess();
		if(Debug.LabelFile.showUnknownTerms) {
			mLabelCalculator.printUnknownTermList();
		}

		mStopWatch = new SimpleStopWatch();
		mStopWatch.start();

		while(inputSequenceSet.hasNext()) {

			sequence = inputSequenceSet.next();

			/*for(int i = 0; i < sequence.length(); i++) {
				if(LabelEncoding.isEntity(sequence.y(i))) {
					updateControl.addSequence(sequence);
					break;
				}
			}*/

			mUpdateControl.addSequence(sequence);
		}

		mLabelCalculator.removeRestrictedTermFromUnknownTermList();
		if(Debug.LabelFile.showUnknownTerms) {
			mLabelCalculator.printUnknownTermList();
		}

		if(Debug.LabelFile.showElapsedTime){
			mStopWatch.show("\nLabel Stream Time:");
			System.out.println();
		}
	}

	protected abstract void labelStreamSub(ArrayList<ArrayList<String>> streamList);

	protected abstract void labelFileSub(String filenameAddressToLabel);

	public abstract DataSequence labelSequence(DataSequence sequence);

	protected abstract int setLabel(DataSequence sequence, HashMap<String, SequenceLabel> proccessedSequenceMap, int index);

	protected void printNumberedSequence(DataSequence pSequence) {
		if(pSequence == null) {
			System.out.println(MessageFormat.format("[!] WARNING: Sequence no. {0} is null.", mSequenceNumber));
			return;
		}

		String vSequence = Symbol.EMPTY;
		for(int i = 0; i < pSequence.length(); i++) {
			vSequence += pSequence.x(i) + ((i < pSequence.length() - 1)? Symbol.SPACE : Symbol.EMPTY);
		}

		System.out.println(mSequenceNumber + ". " + vSequence);
	}

	protected abstract void printFilterStatistics();

	protected void addUpdateControl(AbstractUpdateControl updateControl) {
		this.mUpdateControl = updateControl;
	}

	protected void cleanFilterStatisticsInLabelProcess() {
		for(AbstractFilter filter : mActivityControl.getFilterList()) {
			filter.getFilterProbability().clear();
		}
	}

	public ArrayList<String> getUnknownTermList() {
		return(mLabelCalculator.getUnknownTermList());
	}

	public ArrayList<String> getEntityList() {
		return(mEntityList);
	}

	public String getFileAddressToLabel() {
		return(mFilenameAddressToLabel);
	}

	public String getTaggedFilenameAddress() {
		return(mTaggedFilenameAddress);
	}
}
