package lbd.CRF;

import iitb.CRF.CRF;
import iitb.CRF.DataSequence;
import iitb.CRF.NestedCRF;
import iitb.Model.FeatureGenImpl;
import iitb.Model.NestedFeatureGenImpl;
import iitb.Utils.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import lbd.CRF.HandlingSequenceSet.FileType;
import lbd.DCluster.DCluster;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;
import lbd.Utils.Utils;

//import iitb.CRF.CRF;
//import iitb.Model.FeatureGenImpl;

public class CRFExecutor {

	private FeatureGenImpl featureGen;
	private CRF crfModel;

	private final String TAG_FILE_CRF = "-CRFModel.conf";
	private final String TAG_FILE_FEATURE = "-featureGen.conf";

	private final String ENCODE_USED = "ISO-8859-1";

	private Date startTime, endTime;
	private String neutralFileName;
	private String trainFile;
	private String testFile;
	String trainFormat = ".train";
	String testFormat = ".test";
	private String taggedFileCRF;
	private String statisticsFile;
	private String rootDir;
	private String saveDir;
	private String startSaveTime;
	private String modelGraphType; // "naive" ou "semi-markov"
	private boolean isSegment;
	private int numLabels;
	private SequenceSet sequenceSet;
	private Options option;

	transient Writer logOut;
	transient Writer elipsedTime;
	private double threshouldProbabilitySequenceLabeled = 0;

	//-- Global Statistics
	private ArrayList <String> trainingFileName;
	private ArrayList <String> testFileName;
	private ArrayList <CRFStatistics> crfStatisticsReportList;

	//-- DCluster
	private DCluster dCluster;

	public CRFExecutor(String configFile) throws Exception {

		option = new Options();
		option.load(new FileInputStream(configFile));

		neutralFileName = option.getMandatoryProperty("neutralFileName");
		trainFile = option.getMandatoryProperty("trainFile");
		testFile = option.getMandatoryProperty("testFile");
		statisticsFile = option.getMandatoryProperty("statisticsFile");
		rootDir = option.getMandatoryProperty("rootDir");
		saveDir = option.getMandatoryProperty("saveDir");
		modelGraphType = option.getMandatoryProperty("modelGraphType");
		isSegment = (modelGraphType.equals("semi-markov"));
		numLabels = Integer.parseInt(option.getMandatoryProperty("numLabels"));

		//-- DCluster
		String dirDCluster = "./samples/data/bcs2010/DCluster/";
		//String dClusterInputFilename = "Twitter-36K-Pt-br-PlainFormat-DCluster.dcl"; //-- Pt-br
		String dClusterInputFilename = "tweets2009-06-First348K-DCluster.dcl"; //-- En-us

		//dCluster = new DCluster();
		//dCluster.readDCluster(dirDCluster + dClusterInputFilename, dCluster);

		if(dCluster!=null) {
			System.out.println("-- DCluster Initial");
			System.out.println("Term #: " + dCluster.getTermNumber());
		}
	}

	private void allocModel() throws Exception {

		if (modelGraphType.equals("semi-markov")) {
			NestedFeatureGenImpl nfgen = new NestedFeatureGenImpl(numLabels, option);
			featureGen = nfgen;
			crfModel = new NestedCRF(featureGen.numStates(), nfgen, option);
		} else if(modelGraphType.equals("naive")){
			featureGen = new FeatureGenImpl(modelGraphType, numLabels, logOut);
			crfModel = new CRF(featureGen.numStates(), 1, featureGen, option, logOut, threshouldProbabilitySequenceLabeled);
		} else {
			NestedFeatureGenImpl nfgen = new NestedFeatureGenImpl(numLabels, option);
			featureGen = nfgen;
			crfModel = new NestedCRF(featureGen.numStates(), nfgen, option);
		}
	}

	public void train() throws Exception {

		startTime = new Date ();
		File dir = new File(saveDir);
		dir.mkdirs();
		double featureWts[];

		//@DMZDebug
		System.out.println("\nTraining File: " + trainFile + "\n");
		logOut.write("\nTraining File: " + trainFile + "\n\n");

		sequenceSet = HandlingSequenceSet.transformFileInSequenceSet(rootDir + trainFile,
				FileType.TRAINING, isSegment);

		allocModel();
		NewFeatureTypes.setFeatureModeInTrain();

		featureGen.setTrainingFilenameAddress(rootDir + trainFile);
		featureGen.train(sequenceSet);

		NewFeatureTypes.setFeatureModeUnknown();

		//@DMZ DCluster
		if(dCluster != null) {
			String [] sequenceLowerCase;
			DataSequence sequence;
			sequenceSet = HandlingSequenceSet.transformFileInSequenceSet(rootDir + trainFile,
					FileType.TRAINING, isSegment);

			dCluster.resetTermsInModelStatus();

			//System.out.print("Added Term In Model {");

			while(sequenceSet.hasNext()) {
				sequence = sequenceSet.next();
				sequenceLowerCase = Utils.convertSequenceToLowerCase(sequence, sequence.length());
				dCluster.analyzeTrainingModelSequence(sequenceLowerCase);
			}

			//System.out.println("}\n-- In Training TermInModel#: " + dCluster.getTermInModelNumber() + " Term#: " + dCluster.getTermNumber() + "\n");
		}

		//@DMZDebug
		System.out.println("Starting Tranining");
		logOut.write("Starting Tranining\n");
		featureWts = crfModel.train(sequenceSet);

		//@DMZDebug
		String totalTrainingElipsedTime = "Total Training Elipsed Time : " + (int)(((new Date()).getTime() - startTime.getTime())/60000) + "min";
		System.out.println(totalTrainingElipsedTime);
		//elipsedTime.write(totalTrainingElipsedTime);
		logOut.write(totalTrainingElipsedTime + "\n");

		// Util.printDbg("Training done");
		String saveFile = trainFile.substring(0, trainFile.length() - ".train".length());
		crfModel.write(saveDir + saveFile + TAG_FILE_CRF);
		featureGen.write(saveDir + saveFile + TAG_FILE_FEATURE);

		// Util.printDbg("Writing model to " + saveDir + "\n");
		featureGen.displayModel(featureWts);
		//featureGen.model.printGraph();

		//-- Write new contexts
		//NewFeatureTypes.saveOOVSupportContext();
	}

	public void test() throws Exception {

		//@DMZDebug
		//System.out.println("\n--- Test Mode ---");
		logOut.write("\n--- Test Mode ---\n");

		allocModel();
		NewFeatureTypes.setFeatureModeInTest();

		String saveString = trainFile.substring(0, trainFile.length() - trainFormat.length());

		crfModel.read(saveDir + saveString + TAG_FILE_CRF);
		featureGen.read(saveDir + saveString + TAG_FILE_FEATURE);

		doTest();

		//if(NewFeatureTypes.thesaurusManager != null) NewFeatureTypes.thesaurusManager.writeThesaurusManagerObject("ThesaurusManager.bin");

		//-- Write new contexts
		//NewFeatureTypes.saveOOVSupportContext();
	}

	public void doTest() throws Exception {

		//String saveFile = testFile.substring(0, testFile.length() - testFormat.length());
		String saveFile = trainFile.substring(0, trainFile.length() - trainFormat.length());
		taggedFileCRF = saveDir + saveFile + "-CRF.tagged";
		File dir = new File(saveDir);
		dir.mkdirs();
		int lineNumber = 0;
		int numberSequenceSelected = 0;

		SegmentSequence sequence;

		featureGen.setTestFilenameAddress(rootDir + testFile);
		sequenceSet = HandlingSequenceSet.transformFileInSequenceSet(rootDir + testFile,
				FileType.TEST, isSegment);
		Writer out = new OutputStreamWriter(new FileOutputStream(taggedFileCRF));

		System.out.println("-- In Test");

		for (int i = 0; i < sequenceSet.size(); i++) {

			sequence = sequenceSet.get(i);
			lineNumber += sequence.length() + 1;

			//System.out.println("@Debug Line : " + lineNumber);
			crfModel.apply(sequence);
			featureGen.mapStatesToLabels(sequence);

			//--@DMZ DCluster
			String [] originalSequence = Utils.transformSequenceToArray(sequence, sequence.size());
			ArrayList<Integer> posModified = new ArrayList<Integer>();
			boolean wasModified = false;

			if(dCluster != null) {
				String [] cloneSequence = Utils.convertSequenceToLowerCase(originalSequence, sequence.size());

				cloneSequence = dCluster.normalizeSequence(cloneSequence);
				for(int j = 0; j < cloneSequence.length; j++) {
					if(!cloneSequence[j].equals(originalSequence[j].toLowerCase())){// && sequence.y(j) == 3) {

						sequence.set_x(j, cloneSequence[j]);
						//System.out.println(originalSequence[j] + " >> " + cloneSequence[j]);
						wasModified = true;
						posModified.add(j);
					}
				}

				/*if(wasModified) {
					System.out.print("-- Modified: ");
					for(int j = 0; j < cloneSequence.length; j++) {
						System.out.print(((posModified.contains(j))?"[":"") + sequence.x(j) + ((posModified.contains(j))?"] ":" "));
					} System.out.println("-- Original: ");

					for(int j = 0; j < cloneSequence.length; j++) {
						System.out.print(((posModified.contains(j))?"[":"") + originalSequence[j] + ((posModified.contains(j))?"] ":" "));
					} System.out.println();
				}*/

				crfModel.apply(sequence);
				featureGen.mapStatesToLabels(sequence);

				for(int j = 0; j < originalSequence.length; j++) {
					sequence.set_x(j, originalSequence[j]);
				}

				if(wasModified) {
					int prevPos;
					int nextPos;
					for(Integer pos : posModified) {
						prevPos = (pos > 0)?sequence.y(pos-1):-1;
						nextPos = (pos < sequence.length()-1)?sequence.y(pos+1):-1;
						if(sequence.y(pos) != 3) {
							System.out.println( ((prevPos >= 0)?" p("+prevPos+")":"") +
									sequence.x(pos) + ((nextPos >= 0)?" n("+nextPos+")":""));
							sequence.set_x(pos, originalSequence[pos]);
							crfModel.apply(sequence);
							featureGen.mapStatesToLabels(sequence);
						}
					}
				}
			}

			//if(crfModel.probabilityInformationSet.isSequenceReliable(i)) {
			//if(crfModel.probabilityInformationSet.isReliable(i)) { // DMZ-- Reliable
			HandlingSequenceSet.transformSequenceToFile(out, sequence, isSegment);
			numberSequenceSelected++;
			//}

			//--@DMZ DCluster
			if(dCluster != null) {
				System.out.print("Term #: " + dCluster.getTermNumber());
				System.out.print(" Modified #: " + dCluster.getTermModificationNumber());
				System.out.print(" OOV #: " + dCluster.getTermOutOfVocabulary());
				System.out.println(" Necessary Term #: " + (dCluster.getTermModificationNumber() + dCluster.getTermOutOfVocabulary()));
			}
		}

		//crfModel.printPorcentageDivision();

		//System.out.println("Numer Sequence Selected [Threshould "+threshouldProbabilitySequenceLabeled+"]: " + numberSequenceSelected);
		endTime = new Date();

		out.close();

		//-- It makes Statistical Calculations
		doStatistics();
	}

	private void checkFileStatistics(String fileToCheck, String testFile) {

		this.taggedFileCRF = fileToCheck;

		int startTestFile = testFile.lastIndexOf("/");

		rootDir = testFile.substring(0, startTestFile);
		this.testFile = testFile.substring(startTestFile);

		doStatistics();
	}

	public static CRFStatistics doStatisticsLite(String taggedFile, String testFile, boolean isSegment) {

		SequenceSet sequenceSetTaggedFile = HandlingSequenceSet.transformFileInSequenceSet(taggedFile,
				FileType.VALIDATION, isSegment);
		SequenceSet sequenceSetTestFile = HandlingSequenceSet.transformFileInSequenceSet(testFile,
				FileType.TEST, isSegment);

		Sequence sequenceTaggedFile;
		Sequence sequenceTestFile;

		CRFStatistics crfOverviewStats = new CRFStatistics();
		CRFStatistics crfStats;

		double TP, FP, FN, TN;

		String taggedFileNoAddress = taggedFile.substring(taggedFile.lastIndexOf('/') + 1);
		String result;

		if(sequenceSetTaggedFile.size() != sequenceSetTestFile.size()) {
			try {
				throw new IOException();
			} catch (IOException e) {
				System.err.println("Error! Set TaggedFile different from TestFile");
				e.printStackTrace();
			}
		}

		for (int i = 0; i < sequenceSetTestFile.size(); i++) {

			sequenceTestFile = sequenceSetTestFile.get(i);
			sequenceTaggedFile = sequenceSetTaggedFile.get(i);

			if(sequenceTestFile.length() != sequenceTaggedFile.length()) {
				try {
					throw new IOException();
				} catch (IOException e) {
					System.err.println("Error sequence start: " + sequenceTestFile.x(0) + " " + sequenceTestFile.x(1) + " " + sequenceTestFile.x(2));
					e.printStackTrace();
				}
			}

			crfStats = new CRFStatistics(sequenceTaggedFile, sequenceTestFile);

			crfStats.checkSequence(isSegment);

			crfStats.calculateStatisticalMeasures();

			TP    = crfStats.getTruePositive();
			FP    = crfStats.getFalsePositive();
			FN    = crfStats.getFalseNegative();
			TN    = crfStats.getTrueNegative();

			crfOverviewStats.sumConfusionMatrixElements(TP, FP, FN, TN);
		}

		//-- Overview Statistics
		crfOverviewStats.calculateStatisticalMeasures();

		result = "\nResults: " + taggedFileNoAddress + "\t" + crfOverviewStats.getPrecision();
		result += "\t" + crfOverviewStats.getRecall() + "\t" + crfOverviewStats.getFmeasure();
		System.out.print(result);

		return(crfOverviewStats);
	}


	private void doStatistics() {

		SequenceSet sequenceSetCRFFile = HandlingSequenceSet.transformFileInSequenceSet(taggedFileCRF,
				FileType.VALIDATION, isSegment);
		SequenceSet sequenceSetTestFile = HandlingSequenceSet.transformFileInSequenceSet(rootDir + testFile,
				FileType.TEST, isSegment);

		startTime = (startTime == null)? new Date() : startTime;
		endTime = (endTime == null)? new Date() : endTime;

		Sequence sequenceCRFFile;
		Sequence sequenceTestFile;

		CRFStatistics crfOverviewStats = new CRFStatistics();
		CRFStatistics crfStats;

		crfOverviewStats.initializeStatisticalEntry();

		double TP, FP, FN, TN, Sp, Acc,
		alpha, beta, LRP, LRN, P, R, F;

		Writer outCorrectSequence = null;
		Writer outMisclassifiedSequence = null;

		if(trainFile.lastIndexOf("/") != -1) {
			trainFile = trainFile.substring(trainFile.lastIndexOf("/"));
		}

		//saveDir + testFile.substring(0, testFile.length() - ".test".length());
		int formatLength = trainFile.substring(trainFile.lastIndexOf(".")).length();
		String statisticFileName = saveDir + trainFile.substring(0, trainFile.length() - formatLength);
		String sequence;

		String correctSequenceFilenameAddress = saveDir + trainFile.substring(0, trainFile.length() - formatLength) + ".correct";
		String misclassifiedSequenceFilenameAddress = saveDir + trainFile.substring(0, trainFile.length() - formatLength) + ".errors";

		StatisticsReport statsReport = new StatisticsReport(statisticFileName, false);
		statsReport.createHeader(trainFile, testFile, numLabels, modelGraphType, startTime, endTime, (endTime.getTime() - startTime.getTime())/1000);

		//-- Only look for errors in BILOU format for now
		try {
			outCorrectSequence = new OutputStreamWriter(new FileOutputStream(correctSequenceFilenameAddress), ENCODE_USED);
			outMisclassifiedSequence = new OutputStreamWriter(new FileOutputStream(misclassifiedSequenceFilenameAddress), ENCODE_USED);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < sequenceSetTestFile.size(); i++) {

			sequenceTestFile = sequenceSetTestFile.get(i);
			sequenceCRFFile = sequenceSetCRFFile.get(i);

			/*if(sequenceTestFile.length() != sequenceCRFFile.length()) {
				try {
					throw new IOException();
				} catch (IOException e) {
					System.err.println("Error sequence start: " + sequenceTestFile.x(0) + " " + sequenceTestFile.x(1) + " " + sequenceTestFile.x(2));
					e.printStackTrace();
				}
			}*/

			crfStats = new CRFStatistics(sequenceCRFFile, sequenceTestFile);

			sequence = crfStats.checkSequence(isSegment);

			//-- Write the analysis for errors
			writeSequenceErrorAnalysis(i+1, sequence, outCorrectSequence, outMisclassifiedSequence);

			crfStats.calculateStatisticalMeasures();

			TP    = crfStats.getTruePositive();
			FP    = crfStats.getFalsePositive();
			FN    = crfStats.getFalseNegative();
			TN    = crfStats.getTrueNegative();
			Sp    = crfStats.getSpecificity();
			Acc   = crfStats.getAccuracy();
			alpha = crfStats.getAlpha();
			beta  = crfStats.getBeta();
			LRP   = crfStats.getLikelihoodRatioPositive();
			LRN   = crfStats.getLikelihoodRatioNegative();
			P     = crfStats.getPrecision();
			R     = crfStats.getRecall();
			F     = crfStats.getFmeasure();

			crfOverviewStats.sumConfusionMatrixElements(TP, FP, FN, TN);

			statsReport.addLine(i, TP, FP, FN, TN, Sp, Acc, alpha, beta, LRP, LRN, P, R, F);
		}

		//-- Write Detailed Statistics about each term
		crfOverviewStats.writeStatisticalEntry(saveDir + trainFile.substring(0, trainFile.length() - formatLength) + "-DetailedStatEntry.log");

		try {
			outCorrectSequence.flush();
			outCorrectSequence.close();
			outMisclassifiedSequence.flush();
			outMisclassifiedSequence.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-- Overview Statistics
		crfOverviewStats.calculateStatisticalMeasures();

		TP    = crfOverviewStats.getTruePositive();
		FP    = crfOverviewStats.getFalsePositive();
		FN    = crfOverviewStats.getFalseNegative();
		TN    = crfOverviewStats.getTrueNegative();
		Sp    = crfOverviewStats.getSpecificity();
		Acc   = crfOverviewStats.getAccuracy();
		alpha = crfOverviewStats.getAlpha();
		beta  = crfOverviewStats.getBeta();
		LRP   = crfOverviewStats.getLikelihoodRatioPositive();
		LRN   = crfOverviewStats.getLikelihoodRatioNegative();
		P     = crfOverviewStats.getPrecision();
		R     = crfOverviewStats.getRecall();
		F     = crfOverviewStats.getFmeasure();

		//-- Average
		statsReport.createOverviewHeader();
		statsReport.createOverviewLine("Average",TP, FP, FN, TN, Sp, Acc, alpha, beta, LRP, LRN, P, R, F);

		writeLog(trainFile, P, R, F);

		statsReport.createOverviewFooter();
		statsReport.createFooter();

		//-- Global Statistics
		crfStatisticsReportList.add(crfOverviewStats);
	}

	private void doGlobalStatistics() {

		String statisticFileName = saveDir + neutralFileName;

		StatisticsReport statsReport = new StatisticsReport(statisticFileName, true);

		CRFStatistics crfGlobalOverview = new CRFStatistics ();

		double TP = 0;
		double FP = 0;
		double FN = 0;
		double TN = 0;

		double Sp, Acc, alpha, beta,
		LRP, LRN, P, R, F;

		statsReport.createGlobalHeader(trainingFileName, testFileName, this.numLabels, this.modelGraphType, null, null, -1);

		for(int i = 0; i < crfStatisticsReportList.size(); i++) {

			CRFStatistics crfStatistic = crfStatisticsReportList.get(i);

			TP += crfStatistic.getTruePositive();
			FP += crfStatistic.getFalsePositive();
			FN += crfStatistic.getFalseNegative();
			TN += crfStatistic.getTrueNegative();


			statsReport.addLine(true, (i+1), crfStatistic.getTruePositive(), crfStatistic.getFalsePositive(),
					crfStatistic.getFalseNegative(), crfStatistic.getTrueNegative(),
					crfStatistic.getSpecificity(), crfStatistic.getAccuracy(),
					crfStatistic.getAlpha(), crfStatistic.getBeta(),
					crfStatistic.getLikelihoodRatioPositive(), crfStatistic.getLikelihoodRatioNegative(),
					crfStatistic.getPrecision(), crfStatistic.getRecall(),
					crfStatistic.getFmeasure());
		}

		//-- Indices Calculation
		crfGlobalOverview.sumConfusionMatrixElements(TP, FP, FN, TN);
		crfGlobalOverview.calculateStatisticalMeasures();

		Sp    = crfGlobalOverview.getSpecificity();
		Acc   = crfGlobalOverview.getAccuracy();
		alpha = crfGlobalOverview.getAlpha();
		beta  = crfGlobalOverview.getBeta();
		LRP   = crfGlobalOverview.getLikelihoodRatioPositive();
		LRN   = crfGlobalOverview.getLikelihoodRatioNegative();
		P     = crfGlobalOverview.getPrecision();
		R     = crfGlobalOverview.getRecall();
		F     = crfGlobalOverview.getFmeasure();

		//-- Create Test Overview
		statsReport.createOverviewHeader();
		statsReport.createOverviewLine("Average", TP, FP, FN, TN, Sp, Acc, alpha, beta, LRP, LRN, P, R, F);

		writeLog("Global", P, R, F);

		statsReport.createOverviewFooter();
		statsReport.createFooter();

	}

	private void writeSequenceErrorAnalysis(int sequenceNumber, String sequence,
			Writer outCorrectSequence, Writer outMisclassifiedSequence) {
		try {
			if(sequence.indexOf("[ ") >= 0) {
				outMisclassifiedSequence.write(sequenceNumber + "." + sequence + "\n");
			} else {
				outCorrectSequence.write(sequenceNumber + "." + sequence + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void writeLog(String header, double precision, double recall, double fmeasure) {
		try {

			//-- Altered this part. To put it back together, remove block commented an erase the others

			//ORIGINAL: String resultLog = "Result: " + header + "\t" + precision + "\t" + recall + "\t" + fmeasure;
			String resultLog = precision + "\t" + recall + "\t" + fmeasure;//remove line to back to normal

			/*ORIGINAL: if(!header.equals("Global"))
				System.out.println();*/

			System.out.println(resultLog);

			logOut.write(resultLog + "\n");
			logOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * void test(String testFile) throws IOException {
	 * 
	 * FileData fData = new FileData(); fData.openForRead(testFile, dataDesc);
	 * DataRecord dataRecord = new DataRecord(dataDesc.numColumns);
	 * 
	 * SequenceSet sequenceSetTest = null;
	 * 
	 * int confMat[][] = new int[numLabels][numLabels]; while
	 * (fData.readNext(dataRecord)) { int trueLabel = dataRecord.y();
	 * crfModel.apply(dataRecord); // System.out.println(trueLabel +
	 * " true:pred " + dataRecord.y()); confMat[trueLabel][dataRecord.y()]++; }
	 * 
	 * for (Sequence sequence : sequenceSetTest) { int trueLabel = sequence.y();
	 * crfModel.apply(sequence); }
	 * 
	 * // output confusion matrix etc directly.
	 * System.out.println("Confusion matrix "); for (int i = 0; i <
	 * dataDesc.numLabels; i++) { System.out.print(i); for (int j = 0; j <
	 * dataDesc.numLabels; j++) { System.out.print("\t" + confMat[i][j]); }
	 * System.out.println(); } }
	 */

	/**
	 * Need All (36) files generated in GenTT program with the method
	 * GenerateTrainAndTest
	 **/
	private void exhaustiveTrainAndTest() {

		String trainFile = this.trainFile.substring(0, this.trainFile.length() - ".tagged".length());
		String trainFormat = ".train";
		String testFormat = ".test";
		String reversible = "";

		trainingFileName = new ArrayList <String> ();
		testFileName = new ArrayList <String> ();
		crfStatisticsReportList = new ArrayList <CRFStatistics> ();

		startLogOut(this.neutralFileName);

		for (int iR = 0; iR < 2; iR++) {
			for (int i = 10; i < 100; i = i + 10) {
				this.trainFile = neutralFileName + "-p" + i + reversible + trainFormat;
				this.testFile = neutralFileName + "-p" + (100 - i) + reversible + testFormat;

				trainingFileName.add(this.trainFile);
				testFileName.add(this.testFile);

				try {
					this.train();
					this.test();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			reversible = "R";
		}

		//-- Generate Global Statistics
		doGlobalStatistics();

		endLogOut();
	}

	private void crossValidationTrainAndTest(int kFold) {

		trainingFileName = new ArrayList <String> ();
		testFileName = new ArrayList <String> ();
		crfStatisticsReportList = new ArrayList <CRFStatistics> ();

		startLogOut(this.neutralFileName);

		for (int k = 1; k <= kFold; k++) {

			//-- To use in train and test
			this.trainFile = neutralFileName + "-CV" + k + trainFormat;
			this.testFile = neutralFileName + "-CV" + k + testFormat;

			//-- To use in global statistics
			trainingFileName.add(this.trainFile);
			testFileName.add(this.testFile);

			try {
				this.train();
				this.test();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//-- Generate Global Statistics
		doGlobalStatistics();
		endLogOut();
	}

	private void simpleTrainAndTest2(String paramsTrainFile) {

		trainingFileName = new ArrayList <String> ();
		testFileName = new ArrayList <String> ();
		crfStatisticsReportList = new ArrayList <CRFStatistics> ();
		numLabels = 5;

		//-- removed "0.2"
		String dictionaryListNumber [] = {"8.0", "21.0", "33.0", "46.0", "58.0", "71.0", "84.0",
				"96.0",	"109.0", "121.0", "134.0", "147.0", "159.0", "172.0", "184.0", "197.0", "210.0",
				"222.0", "235.0", "247.0", "260.0",	"273.0", "285.0", "298.0", "310.0", "323.0", "336.0",
				"348.0", "361.0", "373.0", "386.0",	"399.0", "411.0"};

		this.testFile = "2K-Player-MANUAL-BILOU.tagged";

		for(int i = 0; i < dictionaryListNumber.length; i++) {

			this.trainFile = "Twitter-33850(34000)BILOU-NLN-LA("+dictionaryListNumber[i]+")-R1.tagged";

			//-- To use in global statistics
			trainingFileName.add(this.trainFile);
			testFileName.add(this.testFile);

			startLogOut(this.trainFile);

			try {
				this.train();
				this.test();
			} catch (Exception e) {
				e.printStackTrace();
			}

			endLogOut();

		}
	}

	private void simpleTrainAndTest() throws IOException {

		trainingFileName = new ArrayList <String> ();
		testFileName = new ArrayList <String> ();
		crfStatisticsReportList = new ArrayList <CRFStatistics> ();

		for(int i = 1; i <= 5; i++) {

			this.trainFile = "MSM13Collection(LOC)CV"+i+".train";
			this.testFile = "MSM13Collection(LOC)CV"+i+".test";

			this.neutralFileName = this.trainFile.substring(0, this.trainFile.lastIndexOf("."));

			//-- To use in global statistics
			trainingFileName.add(this.trainFile);
			testFileName.add(this.testFile);

			startLogOut(this.trainFile);

			try {
				this.train();
				//NewFeatureTypes.printGlobalFeatureStatistics();
				//NewFeatureTypes.restartGlobalStatistics();
				//((ContextAnalysisFeature)featureGen.features.get(13)).printFeatureStatistics();
				this.test();
				//NewFeatureTypes.printGlobalFeatureStatistics();
				//((ContextAnalysisFeature)featureGen.features.get(13)).printFeatureStatistics();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//-- Generate Global Statistics
		doGlobalStatistics();
	}

	private void trainSelectSampleBalancedByContext() {

		trainingFileName = new ArrayList <String> ();
		testFileName = new ArrayList <String> ();
		crfStatisticsReportList = new ArrayList <CRFStatistics> ();

		startLogOut(this.trainFile);

		for(float i = 0; i <= 1.04; i+=0.1f) {
			this.neutralFileName = "Twitter-33850(2000)BILOU-ND-Bsc-PProc-R1{w=1,T"+(new DecimalFormat("#.##")).format(i)+"-SEntByBalCxt}.tagged";
			this.trainFile = this.neutralFileName;
			this.testFile = "2K-Player-MANUAL-BILOU.tagged";

			//-- To use in global statistics
			trainingFileName.add(this.trainFile);
			testFileName.add(this.testFile);

			try {
				this.train();
				this.test();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		//-- Generate Global Statistics
		doGlobalStatistics();
		endLogOut();
	}

	private void getStatistics() {

		String dir = "./samples/data/bcs2010/";
		String testFile = "./samples/data/bcs2010/2K-Player-MANUAL-BILOU.tagged";

		this.trainFile = "2K-PlayerBILOU-AutoTagger-DicNLNBestSigma.tagged";
		checkFileStatistics(dir + this.trainFile, testFile);

	}

	public void getSpecificStatistics() throws IOException {

		String [] entityType = {"Company", "Facility", "Geo-loc", "Movie", "Musicartist", "Other", "Person", "Product", "Tvshow", "Sportsteam"};
		String [] etzioniEntityType = {"company", "facility", "geo-loc", "movie", "band", "other", "person", "product", "tvshow", "sportsteam"};
		String dir = "./samples/data/bcs2010/";

		String taggedFile;
		String testFile;

		for(int e = 0; e < entityType.length; e++) {

			System.out.println("Entity Type: " + entityType[e]);

			for(int cv = 1; cv <= 5; cv++) {

				taggedFile = "Twitter-Ner-Data-("+entityType[e]+")-Alt-RET-CV"+cv+"-PlainFormat-"+etzioniEntityType[e]+"-DOCRF.tagged";
				testFile = "Twitter-Ner-Data-("+entityType[e]+")-Alt-RET-CV"+cv+".test";

				getStatistics(dir, taggedFile, testFile);
			}
		}
	}

	public void getStatistics(String dir, String trainFile, String testFile) throws IOException {

		String instantTrainingFile = trainFile;

		if(trainFile.lastIndexOf("/") != -1) {
			instantTrainingFile = trainFile.substring(trainFile.lastIndexOf("/") + 1);
		}

		String logOutputFileAddress = "./samples/data/bcs2010/saves/LOG-"+instantTrainingFile+".log";
		crfStatisticsReportList = new ArrayList <CRFStatistics> ();

		DateFormat dfm = new SimpleDateFormat("MM-dd-yyyy,HH.mm.ss");
		startSaveTime = dfm.format((new Date()).getTime());

		logOut = new OutputStreamWriter(new FileOutputStream(logOutputFileAddress));
		logOut.write("Log CRF (" + startSaveTime + ")\n");

		this.trainFile = trainFile;
		checkFileStatistics(dir + this.trainFile, dir + testFile);

		try {
			logOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static CRFStatistics getStatisticsLite(String taggedFile, String testFile, boolean isSegment) {
		return(doStatisticsLite(taggedFile, testFile, isSegment));
	}

	public ArrayList<CRFStatistics> getCRFStatisticsReportList() {
		return(crfStatisticsReportList);
	}

	public void startLogOut(String logOutputFileAddress) {

		DateFormat dfm = new SimpleDateFormat("MM-dd-yyyy,HH.mm.ss");
		startSaveTime = dfm.format((new Date()).getTime());

		int endFilename = logOutputFileAddress.lastIndexOf(".");
		if(endFilename == -1) {
			endFilename = logOutputFileAddress.length();
		}

		String newLogOutputFileAddress = "./Data/Saves/Log/LOG-";
		newLogOutputFileAddress += logOutputFileAddress.substring(0, endFilename);
		newLogOutputFileAddress += ".log";

		try {
			logOut = new OutputStreamWriter(new FileOutputStream(newLogOutputFileAddress));
			logOut.write("Log CRF (" + startSaveTime + ")\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void endLogOut() {
		try {
			logOut.flush();
			logOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * To Execute: java BCS <configure File Name address>
	 * 
	 * Example: java BCS ./samples/bcs2010.conf
	 */
	public static void main(String[] args) throws Exception {

		String configFile = "../CRF-Eclipse/conf/CRF.conf";//args[0];
		CRFExecutor bCS = new CRFExecutor(configFile);

		//-- Exhaustive Train and Test
		//bCS.exhaustiveTrainAndTest();

		//-- Cross Validation Train and Test
		//bCS.crossValidationTrainAndTest(5);

		//bCS.simpleTrainAndTestRTVP();

		//-- Do naive statistics
		/*bCS.getStatistics("./samples/data/bcs2010/",
				"Twitter-Ner-Data-Alt-RET-CV1-RUS-RET-RRT-RRL-RRWS-PlainFormat-Etizioni-geo-loc-DOCRF.tagged",
				"Twitter-Ner-Data-(Geo-loc)-Alt-RET-CV1-RUS-RET-RRT-RRL-RRWS.test");*/

		bCS.simpleTrainAndTest();
		//bCS.getSpecificStatistics();

		//songStorms song = new songStorms();
		//song.play();
	}

}
