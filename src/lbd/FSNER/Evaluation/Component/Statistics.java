package lbd.FSNER.Evaluation.Component;

import java.io.Serializable;

import lbd.FSNER.Configuration.Parameters;

public class Statistics implements Serializable{

	private static final long serialVersionUID = 1L;

	protected int mTP, mFP, mFN, mTN;

	protected double mPrecision;
	protected double mRecall;
	protected double mF1;

	protected boolean mIsInvalid;

	public void addTP() {
		mTP++;
	}

	public void addFP() {
		mFP++;
	}

	public void addFN() {
		mFN++;
	}

	public void addTN() {
		mTN++;
	}

	public void calculateStatistics() {
		mPrecision = ((double)mTP) / (mTP + mFP);
		mRecall = ((double)mTP) / (mTP + mFN);
		mF1 = (2 * getPrecision() * getRecall()) / (getPrecision() + getRecall());

		if(!Parameters.Evaluator.isToUseInvalidResults && Double.isNaN(mRecall)) {
			mIsInvalid = true;
		}
	}

	public double getPrecision() {
		return(Double.isNaN(mPrecision)? 0 : mPrecision);
	}

	public double getRecall() {
		return(Double.isNaN(mRecall)? 0 : mRecall);
	}

	public double getF1() {
		return(Double.isNaN(mF1)? 0 : mF1);
	}

	public boolean isInvalid() {
		return mIsInvalid;
	}
}