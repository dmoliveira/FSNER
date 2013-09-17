package lbd.FSNER;

import lbd.CRF.CRFExecutor;
import lbd.CRF.CRFStatistics;
import lbd.FSNER.Filter.Component.Entity;
import lbd.FSNER.Filter.Component.Term;
import lbd.Utils.Utils;

public class ParametersAdjustment {
	
	protected static final String ACRONYM_OUTPUT_FILENAME = "AII-";
	
	public static double ajustSmoothStandardDeviation(ArtificialIntelligenceInterpreter aII, String stopWordFile, 
			String contextSourceFile, String fileToLabel, String testFile) {
		
		double bestFMeasure = 0;
		double bestSmoothStandardDeviation = 2;
		int iteration = 1;
		
		String outputFile = LogCreator.generateOutputFilename(fileToLabel, ACRONYM_OUTPUT_FILENAME);
		
		CRFStatistics crfStats;
		
		aII.loadStopWords(stopWordFile);
		aII.loadSupportContext(contextSourceFile);
		aII.loadEntityList(contextSourceFile);
		aII.calculateScorePerSequence();
		
		for(double i = bestSmoothStandardDeviation; i >= -2; i -= 0.1) {
			
			printParamStatus("Smooth Standard Deviation", (iteration++),
					i, Entity.EDIT_DISTANCE_ACCEPTABLE, Term.EDIT_DISTANCE_ACCEPTABLE,
					aII.getTermMinimumSize(), aII.getMinimumTermFrequencyPerSequence(), i, 
					aII.getProbabilityFilterCxt(), aII.getProbabilityFilterCxtOut(), aII.getProbabilityFilterOut());
			
			aII.setStandardDeviationParcel(i);
			aII.labelFile();
			
			/* Analysis Result (OutputFile and Test File) */
			crfStats = CRFExecutor.getStatisticsLite(outputFile, testFile, false);
			
			if(bestFMeasure < crfStats.getFmeasure()) {
				bestFMeasure = crfStats.getFmeasure();
				bestSmoothStandardDeviation = i;
			}
		}
		
		return(bestSmoothStandardDeviation);
	}
	
	public static double ajustProbabilityFilterCxt (ArtificialIntelligenceInterpreter aII, 
			String stopWordFile, String contextSourceFile, String fileToLabel, String testFile) {
		
		double bestFMeasure = 0;
		double bestProbabilityFilterCxt = 1;
		int iteration = 1;
		
		String outputFile = LogCreator.generateOutputFilename(fileToLabel, ACRONYM_OUTPUT_FILENAME);
		
		CRFStatistics crfStats;
		
		aII.loadStopWords(stopWordFile);
		aII.loadSupportContext(contextSourceFile);
		aII.loadEntityList(contextSourceFile);
		aII.calculateScorePerSequence();
		
		for(double i = bestProbabilityFilterCxt; i >= 0; i -= 0.1) {
			
			printParamStatus("Probability Filter Cxt", (iteration++),
					i, Entity.EDIT_DISTANCE_ACCEPTABLE, Term.EDIT_DISTANCE_ACCEPTABLE,
					aII.getTermMinimumSize(), aII.getMinimumTermFrequencyPerSequence(), aII.getStandardDeviationParcel(), 
					i, aII.getProbabilityFilterCxtOut(), aII.getProbabilityFilterOut());
			
			aII.setProbabilityFilterCxt(i);
			aII.labelFile();
			
			/* Analysis Result (OutputFile and Test File) */
			crfStats = CRFExecutor.getStatisticsLite(outputFile, testFile, false);
			
			if(bestFMeasure < crfStats.getFmeasure()) {
				bestFMeasure = crfStats.getFmeasure();
				bestProbabilityFilterCxt = i;
			}
		}
		
		return(bestProbabilityFilterCxt);
	}
	
	public static double ajustProbabilityFilterCxtOut (ArtificialIntelligenceInterpreter aII, 
			String stopWordFile, String contextSourceFile, String fileToLabel, String testFile) {
		
		double bestFMeasure = 0;
		double bestProbabilityFilterCxtOut = 1;
		int iteration = 1;
		
		String outputFile = LogCreator.generateOutputFilename(fileToLabel, ACRONYM_OUTPUT_FILENAME);
		
		CRFStatistics crfStats;
		
		aII.loadStopWords(stopWordFile);
		aII.loadSupportContext(contextSourceFile);
		aII.loadEntityList(contextSourceFile);
		aII.calculateScorePerSequence();
		
		for(double i = bestProbabilityFilterCxtOut; i >= 0; i -= 0.1) {
			
			printParamStatus("Probability Filter Cxt Out", (iteration++),
					i, Entity.EDIT_DISTANCE_ACCEPTABLE, Term.EDIT_DISTANCE_ACCEPTABLE,
					aII.getTermMinimumSize(), aII.getMinimumTermFrequencyPerSequence(), aII.getStandardDeviationParcel(), 
					aII.getProbabilityFilterCxt(), i, aII.getProbabilityFilterOut());
			
			aII.setProbabilityFilterCxtOut(i);
			aII.labelFile();
			
			/* Analysis Result (OutputFile and Test File) */
			crfStats = CRFExecutor.getStatisticsLite(outputFile, testFile, false);
			
			if(bestFMeasure < crfStats.getFmeasure()) {
				bestFMeasure = crfStats.getFmeasure();
				bestProbabilityFilterCxtOut = i;
			}
		}
		
		return(bestProbabilityFilterCxtOut);
	}
	
	public static double ajustProbabilityFilterOut (ArtificialIntelligenceInterpreter aII, 
			String stopWordFile, String contextSourceFile, String fileToLabel, String testFile) {
		
		double bestFMeasure = 0;
		double bestProbabilityFilterOut = 1;
		int iteration = 1;
		
		String outputFile = LogCreator.generateOutputFilename(fileToLabel, ACRONYM_OUTPUT_FILENAME);
		
		CRFStatistics crfStats;
		
		aII.loadStopWords(stopWordFile);
		aII.loadSupportContext(contextSourceFile);
		aII.loadEntityList(contextSourceFile);
		aII.calculateScorePerSequence();
		
		for(double i = bestProbabilityFilterOut; i >= 0; i -= 0.1) {
			
			printParamStatus("Probability Filter Out", (iteration++),
					i, Entity.EDIT_DISTANCE_ACCEPTABLE, Term.EDIT_DISTANCE_ACCEPTABLE,
					aII.getTermMinimumSize(), aII.getMinimumTermFrequencyPerSequence(), aII.getStandardDeviationParcel(), 
					aII.getProbabilityFilterCxt(), aII.getProbabilityFilterCxtOut(), i);
			
			aII.setProbabilityFilterOut(i);
			aII.labelFile();
			
			/* Analysis Result (OutputFile and Test File) */
			crfStats = CRFExecutor.getStatisticsLite(outputFile, testFile, false);
			
			if(bestFMeasure < crfStats.getFmeasure()) {
				bestFMeasure = crfStats.getFmeasure();
				bestProbabilityFilterOut = i;
			}
		}
		
		return(bestProbabilityFilterOut);
	}
	
	public static void printParamStatus(String paramTitle, int iteration, double value,
			int entityEditDistance, int termEditDistance, int termMinimumsize, int minimumFrequencyTerm,
			double standardDeviationParcel, double probabilisticFilterContext, double probabilisticFilterContextOut,
			double probabilisticFilterOut) {
		
		//String paramStatus = "\nAjust " + paramTitle + " ITE " + iteration + " value(" + Utils.formatDecimalNumber(value) + ") " ;
    	String paramStatus = "\n\tITE " + iteration + " value(" + Utils.formatDecimalNumber(value) + ") " ;
		
		paramStatus += "[EED: " + entityEditDistance;
		paramStatus += " TED: " + termEditDistance;
		paramStatus += " TMS: " + termMinimumsize;
		paramStatus += " MTFPS: " + minimumFrequencyTerm;
		paramStatus += " SDP: " + Utils.formatDecimalNumber(standardDeviationParcel);
		paramStatus += " PFC: " + Utils.formatDecimalNumber(probabilisticFilterContext);
		paramStatus += " PFCO: " + Utils.formatDecimalNumber(probabilisticFilterContextOut);
		paramStatus += " PFO: " + Utils.formatDecimalNumber(probabilisticFilterOut) + "]";
		
		System.out.print(paramStatus);		
	}
}
