package lbd.NewModels.Context;

import java.io.Serializable;

import lbd.FSNER.Utils.LabelEncoding.BILOU;

public class ContextUnit implements Serializable {

	private static final long serialVersionUID = 5928671906988916648L;
	
	protected String mainTerm;
	
	protected static int globalContextNumberId;
	protected int contextNumberId;
	
	protected String contextId;
	
	protected double probability;
	protected int frequency, totalFrequency;
	
	public ContextUnit(String contextId) {
		
		this.contextNumberId = ++globalContextNumberId;
		this.contextId = contextId;
		probability = 1;
	}
	
	public ContextUnit(String contextId, String mainTerm, int label) {
		
		this(contextId, mainTerm);
		
		totalFrequency++;
		if(label != BILOU.Outside.ordinal() && label < BILOU.values().length) frequency++;
		
		probability = 0;
	}
	
	public ContextUnit(String contextId, String mainTerm) {
		
		this.contextNumberId = ++globalContextNumberId;
		this.contextId = contextId;
		this.mainTerm = mainTerm;
		probability = 1;
	}
	
	public int getContextNumberId() {
		return(contextNumberId);
	}
	
	public String getContextID() {
		return(contextId);
	}
	
	public void setContextID(String contextID) {
		this.contextId = contextID;
	}
	
	public void setMainTerm(String term) {
		this.mainTerm = term;
	}
	
	public String getMainTerm() {
		return(mainTerm);
	}
}
