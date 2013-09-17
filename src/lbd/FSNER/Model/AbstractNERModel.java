package lbd.FSNER.Model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Configuration.Constants;
import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Configuration.FilterParameters;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Utils.FileUtils;

public abstract class AbstractNERModel implements Serializable {

	private static final long serialVersionUID = 1L;
	//-- Main Objects
	protected AbstractActivityControl activityControl;
	protected AbstractUpdateControl updateControl;
	protected AbstractLabelFile labelFile;
	protected AbstractEvaluator evaluator;

	protected FilterParameters mFilterParameters;

	//-- Files
	protected String contextFilenameAddress;

	//-- used to calculate generalization
	protected HashMap<String, Object> entityMap;
	protected int entityGeneralizedNumber;

	public AbstractNERModel() {
		entityMap = new HashMap<String, Object>();
	}

	public void allocModel(String [] initializeFilenameList) {

		setSubComponents();

		allocModelSub(initializeFilenameList);
	}

	protected abstract void allocModelSub(String [] initializeFilenameList);

	public void load(String contextFilenameAddress) {

		this.contextFilenameAddress = contextFilenameAddress;

		loadSub(contextFilenameAddress);
		addLoadedEntity();
	}

	protected void loadSub(String contextFilenameAddress) {
		activityControl.startActivityControl(contextFilenameAddress);
	}

	public void label(String filenameAddressToLabel, boolean isUnrealibleSituation) {
		labelFileSub(filenameAddressToLabel, isUnrealibleSituation);
		addLabeledEntity();
	}

	public void labelFile(String filenameAddressToLabel) {
		labelFileSub(filenameAddressToLabel, false);
		addLabeledEntity();
	}

	protected void labelFileSub(String filenameAddressToLabel, boolean isUnrealibleSituation) {
		updateControl.restartForNextUpdate();
		labelFile.labelFile(filenameAddressToLabel, isUnrealibleSituation);
	}

	public void labelStream(ArrayList<ArrayList<String>> streamList, boolean isUnrealibleSituation) {
		labelStreamSub(streamList, isUnrealibleSituation);
		addLabeledEntity();
	}

	protected void labelStreamSub(ArrayList<ArrayList<String>> streamList, boolean isUnrealibleSituation) {
		updateControl.restartForNextUpdate();
		labelFile.labelStream(streamList, isUnrealibleSituation);
	}

	public void updateWithLabeledFile(String filenameAddressToLabel) {
		updateControl.restartForNextUpdate();
		labelFile.updateWithLabeledFile(filenameAddressToLabel);
		update("LabeledFile");
	}

	public void evaluate(String taggedFilenameAddress, String testFilenameAddress, String observation) {
		evaluator.evaluate(taggedFilenameAddress, testFilenameAddress, observation);
	}

	public void writeEvaluation(String observation) {
		evaluator.writeOverviewStatistics(observation);
	}

	public void update(String updateSource) {
		updateSub(updateSource);
		updateControl.restartForNextUpdate();
	}

	protected abstract void updateSub(String updateSource);

	public void addModelActivityControl(AbstractActivityControl activityControl) {
		this.activityControl = activityControl;
	}

	public void addModelUpdateControl(AbstractUpdateControl updateControl) {
		this.updateControl = updateControl;
	}

	public void addModelLabelFile(AbstractLabelFile labelFile) {
		this.labelFile = labelFile;
	}

	public void addModelEvaluator(AbstractEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	protected void setSubComponents() {
		labelFile.addActivityControl(activityControl);
		labelFile.addUpdateControl(updateControl);
	}

	protected void addLoadedEntity() {
		//System.out.println("Entity in Train: ");
		for(String entityValue : activityControl.getEntityList()) {
			if(!entityMap.containsKey(entityValue)) {
				entityMap.put(entityValue, null);
				//System.out.println(entityValue);
			}
		}
	}

	protected void addLabeledEntity() {
		//System.out.println("Entity in Test: ");
		for(String entityValue : labelFile.getEntityList()) {
			if(!entityMap.containsKey(entityValue)) {
				entityMap.put(entityValue, null);
				entityGeneralizedNumber++;
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
			for(AbstractFilter cFilter : activityControl.getFilterList()) {
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
		return(updateControl.sequenceListToUpdate.size() > 0);
	}

	public String getContextFilenameAddress() {
		return(contextFilenameAddress);
	}

	public AbstractLabelFile getLabelFile() {
		return labelFile;
	}

	public ArrayList<String> getUnknownTermList() {
		return(labelFile.getUnknownTermList());
	}

	public String getTaggedFilenameAddress() {
		return(labelFile.getTaggedFilenameAddress());
	}

	public HashMap<String, Object> getEntityMap() {
		return(entityMap);
	}

	public ArrayList<String> getEntityList() {
		return(activityControl.getEntityList());
	}

	public AbstractUpdateControl getUpdateControl() {
		return(updateControl);
	}

}
