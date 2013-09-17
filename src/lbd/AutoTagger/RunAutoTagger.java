package lbd.AutoTagger;

import java.io.IOException;

import lbd.CRF.CRFExecutor;
import lbd.CRF.CRFStatistics;
import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.SequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;
import lbd.FSNER.ArtificialIntelligenceInterpreter;
import lbd.FSNER.ParametersAdjustment;
import lbd.FSNER.Filter.Component.Entity;
import lbd.FSNER.Filter.Component.Term;
import lbd.Model.SupportContext;
import lbd.Utils.RemoveStopWordsTool;
import lbd.Utils.Utils;

public class RunAutoTagger {
	
	public static final String DIR = "./samples/data/bcs2010/AutoTagger/";
	public static final String DIC = "Dictionary/";
	public static final String INPUT = "Input/";
	
	/* Optimizing Stop Words */
	private static RemoveStopWordsTool stopWord;
	
	/* Optimizing SupportContext load */
	private static SupportContext supportContextWS3;
	private static SupportContext supportContextWS2;
	private static SupportContext supportContextWS1;
	
	public static void main(String [] args) throws Exception {
		
		/*String sourceFile = "./samples/data/bcs2010/AutoTagger/Input/Twitter-33850(34000)BILOU-NLN-LA(411.0)-R1.tagged";//Twitter-33850(2000)BILOU-ND-Bsc-PProc-R1.tagged";
		String testFile = "./samples/data/bcs2010/2K-Player-MANUAL-BILOU.tagged";
		
		//String trainFile = (new SelectEntitiesByContextExtended(sourceFile)).selectEntities(testFile);
		
		trainFile = trainFile.substring(trainFile.lastIndexOf("/"));
		
		CRFExecutor crf = new CRFExecutor("./samples/bcs2010.conf");
		crf.getStatistics("./samples/data/bcs2010/AutoTagger/Output/", trainFile, testFile);*/
		
		// -- Select Entities by Balanced Context (Alpha, Beta & Teta)
		/*SelectEntitiesByBalancedContext sEByBalCxt = new SelectEntitiesByBalancedContext(2);
		String inputFilenameAddress = "./samples/data/bcs2010/AutoTagger/Input/Twitter-33850(2000)BILOU-ND-Bsc-PProc-R1.tagged";
		
		for(float i = 0; i < 0.44f; i+=0.05f)
			sEByBalCxt.selectEntities(inputFilenameAddress, i);*/
		
		/** -- Select Entities by Balanced Context (Alpha, Beta & Teta) **/
		/*String contextSourceFilenameAddress = "./samples/data/bcs2010/AutoTagger/Input/Twitter-33850(34000)BILOU-NLN-LA(411.0)-PProc-R1-POSTag.tagged";
		String inputFilenameAddress = "./samples/data/bcs2010/AutoTagger/Input/Twitter-33850(34000)BILOU-NLN-LA(411.0)-PProc-R1-POSTag.tagged";
		String noEntitiesTestFile = "./samples/data/bcs2010/AutoTagger/Input/2K-Player-MANUAL-BILOU-POSTag-NoEntitites.tagged";
		
		String testFilenameAddress = "./samples/data/bcs2010/2K-Player-MANUAL-BILOU-POSTag.tagged";*/
		
		/** Select by POSTag **/
		/*SelectByPOSTag selByPOSTag = new SelectByPOSTag(inputFilenameAddress, 1, true, 0.1);
		
		selByPOSTag.analyzePOSTagContext(contextSourceFilenameAddress, inputFilenameAddress);
		String outputFilenameAddress = selByPOSTag.selectEntities(noEntitiesTestFile);
		
		CRFExecutor crf = new CRFExecutor("./samples/bcs2010.conf");
		crf.getStatistics("", outputFilenameAddress, testFilenameAddress);*/
		
		/** Select by Context **/
		/*SelectByContext selByCxt = new SelectByContext(inputFilenameAddress, 2, true, 0.6);
		
		selByCxt.analyzeContext(contextSourceFilenameAddress, inputFilenameAddress);
		String outputFilenameAddress = selByCxt.selectEntities(inputFilenameAddress);*/
		
		/** Select Entity (by Context NEW)**/
		/*for(int i = 1; i <= 1; i++) {
			
			final String DIR = "./samples/data/bcs2010/AutoTagger/Input/";
			String dictionary = DIR + "Organization List(100p)-CapSel[p30.0].dic";
			//String contextSource = DIR + "weps-3_task-2_training-CV"+i+"-AutoTagger-CapSel[90]--RUS-REWE-RRT-RRL-RRWS.tmp";
			String contextSource = DIR + "weps-3_task-2_training-CV"+i+"-SplitF-Seed(2000)--RUS-RRT-RRL-RRWS.-CRF.tagged";
			String fileToLabel = DIR + "weps-3_task-2_training-CV"+i+"-SplitF-Seed(2000)--RUS-RRT-RRL-RRWS.-CRF.tagged";//"weps-3_task-2_training-CV"+i+"--RUS-RRT-RRL-RRWS.tmp";
			
			SelectEntity selEnt = new SelectEntity(1);
			selEnt.labelFile(dictionary, contextSource, fileToLabel);
		
		}*/
		
		/** Artificial Intelligence Interpreter (AII) **/
		//String stopWordFile = DIR + DIC + "EnglishStopWords-Tweet.dic";
		String stopWordFile = DIR + DIC + "PortugueseStopWords-Tweet.dic";
		stopWord = new RemoveStopWordsTool(stopWordFile);
		
		final int ADJUSTMENT = 0;
		final int EXECUTION = 1;
		final int FAST_EXECUTION = 2;
		final int MODE_SIZE = 2;
		
		int setSize = 1;
		
		int [] bestEntityEditDistance = new int [setSize];
		int [] bestTermEditDistance = new int [setSize];
		int [] bestTermMinimumSize = new int [setSize];
		int [] bestMinimumTermFrequency = new int [setSize];
		double [] bestSmoothStandardDeviation = new double [setSize];
		double [] bestProbabilityFilterCxt = new double [setSize];
		double [] bestProbabilityFilterCxtOut = new double [setSize];
		double [] bestProbabilityFilterOut = new double [setSize];
		
		for(int mode = 2; mode <= MODE_SIZE; mode++) {
		
			if(mode == EXECUTION)
				System.out.println("\n\n\n**********");
			
			for(int i = 1; i <= setSize; i++) {
				
				System.out.println("\nCV" + i);
			
				/* Organization WEPS3-Task2 (O) */
				String OSeed = DIR + INPUT + "weps-3_task-2_training-CV5--RUS-RRT-RRL-RRWS-SplitF2000.train";
				String ODevelopment = DIR + INPUT + "weps-3_task-2_training-CV5--RUS-RRT-RRL-RRWS-SplitF17000.train";
				String OContextSourceFile = DIR + INPUT + "weps-3_task-2_training-CV"+i+"--RUS-RRT-RRL-RRWS.train";
				String OFileToLabel = DIR + INPUT + "weps-3_task-2_training-CV"+i+"--RUS-RRT-RRL-RRWS-NotLabeled.test";//"weps-3_task-2_training-CV1--RUS-RRT-RRL-RRWS-EntityFilter(Folio).test";
				String OTestFile = DIR + INPUT + "weps-3_task-2_training-CV"+i+"--RUS-RRT-RRL-RRWS.test";//"weps-3_task-2_training-CV1--RUS-RRT-RRL-RRWS-EntityFilter(Folio).test";
				String OAutomaticEntityList = DIR + INPUT + "weps-3_task-2_training-CV"+i+"--RUS-RRT-RRL-RRWS-AutoTagger-EntityList.tagged";
				String OAutomaticBasedSeedEntityList = DIR + INPUT + "weps-3_task-2_training-CV"+i+"--RUS-RRT-RRL-RRWS-Seed2000-AutoTagger-EntityList.tagged";
				String OReferenceData = DIR + INPUT + "URLsFROMAllKeyWords-rng(0-101)-maxRst(10)-rstPg(50)-loc(US)-lang(en)-mrk(en-US)-URLs-DataRef.lst";
				String OReferenceDataFix1 = DIR + INPUT + "URLsFROMAllKeyWords-FixCatcher-rng(0-101)-maxRst(5)-rstPg(50)-loc(US)-lang(en)-mrk(en-US)-URLs-DataRef.lst";
				String OReferenceDataFix2 = DIR + INPUT + "AII--PartialResult-URLsFROMAllKeyWords-FixCatcher-rng(0-101)-maxRst(20)-rstPg(50)-loc(US)-lang(en)-mrk(en-US)-URLs-DataRef.lst";
				String OReferenceData2 = DIR + INPUT + "AII--PartialResult-URLsFROMAllKeyWords-rng(0-101)-maxRst(20)-rstPg(50)-loc(US)-lang(en)-mrk(en-US)-URLs-DataRef.lst";
				String OReferenceDataDev = DIR + INPUT + "URLsFROMAllKeyWords+7Channels-rng(0-101)-maxRst(10)-rstPg(50)-loc(US)-lang(en)-mrk(en-US)-URLs-DataRef.lst";
				String OAllTask3Data = DIR + INPUT + "weps-3_task-2_training--RUS-RRT-RRL-RRWS.tagged";
				String OBestResult = DIR + INPUT + "AII--PartialResult-weps-3_task-2_training--RUS-RRT-RRL-RRWS(BESTResult).tagged";
				String OBestPrecision = DIR + INPUT + "AII--PartialResult-weps-3_task-2_training--RUS-RRT-RRL-RRWS(BESTPrecision-5URL).tagged";
				String OEntitySynonymList = DIR + INPUT + "GSearch-key(Org)-sub(organization)-maxRst(5)-lang(en)-SynonymEntList.urls";
				String ODevelopmentParam = DIR + INPUT + "GSearch-key(Org)-sub(organization)-maxRst(5)-lang(en)-DataRef.urls";
				String OTestParam = DIR + INPUT + "URLsFROMAllKeyWords-Organization-Web-rng(0-101)-maxRst(20)-rstPg(50)-loc(US)-lang(en)-mrk(en-US)-URLs-DataRef-R"+i+"-SplitF588-Test.lst"; 
				
				/* Teams (T) */
				String TSeed = DIR + INPUT + "Twitter-2000-MANUAL-LABELED-BILOU-CV5--RUS-RRT-RRL-RRWS-SplitF1000.train";
				String TDevelopment = DIR + INPUT + "Twitter-2000-MANUAL-LABELED-BILOU-CV5--RUS-RRT-RRL-RRWS-SplitF600.train";
				String TContextSourceFile = DIR + INPUT + "Twitter-2000-MANUAL-LABELED-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS.train";
				String TFileToLabel = DIR + INPUT + "Twitter-2000-MANUAL-LABELED-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS-NotLabeled.test";
				String TTestFile = DIR + INPUT + "Twitter-2000-MANUAL-LABELED-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS.test";
				String TAutomaticEntityList = DIR + INPUT + "Twitter-2000-MANUAL-LABELED-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS-AutoTagger-EntityList.tagged";
				String TAutomaticBasedSeedEntityList = DIR + INPUT + "Twitter-2000-MANUAL-LABELED-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS-Seed1000-AutoTagger-EntityList.tagged";
				String TReferenceData = DIR + INPUT + "URLsFROMAllKeyWords-Team-Web-rng(0-101)-maxRst(5)-rstPg(50)-loc(US)-lang(en)-mrk(en-US)-URLs-DataRef.lst";
				String TEntitySynonymList = DIR + INPUT + "GSearch-key(Team)-sub(clube futebol)-maxRst(5)-lang(pt)-SynonymEntList.urls";
				String TDevelopmentParam = DIR + INPUT + "GSearch-key(Team)-sub(clube futebol)-maxRst(5)-lang(pt)-DataRef.urls";
				String TTestParam = DIR + INPUT + "URLsFROMAllKeyWords-Team-Web-rng(0-101)-maxRst(20)-rstPg(50)-loc(US)-lang(en)-mrk(en-US)-URLs-DataRef-R"+i+"-SplitF2001-Test.lst";
				
				/* Player (P) */
				String PSeed = DIR + INPUT + "Twitter-2000-Player-MANUAL-LABELED-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS-SplitF1000.train";
				String PDevelopment = DIR + INPUT + "Twitter-2000-Player-MANUAL-LABELED-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS-SplitF600.train";
				String PContextSourceFile = DIR + INPUT + "Twitter-2000-Player-MANUAL-LABELED-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS.train";
				String PFileToLabel = DIR + INPUT + "Twitter-2000-Player-MANUAL-LABELED-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS-NotLabeled.test";
				String PTestFile = DIR + INPUT + "Twitter-2000-Player-MANUAL-LABELED-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS.test";
				String PAutomaticEntityList = DIR + INPUT + "Twitter-2000-Player-MANUAL-LABELED-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS-AutoTagger-EntityList.tagged";
				String PAutomaticBasedSeedEntityList = DIR + INPUT + "Twitter-2000-Player-MANUAL-LABELED-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS-Seed1000-AutoTagger-EntityList.tagged";
				String PReferenceData = DIR + INPUT + "URLsFROMAllKeyWords-Player-Web-rng(0-101)-maxRst(5)-rstPg(50)-loc(US)-lang(en)-mrk(en-US)-URLs-DataRef.lst";
				String PEntitySynonymList = DIR + INPUT + "GSearch-key(Player)-sub(jogador futebol)-maxRst(5)-lang(pt)-SynonymEntList.urls";
				String PDevelopmentParam = DIR + INPUT + "GSearch-key(Player)-sub(jogador futebol)-maxRst(5)-lang(pt)-DataRef.urls";
				String PTestParam = DIR + INPUT + "URLsFROMAllKeyWords-Player-Web-rng(0-101)-maxRst(4)-rstPg(50)-loc(US)-lang(en)-mrk(en-US)-URLs-DataRef-R"+i+"-SplitF1676-Test.lst";
				
				/* Stadiums (S) */
				String SSeed = DIR + INPUT + "2K-Stadium-MANUAL-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS-SplitF1000.train";
				String SDevelopment = DIR + INPUT + "2K-Stadium-MANUAL-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS-SplitF600.train";
				String SContextSourceFile = DIR + INPUT + "2K-Stadium-MANUAL-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS.train";
				String SFileToLabel = DIR + INPUT + "2K-Stadium-MANUAL-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS-NotLabeled.test";
				String STestFile = DIR + INPUT + "2K-Stadium-MANUAL-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS.test";
				String SAutomaticEntityList = DIR + INPUT + "2K-Stadium-MANUAL-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS-AutoTagger-EntityList.tagged";
				String SAutomaticBasedSeedEntityList = DIR + INPUT + "2K-Stadium-MANUAL-BILOU-CV"+i+"--RUS-RRT-RRL-RRWS-Seed1000-AutoTagger-EntityList.tagged";
				String SReferenceData = DIR + INPUT + "URLsFROMAllKeyWords-Venue-Web-rng(0-101)-maxRst(5)-rstPg(50)-loc(US)-lang(en)-mrk(en-US)-URLs-DataRef.lst";
				String SEntitySynonymList = DIR + INPUT + "GSearch-key(Venue)-sub(estádio futebol)-maxRst(5)-lang(pt)-SynonymEntList.urls";
				String SDevelopmentParam = DIR + INPUT + "GSearch-key(Venue)-sub(estádio futebol)-maxRst(5)-lang(pt)-DataRef.urls";
				String STestParam = DIR + INPUT + "URLsFROMAllKeyWords-Venue-Web-JogoFutebol-rng(0-101)-maxRst(4)-rstPg(50)-loc(US)-lang(en)-mrk(en-US)-URLs-DataRef-R"+i+"-SplitF205-Test.lst";
				
				if(mode == ADJUSTMENT) { //-- Adjustment
					
					ArtificialIntelligenceInterpreter.setAjdustmentMode(true);
					
					double [] params = adjustAIIParams(stopWordFile, OReferenceData, OReferenceDataDev, OReferenceDataDev);
					
					bestEntityEditDistance[i-1] = (int) params[0];
					bestTermEditDistance[i-1] = (int) params[1];
					bestTermMinimumSize[i-1] = (int) params[2];
					bestMinimumTermFrequency[i-1] = (int) params[3];
					bestSmoothStandardDeviation[i-1] = params[4];
					bestProbabilityFilterCxt[i-1] = params[5];
					bestProbabilityFilterCxtOut[i-1] = params[6];
					bestProbabilityFilterOut[i-1] = params[7];
					
				} else if(mode == EXECUTION) {
					
					/* AII - Artificial Intelligence Interpreter */					
					ArtificialIntelligenceInterpreter aII = new ArtificialIntelligenceInterpreter();
					ArtificialIntelligenceInterpreter.setAjdustmentMode(false);
					
					//-- Set BEST Params
					aII.setEntityEditDistance(bestEntityEditDistance[i-1]);
					aII.setTermEditDistance(bestTermEditDistance[i-1]);
					aII.setTermMinimumSize(bestTermMinimumSize[i-1]);
					aII.setMinimumTermFrequencyPerSequence(bestMinimumTermFrequency[i-1]);
					aII.setStandardDeviationParcel(bestSmoothStandardDeviation[i-1]);
					aII.setProbabilityFilterCxt(bestProbabilityFilterCxt[i-1]);
					aII.setProbabilityFilterCxtOut(bestProbabilityFilterCxtOut[i-1]);
					aII.setProbabilityFilterOut(bestProbabilityFilterOut[i-1]);
					
					aII.executeInterpreter(stopWordFile, OReferenceData, OFileToLabel);
					
					String outputFile = aII.getOutputFilename();
					
					System.out.print("\nBest Params [EED: " + bestEntityEditDistance[i-1] + " TED: " + bestTermEditDistance[i-1]);
					System.out.print(" TMS: " + bestTermMinimumSize[i-1] + " MTFPS: " + bestMinimumTermFrequency[i-1]);
					System.out.print(" SDP: " + Utils.formatDecimalNumber(bestSmoothStandardDeviation[i-1]));
					System.out.print(" PFC: " + Utils.formatDecimalNumber(bestProbabilityFilterCxt[i-1]));
					System.out.print(" PFCO: " + Utils.formatDecimalNumber(bestProbabilityFilterCxtOut[i-1]));
					System.out.print(" PFO: " + Utils.formatDecimalNumber(bestProbabilityFilterOut[i-1]) + "]");
					
					/* Analysis Result (OutputFile and Test File) */
					CRFExecutor crf = new CRFExecutor("./samples/bcs2010.conf");
					crf.getStatistics("", outputFile, OTestFile);
				} else if(mode == FAST_EXECUTION) {
					
					//executeFastArtificialIntelligenceInterpreter(stopWordFile, OReferenceDataFix1, OReferenceDataFix2, OReferenceDataFix2);
					//for(int t = 0; t < 5; t++)
					SDevelopmentParam = "./samples/data/bcs2010/Twitter-2000-MANUAL-LABELED-BILOU-CV1--RUS-RRT-RRL-RRWS.train";
					STestFile = "./samples/data/bcs2010/Twitter-2000-MANUAL-LABELED-BILOU-CV1--RUS-RRT-RRL-RRWS.test";
						executeFastArtificialIntelligenceInterpreter(stopWordFile, SEntitySynonymList, SDevelopmentParam, STestFile, STestFile);
					
				}
			}
		}
	}
	
	public static void executeFastArtificialIntelligenceInterpreter(String stopWordFile, String entitySynonymList,
			String contextFile, String fileToLabel, String testFile){
		
		/* AII - Artificial Intelligence Interpreter */					
		ArtificialIntelligenceInterpreter aII = new ArtificialIntelligenceInterpreter();
		ArtificialIntelligenceInterpreter.setAjdustmentMode(false);
		
		//-- Set Average Parameters
		aII.setEntityEditDistance(0);
		aII.setTermEditDistance(0);
		aII.setTermMinimumSize(3);
		aII.setMinimumTermFrequencyPerSequence(1);
		aII.setStandardDeviationParcel(2);
		aII.setProbabilityFilterCxt(1);
		aII.setProbabilityFilterCxtOut(1);
		aII.setProbabilityFilterOut(1);
		
		aII.executeInterpreter(stopWordFile, entitySynonymList, contextFile, fileToLabel);
		
		//Get Back: String outputFile = aII.getOutputFilename();
		
		/*System.out.print("\nBest Params [EED: " + Entity.EDIT_DISTANCE_ACCEPTABLE + " TED: " + Term.EDIT_DISTANCE_ACCEPTABLE);
		System.out.print(" TMS: " + aII.getTermMinimumSize() + " MTFPS: " + aII.getMinimumTermFrequencyPerSequence());
		System.out.print(" SDP: " + aII.getStandardDeviationParcel());
		System.out.print(" PFC: " + aII.getProbabilityFilterCxt());
		System.out.print(" PFCO: " + aII.getProbabilityFilterCxtOut());
		System.out.print(" PFO: " + aII.getProbabilityFilterOut());
		System.out.print("]");*/
		
		/* Analysis Result (OutputFile and Test File) */		
		//Get Back: try {
		//	CRFExecutor crf = new CRFExecutor("./samples/bcs2010.conf");
		//	crf.getStatistics("", outputFile, testFile);
		//} catch (Exception e) {
		//	e.printStackTrace();
		//}
	}

	/**
	 * Order of Adjust Params
	 * 1. Entity Edit Distance
	 * 2. Term Edit Distance
	 * 3. Term Minimum Size
	 * 4. Minimum Term Frequency
	 * 5. Smooth Standard Deviation
	 * 6. Probability Filter Context
	 * 7. Probability Filter Context Out
	 * 8. Probability Filter Out
	 */
	public static double [] adjustAIIParams(String stopWordFile, String contextSourceFile, String fileToLabel, String testFile) throws Exception {
		
		loadSupportContext(contextSourceFile);
		
		int startTermEditDistance = 1;
		int startTermMinimumSize = 3;
		int startMinimumTermFrequency = 1;
		double startSmoothStandardDeviation = 2;
		
		//System.out.print("\n** Ajust Entity Edit Distance");
		int bestEntityEditDistance = 0;/*ajustEntityEditDistance(stopWordFile, contextSourceFile, fileToLabel, testFile,
				startTermEditDistance, startTermMinimumSize, startMinimumTermFrequency, startSmoothStandardDeviation);*/
		
		//System.out.print("\n** Ajust Term Edit Distance");
		int bestTermEditDistance = 1;/*ajustTermEditDistance(stopWordFile, contextSourceFile, fileToLabel, testFile,
				bestEntityEditDistance, startTermMinimumSize, startMinimumTermFrequency, startSmoothStandardDeviation);*/
		
		/** Adjust Term Minimum Size [5 Iterations] **/
		//System.out.print("\n** Ajust Term Minimum Size");
		int bestTermMinimumSize = 3;/*ajustTermMinimumSize(stopWordFile, contextSourceFile, fileToLabel, testFile, 
				bestEntityEditDistance, bestTermEditDistance, startMinimumTermFrequency, startSmoothStandardDeviation);*/
		
		//EED: 0 TED: 1 TMS: 3 MTFPS: 1 SDP: 1 PFC: 0.5 PFCO: 0.9 PFO: 1
		//System.out.print("\n** Ajust Minimum Term Frequency");
		int bestMinimumTermFrequency = 1;/*ajustMinimumTermFrequency(stopWordFile, contextSourceFile, fileToLabel,
				testFile, bestEntityEditDistance, bestTermEditDistance, bestTermMinimumSize, startSmoothStandardDeviation);*/
		
		/** Adjust Smooth Standard Deviation [10 Iterations] **/
		System.out.print("\n** Ajust Smooth Standard Deviation");
		double bestSmoothStandardDeviation = ajustSmoothStandardDeviation(stopWordFile, contextSourceFile, 
				fileToLabel, testFile, bestEntityEditDistance, bestTermEditDistance, bestTermMinimumSize, bestMinimumTermFrequency);
		
		//System.out.print("\n** Ajust Probability Filter Context");
		double bestProbabilityFilterCxt = 1;/*ajustProbabilityFilterCxt(stopWordFile, contextSourceFile, 
				fileToLabel, testFile, bestEntityEditDistance, bestTermEditDistance, 
				bestTermMinimumSize, bestMinimumTermFrequency, bestSmoothStandardDeviation);*/
		
		//System.out.print("\n** Ajust Probability Filter Context Out");
		double bestProbabilityFilterCxtOut = 1;/*ajustProbabilityFilterCxtOut(stopWordFile, contextSourceFile, 
				fileToLabel, testFile, bestEntityEditDistance, bestTermEditDistance, bestTermMinimumSize, bestMinimumTermFrequency,
				bestSmoothStandardDeviation, bestProbabilityFilterCxt);*/
		
		//System.out.print("\n** Ajust Probability Filter Out");
		double bestProbabilityFilterOut = 1;/*ajustProbabilityFilterOut(stopWordFile, contextSourceFile, 
				fileToLabel, testFile, bestEntityEditDistance, bestTermEditDistance, 
				bestTermMinimumSize, bestMinimumTermFrequency, bestSmoothStandardDeviation, 
				bestProbabilityFilterCxt, bestProbabilityFilterCxtOut);*/
		
		 /** Write Params **/ 
		//-- Do after..
		
		 return(new double [] {bestEntityEditDistance, bestTermEditDistance, bestTermMinimumSize, bestMinimumTermFrequency,
				 bestSmoothStandardDeviation, bestProbabilityFilterCxt, bestProbabilityFilterCxtOut, bestProbabilityFilterOut});
		
		//return(executePartialParametersEstimation(stopWordFile, contextSourceFile, fileToLabel, testFile));
	}
	
	protected static void loadSupportContext(String contextSourceFile) {
		
		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(contextSourceFile,
				FileType.TRAINING, false);
		
		/* Optimizing SupportContext load */		
		supportContextWS1 = new SupportContext(1, "BILOU", true);
		supportContextWS1.generateContext(inputSequenceSet);
		
		supportContextWS2 = new SupportContext(2, "BILOU", true);
		supportContextWS2.generateContext(inputSequenceSet);
		
		supportContextWS3 = new SupportContext(3, "BILOU", true);
		supportContextWS3.generateContext(inputSequenceSet);
		
	}
	
	protected static int ajustEntityEditDistance(String stopWordFile, String contextSourceFile,
			String fileToLabel, String testFile, int termEditDistance, int minimumTermSize,
			int minimumFrequencyTerm, double smoothStandardDeviation) throws Exception {
			
			int initialEditDistance = 0;
			int iteration = 2;
			
			int bestEntityEditDistance = 0;
			double bestFMeasure = 0;
			
			CRFExecutor crf;
			ArtificialIntelligenceInterpreter aII;
			String outputFile;
			
			/* Adjust Term Minimum Size [5 Iterations] */
			for(int i = initialEditDistance; i < initialEditDistance + iteration; i++) {
				
				ParametersAdjustment.printParamStatus("Entity Edit Distance", (i+1-initialEditDistance),
						i, i, termEditDistance, minimumTermSize, minimumFrequencyTerm, smoothStandardDeviation, 
						1, 1, 1);
				
				aII = new ArtificialIntelligenceInterpreter();
				
				aII.setTermEditDistance(termEditDistance);
				aII.setTermMinimumSize(minimumTermSize);
				aII.setStandardDeviationParcel(smoothStandardDeviation);
				aII.setMinimumTermFrequencyPerSequence(minimumFrequencyTerm);
				
				aII.setStopWord(stopWord);
				aII.setSupportContextWS1(supportContextWS1);
				aII.setSupportContextWS2(supportContextWS2);
				aII.setSupportContextWS3(supportContextWS3);
				
				Entity.EDIT_DISTANCE_ACCEPTABLE = i;
				
				aII.executeInterpreter(stopWordFile, contextSourceFile, fileToLabel);
				
				outputFile = aII.getOutputFilename();
				
				/* Analysis Result (OutputFile and Test File) */
				crf = new CRFExecutor("./samples/bcs2010.conf");
				crf.getStatistics("", outputFile, testFile);
				
				if(bestFMeasure < crf.getCRFStatisticsReportList().get(0).getFmeasure()) {
					bestFMeasure = crf.getCRFStatisticsReportList().get(0).getFmeasure();
					bestEntityEditDistance = i;
				}
			}
			
			return(bestEntityEditDistance);
		}
		
		protected static int ajustTermEditDistance(String stopWordFile, String contextSourceFile, 
			String fileToLabel, String testFile, int entityEditDistance, int minimumTermSize,
			int minimumFrequencyTerm, double smoothStandardDeviation) throws Exception {
			
		int initialEditDistance = 0;
		int iteration = 3;
		
		int bestTermEditDistance = 0;
		double bestFMeasure = 0;
		
		CRFExecutor crf;
		ArtificialIntelligenceInterpreter aII;
		String outputFile;
		
		/* Adjust Term Minimum Size [5 Iterations] */
		for(int i = initialEditDistance; i < initialEditDistance + iteration; i++) {
			
			ParametersAdjustment.printParamStatus("Term Edit Distance", (i+1-initialEditDistance),
					i, entityEditDistance, i, minimumTermSize, minimumFrequencyTerm, smoothStandardDeviation, 
					1, 1, 1);
			
			aII = new ArtificialIntelligenceInterpreter();
			
			aII.setEntityEditDistance(entityEditDistance);
			aII.setTermEditDistance(i);
			aII.setTermMinimumSize(minimumTermSize);
			aII.setMinimumTermFrequencyPerSequence(minimumFrequencyTerm);
			aII.setStandardDeviationParcel(smoothStandardDeviation);
			
			aII.setStopWord(stopWord);
			aII.setSupportContextWS1(supportContextWS1);
			aII.setSupportContextWS2(supportContextWS2);
			aII.setSupportContextWS3(supportContextWS3);
			
			aII.executeInterpreter(stopWordFile, contextSourceFile, fileToLabel);
			
			outputFile = aII.getOutputFilename();
			
			/* Analysis Result (OutputFile and Test File) */
			crf = new CRFExecutor("./samples/bcs2010.conf");
			crf.getStatistics("", outputFile, testFile);
			
			if(bestFMeasure < crf.getCRFStatisticsReportList().get(0).getFmeasure()) {
				bestFMeasure = crf.getCRFStatisticsReportList().get(0).getFmeasure();
				bestTermEditDistance = i;
			}
		}
		
		return(bestTermEditDistance);
	}
	
	protected static int ajustTermMinimumSize(String stopWordFile, String contextSourceFile,
			String fileToLabel, String testFile, int entityEditDistance, int termEditDistance, 
			int minimumTermFrequency, double smoothStandardDeviation) throws Exception {
		
		int initialTermMinimumSize = 3;
		int iteration = 6;//5
		
		int bestTermMinimumSize = 0;
		double bestFMeasure = 0;
		
		CRFExecutor crf;
		ArtificialIntelligenceInterpreter aII;
		String outputFile;
		
		/* Adjust Term Minimum Size [5 Iterations] */
		for(int i = initialTermMinimumSize; i < initialTermMinimumSize + iteration; i++) {
			
			ParametersAdjustment.printParamStatus("Term Minimum Size", (i+1-initialTermMinimumSize),
					i, entityEditDistance, termEditDistance, i, minimumTermFrequency, smoothStandardDeviation, 
					1, 1, 1);
			
			aII = new ArtificialIntelligenceInterpreter();
			
			aII.setEntityEditDistance(entityEditDistance);
			aII.setTermEditDistance(termEditDistance);
			aII.setTermMinimumSize(i);
			aII.setMinimumTermFrequencyPerSequence(minimumTermFrequency);
			aII.setStandardDeviationParcel(smoothStandardDeviation);
			
			aII.setStopWord(stopWord);
			aII.setSupportContextWS1(supportContextWS1);
			aII.setSupportContextWS2(supportContextWS2);
			aII.setSupportContextWS3(supportContextWS3);
			
			aII.executeInterpreter(stopWordFile, contextSourceFile, fileToLabel);
			
			outputFile = aII.getOutputFilename();
			
			/* Analysis Result (OutputFile and Test File) */
			crf = new CRFExecutor("./samples/bcs2010.conf");
			crf.getStatistics("", outputFile, testFile);
			
			if(bestFMeasure < crf.getCRFStatisticsReportList().get(0).getFmeasure()) {
				bestFMeasure = crf.getCRFStatisticsReportList().get(0).getFmeasure();
				bestTermMinimumSize = i;
			}
		}
		
		return(bestTermMinimumSize);
	}
	
	protected static int ajustMinimumTermFrequency(String stopWordFile, String contextSourceFile, 
			String fileToLabel, String testFile, int entityEditDistance, int termEditDistance,
			int minimumTermSize, double smoothStandardDeviation) throws Exception {
		
		int initialTermFrequency = 1;
		int iteration = 10;//7
		
		int bestMinimumTermFrequency = 0;
		double bestFMeasure = 0;
		
		CRFStatistics crfStats;
		ArtificialIntelligenceInterpreter aII;
		String outputFile;
		
		/* Adjust Term Minimum Size [5 Iterations] */
		for(int i = initialTermFrequency; i < initialTermFrequency + iteration; i++) {
			
			ParametersAdjustment.printParamStatus("Minimum Term Frequency", (i+1-initialTermFrequency),
					i, entityEditDistance, termEditDistance, minimumTermSize, i, smoothStandardDeviation, 
					1, 1, 1);
			
			aII = new ArtificialIntelligenceInterpreter();
			
			aII.setEntityEditDistance(entityEditDistance);
			aII.setTermEditDistance(termEditDistance);
			aII.setTermMinimumSize(minimumTermSize);
			aII.setMinimumTermFrequencyPerSequence(i);
			aII.setStandardDeviationParcel(smoothStandardDeviation);
			
			aII.setStopWord(stopWord);
			aII.setSupportContextWS1(supportContextWS1);
			aII.setSupportContextWS2(supportContextWS2);
			aII.setSupportContextWS3(supportContextWS3);
			
			aII.executeInterpreter(stopWordFile, contextSourceFile, fileToLabel);
			
			outputFile = aII.getOutputFilename();
			
			/* Analysis Result (OutputFile and Test File) */
			crfStats = CRFExecutor.getStatisticsLite(outputFile, testFile, false);
			
			if(bestFMeasure < crfStats.getFmeasure()) {
				bestFMeasure = crfStats.getFmeasure();
				bestMinimumTermFrequency = i;
			}
		}
		
		return(bestMinimumTermFrequency);
	}
	
	protected static double ajustSmoothStandardDeviation(String stopWordFile, String contextSourceFile, 
			String fileToLabel, String testFile, int entityEditDistance, int termEditDistance,
			int termMinimumSize, int minimumTermFrequency) {
		
		ArtificialIntelligenceInterpreter aII = new ArtificialIntelligenceInterpreter();
		
		aII.setEntityEditDistance(entityEditDistance);
		aII.setTermEditDistance(termEditDistance);
		aII.setTermMinimumSize(termMinimumSize);
		aII.setMinimumTermFrequencyPerSequence(minimumTermFrequency);
		
		aII.setStopWord(stopWord);
		aII.setSupportContextWS1(supportContextWS1);
		aII.setSupportContextWS2(supportContextWS2);
		aII.setSupportContextWS3(supportContextWS3);
		
		double bestSmoothStandardDeviation = ParametersAdjustment.ajustSmoothStandardDeviation(aII, stopWordFile,
				contextSourceFile, fileToLabel, testFile);
		
		return(bestSmoothStandardDeviation);
	}
	
	protected static double ajustProbabilityFilterCxt(String stopWordFile, String contextSourceFile, 
			String fileToLabel, String testFile, int entityEditDistance, int termEditDistance, 
			int termMinimumSize, int minimumTermFrequency, double smoothStandardDeviation) {
		
		ArtificialIntelligenceInterpreter aII = new ArtificialIntelligenceInterpreter();
		
		aII.setEntityEditDistance(entityEditDistance);
		aII.setTermEditDistance(termEditDistance);
		aII.setTermMinimumSize(termMinimumSize);
		aII.setMinimumTermFrequencyPerSequence(minimumTermFrequency);
		aII.setStandardDeviationParcel(smoothStandardDeviation);
		
		aII.setStopWord(stopWord);
		aII.setSupportContextWS1(supportContextWS1);
		aII.setSupportContextWS2(supportContextWS2);
		aII.setSupportContextWS3(supportContextWS3);
		
		double bestProbabilityFilterCxt = ParametersAdjustment.ajustProbabilityFilterCxt(aII, stopWordFile,
				contextSourceFile, fileToLabel, testFile);
		
		return(bestProbabilityFilterCxt);
	}
	
	protected static double ajustProbabilityFilterCxtOut(String stopWordFile, String contextSourceFile, 
			String fileToLabel, String testFile, int entityEditDistance, int termEditDistance,
			int termMinimumSize, int minimumTermFrequency, double smoothStandardDeviation, double probabilityFilterCxt) {
		
		ArtificialIntelligenceInterpreter aII = new ArtificialIntelligenceInterpreter();
		
		aII.setEntityEditDistance(entityEditDistance);
		aII.setTermEditDistance(termEditDistance);
		aII.setTermMinimumSize(termMinimumSize);
		aII.setMinimumTermFrequencyPerSequence(minimumTermFrequency);
		aII.setStandardDeviationParcel(smoothStandardDeviation);
		aII.setProbabilityFilterCxt(probabilityFilterCxt);
		
		aII.setStopWord(stopWord);
		aII.setSupportContextWS1(supportContextWS1);
		aII.setSupportContextWS2(supportContextWS2);
		aII.setSupportContextWS3(supportContextWS3);
		
		double bestProbabilityFilterCxtOut = ParametersAdjustment.ajustProbabilityFilterCxtOut(aII, stopWordFile,
				contextSourceFile, fileToLabel, testFile);
		
		return(bestProbabilityFilterCxtOut);
	}
	
	protected static double ajustProbabilityFilterOut(String stopWordFile, String contextSourceFile, 
			String fileToLabel, String testFile, int entityEditDistance, int termEditDistance, 
			int termMinimumSize, int minimumTermFrequency, double smoothStandardDeviation,
			double probabilityFilterCxt, double probabilityFilterCxtOut) {
		
		ArtificialIntelligenceInterpreter aII = new ArtificialIntelligenceInterpreter();
		
		aII.setEntityEditDistance(entityEditDistance);
		aII.setTermEditDistance(termEditDistance);
		aII.setTermMinimumSize(termMinimumSize);
		aII.setMinimumTermFrequencyPerSequence(minimumTermFrequency);
		aII.setStandardDeviationParcel(smoothStandardDeviation);
		aII.setProbabilityFilterCxt(probabilityFilterCxt);
		aII.setProbabilityFilterCxtOut(probabilityFilterCxtOut);
		
		aII.setStopWord(stopWord);
		aII.setSupportContextWS1(supportContextWS1);
		aII.setSupportContextWS2(supportContextWS2);
		aII.setSupportContextWS3(supportContextWS3);
		
		double bestProbabilityFilterOut = ParametersAdjustment.ajustProbabilityFilterOut(aII, stopWordFile,
				contextSourceFile, fileToLabel, testFile);
		
		return(bestProbabilityFilterOut);
	}
	
	/**
	 * Order of Adjust Params
	 * 1. Entity Edit Distance
	 * 2. Term Edit Distance
	 * 3. Term Minimum Size
	 * 4. Minimum Term Frequency
	 * 5. Smooth Standard Deviation
	 * 6. Probability Filter Context
	 * 7. Probability Filter Context Out
	 * 8. Probability Filter Out
	 * @throws Exception 
	 */
	protected static double[] executeCompleteParametersEstimation(String stopWordFile, 
			String contextSourceFile, String fileToLabel, String testFile) throws Exception {
		
		final int START_INTERVAL = 0;
		final int END_INTERVAL = 1;
		
		int iteration = 1;
		
		int [] intervalEntityEditDistance = {0, 2};
		int [] intervalTermEditDistance = {0, 3};
		int [] intervalTermMinimumSize = {3, 8};
		int [] intervalMinimumTermFrequency = {1, 10};
		double [] intervalSmoothStandardDeviation = {2.0, 1.0};
		double [] intervalProbabilityFilterContext = {1.0, 0.0};
		double [] intervalProbabilityFilterContextOut = {1.0, 0.0};
		double [] intervalProbabilityFilterOut = {1.0, 0.0};
		
		int entityEditDistance;
		int termEditDistance;
		int termMinimumSize;
		int minimumTermFrequency;
		double smoothStandardDeviation;
		double probabilityFilterContext;
		double probabilityFilterContextOut;
		double probabilityFilterOut;
		
		int bestEntityEditDistance = intervalEntityEditDistance[START_INTERVAL];
		int bestTermEditDistance = intervalTermEditDistance[START_INTERVAL];
		int bestTermMinimumSize = intervalTermMinimumSize[START_INTERVAL];
		int bestMinimumTermFrequency = intervalMinimumTermFrequency[START_INTERVAL];
		double bestSmoothStandardDeviation = intervalSmoothStandardDeviation[START_INTERVAL];
		double bestProbabilityFilterContext = intervalProbabilityFilterContext[START_INTERVAL];
		double bestProbabilityFilterContextOut = intervalProbabilityFilterContextOut[START_INTERVAL];
		double bestProbabilityFilterOut = intervalProbabilityFilterOut[START_INTERVAL];
		
		double bestFMeasure = 0;
		
		CRFExecutor crf;
		ArtificialIntelligenceInterpreter aII;
		String outputFile;
		
		for(entityEditDistance = intervalEntityEditDistance[START_INTERVAL]; entityEditDistance <= intervalEntityEditDistance[END_INTERVAL]; entityEditDistance++) {
			for(termEditDistance = intervalTermEditDistance[START_INTERVAL]; termEditDistance <= intervalTermEditDistance[END_INTERVAL]; termEditDistance++) {
				for(termMinimumSize = intervalTermMinimumSize[START_INTERVAL]; termMinimumSize <= intervalTermMinimumSize[END_INTERVAL]; termMinimumSize++) {
					for(minimumTermFrequency = intervalMinimumTermFrequency[START_INTERVAL]; minimumTermFrequency <= intervalMinimumTermFrequency[END_INTERVAL]; minimumTermFrequency++) {
						for(smoothStandardDeviation = intervalSmoothStandardDeviation[START_INTERVAL]; smoothStandardDeviation >= intervalSmoothStandardDeviation[END_INTERVAL]; smoothStandardDeviation -= 0.2) {
							for(probabilityFilterContext = intervalProbabilityFilterContext[START_INTERVAL]; probabilityFilterContext >= intervalProbabilityFilterContext[END_INTERVAL]; probabilityFilterContext -= 0.1) {
								for(probabilityFilterContextOut = intervalProbabilityFilterContextOut[START_INTERVAL]; probabilityFilterContextOut >= intervalProbabilityFilterContextOut[END_INTERVAL]; probabilityFilterContextOut -= 0.1) {
									for(probabilityFilterOut = intervalProbabilityFilterOut[START_INTERVAL]; probabilityFilterOut >= intervalProbabilityFilterOut[END_INTERVAL]; probabilityFilterOut -= 0.1) {
										
										ParametersAdjustment.printParamStatus("Complete Parameters Estimation", (iteration++),
												-1, entityEditDistance, termEditDistance, termMinimumSize, minimumTermFrequency, 
												smoothStandardDeviation, probabilityFilterContext, probabilityFilterContextOut, probabilityFilterOut);

										aII = new ArtificialIntelligenceInterpreter();
										
										aII.setEntityEditDistance(entityEditDistance);
										aII.setTermEditDistance(termEditDistance);
										aII.setTermMinimumSize(termMinimumSize);
										aII.setMinimumTermFrequencyPerSequence(minimumTermFrequency);
										aII.setStandardDeviationParcel(smoothStandardDeviation);
										aII.setProbabilityFilterCxt(probabilityFilterContext);
										aII.setProbabilityFilterCxtOut(probabilityFilterContextOut);
										aII.setProbabilityFilterOut(probabilityFilterOut);
										
										aII.setStopWord(stopWord);
										aII.setSupportContextWS1(supportContextWS1);
										aII.setSupportContextWS2(supportContextWS2);
										aII.setSupportContextWS3(supportContextWS3);
										
										aII.executeInterpreter(stopWordFile, contextSourceFile, fileToLabel);
										
										outputFile = aII.getOutputFilename();
										
										crf = new CRFExecutor("./samples/bcs2010.conf");
										crf.getStatistics("", outputFile, testFile);
										
										if(bestFMeasure <= crf.getCRFStatisticsReportList().get(0).getFmeasure()) {
											
											bestFMeasure = crf.getCRFStatisticsReportList().get(0).getFmeasure();
											
											bestEntityEditDistance = entityEditDistance;
											bestTermEditDistance = termEditDistance;
											bestTermMinimumSize = termMinimumSize;
											bestMinimumTermFrequency = minimumTermFrequency;
											bestSmoothStandardDeviation = smoothStandardDeviation;
											bestProbabilityFilterContext = probabilityFilterContext;
											bestProbabilityFilterContextOut = probabilityFilterContextOut;
											bestProbabilityFilterOut = probabilityFilterOut;
										}
										
									}
								}
							}	
						}
					}
				}
			}
		}
		
		return(new double [] {bestEntityEditDistance, bestTermEditDistance, bestTermMinimumSize, bestMinimumTermFrequency,
				bestSmoothStandardDeviation, bestProbabilityFilterContext, bestProbabilityFilterContextOut, bestProbabilityFilterOut});
	}
	
	protected static double[] executePartialParametersEstimation(String stopWordFile, 
			String contextSourceFile, String fileToLabel, String testFile) throws Exception {
		
		final int START_INTERVAL = 0;
		final int END_INTERVAL = 1;
		
		int iteration = 1;
		
		int [] intervalEntityEditDistance = {0, 2};
		int [] intervalTermEditDistance = {0, 3};
		int [] intervalTermMinimumSize = {3, 8};
		int [] intervalMinimumTermFrequency = {1, 10};
		double [] intervalSmoothStandardDeviation = {2.0, 1.0};
		double [] intervalProbabilityFilterContext = {1.0, 0.0};
		double [] intervalProbabilityFilterContextOut = {1.0, 0.0};
		double [] intervalProbabilityFilterOut = {1.0, 0.0};
		
		int entityEditDistance = 0;
		int termEditDistance = 1;
		int termMinimumSize;
		int minimumTermFrequency;
		double smoothStandardDeviation = 2;
		double probabilityFilterContext = 1;
		double probabilityFilterContextOut = 1;
		double probabilityFilterOut = 1;
		
		int bestEntityEditDistance = intervalEntityEditDistance[START_INTERVAL];
		int bestTermEditDistance = intervalTermEditDistance[START_INTERVAL];
		int bestTermMinimumSize = intervalTermMinimumSize[START_INTERVAL];
		int bestMinimumTermFrequency = intervalMinimumTermFrequency[START_INTERVAL];
		double bestSmoothStandardDeviation = intervalSmoothStandardDeviation[START_INTERVAL];
		double bestProbabilityFilterContext = intervalProbabilityFilterContext[START_INTERVAL];
		double bestProbabilityFilterContextOut = intervalProbabilityFilterContextOut[START_INTERVAL];
		double bestProbabilityFilterOut = intervalProbabilityFilterOut[START_INTERVAL];
		
		double bestFMeasure = 0;
		
		CRFExecutor crf;
		ArtificialIntelligenceInterpreter aII;
		String outputFile;
		
		for(termMinimumSize = intervalTermMinimumSize[START_INTERVAL]; termMinimumSize <= intervalTermMinimumSize[END_INTERVAL]; termMinimumSize++) {
			for(minimumTermFrequency = intervalMinimumTermFrequency[START_INTERVAL]; minimumTermFrequency <= intervalMinimumTermFrequency[END_INTERVAL]; minimumTermFrequency++) {
										
				ParametersAdjustment.printParamStatus("Complete Parameters Estimation", (iteration++),
						-1, entityEditDistance, termEditDistance, termMinimumSize, minimumTermFrequency, 
						smoothStandardDeviation, probabilityFilterContext, probabilityFilterContextOut, probabilityFilterOut);
	
				aII = new ArtificialIntelligenceInterpreter();
				
				aII.setEntityEditDistance(entityEditDistance);
				aII.setTermEditDistance(termEditDistance);
				aII.setTermMinimumSize(termMinimumSize);
				aII.setMinimumTermFrequencyPerSequence(minimumTermFrequency);
				aII.setStandardDeviationParcel(smoothStandardDeviation);
				aII.setProbabilityFilterCxt(probabilityFilterContext);
				aII.setProbabilityFilterCxtOut(probabilityFilterContextOut);
				aII.setProbabilityFilterOut(probabilityFilterOut);
				
				aII.setStopWord(stopWord);
				aII.setSupportContextWS1(supportContextWS1);
				aII.setSupportContextWS2(supportContextWS2);
				aII.setSupportContextWS3(supportContextWS3);
				
				aII.executeInterpreter(stopWordFile, contextSourceFile, fileToLabel);
				
				outputFile = aII.getOutputFilename();
				
				crf = new CRFExecutor("./samples/bcs2010.conf");
				crf.getStatistics("", outputFile, testFile);
				
				if(bestFMeasure <= crf.getCRFStatisticsReportList().get(0).getFmeasure()) {
					
					bestFMeasure = crf.getCRFStatisticsReportList().get(0).getFmeasure();
					
					bestEntityEditDistance = entityEditDistance;
					bestTermEditDistance = termEditDistance;
					bestTermMinimumSize = termMinimumSize;
					bestMinimumTermFrequency = minimumTermFrequency;
				}
			}
		}	
		
		bestSmoothStandardDeviation = ajustSmoothStandardDeviation(stopWordFile, contextSourceFile, fileToLabel,
				testFile, bestEntityEditDistance, bestTermEditDistance, bestTermMinimumSize, bestMinimumTermFrequency);
		bestProbabilityFilterContext = ajustProbabilityFilterCxt(stopWordFile, contextSourceFile, fileToLabel,
				testFile, bestEntityEditDistance, bestTermEditDistance, bestTermMinimumSize, bestMinimumTermFrequency, bestSmoothStandardDeviation);
		bestProbabilityFilterContextOut = ajustProbabilityFilterCxtOut(stopWordFile, contextSourceFile, fileToLabel,
				testFile, bestEntityEditDistance, bestTermEditDistance, bestTermMinimumSize, bestMinimumTermFrequency, bestSmoothStandardDeviation,
				bestProbabilityFilterContext);
		bestProbabilityFilterOut = ajustProbabilityFilterOut(stopWordFile, contextSourceFile, fileToLabel,
				testFile, bestEntityEditDistance, bestTermEditDistance, bestTermMinimumSize, bestMinimumTermFrequency, bestSmoothStandardDeviation,
				bestProbabilityFilterContext, bestProbabilityFilterOut);
		
		return(new double [] {bestEntityEditDistance, bestTermEditDistance, bestTermMinimumSize, bestMinimumTermFrequency,
				bestSmoothStandardDeviation, bestProbabilityFilterContext, bestProbabilityFilterContextOut, bestProbabilityFilterOut});
	}
}
