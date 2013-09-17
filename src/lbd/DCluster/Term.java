package lbd.DCluster;

import java.io.Serializable;

public class Term implements Serializable {
	
	private static final long serialVersionUID = 2573829384494388667L;

	private static int globalTermId;
	
	protected int termId;
	protected int clusterId;
	
	protected int frequency;
	protected int modelFrequency;
	
	protected String value;
	protected boolean existInModel;
	
	public Term(String value, int frequency, int clusterId) {
		
		termId = ++globalTermId;
		this.clusterId = clusterId;
		
		frequency = 0;
		modelFrequency = 0;
		
		this.value = value;
		this.frequency = frequency;
	}

	public int getTermId() {
		return termId;
	}

	public int getClusterId() {
		return clusterId;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	
	public void addModelFrequency() {
		modelFrequency++;
	}

	public int getModelFrequency() {
		return modelFrequency;
	}

	public void setModelFrequency(int modelFrequency) {
		this.modelFrequency = modelFrequency;
	}

	public String getValue() {
		return value;
	}

	public boolean isExistInModel() {
		return existInModel;
	}

	public void setExistInModel(boolean existInModel) {
		this.existInModel = existInModel;
	}

}
