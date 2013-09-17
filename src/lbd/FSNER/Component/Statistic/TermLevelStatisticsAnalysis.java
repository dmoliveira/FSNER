package lbd.FSNER.Component.Statistic;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;

import lbd.FSNER.Configuration.Debug;
import lbd.FSNER.Configuration.Parameters;

public class TermLevelStatisticsAnalysis implements Serializable{
	private static final long serialVersionUID = 1L;

	protected ArrayList<String> mTermsLabeledAsEntities;
	protected ArrayList<String> mWrongTermsLabeledAsEntities;
	protected ArrayList<String> mMissedEntityTerms;

	public TermLevelStatisticsAnalysis() {
		mTermsLabeledAsEntities = new ArrayList<String>();
		mWrongTermsLabeledAsEntities = new ArrayList<String>();
		mMissedEntityTerms = new ArrayList<String>();
	}

	public void printAllStatistics() {
		if(Debug.LabelFile.printTermsLabeledAsEntity) {
			printTermsLabeledAsEntities();
		}

		if(Debug.LabelFile.printWrongTermsLabeledAsEntity) {
			printWrongTermsLabeledAsEntities();
		}

		if(Debug.LabelFile.printMissedEntityTerms) {
			printMissedEntityTerms();
		}
	}

	public void printList(ArrayList<String> pTermList, String pTypeList) {
		System.out.println(MessageFormat.format("-- Has a total of ({0}) {1}.", pTermList.size(), pTypeList));
		String vBuffer = "    ";

		for(String term : pTermList) {
			if(vBuffer.length() < Parameters.Display.NUMBERS_CHARACTERS_FOR_WRAP) {
				vBuffer += term + ", ";
			} else {
				System.out.println(vBuffer);
				vBuffer = "    ";
			}
		}

		if(!vBuffer.equals("    ")) {
			System.out.println(vBuffer);
		}
	}

	public void printTermsLabeledAsEntities() {
		printList(mTermsLabeledAsEntities, "terms labeled as entities");
	}

	public void printWrongTermsLabeledAsEntities() {
		printList(mWrongTermsLabeledAsEntities, "wrong terms labeled as entities");
	}

	public void printMissedEntityTerms() {
		printList(mMissedEntityTerms, "missed entity terms");
	}

	public void addTermLabeledAsEntity(String pTerm) {
		mTermsLabeledAsEntities.add(pTerm);
	}

	public void addWrongTermsLabeledAsEntities(String pTerm) {
		mWrongTermsLabeledAsEntities.add(pTerm);
	}

	public void addMissedEntityTerms(String pTerm) {
		mMissedEntityTerms.add(pTerm);
	}
}
