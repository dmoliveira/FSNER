package lbd.FSNER.Model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lbd.FSNER.Collection.DataCollection;
import lbd.FSNER.Configuration.Constants;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Configuration.FilterParameters;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Utils.FileUtils;
import lbd.FSNER.Utils.SimpleStopWatch;
import lbd.fsner.labelFile.level2.AbstractLabelFileLevel2;

public abstract class AbstractNERModel implements Serializable {

	private static final long serialVersionUID = 1L;

	//-- Main Objects
	protected AbstractActivityControl mActivityControl;
	protected AbstractUpdateControl mUpdateControl;
	protected AbstractLabelFile mLabelFile;
	protected AbstractLabelFileLevel2 mLabelFileLevel2;
	protected AbstractEvaluator mEvaluator;

	//-- Filters
	protected FilterParameters mFilterParameters;

	//-- Files
	protected String mTrainingFilenameAddress;
	protected DataCollection mDataCollection;

	//-- used to calculate generalization
	protected Map<String, Object> mEntityMap;
	protected int mEntityGeneralizedNumber;

	public AbstractNERModel(DataCollection pDataCollection) {
		mDataCollection = pDataCollection;
		mEntityMap = new HashMap<String, Object>();
	}

	public void allocModel(String [] pInitializeFilenameList) {

		setSubComponents();

		allocModelSub(pInitializeFilenameList);
	}

	protected abstract void allocModelSub(String [] pInitializeFilenameList);

	public void load(String pTrainingFilenameAddress) {

		mTrainingFilenameAddress = pTrainingFilenameAddress;

		loadSub(pTrainingFilenameAddress);
		addLoadedEntity();
	}

	protected void loadSub(String pTrainingFilenameAddress) {
		mActivityControl.startActivityControl(pTrainingFilenameAddress);
		trainLabelLevel2();
	}

	private void trainLabelLevel2() {
		if(Parameters.LabelFileLevel2.mIsToUseLabelLevel2) {
			SimpleStopWatch vStopWatch = new SimpleStopWatch();
			vStopWatch.start();
			mLabelFileLevel2.trainLabelSequenceLevel2(mActivityControl.getSequenceList(),
					mActivityControl.getClassNameSingleFilterMap(), mLabelFile);
			if (Debug.ActivityControl.showElapsedTime) {
				vStopWatch.show("Labeling Level 2 Time:");
			}
		}
	}

	public void label(String pFilenameAddressToLabel, boolean pIsUnrealibleSituation) {
		if(mLabelFileLevel2 == null) {
			throw new NullPointerException("Error: Initiate Label File Level 2.");
		}

		labelFileSub(pFilenameAddressToLabel, pIsUnrealibleSituation);
		addLabeledEntity();
	}

	public void labelFile(String pFilenameAddressToLabel) {
		labelFileSub(pFilenameAddressToLabel, false);
		addLabeledEntity();
	}

	protected void labelFileSub(String pFilenameAddressToLabel, boolean pIsUnrealibleSituation) {
		mUpdateControl.restartForNextUpdate();
		mLabelFile.labelFile(pFilenameAddressToLabel, pIsUnrealibleSituation);
	}

	public void labelStream(List<List<String>> pStreamList, boolean pIsUnrealibleSituation) {
		labelStreamSub(pStreamList, pIsUnrealibleSituation);
		addLabeledEntity();
	}

	protected void labelStreamSub(List<List<String>> streamList, boolean isUnrealibleSituation) {
		mUpdateControl.restartForNextUpdate();
		mLabelFile.labelStream(streamList, isUnrealibleSituation);
	}

	public void updateWithLabeledFile(String filenameAddressToLabel) {
		mUpdateControl.restartForNextUpdate();
		mLabelFile.updateWithLabeledFile(filenameAddressToLabel);
		update("LabeledFile");
	}

	public void evaluate(String taggedFilenameAddress, String testFilenameAddress, String observation) {
		mEvaluator.evaluate(taggedFilenameAddress, testFilenameAddress, observation);
	}

	public void writeEvaluation(String observation) {
		mEvaluator.writeOverviewStatistics(observation);
	}

	public void update(String updateSource) {
		updateSub(updateSource);
		mUpdateControl.restartForNextUpdate();
	}

	protected abstract void updateSub(String pUpdateSource);

	public void addModelActivityControl(AbstractActivityControl pActivityControl) {
		mActivityControl = pActivityControl;
	}

	public void addModelUpdateControl(AbstractUpdateControl pUpdateControl) {
		mUpdateControl = pUpdateControl;
	}

	public void addModelLabelFile(AbstractLabelFile pLabelFile) {
		mLabelFile = pLabelFile;
	}

	public void addLabelFileLevel2(AbstractLabelFileLevel2 pLabelFileLevel2) {
		mLabelFileLevel2 = pLabelFileLevel2;
		mLabelFile.addLabelFileLevel2(pLabelFileLevel2);
	}

	public void addModelEvaluator(AbstractEvaluator evaluator) {
		this.mEvaluator = evaluator;
	}

	protected void setSubComponents() {
		mLabelFile.addActivityControl(mActivityControl);
		mLabelFile.addUpdateControl(mUpdateControl);
	}

	protected void addLoadedEntity() {
		//System.out.println("Entity in Train: ");
		for(String entityValue : mActivityControl.getEntitySet()) {
			if(!mEntityMap.containsKey(entityValue)) {
				mEntityMap.put(entityValue, null);
				//System.out.println(entityValue);
			}
		}
	}

	protected void addLabeledEntity() {
		//System.out.println("Entity in Test: ");
		for(String entityValue : mLabelFile.getEntityList()) {
			if(!mEntityMap.containsKey(entityValue)) {
				mEntityMap.put(entityValue, null);
				mEntityGeneralizedNumber++;
				//System.out.println(entityValue);
			}
		}
	}

	public void addFilterParameters(FilterParameters pFilterParameters) {
		mFilterParameters = pFilterParameters;
	}

	public void writeNERModelSpecification(ArrayList<String> pFilenameList, FilterParameters pFilterParameters) {
		if(!Debug.NERModel.writeNERModelSpecification) {
			return;
		}

		try {
			Writer vOutputFile = FileUtils.createOutputStreamWriter(
					FileUtils.createCommonFilename(pFilenameList),
					Constants.FileExtention.NERModelSpecifications);

			vOutputFile.write("-- FS-NER Specification\n\n");
			vOutputFile.write("\tFilters used: " + pFilterParameters + "\n");
			vOutputFile.write("\tUse filter combination: " + Parameters.SimpleActivityControl.isToCombineFilters + "\n");
			vOutputFile.write("\n\tDetails of filters\n");
			for(AbstractFilter cFilter : mActivityControl.getFilterList()) {
				vOutputFile.write("\t\t" + cFilter.getActivityName() + "\n");
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

	public boolean hasSequenceToUpdate() {
		return(mUpdateControl.sequenceListToUpdate.size() > 0);
	}

	public AbstractActivityControl getActivityControl() {
		return mActivityControl;
	}

	public String getContextFilenameAddress() {
		return(mTrainingFilenameAddress);
	}

	public AbstractLabelFile getLabelFile() {
		return mLabelFile;
	}

	public List<String> getUnknownTermList() {
		return(mLabelFile.getUnknownTermList());
	}

	public String getTaggedFilenameAddress() {
		return(mLabelFile.getTaggedFilenameAddress());
	}

	public Map<String, Object> getEntityMap() {
		return(mEntityMap);
	}

	public Set<String> getEntitySet() {
		return(mActivityControl.getEntitySet());
	}

	public AbstractUpdateControl getUpdateControl() {
		return(mUpdateControl);
	}

}
