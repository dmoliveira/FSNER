package lbd.CRF;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.CRF.LabelMap.OrthographicState;
import lbd.Report.EntityStatisticalAnalysis;

public class CRFStatistics {

	private String ENCODE_USED = "ISO-8859-1";

	private double truePositive;
	private double falsePositive;
	private double falseNegative;
	private double trueNegative;
	private double specificity;
	private double accuracy;
	private double alpha;
	private double beta;
	private double likelihoodRatioPositive;
	private double likelihoodRatioNegative;
	private double precision;
	private double recall;
	private double fMeasure;

	private Sequence seqToCheck;
	private Sequence seqValid;

	private int lineChecked;

	private static HashMap<String, StatisticalEntry> statisticalEntryMap;

	public CRFStatistics() {
		restartAllVariables();
	}

	/*
	 * seqToCheck : File that need to be checked. seqValid: File that have the
	 * correct label answers.
	 */
	public CRFStatistics(Sequence seqToCheck, Sequence seqValid) {

		this.seqToCheck = seqToCheck;
		this.seqValid = seqValid;

		restartAllVariables();
	}

	public void initializeStatisticalEntry() {
		statisticalEntryMap = new HashMap<String, StatisticalEntry>();
	}

	private void restartAllVariables() {
		lineChecked = 0;
		truePositive = 0;
		falsePositive = 0;
		falseNegative = 0;
		trueNegative = 0;
		specificity = 0;
		accuracy = 0;
		alpha = 0;
		beta = 0;
		likelihoodRatioPositive = 0;
		likelihoodRatioNegative = 0;
		precision = 0;
		recall = 0;
		fMeasure = 0;
	}

	public String checkSequence(boolean isSegment) {

		String sequenceAnalysis = "";

		if (!isSegment)
			sequenceAnalysis = checkSequence();
		else
			checkSequenceSemiCRF();

		return (sequenceAnalysis);
	}

	/**
	 * For now, the program only do binary checking.
	 */
	public String checkSequence() {

		// Alternative Multiple Confusion Matrix (0 - Beginning, 1 - Inside, 2 -
		// Last, 3 - Outside, 4 - Unit-Token)
		String tagSet = "Context";// "BILOU";
		String sequence = "";

		// int j = -1;

		for (int i = 0; i < seqValid.size(); i++) {

			/** Only to calculate Etzioni CRF **/
			/*
			 * ++j;
			 * 
			 * if(seqToCheck.size() > j &&
			 * !((String)seqValid.x(i)).equals((String)seqToCheck.x(j))) {
			 * if(seqValid.size() > i+1 && seqToCheck.size() > j+1) {
			 * while(seqToCheck.size() > j+1 &&
			 * !seqValid.x(i+1).equals(seqToCheck.x(j++))); j--; } continue; }
			 * 
			 * if(seqToCheck.size() == j){ break; }
			 */

			lineChecked++;

			if (tagSet.equals("IO")) {
				checkIO(i);
			} else if (tagSet.equals("BIO")) {
				checkBIO(i);
			} else if (tagSet.equals("BILOU")) {
				sequence += " " + checkBILOU(i, i);
			} else if (tagSet.equals("PreInSuFix")) {
				checkPreInSuFix(i);
			} else if (tagSet.equals("Context")) {
				checkContext(i);
			} else if (tagSet.equals("POSTagPTBR")) {
				sequence += " " + checkPOSTagPTBR(i);
			} else if (tagSet.equals("OrthographicalState")) {
				checkOrthographicalState(i);
			}
		}
		
		EntityStatisticalAnalysis.writeEntityStatisticAnalysis("",
				statisticalEntryMap);
		
		return (sequence);

		// Binary Confusion Matrix (0 - Other, 1 - Team)
		/*
		 * for (int i = 0; i < seqValid.size(); i++) { if (seqValid.y(i) == 1) {
		 * if (seqToCheck.y(i) == 1) truePositive++; else falseNegative++; }
		 * else if (seqValid.y(i) == 0) { if (seqToCheck.y(i) == 0)
		 * trueNegative++; else falsePositive++; } }
		 */
	}

	private void checkPreInSuFix(int i) {

		// 3 = Inside
		if (seqValid.y(i) == 3) {
			if (seqToCheck.y(i) == 3) {
				truePositive++;
			} else {
				falseNegative++;
			}
		} else {
			if (seqToCheck.y(i) == 3) {
				falsePositive++;
			} else {
				trueNegative++;
			}
		}
	}

	public void checkIO(int i) {
		// 0 -- Inside, 1 -- Outside
		if (seqValid.y(i) == 0) { // Inside
			if (seqToCheck.y(i) == 0)
				truePositive++;
			else
				falseNegative++;

		} else if (seqValid.y(i) == 1) { // Outside
			if (seqToCheck.y(i) == 1)
				trueNegative++;
			else
				falsePositive++;
		}
	}

	private void checkBIO(int i) {
		if (seqValid.y(i) == 0) { // Beginning
			if (seqToCheck.y(i) == 0 || seqToCheck.y(i) == 1)
				truePositive++;
			else
				falseNegative++;

		} else if (seqValid.y(i) == 1) { // Inside
			if (seqToCheck.y(i) == 1 || seqToCheck.y(i) == 0)
				truePositive++;
			else
				falseNegative++;

		} else if (seqValid.y(i) == 2) { // Outside
			if (seqToCheck.y(i) == 2)
				trueNegative++;
			else
				falsePositive++;
		}
	}

	private String checkBILOU(int i, int j) {

		String tokenAnalysis = "";
		StatisticalEntry statisticalEntry = null;

		if (!statisticalEntryMap.containsKey(seqValid.x(i))
				&& (seqValid.y(i) != 3 || seqToCheck.y(i) != 3)
				&& seqValid.y(i) < 5)
			statisticalEntryMap.put((String) seqValid.x(i),
					new StatisticalEntry((String) seqValid.x(i)));

		statisticalEntry = statisticalEntryMap.get(seqValid.x(i));

		if (seqValid.y(i) == 0) { // Beginning
			if (seqToCheck.y(j) == 0 || seqToCheck.y(j) == 1
					|| seqToCheck.y(j) == 2 || seqToCheck.y(j) == 4) {

				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
				tokenAnalysis = "(" + (String) seqToCheck.x(j) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(j) + ")";
			} else if (seqToCheck.y(j) == 3) {

				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
				tokenAnalysis = "[ " + (String) seqToCheck.x(j) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(j) + "]";
			}

		} else if (seqValid.y(i) == 1) { // Inside
			if (seqToCheck.y(j) == 1 || seqToCheck.y(j) == 0
					|| seqToCheck.y(j) == 2 || seqToCheck.y(j) == 4) {

				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
				tokenAnalysis = "(" + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(j) + ")";
			} else if (seqToCheck.y(j) == 3) {

				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
				tokenAnalysis = "[ " + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(j) + "]";
			}

		} else if (seqValid.y(i) == 2) { // Last
			if (seqToCheck.y(j) == 2 || seqToCheck.y(j) == 0
					|| seqToCheck.y(j) == 1 || seqToCheck.y(j) == 4) {

				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
				tokenAnalysis = "(" + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(j) + ")";
			} else if (seqToCheck.y(j) == 3) {

				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
				tokenAnalysis = "[ " + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(j) + "]";
			}
		} else if (seqValid.y(i) == 3) { // Outside
			if (seqToCheck.y(j) == 3) {

				trueNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addTrueNegative();
				tokenAnalysis = (String) seqToCheck.x(i);
			} else if (seqToCheck.y(j) == 0 || seqToCheck.y(j) == 1
					|| seqToCheck.y(j) == 2 || seqToCheck.y(j) == 4) {

				falsePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addFalsePositive();
				tokenAnalysis = "[ " + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(j) + "]";
			}
		} else if (seqValid.y(i) == 4) { // Unit-Token
			if (seqToCheck.y(j) == 4 || seqToCheck.y(j) == 0
					|| seqToCheck.y(j) == 1 || seqToCheck.y(j) == 2) {

				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
				tokenAnalysis = "(" + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(j) + ")";
			} else if (seqToCheck.y(j) == 3) {

				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
				tokenAnalysis = "[ " + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(j) + "]";
			}
		}

		return (tokenAnalysis);
	}

	/**
	 * Labels: 0 - Beginning; 1 - Inside; 2 - Last; 3 - Outside; 4 - UnitToken;
	 * 5 - Article; 6 - Preposition; 7 - Conjunction; 8 - Pronoun; 9 - Adverb;
	 * 10 - Numeral;
	 * 
	 * @param i
	 * @return
	 */
	private String checkPOSTagPTBR(int i) {

		String tokenAnalysis = "";
		String term = ((String) seqValid.x(i)).toLowerCase();
		StatisticalEntry statisticalEntry = null;

		if (!statisticalEntryMap.containsKey(term) && seqValid.y(i) != 3
				&& seqValid.y(i) < 5)
			statisticalEntryMap.put(term, new StatisticalEntry(term));

		statisticalEntry = statisticalEntryMap.get(seqValid.x(i));

		if (seqValid.y(i) == 0) { // Beginning
			if (seqToCheck.y(i) == 0 || seqToCheck.y(i) == 1
					|| seqToCheck.y(i) == 2 || seqToCheck.y(i) == 4) {

				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
				tokenAnalysis = "(" + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(i) + ")";
			} else {

				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
				tokenAnalysis = "[ " + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(i) + "]";
			}

		} else if (seqValid.y(i) == 1) { // Inside
			if (seqToCheck.y(i) == 1 || seqToCheck.y(i) == 0
					|| seqToCheck.y(i) == 2 || seqToCheck.y(i) == 4) {

				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
				tokenAnalysis = "(" + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(i) + ")";
			} else {

				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
				tokenAnalysis = "[ " + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(i) + "]";
			}

		} else if (seqValid.y(i) == 2) { // Last
			if (seqToCheck.y(i) == 2 || seqToCheck.y(i) == 0
					|| seqToCheck.y(i) == 1 || seqToCheck.y(i) == 4) {

				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
				tokenAnalysis = "(" + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(i) + ")";
			} else {

				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
				tokenAnalysis = "[ " + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(i) + "]";
			}
		} else if (seqValid.y(i) == 3 || seqValid.y(i) == 5
				|| seqValid.y(i) == 6 || seqValid.y(i) == 7
				|| seqValid.y(i) == 8 || seqValid.y(i) == 9
				|| seqValid.y(i) == 10 || seqValid.y(i) == 11) { // Outside,
																	// Article,
																	// Preprosition,
																	// Conjunction,
																	// Pronoun,
																	// Adverb,
																	// Numeral
			if (seqToCheck.y(i) == 3 || seqToCheck.y(i) == 5
					|| seqToCheck.y(i) == 6 || seqToCheck.y(i) == 7
					|| seqToCheck.y(i) == 8 || seqToCheck.y(i) == 9
					|| seqToCheck.y(i) == 10 || seqToCheck.y(i) == 11) {

				trueNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addTrueNegative();
				tokenAnalysis = (String) seqToCheck.x(i);
			} else if (seqToCheck.y(i) == 0 || seqToCheck.y(i) == 1
					|| seqToCheck.y(i) == 2 || seqToCheck.y(i) == 4) {

				falsePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addFalsePositive();
				tokenAnalysis = "[ " + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(i) + "]";
			}
		} else if (seqValid.y(i) == 4) { // Unit-Token
			if (seqToCheck.y(i) == 4 || seqToCheck.y(i) == 0
					|| seqToCheck.y(i) == 1 || seqToCheck.y(i) == 2) {

				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
				tokenAnalysis = "(" + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(i) + ")";
			} else {

				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
				tokenAnalysis = "[ " + (String) seqToCheck.x(i) + " "
						+ seqValid.y(i) + "/" + seqToCheck.y(i) + "]";
			}
		}

		return (tokenAnalysis);
	}

	private void checkContext(int i) { // 0-Begin, 1-Inside, 2-Last, 3-Other,
										// 4-UnitToken
		// 5-InputContext or 5-Context, 6-SharedContext, 7-OutputContext

		StatisticalEntry statisticalEntry = null;
		boolean debug = false;

		if (!statisticalEntryMap.containsKey(seqValid.x(i))
				&& (seqValid.y(i) != 3 || seqToCheck.y(i) != 3)
				&& seqValid.y(i) < 5)
			statisticalEntryMap.put((String) seqValid.x(i),
					new StatisticalEntry((String) seqValid.x(i)));

		statisticalEntry = statisticalEntryMap.get(seqValid.x(i));

		if (seqValid.y(i) == 0) { // Beginning
			if (seqToCheck.y(i) == 0 || seqToCheck.y(i) == 1
					|| seqToCheck.y(i) == 2 || seqToCheck.y(i) == 4) {
				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
				if (i > 0 && debug) {
					System.out.print("\n(" + seqToCheck.x(i - 1) + ") "
							+ seqToCheck.x(i) + " [OK!] ");
					printSequence(i);
				}
			} else {
				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
				if (i > 0 && debug) {
					System.out.print("\n(" + seqToCheck.x(i - 1) + ") "
							+ seqToCheck.x(i) + " [WRONG-FN!] ");
					printSequence(i);
				}
				;
			}

		} else if (seqValid.y(i) == 1) { // Inside
			if (seqToCheck.y(i) == 1 || seqToCheck.y(i) == 0
					|| seqToCheck.y(i) == 2 || seqToCheck.y(i) == 4) {
				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
			} else {
				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
			}

		} else if (seqValid.y(i) == 2) { // Last
			if (seqToCheck.y(i) == 2 || seqToCheck.y(i) == 0
					|| seqToCheck.y(i) == 1 || seqToCheck.y(i) == 4) {
				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
			} else {
				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
			}
		} else if (seqValid.y(i) == 3 || seqValid.y(i) == 5
				|| seqValid.y(i) == 6 || seqValid.y(i) == 7) { // Outside ==
																// Context
			if (seqToCheck.y(i) == 3 || seqToCheck.y(i) == 5
					|| seqToCheck.y(i) == 6 || seqToCheck.y(i) == 7) {
				trueNegative++;
			} else {
				falsePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addFalsePositive();
				if (i > 0 && debug) {
					System.out.print("\n(" + seqToCheck.x(i - 1) + ") "
							+ seqToCheck.x(i) + " [WRONG-FP!] ");
					printSequence(i);
				}
				;
			}
		} else if (seqValid.y(i) == 4) { // Unit-Token
			if (seqToCheck.y(i) == 4 || seqToCheck.y(i) == 0
					|| seqToCheck.y(i) == 1 || seqToCheck.y(i) == 2) {
				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
				if (i > 0 && debug) {
					System.out.print("\n(" + seqToCheck.x(i - 1) + ") "
							+ seqToCheck.x(i) + " [OK!] ");
					printSequence(i);
				}
				;
			} else {
				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
				if (i > 0 && debug) {
					System.out.print("\n(" + seqToCheck.x(i - 1) + ") "
							+ seqToCheck.x(i) + " [WRONG-FN!] ");
					printSequence(i);
				}
				;
			}
		}
	}

	private void checkOrthographicalState(int i) { // 0-Begin, 1-Inside, 2-Last, 3-Other,
										// 4-UnitToken
		// 5-InputContext or 5-Context, 6-SharedContext, 7-OutputContext

		StatisticalEntry statisticalEntry = null;

		if (!statisticalEntryMap.containsKey(seqValid.x(i))
				&& (seqValid.y(i) != 3 || seqToCheck.y(i) != 3)
				&& seqValid.y(i) < 5)
			statisticalEntryMap.put((String) seqValid.x(i),
					new StatisticalEntry((String) seqValid.x(i)));

		statisticalEntry = statisticalEntryMap.get(seqValid.x(i));

		if (seqValid.y(i) == 0) { // Beginning
			if (seqToCheck.y(i) == 0 || seqToCheck.y(i) == 1
					|| seqToCheck.y(i) == 2 || seqToCheck.y(i) == 4) {
				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
				if (i > 0) {
					System.out.print("\n(" + seqToCheck.x(i - 1) + ") "
							+ seqToCheck.x(i) + " [OK!] ");
					printSequence(i);
				}
			} else {
				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
				if (i > 0) {
					System.out.print("\n(" + seqToCheck.x(i - 1) + ") "
							+ seqToCheck.x(i) + " [WRONG-FN!] ");
					printSequence(i);
				}
				;
			}

		} else if (seqValid.y(i) == 1) { // Inside
			if (seqToCheck.y(i) == 1 || seqToCheck.y(i) == 0
					|| seqToCheck.y(i) == 2 || seqToCheck.y(i) == 4) {
				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
			} else {
				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
			}

		} else if (seqValid.y(i) == 2) { // Last
			if (seqToCheck.y(i) == 2 || seqToCheck.y(i) == 0
					|| seqToCheck.y(i) == 1 || seqToCheck.y(i) == 4) {
				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
			} else {
				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
			}
		} else if (seqValid.y(i) == 3 || seqValid.y(i) >= 5) { // Outside == OrthographicalState
															
			if (seqToCheck.y(i) == 3 || seqToCheck.y(i) >= 5) {
				trueNegative++;
			} else {
				falsePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addFalsePositive();
				if (i > 0) {
					System.out.print("\n(" + seqToCheck.x(i - 1) + ") "
							+ seqToCheck.x(i) + " [WRONG-FP!] ");
					printSequence(i);
				}
				;
			}
		} else if (seqValid.y(i) == 4) { // Unit-Token
			if (seqToCheck.y(i) == 4 || seqToCheck.y(i) == 0
					|| seqToCheck.y(i) == 1 || seqToCheck.y(i) == 2) {
				truePositive++;
				if (statisticalEntry != null)
					statisticalEntry.addTruePositive();
				if (i > 0) {
					System.out.print("\n(" + seqToCheck.x(i - 1) + ") "
							+ seqToCheck.x(i) + " [OK!] ");
					printSequence(i);
				}
				;
			} else {
				falseNegative++;
				if (statisticalEntry != null)
					statisticalEntry.addFalseNegative();
				if (i > 0) {
					System.out.print("\n(" + seqToCheck.x(i - 1) + ") "
							+ seqToCheck.x(i) + " [WRONG-FN!] ");
					printSequence(i);
				}
				;
			}
		}
	}

	protected void printSequence(int index) {

		String sequence = "";

		for (int i = 0; i < seqValid.length(); i++)
			sequence += ((i == index) ? "(" : "") + seqValid.x(i)
					+ ((i == index) ? ") " : " ");

		System.out.print(sequence);
	}

	public void checkSequenceSemiCRF() {
		// 0 -- Outside, 1 -- Inside
		for (int i = 0; i < seqValid.size(); i++) {
			if (seqValid.y(i) == 0) { // Outside
				if (seqToCheck.y(i) == 0)
					trueNegative++;
				else
					falsePositive++;

			} else if (seqValid.y(i) == 1) { // Inside
				if (seqToCheck.y(i) == 1)
					truePositive++;
				else
					falseNegative++;
			}
		}
	}

	public void writeStatisticalEntry(String outputFilenameAddress) {

		Iterator<Entry<String, StatisticalEntry>> ite = statisticalEntryMap
				.entrySet().iterator();

		DecimalFormat decimalFormat = new DecimalFormat("#.######");
		String outputMessage;
		int termNumber = 1;

		try {

			Writer out = new OutputStreamWriter(new FileOutputStream(
					outputFilenameAddress), ENCODE_USED);
			StatisticalEntry statisticalEntry;

			out.write("*** Report: Statistical Entity Result\n");

			while (ite.hasNext()) {

				statisticalEntry = ite.next().getValue();
				statisticalEntry.calculateStatisticalMeasures();

				if (!Double.isNaN(statisticalEntry.getfMeasure())) {
					outputMessage = (termNumber++) + ". "
							+ statisticalEntry.getId();
					outputMessage += " P: "
							+ decimalFormat.format(100 * statisticalEntry
									.getPrecision()) + "%";
					outputMessage += " R: "
							+ decimalFormat.format(100 * statisticalEntry
									.getRecall()) + "%";
					outputMessage += " F: "
							+ decimalFormat.format(100 * statisticalEntry
									.getfMeasure()) + "%\n";

					out.write(outputMessage);
				}

			}

			out.flush();
			out.close();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void calculateStatisticalMeasures(double truePositive,
			double falsePositive, double falseNegative, double trueNegative) {
		setTruePositive(truePositive);
		setFalsePositive(falsePositive);
		setFalseNegative(falseNegative);
		setTrueNegative(trueNegative);

		calculateStatisticalMeasures();
	}

	public void calculateStatisticalMeasures() {
		calculatePrecision();
		calculateRecall();
		calculateFMeasure();
		calculateSpecificity();
		calculateAccuracy();
		calculateAlpha();
		calculateBeta();
		calculateLikelihoodRatioPositive();
		calculateLikelihoodRatioNegative();
	}

	public double calculatePrecision() {
		precision = truePositive / (truePositive + falsePositive);
		return (precision);
	}

	public double calculateRecall() {
		recall = truePositive / (truePositive + falseNegative);
		return (recall);
	}

	public double calculateFMeasure() {
		fMeasure = (2 * precision * recall) / (precision + recall);
		return (0);
	}

	public double calculateSpecificity() {
		specificity = trueNegative / (trueNegative + falsePositive);
		return (specificity);
	}

	public double calculateAccuracy() {
		accuracy = (truePositive + trueNegative)
				/ (truePositive + falsePositive + falseNegative + trueNegative);
		return (accuracy);
	}

	public double calculateAlpha() {
		alpha = 1 - specificity;
		return (alpha);
	}

	public double calculateBeta() {
		beta = 1 - recall;
		return (beta);
	}

	public double calculateLikelihoodRatioPositive() {
		likelihoodRatioPositive = recall / (1 - specificity);
		return (likelihoodRatioPositive);
	}

	public double calculateLikelihoodRatioNegative() {
		likelihoodRatioNegative = (1 - recall) / specificity;
		return (likelihoodRatioNegative);
	}

	public void sumConfusionMatrixElements(double truePositive,
			double falsePositive, double falseNegative, double trueNegative) {
		sumToTruePositive(truePositive);
		sumToFalsePositive(falsePositive);
		sumToFalseNegative(falseNegative);
		sumToTrueNegative(trueNegative);
	}

	public void sumToTruePositive(double truePositive) {
		this.truePositive += truePositive;
	}

	public void sumToFalsePositive(double falsePositive) {
		this.falsePositive += falsePositive;
	}

	public void sumToFalseNegative(double falseNegative) {
		this.falseNegative += falseNegative;
	}

	public void sumToTrueNegative(double trueNegative) {
		this.trueNegative += trueNegative;
	}

	/************************************************************************************
	 * 
	 * Gets and Sets
	 * 
	 ************************************************************************************/

	public double getTruePositive() {
		return truePositive;
	}

	public void setTruePositive(double truePositive) {
		this.truePositive = truePositive;
	}

	public double getFalsePositive() {
		return falsePositive;
	}

	public void setFalsePositive(double falsePositive) {
		this.falsePositive = falsePositive;
	}

	public double getFalseNegative() {
		return falseNegative;
	}

	public void setFalseNegative(double falseNegative) {
		this.falseNegative = falseNegative;
	}

	public double getTrueNegative() {
		return trueNegative;
	}

	public void setTrueNegative(double trueNegative) {
		this.trueNegative = trueNegative;
	}

	public double getSpecificity() {
		return specificity;
	}

	public void setSpecificity(double specificity) {
		this.specificity = specificity;
	}

	public double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getBeta() {
		return beta;
	}

	public void setBeta(double beta) {
		this.beta = beta;
	}

	public double getLikelihoodRatioPositive() {
		return likelihoodRatioPositive;
	}

	public void setLikelihoodRatioPositive(double likelihoodRatioPositive) {
		this.likelihoodRatioPositive = likelihoodRatioPositive;
	}

	public double getLikelihoodRatioNegative() {
		return likelihoodRatioNegative;
	}

	public void setLikelihoodRatioNegative(double likelihoodRatioNegative) {
		this.likelihoodRatioNegative = likelihoodRatioNegative;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public double getFmeasure() {
		return fMeasure;
	}

	public void setFmeasure(double fMeasure) {
		this.fMeasure = fMeasure;
	}

	public Sequence getSeqToCheck() {
		return seqToCheck;
	}

	public void setSeqToCheck(Sequence seqToCheck) {
		this.seqToCheck = seqToCheck;
	}

	public Sequence getSeqValid() {
		return seqValid;
	}

	public void setSeqValid(Sequence seqValid) {
		this.seqValid = seqValid;
	}

}
