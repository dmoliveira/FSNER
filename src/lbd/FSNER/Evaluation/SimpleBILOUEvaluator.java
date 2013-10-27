package lbd.FSNER.Evaluation;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Evaluation.Component.TermLabeled;
import lbd.FSNER.Model.AbstractEvaluator;
import lbd.FSNER.Utils.Symbol;

public class SimpleBILOUEvaluator extends AbstractEvaluator{

	private static final long serialVersionUID = 1L;

	private Map<String, Integer> mLabelAssignedWrong;
	private int mTotalLabelAssignedWrong;

	public SimpleBILOUEvaluator(ArrayList<String> pFilenameList, OutputStyle outputStyle) {
		super(pFilenameList, outputStyle);
		mLabelAssignedWrong = new HashMap<String, Integer>();
		mTotalLabelAssignedWrong = 0;
	}

	@Override
	protected void evaluateTerm(TermLabeled pTermFromTagged, TermLabeled pTermFromTest, int pLineNumber) {

		if(!pTermFromTagged.getTerm().equals(pTermFromTest.getTerm())) {
			new Throwable(MessageFormat.format("Error: term \"{0}\" are different from term\"{1}\" (line {2})",
					pTermFromTagged.getTerm(), pTermFromTest.getTerm(), pLineNumber));
		}

		String vLabelTagged = pTermFromTagged.getLabel();
		String vLabelTest = pTermFromTest.getLabel();

		/** Naive Evaluation **/
		/*if(!BILOU.Outside.name().equals(labelTest) && !BILOU.Outside.name().equals(labelTagged)) {
			mCurrentStatisticalFile.addTP();
		} else if (!BILOU.Outside.name().equals(labelTest) && BILOU.Outside.name().equals(labelTagged)) {
			mCurrentStatisticalFile.addFN();
		} else if(BILOU.Outside.name().equals(labelTest) && BILOU.Outside.name().equals(labelTagged)) {
			mCurrentStatisticalFile.addTN();
		} else if(BILOU.Outside.name().equals(labelTest) && !BILOU.Outside.name().equals(labelTagged)) {
			mCurrentStatisticalFile.addFP();
		}*/

		/** Very Strict Evaluation for Entity Labels **/
		if(vLabelTest.equals(Parameters.DataHandler.mLabelEncoding.getOutsideLabel().name())
				&& !vLabelTagged.equals(Parameters.DataHandler.mLabelEncoding.getOutsideLabel().name())) {
			mCurrentStatisticalFile.addFP();
		} else if(!vLabelTest.equals(Parameters.DataHandler.mLabelEncoding.getOutsideLabel().name())) {

			if(!vLabelTagged.equals(vLabelTest)) {
				String vKey = vLabelTest.substring(0,1) + Symbol.HYPHEN + vLabelTagged.substring(0,1);

				if(!mLabelAssignedWrong.containsKey(vKey)) {
					mLabelAssignedWrong.put(vKey, 0);
				}
				mLabelAssignedWrong.put(vKey, mLabelAssignedWrong.get(vKey) + 1);
				mTotalLabelAssignedWrong++;
			}

			if(((Parameters.Evaluator.mIsToEvaluateOnTokenLevel)?
					!vLabelTagged.equals(Parameters.DataHandler.mLabelEncoding.getOutsideLabel().name())
					: vLabelTagged.equals(vLabelTest))) {
				mCurrentStatisticalFile.addTP();
			} else {
				mCurrentStatisticalFile.addFN();
			}
		}
	}

	public void printLabelAssignedWrongStatistics() {
		System.out.println("\n\n-- Label Assigned Wrong In Test");
		for(String cKey : mLabelAssignedWrong.keySet()) {
			System.out.println(cKey + ": " + MessageFormat.format("{0,number,#.##}",
					(100.0 * mLabelAssignedWrong.get(cKey))/mTotalLabelAssignedWrong));
		}
	}

	@Override
	protected void afterEvaluate(String pTaggedFilenameAddress,	String pTestFilenameAddress) throws IOException {
		super.afterEvaluate(pTaggedFilenameAddress, pTestFilenameAddress);
		if(Debug.Evaluator.isToPrintLabelAssignedWrong) {
			printLabelAssignedWrongStatistics();
		}
	}

}
