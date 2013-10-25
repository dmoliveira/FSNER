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
import lbd.FSNER.Utils.LabelEncoding;

public abstract class AbstractNERModel implements Serializable {

	private static final long serialVersionUID = 1L;

	//-- Main Objects
	protected AbstractActivityControl mActivityControl;
	protected AbstractUpdateControl mUpdateControl;
	protected AbstractLabelFile mLabelFile;
	protected AbstractEvaluator mEvaluator;

	//-- Filters
	protected FilterParameters mFilterParameters;

	//-- Files
	protected String mContextFilenameAddress;
	protected DataCollection mDataCollection;

	//-- used to calculate generalization
	protected Map<String, Object> mEntityMap;
	protected int mEntityGeneralizedNumber;

	public AbstractNERModel(DataCollection pDataCollection) {

		//-- Set Label Encoding (Mandatory!)
		LabelEncoding.checkLabelEncoding();

		mDataCollection = pDataCollection;
		mEntityMap = new HashMap<String, Object>();
	}

	public void allocModel(String [] pInitializeFilenameList) {

		setSubComponents();

		allocModelSub(pInitializeFilenameList);
	}

	protected abstract void allocModelSub(String [] pInitializeFilenameList);

	public void load(String pContextFilenameAddress) {

		this.mContextFilenameAddress = pContextFilenameAddress;

		loadSub(pContextFilenameAddress);
		addLoadedEntity();
	}

	protected void loadSub(String pContextFilenameAddress) {
		mActivityControl.startActivityControl(pContextFilenameAddress);
	}

	public void label(String pFilenameAddressToLabel, boolean pIsUnrealibleSituation) {
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

	protected abstract void updateSub(String updateSource);

	public void addModelActivityControl(AbstractActivityControl activityControl) {
		this.mActivityControl = activityControl;
	}

	public void addModelUpdateControl(AbstractUpdateControl updateControl) {
		this.mUpdateControl = updateControl;
	}

	public void addModelLabelFile(AbstractLabelFile labelFile) {
		this.mLabelFile = labelFile;
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
		return(mContextFilenameAddress);
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
