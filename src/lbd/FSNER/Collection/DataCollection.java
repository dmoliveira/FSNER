package lbd.FSNER.Collection;

import java.io.Serializable;
import java.util.ArrayList;

public class DataCollection implements Serializable{

	private static final long serialVersionUID = 1L;

	public String mDataCollectionName;
	public String mCollectionAddress;
	public String mDictionaryName;
	public String mDictionaryAddress;
	public String mTermListRestrictionName;
	public ArrayList<String> mFilenameList;
	public ArrayList<String> mReferenceDataList;

	public DataCollection(String pDataCollectionName) {
		mDataCollectionName = pDataCollectionName;
		mFilenameList = new ArrayList<String>();
	}

	public void addFilename(String pFilename) {
		mFilenameList.add(pFilename);
	}

	public String getFilename(int pIndex) {
		return mFilenameList.get(pIndex);
	}

	public void addReferenceData(String pReferenceData) {
		mReferenceDataList.add(pReferenceData);
	}

	public String getReferenceData(int pReferenceData) {
		return mReferenceDataList.get(pReferenceData);
	}

	public int getNumberFiles() {
		return mFilenameList.size();
	}
}
