package lbd.FSNER.Model;

import java.io.Serializable;

import lbd.FSNER.Utils.Annotations.Comment;

public abstract class AbstractActivity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected static int _globalId;
	protected int mId;
	protected String mActivityName;
	
	@Comment(message="Used only to preparate the activity.")
	protected transient String mInitializeFile;
	
	public AbstractActivity(String pActivityName) {
		mId = _globalId++;
		this.mActivityName = pActivityName;
	}
	
	public abstract void initialize();
	
	public int getId() {
		return(mId);
	}
	
	public String getActivityName() {
		return(mActivityName);
	}
	
	public void setInitializeFile(String pInitializeFile) {
		this.mInitializeFile = pInitializeFile;
	}

}
