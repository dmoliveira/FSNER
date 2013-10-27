package lbd.FSNER.Model;


import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Component.Statistic.TermLevelStatisticsAnalysis;
import lbd.FSNER.Configuration.Constants;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractFilter.FilterStage;
import lbd.FSNER.Utils.SimpleStopWatch;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;
import lbd.data.handler.SequenceSet;

public abstract class AbstractLabelFile implements Serializable {

	private static final long serialVersionUID = 1L;

	protected AbstractActivityControl mActivityControl;
	protected AbstractUpdateControl mUpdateControl;
	protected AbstractLabelFileLabelCalculatorModel mLabelCalculator;

	protected SimpleStopWatch mStopWatch;

	protected String mTaggedFilenameAddress;
	protected String mFilenameAddressToLabel;
	protected List<String> mEntityList;

	protected int mSequenceNumber;

	protected TermLevelStatisticsAnalysis mTermLevelStatisticsAnalysis;

	public AbstractLabelFile() {
		mEntityList = new ArrayList<String>();
		mTermLevelStatisticsAnalysis = new TermLevelStatisticsAnalysis();
	}

	public void addActivityControl(AbstractActivityControl pActivityControl) {
		this.mActivityControl = pActivityControl;
	}

	public void addSequenceScoreCalculatorModel(AbstractLabelFileLabelCalculatorModel pLabelCalculator) {
		this.mLabelCalculator = pLabelCalculator;
	}

	public void labelFile(String pFilenameAddressToLabel, boolean pIsUnrealibleSituation) {

		//-- File to label and set the reliability of the file to label
		mFilenameAddressToLabel = pFilenameAddressToLabel;
		mLabelCalculator.setIsUnrealibleSituation(pIsUnrealibleSituation);

		//-- Set Filter Mode
		AbstractFilter.setFilterStage(FilterStage.Label);

		mSequenceNumber = 0;

		//-- Clear some objects
		mEntityList.clear();
		mLabelCalculator.cleanUnknownTermLists();
		cleanFilterStatisticsInLabelProcess();

		mStopWatch = new SimpleStopWatch();
		mStopWatch.start();
		labelFileSub(pFilenameAddressToLabel);
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

	public void labelStream(List<List<String>> pStreamList, boolean pIsUnrealibleSituation) {

		//-- set the reliability of the file to label
		mLabelCalculator.setIsUnrealibleSituation(pIsUnrealibleSituation);

		//-- Set Filter Mode
		AbstractFilter.setFilterStage(FilterStage.Label);

		//-- Clear some objects
		mEntityList.clear();
		mLabelCalculator.cleanUnknownTermLists();
		cleanFilterStatisticsInLabelProcess();

		mStopWatch = new SimpleStopWatch();
		mStopWatch.start();
		labelStreamSub(pStreamList);
		mLabelCalculator.removeRestrictedTermFromUnknownTermList();
		if(Debug.LabelFile.showUnknownTerms) {
			mLabelCalculator.printUnknownTermList();
		}

		if(Debug.LabelFile.showElapsedTime){
			mStopWatch.show("\nLabel Stream Time:");
			System.out.println();
		}
	}

	public void updateWithLabeledFile(String pFilenameAddressToLabel) {

		SequenceSet vInputSequenceSet =  Parameters.DataHandler.mSequenceSetHandler.getSequenceSetFromFile(pFilenameAddressToLabel,
				Constants.FileType.TRAIN, false);

		ISequence vSequence;

		//-- Clear some objects
		mEntityList.clear();

		cleanFilterStatisticsInLabelProcess();
		if(Debug.LabelFile.showUnknownTerms) {
			mLabelCalculator.printUnknownTermList();
		}

		mStopWatch = new SimpleStopWatch();
		mStopWatch.start();

		while(vInputSequenceSet.hasNext()) {

			vSequence = vInputSequenceSet.next();

			/*for(int i = 0; i < sequence.length(); i++) {
				if(LabelEncoding.isEntity(sequence.y(i))) {
					updateControl.addSequence(sequence);
					break;
				}
			}*/

			mUpdateControl.addSequence(vSequence);
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

	protected abstract void labelStreamSub(List<List<String>> pStreamList);

	protected abstract void labelFileSub(String pFilenameAddressToLabel);

	public abstract ISequence labelSequence(ISequence pSequence);

	protected abstract int getLabel(ISequence pSequence, Map<String, SequenceLabel> pProccessedSequenceMap, int pIndex);

	protected void printNumberedSequence(ISequence pSequence) {
		if(pSequence == null) {
			System.out.println(MessageFormat.format("[!] WARNING: Sequence no. {0} is null.", mSequenceNumber));
			return;
		}

		String vSequence = Symbol.EMPTY;
		for(int i = 0; i < pSequence.length(); i++) {
			vSequence += pSequence.getToken(i) + ((i < pSequence.length() - 1)? Symbol.SPACE : Symbol.EMPTY);
		}

		System.out.println(mSequenceNumber + ". " + vSequence);
	}

	protected abstract void printFilterStatistics();

	protected void addUpdateControl(AbstractUpdateControl pUpdateControl) {
		this.mUpdateControl = pUpdateControl;
	}

	protected void cleanFilterStatisticsInLabelProcess() {
		for(AbstractFilter filter : mActivityControl.getFilterList()) {
			filter.getFilterProbability().clear();
		}
	}

	public List<String> getUnknownTermList() {
		return(mLabelCalculator.getUnknownTermList());
	}

	public List<String> getEntityList() {
		return(mEntityList);
	}

	public String getFileAddressToLabel() {
		return(mFilenameAddressToLabel);
	}

	public String getTaggedFilenameAddress() {
		return(mTaggedFilenameAddress);
	}
}
