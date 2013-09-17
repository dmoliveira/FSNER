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

public class AmbiguityEvaluator extends SimpleBILOUEvaluator {

	protected enum AmbiguityType {None, Outside, Entity, Both};

	protected HashMap<String, AmbiguityType> termsInTestFile;
	protected HashMap<String, Object> entityTermsInTestFile;
	protected HashMap<String, Object> ambiguityEntityTermsInTestFile;

	protected int totalTermCases;
	protected int totalAmbiguityCases;

	protected boolean showOnlyResult;

	public AmbiguityEvaluator(ArrayList<String> pFilenameList, boolean showOnlyResult, OutputStyle outputStyle) {
		super(pFilenameList, outputStyle);
		this.showOnlyResult = showOnlyResult;
	}

	@Override
	protected void beforeEvaluate(String taggedFilenameAddress, String testFilenameAddress) throws IOException {

		termsInTestFile = new HashMap<String, AmbiguityType>();
		entityTermsInTestFile = new HashMap<String, Object>();
		ambiguityEntityTermsInTestFile = new HashMap<String, Object>();

		BufferedReader testIn = new BufferedReader(new InputStreamReader(
				new FileInputStream(testFilenameAddress), Parameters.dataEncoding));

		String lineTest;
		String termLowerCase;
		TermLabeled termTest;

		while((lineTest = testIn.readLine()) != null) {

			if(!lineTest.isEmpty()) {

				termTest = getTerm(lineTest);
				termLowerCase = termTest.getTerm().toLowerCase();

				if(!termsInTestFile.containsKey(termLowerCase)) {
					termsInTestFile.put(termLowerCase, AmbiguityType.None);
				}

				if(termTest.getLabel().equals(BILOU.Outside.name())) {
					if(AmbiguityType.None == termsInTestFile.get(termLowerCase)) {
						termsInTestFile.put(termLowerCase, AmbiguityType.Outside);
					} else if(AmbiguityType.Entity == termsInTestFile.get(termLowerCase)) {
						termsInTestFile.put(termLowerCase, AmbiguityType.Both);
					}
				} else {
					if(AmbiguityType.None == termsInTestFile.get(termLowerCase)) {
						termsInTestFile.put(termLowerCase, AmbiguityType.Entity);
					} else if(AmbiguityType.Outside == termsInTestFile.get(termLowerCase)) {
						termsInTestFile.put(termLowerCase, AmbiguityType.Both);
					}
				}
			}
		}

		testIn.close();

	};

	@Override
	protected void evaluateTerm(TermLabeled termFromTagged,
			TermLabeled termFromTest, int lineNumber) {

		if(!termFromTest.getLabel().equals(BILOU.Outside.name())) {
			entityTermsInTestFile.put(termFromTest.getTerm().toLowerCase(), null);
		}

		if(termsInTestFile.get(termFromTest.getTerm().toLowerCase()) == AmbiguityType.Both) {
			super.evaluateTerm(termFromTagged, termFromTest, lineNumber);
			ambiguityEntityTermsInTestFile.put(termFromTest.getTerm().toLowerCase(), null);
		}
	}

	@Override
	protected void writeAndPrintResults(Writer out, String message, boolean pIsToWrite,
			boolean pIsToPrint) throws IOException {

		String applicableCasesMessage = (showOnlyResult)? "" : "Ambiguity applicable cases: ";
		applicableCasesMessage += (((double)ambiguityEntityTermsInTestFile.size())/entityTermsInTestFile.size()) + "\t";
		super.writeAndPrintResults(out, applicableCasesMessage + message, pIsToWrite, pIsToPrint);
	}

}
