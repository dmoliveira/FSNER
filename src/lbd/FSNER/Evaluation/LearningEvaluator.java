package lbd.FSNER.Evaluation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Evaluation.Component.TermLabeled;
import lbd.FSNER.Utils.LabelEncoding.BILOU;

public class LearningEvaluator extends SimpleBILOUEvaluator {

	private static final long serialVersionUID = 1L;

	protected String trainingFilenameAddress;

	protected HashMap<String, Object> entityTermsInTrainingFile;
	protected HashMap<String, Object> termsInTestFile;
	protected HashMap<String, Object> entityTermsTrainingInTestFile;

	protected boolean showOnlyResult;

	public LearningEvaluator(ArrayList<String> pFilenameList, boolean showOnlyResult, OutputStyle outputStyle) {
		super(pFilenameList, outputStyle);
		this.showOnlyResult = showOnlyResult;
	}

	public void evaluate(String trainingFilenameAddress, String taggedFilenameAddress, String testFilenameAddress, String observation) {
		this.trainingFilenameAddress = trainingFilenameAddress;
		super.evaluate(taggedFilenameAddress, testFilenameAddress, observation);
	}

	@Override
	protected void beforeEvaluate(String taggedFilenameAddress, String testFilenameAddress) throws IOException {

		entityTermsInTrainingFile = new HashMap<String, Object>();
		termsInTestFile = new HashMap<String, Object>();
		entityTermsTrainingInTestFile = new HashMap<String, Object>();

		BufferedReader trainingIn = new BufferedReader(new InputStreamReader(
				new FileInputStream(trainingFilenameAddress), Parameters.dataEncoding));

		String lineTraining;
		String termLowerCase;
		TermLabeled termTraining;

		while((lineTraining = trainingIn.readLine()) != null) {

			if(!lineTraining.isEmpty()) {

				termTraining = getTerm(lineTraining);
				termLowerCase = termTraining.getTerm().toLowerCase();

				if(!termTraining.getLabel().equals(BILOU.Outside.name())) {
					entityTermsInTrainingFile.put(termLowerCase, null);
				}
			}
		}

		trainingIn.close();
	}

	@Override
	@Deprecated
	public void evaluate(String taggedFilenameAddress, String testFilenameAddress, String observation) {};

	@Override
	protected void evaluateTerm(TermLabeled termFromTagged,
			TermLabeled termFromTest, int lineNumber) {

		if(!termFromTest.getLabel().equals(BILOU.Outside.name())) {
			termsInTestFile.put(termFromTagged.getTerm().toLowerCase(), null);
		}

		if(entityTermsInTrainingFile.containsKey(termFromTagged.getTerm().toLowerCase())) {
			super.evaluateTerm(termFromTagged, termFromTest, lineNumber);

			if(!termFromTest.getLabel().equals(BILOU.Outside.name())) {
				entityTermsTrainingInTestFile.put(termFromTagged.getTerm().toLowerCase(), null);
			}
		}

	}

	@Override
	protected void writeAndPrintResults(Writer out, String message,
			boolean pIsToWrite, boolean pIsToPrint) throws IOException {

		String applicableCasesMessage = (showOnlyResult)? "" : "Learning applicable cases: ";
		applicableCasesMessage += (((double)entityTermsTrainingInTestFile.size())/termsInTestFile.size()) + "\t";
		super.writeAndPrintResults(out, applicableCasesMessage + message, pIsToWrite, pIsToPrint);
	}
}
