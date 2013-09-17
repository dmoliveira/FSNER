package lbd.GenTT;

import iitb.CRF.DataSequence;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;
import lbd.CRF.LabelMap;
import lbd.CRF.SequenceSet;
import lbd.Model.ContextToken;
import lbd.Model.SupportContext;
import lbd.Utils.Utils;

public class SelectSampleByBalancedContext {

	//--
	protected final String ENCODE_USED = "ISO-8859-1";
	protected final String DELIMITER_DOCRF = "|";

	protected double [] alphaContextFrequency;
	protected double [] betaContextFrequency;
	protected double [] tetaContextFrequency;
	protected SupportContext supportContext;

	protected String inputFilenameAddress;
	protected String outputFilenameAddress;
	protected String logOutputFilenameAddress;
	protected String acronym = "-SSamByBalCxt";

	protected int totalSequenceNumber;
	protected int relativeSequenceNumber;
	protected int sequenceNumber;
	protected int windowSize;

	protected double tetaThreshold;
	protected transient Writer logOutput;

	protected final String DECIMAL_FORMAT = "#.#####";

	public SelectSampleByBalancedContext(int windowSize) {
		supportContext = new SupportContext(windowSize, "BILOU", false);
		this.windowSize = windowSize;
	}

	/**
	 * loadSupportContext():
	 */
	protected void loadSupportContext() {

		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(inputFilenameAddress,
				FileType.TRAINING, false);

		supportContext.generateContext(inputSequenceSet);
	}

	public void test(String inputFilenameAddress) {
		this.inputFilenameAddress = inputFilenameAddress;
		loadSupportContext();
	}

	/************************************************** Calculate Context *******************************************************/

	public void selectSamples(String inputFilenameAddress, double threshold) {

		String extention = "{w="+windowSize+",T" + (new DecimalFormat("#.##")).format(threshold) + acronym +"}";

		this.tetaThreshold = threshold;
		this.inputFilenameAddress = inputFilenameAddress;
		this.outputFilenameAddress = Utils.generateOutputFilenameAddress(inputFilenameAddress, extention);
		this.logOutputFilenameAddress = Utils.generateOutputFilenameAddress(inputFilenameAddress, extention, ".log");
		totalSequenceNumber = Utils.countSequenceInFile(inputFilenameAddress);

		outputFilenameAddress = outputFilenameAddress.replace("/Input/", "/Output/");
		logOutputFilenameAddress = logOutputFilenameAddress.replace("/Input/", "/Output/");

		try {

			logOutput = new OutputStreamWriter(new FileOutputStream(logOutputFilenameAddress), ENCODE_USED);

			loadSupportContext();

			//-- Calculate the Alpha, Beta e Teta metrics
			calculateMetrics();

			//--
			System.out.print("Starting to Select Samples");
			selectSampleByTetaThreshould();

			logOutput.flush();
			logOutput.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void calculateMetrics() throws IOException {

		alphaContextFrequency = new double [supportContext.getContextListSize()];
		betaContextFrequency = new double [supportContext.getContextListSize()];
		tetaContextFrequency = new double [supportContext.getContextListSize()];

		//-- @DMZDebug
		System.out.print("Starting to calculate Alpha");
		Date startTime = new Date ();

		//-- Sorry to be O(n²)
		calculateAlpha();

		//-- @DMZDebug
		System.out.println(" (" + ((int)((new Date()).getTime() - startTime.getTime())/1000) + ")s\n");
		System.out.print("Starting to calculate Beta");
		startTime = new Date();

		//-- Sorry to be O(n²)
		calculateBeta();

		//-- @DMZDebug
		System.out.println(" (" + ((int)((new Date()).getTime() - startTime.getTime())/1000) + ")s\n");
		System.out.print("Starting to calculate Teta");

		startTime = new Date();

		//-- It is O(n)
		calculateTeta();
		System.out.println(" (" + ((int)((new Date()).getTime() - startTime.getTime())/1000) + ")s\n");

	}

	protected void calculateAlpha() throws IOException {

		//@DMZDebug
		logOutput.write("*** Confidence Values ****\n");

		//-- put to 0 otherwise will be crashed.
		int tokenIndex = 0;
		double maxAlphaFrequency = -1;

		DecimalFormat decFormat = new DecimalFormat(DECIMAL_FORMAT);

		for(ContextToken contextTokenA : supportContext.getContextList()) {

			for(ContextToken contextTokenB : supportContext.getContextListNotNormalized()) {
				if(supportContext.isContextEquals(contextTokenA, contextTokenB) &&
						!contextTokenA.getTokenValue().equals(contextTokenB.getTokenValue())) {
					alphaContextFrequency[tokenIndex]++;
				}
			}

			//-- This one is for the own context, only to not be zero in some cases
			alphaContextFrequency[tokenIndex]++;

			if(maxAlphaFrequency < alphaContextFrequency[tokenIndex]) {
				maxAlphaFrequency = alphaContextFrequency[tokenIndex];
			}

			tokenIndex++;
		}

		//-- Normalize procedure
		for(int i = 0; i < alphaContextFrequency.length; i++) {
			alphaContextFrequency[i] /= maxAlphaFrequency;

			//@DMZDebug
			logOutput.write("A#" + i + "," + supportContext.getContextList().get(i).getKey());
			logOutput.write(": " + decFormat.format(alphaContextFrequency[i]) + "\n");
		}
	}

	protected void calculateBeta() throws IOException {

		//@DMZDebug
		logOutput.write("\n*** Diversity Values ****\n");

		HashMap<Integer, HashMap<String, Boolean>> contextMap = new HashMap<Integer, HashMap<String, Boolean>>();
		ContextToken context;

		DataSequence sequence;

		String sequenceIndexKey = "";
		String [] sequenceList;

		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(inputFilenameAddress,
				FileType.TRAINING, false);

		for (inputSequenceSet.startScan(); inputSequenceSet.hasNext();) {

			sequence = inputSequenceSet.next();
			sequenceList = Utils.convertSequenceToLowerCase(sequence, sequence.length());

			for(int i = 0; i < sequenceList.length; i++) {

				if(sequence.y(i) != 3) { // label != outside (BILOU)

					context = supportContext.getFastAccessContextList().get(supportContext.generateKeyFromSequence(sequenceList, i, false));
					sequenceIndexKey = generateSequenceIndexKey(sequenceList, i);

					if(context != null && !existContextMap(context.getContextTokenID(), sequenceIndexKey, contextMap)) {
						betaContextFrequency[supportContext.getContextList().indexOf(context)]++;
						contextMap.get(context.getContextTokenID()).put(sequenceIndexKey,true);
					}
				}
			}
		}

		//-- Normalize beta context frequency considering the total number the best context frequency
		normalizeBetaContextFrequency(maxBetaFrequency());//generalContextMap.size());
	}

	protected double maxBetaFrequency() {
		double maxBetaFrequency = -1;

		for(int i = 0; i < betaContextFrequency.length; i++) {
			if(maxBetaFrequency < betaContextFrequency[i]) {
				maxBetaFrequency = betaContextFrequency[i];
			}
		}

		return(maxBetaFrequency);
	}

	protected boolean existContextMap(int idContext, String key,
			HashMap<Integer, HashMap<String, Boolean>> contextListMap) {
		HashMap<String, Boolean> contextMap = contextListMap.get(idContext);

		if(contextMap != null ) {
			return((contextMap.get(key) != null));
		} else {
			contextListMap.put(idContext, new HashMap<String, Boolean>());
			return(false);
		}

	}

	protected void normalizeBetaContextFrequency(double totalBetaFrequency) throws IOException {

		DecimalFormat decFormat = new DecimalFormat(DECIMAL_FORMAT);

		for(int i = 0; i < betaContextFrequency.length; i++) {
			betaContextFrequency[i] /= totalBetaFrequency;

			//@DMZDebug
			logOutput.write("B#" + i + "," + supportContext.getContextList().get(i).getKey());
			logOutput.write(": " + decFormat.format(betaContextFrequency[i]) + "\n");

		}
	}

	protected String generateSequenceIndexKey(String[] seq, int index) {

		String sequenceContextKey = SupportContext.START_LEFT_KEY;

		for(int i = index - 1; 0 <= i; i--) {
			sequenceContextKey += seq[i];
		}

		sequenceContextKey += SupportContext.START_RIGHT_KEY;

		for(int i = index + 1; i < seq.length; i++) {
			sequenceContextKey += seq[i];
		}

		return(sequenceContextKey);
	}

	protected void calculateTeta() throws IOException {

		//@DMZDebug
		logOutput.write("\n*** Teta Values ****\n");

		DecimalFormat decFormat = new DecimalFormat(DECIMAL_FORMAT);
		double alphaBetaProduct;
		double alphaBetaSum;


		for(int i = 0; i < supportContext.getContextListSize(); i++) {

			alphaBetaProduct = (alphaContextFrequency[i] * betaContextFrequency[i]);
			alphaBetaSum = (alphaContextFrequency[i] + betaContextFrequency[i]);

			tetaContextFrequency[i] = 2 * (alphaBetaProduct/alphaBetaSum);

			logOutput.write("T#"+i+","+supportContext.getContextList().get(i).getKey()+": " + decFormat.format(tetaContextFrequency[i]) + "\n");
		}

	}

	/************************************** Apply Confidence & Diversity to Sequence *******************************************/

	protected void selectSampleByTetaThreshould() throws IOException {

		try {

			//@DMZDebug
			logOutput.write("\n*** Teta Sequence Values ****\n");
			System.out.println("\n\tCalculating TetaFactor According with the Document\n");

			totalSequenceNumber = Utils.countSequenceInFile(inputFilenameAddress);

			Date startTime = new Date();
			DecimalFormat decFormat = new DecimalFormat(DECIMAL_FORMAT);
			double tetaSequence;

			int sequenceNumber = 0;
			int addedSequence = 0;

			String debugStatus;

			Writer output = new OutputStreamWriter(new FileOutputStream(outputFilenameAddress), ENCODE_USED);
			ArrayList<Integer> contextIndexList;

			DataSequence sequence;
			String [] sequenceList;

			SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(inputFilenameAddress,
					FileType.TRAINING, false);

			for (inputSequenceSet.startScan(); inputSequenceSet.hasNext();) {

				sequence = inputSequenceSet.next();
				sequenceList = Utils.convertSequenceToLowerCase(sequence, sequence.length());
				sequenceNumber++;

				contextIndexList = getContextIndex(sequenceList, sequence);

				if(contextIndexList.size() > 0) {

					tetaSequence = calculateTetaSequenceValue(contextIndexList);

					logOutput.write("TS#"+ sequenceNumber + ",");
					logOutput.write(contextIndexList.size() + "cxt: " + decFormat.format(tetaSequence) + "\n");

					//-- Write sequence if greater or equals tetaThreshold
					if(tetaSequence >= tetaThreshold) {

						writeSequence(sequence, output);

						//@DMZDebug
						debugStatus = "** #" + (++addedSequence);
						debugStatus += ", inserting instance " + sequenceNumber;
						debugStatus += ", Relative Size: ";
						debugStatus += (new DecimalFormat("#.##")).format(100 * addedSequence/((double)totalSequenceNumber));
						debugStatus += "%, Elipsed Time: " + ((new Date()).getTime() - startTime.getTime())/1000 + "s";
						System.out.println(debugStatus);

					}
				}

			}

			logOutput.write("\n\nFinal Relative Size: " + (new DecimalFormat("#.##")).format(100 * addedSequence/((double)totalSequenceNumber)) + "%");
			logOutput.write("\nTotal Elipsed Time: " + ((new Date()).getTime() - startTime.getTime())/1000 + "s");

			System.out.println("\nTotal Elipsed Time: " + ((new Date()).getTime() - startTime.getTime())/1000 + "s");

			output.flush();
			output.close();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected ArrayList<Integer> getContextIndex(String [] sequenceList, DataSequence sequence) {

		ArrayList<Integer> contextIndexList = new ArrayList<Integer>();
		ContextToken context;
		String currentKey;

		for(int i = 0; i < sequenceList.length; i++) {

			currentKey = supportContext.generateKeyFromSequence(sequenceList, i, false);

			if(sequence.y(i) != 3 && (context = supportContext.getFastAccessContextList().get(currentKey)) != null) {
				contextIndexList.add(supportContext.getContextList().indexOf(context));
			}
		}

		return(contextIndexList);
	}

	protected double calculateTetaSequenceValue(ArrayList<Integer> contextIndexList) {

		double tetaPreCalculated = 0;

		for(Integer index : contextIndexList) {
			tetaPreCalculated += tetaContextFrequency[index];
		}

		tetaPreCalculated /= contextIndexList.size();

		return(tetaPreCalculated);
	}

	protected void writeSequence(DataSequence sequence, Writer output) {

		try {

			for(int i = 0; i < sequence.length(); i++) {
				output.write(sequence.x(i) + DELIMITER_DOCRF + LabelMap.getLabelNameBILOU(sequence.y(i)) + "\n");
			}

			output.write("\n");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
