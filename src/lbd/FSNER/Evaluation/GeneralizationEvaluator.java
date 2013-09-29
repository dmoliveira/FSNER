package lbd.FSNER.Evaluation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Configuration.Constants;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Evaluation.Component.TermLabeled;
import lbd.FSNER.Utils.FileUtils;

public class GeneralizationEvaluator extends SimpleBILOUEvaluator {

	private static final long serialVersionUID = 1L;

	protected String trainingFilenameAddress;

	protected HashMap<String, Integer> entityTermsOnlyInTestFile;
	protected HashMap<String, Integer> entityTermsInTestFile;

	protected boolean showOnlyResult;

	public GeneralizationEvaluator(ArrayList<String> pFilenameList, boolean showOnlyResult, OutputStyle outputStyle) {
		super(pFilenameList, outputStyle);
		this.showOnlyResult = showOnlyResult;
	}

	@Override
	protected void initializeOutputFile(ArrayList<String> pFilenameList) {
		try {
			mOutputFile = FileUtils.createOutputStreamWriter(
					FileUtils.createCommonFilename(pFilenameList),
					Constants.FileExtention.generalizationStatistics);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void evaluate(String trainingFilenameAddress, String taggedFilenameAddress, String testFilenameAddress, String observation) {
		this.trainingFilenameAddress = trainingFilenameAddress;
		super.evaluate(taggedFilenameAddress, testFilenameAddress, observation);
	}

	@Override
	protected void beforeEvaluate(String taggedFilenameAddress, String testFilenameAddress) throws IOException {

		entityTermsOnlyInTestFile = new HashMap<String, Integer>();
		entityTermsInTestFile = new HashMap<String, Integer>();

		BufferedReader testIn = new BufferedReader(new InputStreamReader(
				new FileInputStream(testFilenameAddress), Parameters.dataEncoding));

		String lineTest;
		String termLowerCase;
		TermLabeled termTest;

		while((lineTest = testIn.readLine()) != null) {

			if(!lineTest.isEmpty()) {

				termTest = getTerm(lineTest);
				termLowerCase = termTest.getTerm().toLowerCase();

				//if(!termTest.getLabel().equals(BILOU.Outside.name())) {
				entityTermsOnlyInTestFile.put(termLowerCase, null);
				entityTermsInTestFile.put(termLowerCase, null);
				//}
			}
		}

		testIn.close();

		removeEntityTermsCommonInTrainingFile();
	}

	@Override
	@Deprecated
	public void evaluate(String taggedFilenameAddress, String testFilenameAddress, String observation) {};

	protected void removeEntityTermsCommonInTrainingFile()
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException {
		BufferedReader trainingIn = new BufferedReader(new InputStreamReader(
				new FileInputStream(trainingFilenameAddress), Parameters.dataEncoding));

		String lineTraining;
		TermLabeled termTraining;

		while((lineTraining = trainingIn.readLine()) != null) {

			if(!lineTraining.isEmpty()) {

				termTraining = getTerm(lineTraining);

				if(entityTermsOnlyInTestFile.containsKey(termTraining.getTerm().toLowerCase())) {
					/*if(!termTraining.getLabel().equals(BILOU.Outside.name()) &&
						entityTermsOnlyInTestFile.containsKey(termTraining.getTerm().toLowerCase())) {*/
					entityTermsOnlyInTestFile.remove(termTraining.getTerm().toLowerCase());
				}
			}
		}

		trainingIn.close();
	};

	@Override
	protected void evaluateTerm(TermLabeled termFromTagged,
			TermLabeled termFromTest, int lineNumber) {
		if(entityTermsOnlyInTestFile.containsKey(termFromTagged.getTerm().toLowerCase())) {
			super.evaluateTerm(termFromTagged, termFromTest, lineNumber);
		}

	}

	@Override
	protected void writeAndPrintResults(Writer out, String message,
			boolean pIsToWrite, boolean pIsToPrint) throws IOException {

		String applicableCasesMessage = (showOnlyResult)? "" : "Generalization applicable cases: ";
		applicableCasesMessage += (((double)entityTermsOnlyInTestFile.size())/entityTermsInTestFile.size()) + "\t";
		super.writeAndPrintResults(out, applicableCasesMessage + message, pIsToWrite, pIsToPrint);
	}

}
