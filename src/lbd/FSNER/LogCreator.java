package lbd.FSNER;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import lbd.FSNER.Component.Cluster;
import lbd.FSNER.Component.ClusterHandler;
import lbd.FSNER.Component.Sequence;
import lbd.FSNER.Filter.Component.Entity;
import lbd.FSNER.Filter.Component.Term;
import lbd.data.handler.DataSequence;

public class LogCreator {
	
	protected static String ENCODE_USED = "ISO-8859-1";
	
	protected static final String FOLDER_INPUT = "/Input/";
	protected static final String FOLDER_OUTPUT = "/Output/";
	protected static final String ACRONYM_TERM_STATISTICS = "LOG-TermStatistics-";
	protected static final String ACRONYM_SCORE_STATISTICS = "LOG-ScoreStatistics-";
	protected static final String ACRONYM_CLUSTER_SCORE = "LOG-ClusterScore-";
	protected static final String ACRONYM_TERM_LENGHT_DISTRIBUTION = "LOG-TermLenghtDistribuition-";
	protected static final String ACRONYM_PARAMS = "LOG-Params-"; 
	
	protected static String SPACE = " ";
	
	/** Write Log Score Statistics **/
	protected static void writeLogScoreStatistics(String inputFilenameAddress, 
			ArrayList<Entity> entityList, ArrayList<Term> allTermList, Integer [] entityTermSizeList) {
		
		String statisticalMessage;
		
		ArrayList<Term> termList;
		
		Entity entity;
		Term term;
		
		try {
			
			Writer logOutTermStatistics = new OutputStreamWriter(new FileOutputStream(
					generateOutputFilename(inputFilenameAddress, ACRONYM_TERM_STATISTICS)), ENCODE_USED);
			
			logOutTermStatistics.write("-- Log Score Statistics \n");
			
			for(int e = entityList.size() - 1; e >= 0; e--) {
				
				entity = entityList.get(e);
				termList = entity.getTermList();
				
				statisticalMessage = entity.getId() + "(f" + entity.getFrequency() + ", tS" + termList.size() + ")" + " { ";
				
				for(int i = termList.size()-1; i >= 0; i--) {
					
					term = termList.get(i);
					
					statisticalMessage += term.getId() + "(f:" + term.getFrequency() + ", fps:" + term.getFrequencyPerSequence(); 
					statisticalMessage += ", " + (new DecimalFormat("#.##")).format(100.0*term.getScore()) + "%)";
					statisticalMessage += (i > 0)? ", " : " }\n\n";
				}
				
				logOutTermStatistics.write(statisticalMessage);
				//System.out.print(statisticalMessage);
			}
			
			/* Log All term List only when entityTermSizeList is not null */
			if(entityTermSizeList != null) {
				statisticalMessage = "All terms list (tS" + allTermList.size() + ")" + " { "; 
				
				for(int i = allTermList.size()-1; i >= 0; i--) {
					term = allTermList.get(i);
					
					statisticalMessage += term.getId() + "(f:" + term.getFrequency() + ", fps:" + term.getFrequencyPerSequence(); 
					statisticalMessage += ", " + (new DecimalFormat("#.##")).format(100.0*term.getScore()) + "%)";
					statisticalMessage += (i > 0)? ", " : " }\n\n";
				}
				
				logOutTermStatistics.write(statisticalMessage);
				writeTermLengthDistribution(inputFilenameAddress, allTermList);
			}			
			
			logOutTermStatistics.flush();
			logOutTermStatistics.close();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Write Term Length Distribution **/
	protected static void writeTermLengthDistribution(String inputFileNameAddress, ArrayList<Term> allTermList) throws IOException {
		
		final int TERM_SIZE_LIMIT = 11;
		
		int [] frequencyTermLength = new int [TERM_SIZE_LIMIT]; 
		double [] avgScore = new double [TERM_SIZE_LIMIT];
		int termLength;
		
		double percentage;
		double accumulatePercentage = 0;
		
		Writer logTermLengthDistribution = new OutputStreamWriter(new FileOutputStream(
				generateOutputFilename(inputFileNameAddress, ACRONYM_TERM_LENGHT_DISTRIBUTION)), ENCODE_USED);
		
		for(Term term : allTermList) {
			
			termLength = term.getId().length();
			
			if(termLength > 0 && termLength < TERM_SIZE_LIMIT) {
				frequencyTermLength[termLength-1]++;
				avgScore[termLength-1] += term.getScore();
			} else if (termLength > TERM_SIZE_LIMIT - 1) {
				frequencyTermLength[frequencyTermLength.length-1]++;
				avgScore[frequencyTermLength.length-1] += term.getScore();
			}
		}
		
		for(int i = 0; i < frequencyTermLength.length-1; i++) {
			
			percentage = frequencyTermLength[i]/((double)allTermList.size()) ;
			accumulatePercentage += percentage;
			
			logTermLengthDistribution.write("Length(" + (i+1) + "): " + (new DecimalFormat("#.##").format(100 * percentage)) + "%");
			logTermLengthDistribution.write(" acc: " + (new DecimalFormat("#.##").format(100 * accumulatePercentage)) + "%");
			logTermLengthDistribution.write(" score: " + (new DecimalFormat("#.##").format(avgScore[i])));
			logTermLengthDistribution.write(" avg-score: " + (new DecimalFormat("#.##").format(avgScore[frequencyTermLength.length-1]/((double)frequencyTermLength[i]))) + "\n");
		}
		
		percentage = frequencyTermLength[frequencyTermLength.length-1]/((double)allTermList.size());
			
		logTermLengthDistribution.write("Length(> 10): " + (new DecimalFormat("#.##").format(100 * percentage)) + "%");
		logTermLengthDistribution.write(" acc: 100%");
		logTermLengthDistribution.write(" score: " + (new DecimalFormat("#.##").format(avgScore[frequencyTermLength.length-1])));
		logTermLengthDistribution.write(" avg-score: " + (new DecimalFormat("#.##").format(avgScore[frequencyTermLength.length-1]/((double)frequencyTermLength[frequencyTermLength.length-1]))) + "\n");
		
		logTermLengthDistribution.flush();
		logTermLengthDistribution.close();
		
	}
	
	/**
	 * 
	 * Phase II
	 * 
	 **/
	
	protected static void writeLogStatisticalScore(String inputFilenameAddress, ArrayList<Entity> entityList) {
		
		try {
			String logMessage;
			int index;
			
			Writer logOutScoreStatistics = new OutputStreamWriter(
					new FileOutputStream(generateOutputFilename(inputFilenameAddress, ACRONYM_SCORE_STATISTICS)), ENCODE_USED);
			
			for(Entity entity : entityList) {
				
				logMessage = entity.getId() + "[min: " + entity.getMinScore();
				logMessage += ", max: " + entity.getMaxScore();
				logMessage += ", avg: " + entity.getAverageScore();
				logMessage += ", stdDev: " + entity.getStardardDeviationScore() + "]\n";
				
				index = 1;
				
				for(Double score : entity.getScoreSequenceList()) {
					logMessage += "\t[" + (index++) + "] " + score + "\n"; 
				}
				
				logMessage += "\n";
				
				logOutScoreStatistics.write(logMessage);
			}
			
			logOutScoreStatistics.flush();
			logOutScoreStatistics.close();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	protected static void writeLogClusterScore(String inputFilenameAddress, ClusterHandler clusterHandler) {
		
		try {
			
			String logMessage;
			String clusterTerms;
			int globalClusterNumber = 1;
			int clusterNumber = 1;
			int scoreNumber = 1;
			int termNumber = 0;
			
			Writer logClusterScore = new OutputStreamWriter(
					new FileOutputStream(LogCreator.generateOutputFilename(inputFilenameAddress, ACRONYM_CLUSTER_SCORE)), ENCODE_USED);
			
			ArrayList<Cluster> clusterList;
			Iterator <Entry<String, ArrayList<Cluster>>> iteClusterMap = clusterHandler.getClusterMap().entrySet().iterator();
			Iterator <Entry<String, Term>> iteTerms;
			
			logClusterScore.write("** Cluster Score Log - Coefficient of Similarity: " + clusterHandler.getCoefficientOfSimilarity() + "\n\n");
			
			while(iteClusterMap.hasNext()) {
				
				clusterList = iteClusterMap.next().getValue();
				
				logMessage = "-- Entity: " + clusterList.get(0).getEntity().getId() + "\n\n";
				clusterNumber = 1;
				
				for(Cluster cluster : clusterList) {
					
					logMessage += "\tCluster[" + (globalClusterNumber++) + ", " + (clusterNumber++) + "]";
					logMessage += " [Min: " + cluster.getMinimumScore();
					logMessage += " Avg: " + cluster.getAverageScore();
					logMessage += " Max: " + cluster.getMaximumScore() + "]\n\n";
					
					clusterTerms = "{ ";
					scoreNumber = 1;
					termNumber = 0;
					iteTerms = cluster.getTermMap().entrySet().iterator();
					
					for(Sequence sequence : cluster.getSequenceList()) {
						logMessage += "\t\t[" + (scoreNumber++) + "] " + sequence.getScore();
						logMessage += " similarity(" + (new DecimalFormat("#.##")).format(sequence.getSimilarity()) + ")";
						
						logMessage += "\n\t\t";
						
						for(Term term : sequence.getTermList())
							logMessage += term.getId() + "(" + (new DecimalFormat("#.##")).format(term.getScore()) + ") ";
						
						logMessage += "\n\n";
					}
					
					while(iteTerms.hasNext()) {
						clusterTerms += iteTerms.next().getValue().getId() + ", ";
						termNumber++;
					}
					
					clusterTerms += "}";
					logMessage += "\t\tTermsList(" + termNumber + ") " + clusterTerms + "\n\n";
				}
				
				logMessage += "\n";
				
				logClusterScore.write(logMessage);
			}
			
			logClusterScore.flush();
			logClusterScore.close();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/************************************************************************************************************************
	 * 
	 * 
	 * Phase III: Label File
	 *
	 * 
	 *************************************************************************************************************************/
	
	protected static void writeSequenceScore(Writer out, DataSequence sequence, ArrayList<Double> scoreList) throws IOException {
		
		int scoreIndex = 0;
		
		for(int i = 0; i < sequence.length(); i++)
			out.write(sequence.x(i) + SPACE);
		
		out.write("\n\t Score: " + ((scoreList.size() == 0)?"---":""));
		
		for(Double score : scoreList)
			out.write("[" + (scoreIndex++) + "] " + score + " ");
		
		out.write("\n\n");
	}
	
	protected static void writeParams(String inputFilenameAddress, boolean useSmoothParam,
			int termMinimumSize, int minimumTermFrequencyPerSequence, 
			double standardDeviationParcel) {
		
		try {
			
			Writer logParams = new OutputStreamWriter(new FileOutputStream(generateOutputFilename(inputFilenameAddress, ACRONYM_PARAMS)), ENCODE_USED);
			
			logParams.write("-- ArtificialInteligenceInterpreter Class (Params)\n");
			logParams.write("\t* Use Smooth Param: " + useSmoothParam + "\n");
			logParams.write("\t* Term Minimum Size: " + termMinimumSize + "\n");
			logParams.write("\t* Minimum Term Frequence: " + minimumTermFrequencyPerSequence + "\n");
			logParams.write("\t* Standard Deviation Parcel: " + standardDeviationParcel + "\n");
			
			logParams.write("\n-- Entity Class (Params)\n");
			logParams.write("\t* Minimum Relax Matching Length: " + Entity.MINIMUM_RELAX_MATCHING_LENGTH + "\n");
			logParams.write("\t* Edit Distance Acceptable: " + Entity.EDIT_DISTANCE_ACCEPTABLE + "\n");
			
			logParams.write("\n-- Term Class (Params)\n");
			logParams.write("\t* Minimum Relax Matching Length: " + Term.MINIMUM_RELAX_MATCHING_LENGTH + "\n");
			logParams.write("\t* Edit Distance Acceptable: " + Term.EDIT_DISTANCE_ACCEPTABLE + "\n");
			
			logParams.flush();
			logParams.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/************************************************************************************************************************
	 * 
	 * 
	 * Others
	 *
	 * 
	 *************************************************************************************************************************/
	
	protected static String generateOutputFilename(String fileInputNameAddress, String acronym) {
		
		int endPos = fileInputNameAddress.lastIndexOf("/") + 1;
		
		String outputFile = fileInputNameAddress.substring(0, endPos);
		outputFile = outputFile.replace(FOLDER_INPUT, FOLDER_OUTPUT);
		
		return(outputFile + acronym + fileInputNameAddress.substring(endPos));
	}

}
