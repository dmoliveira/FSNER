package lbd.FSNER.Evaluation;

import java.text.MessageFormat;
import java.util.ArrayList;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Evaluation.Component.TermLabeled;
import lbd.FSNER.Model.AbstractEvaluator;
import lbd.FSNER.Utils.LabelEncoding.BILOU;

public class SimpleBILOUEvaluator extends AbstractEvaluator{

	private static final long serialVersionUID = 1L;

	public SimpleBILOUEvaluator(ArrayList<String> pFilenameList, OutputStyle outputStyle) {
		super(pFilenameList, outputStyle);
	}

	@Override
	protected void evaluateTerm(TermLabeled termFromTagged,
			TermLabeled termFromTest, int lineNumber) {

		if(!termFromTagged.getTerm().equals(termFromTest.getTerm())) {
			new Throwable(MessageFormat.format("Error: term \"{0}\" are different from term\"{1}\" (line {2})",
					termFromTagged.getTerm(), termFromTest.getTerm(), lineNumber));
		}

		String labelTagged = termFromTagged.getLabel();
		String labelTest = termFromTest.getLabel();

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

		/** Very Strict Evaluation for Entity Labels**/
		if(labelTest.equals(BILOU.Outside.name()) && !labelTagged.equals(BILOU.Outside.name())) {
			mCurrentStatisticalFile.addFP();
		} else if(!labelTest.equals(BILOU.Outside.name())) {
			if(((Parameters.Evaluator.isToEvaluateOnTokenLevel)? !labelTagged.equals(BILOU.Outside.name()) : labelTagged.equals(labelTest))) {
				mCurrentStatisticalFile.addTP();
			} else {
				mCurrentStatisticalFile.addFN();
			}
		}
	}

}
