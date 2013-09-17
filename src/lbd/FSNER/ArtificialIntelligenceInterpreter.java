package lbd.FSNER;

import iitb.CRF.DataSequence;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Map.Entry;

import lbd.CRF.CRFExecutor;
import lbd.CRF.CRFStatistics;
import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.LabelMap;
import lbd.CRF.SequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;
import lbd.Evaluation.Evaluator;
import lbd.FSNER.Component.Cluster;
import lbd.FSNER.Component.ClusterHandler;
import lbd.FSNER.Component.EntityLinker;
import lbd.FSNER.Component.Sequence;
import lbd.FSNER.Filter.Component.Entity;
import lbd.FSNER.Filter.Component.Term;
import lbd.Model.ContextToken;
import lbd.Model.SupportContext;
import lbd.NewModels.Context.ContextManager;
import lbd.NewModels.Context.ContextManager.ContextType;
import lbd.Utils.ExtendsAccentVariabilityInTweet;
import lbd.Utils.QuickSort;
import lbd.Utils.RemoveStopWordsTool;
import lbd.Utils.Utils;

public class ArtificialIntelligenceInterpreter implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	//-- Constants
	protected final String ENCODE_USED = "ISO-8859-1";
	
	protected final String DELIMITER_LABEL = "|";
	protected final String TAG_SPACE = " ";
	protected final String TAG_BLANK = "";
	protected final String TAG_COMMA = ",";
	
	protected final boolean USE_SMOOTH_PARAM = false;
	protected final boolean USE_FEEDBACK = false;
	protected static boolean isAjdustmentMode = false;
	protected static boolean isProbabilisticFilter = false;
	
	protected SupportContext supportContextWS3;
	protected SupportContext supportContextWS2;
	protected SupportContext supportContextWS1;
	
	protected ContextManager contextManager;
	protected int counter;
	protected ContextType contextType;
	protected int windowSize;
	
	//-- Term Params - CanAddTerm, RemoveTermBelow,
	protected int termMinimumSize = 3;//7-3 [3..7]
	protected int minimumTermFrequencyPerSequence = 1;//1-3 [1..10]
	protected double standardDeviationParcel = 0;//[0..5]
	
	//-- Probabilistic Filter associated with anothers
	protected double probabilityFilterCxt = 0;//0.7;
	protected double probabilityFilterCxtOut = 0;//0.7;
	protected double probabilityFilterOut = 0;//0.7;
	protected double probabilityTFIDFFilter = 1;//1 -- Used to adjust Threshold in some cases
	protected int [] enableFilterCxt;
	protected String filterCode;
	protected int [] enableFilters;
	protected int [] filterOrder;
	
	//-- Update Ratio
	protected int DECAY_RATIO = 25000;
	
	//-- Cluster Similarity Params
	protected double COEFFICIENT_OF_SIMILARITY = 0;
	protected double COEFFICIENT_OF_GENERAL_SIMILARITY = 0.5;
	
	protected final String ACRONYM_SCORE_OUTPUT_SEQUENCE = "LOG-ScoreOutputSequence-";
	protected final String ACRONYM_OUTPUT_FILENAME = "AII-";
	
	//-- Objects
	protected ClusterHandler clusterHandler;
	protected Integer [] entityTermSizeList;
	protected ArrayList<Term> allTermList;
	protected ArrayList<Entity> entityList;
	protected RemoveStopWordsTool stopWord;
	protected EntityLinker entityLinker;
	protected HashMap<String, ArrayList<String>> entitySynonymMap;
	
	//-- Filenames
	protected String stopWordFile;
	protected String contextSourceFile;
	protected String fileToLabel;
	protected String entitySynonymFile;
	
	//-- Output files
	protected String outputFilename;
	
	public ArtificialIntelligenceInterpreter() {
		entityList = new ArrayList<Entity>();
		allTermList = new ArrayList<Term>();
		clusterHandler = new ClusterHandler(COEFFICIENT_OF_SIMILARITY, COEFFICIENT_OF_GENERAL_SIMILARITY);
		entityLinker = new EntityLinker();
		entitySynonymMap = new HashMap<String, ArrayList<String>>();
		contextManager = new ContextManager();
	}
	
	public ArtificialIntelligenceInterpreter(String stopWordFile, String contextSourceFile, String fileToLabel) {
		this(stopWordFile, "", contextSourceFile, fileToLabel);
	}
	
	public ArtificialIntelligenceInterpreter(String stopWordFile, String synonymEntityFile, String contextSourceFile, String fileToLabel) {
		entityList = new ArrayList<Entity>();
		allTermList = new ArrayList<Term>();
		clusterHandler = new ClusterHandler(COEFFICIENT_OF_SIMILARITY, COEFFICIENT_OF_GENERAL_SIMILARITY);
		entityLinker = new EntityLinker();
		entitySynonymMap = new HashMap<String, ArrayList<String>>();
		
		this.stopWordFile = stopWordFile;
		this.contextSourceFile = contextSourceFile;
		this.fileToLabel = fileToLabel;
		this.entitySynonymFile = synonymEntityFile;
	}
	
	public void executeInterpreter(String stopWordFile, String contextSourceFile, String fileToLabel) {
		executeInterpreter(stopWordFile, "", contextSourceFile, fileToLabel);
	}
	
	public void executeInterpreter(String stopWordFile, String entitySynonymFile, String contextSourceFile, String fileToLabel) {
		
		Date date;
		
		this.stopWordFile = stopWordFile;
		this.contextSourceFile = contextSourceFile;
		this.fileToLabel = fileToLabel;
		
		//System.out.println("-- Start");
		
		//-- Load Entity Linker
		/*date = new Date();
		entityLinker.loadEntityMap("./samples/data/bcs2010/AutoTagger/Input/Lookup Lists/WEPS3-Task2 Wikipedia(Objective).lst");
		System.out.println("   Load Entity Names (" + (((new Date()).getTime() - date.getTime())/1000) + "s)");*/
		
		//-- Load Stopwords List
		date = new Date();
		loadStopWords(stopWordFile);
		//System.out.println("   Load Stop Words (" + (((new Date()).getTime() - date.getTime())/1000) + "s)");
		
		//-- Load SupportContext
		date = new Date();
		loadSupportContext(contextSourceFile);
		//System.out.println("   Load Support Context (" + (((new Date()).getTime() - date.getTime())/1000) + "s)");
		
		//-- Load Entity List
		date = new Date();
		loadEntityList(contextSourceFile);
		//System.out.println("   Load Entity List (" + (((new Date()).getTime() - date.getTime())/1000) + "s)");
		
		//-- Create Entity Name Variation Reference List
		date = new Date();
		createEntityNameVariationReference();
		//System.out.println("   Create Entity Name Variation Reference List (" + (((new Date()).getTime() - date.getTime())/1000) + "s)");
		
		//-- Create Synonym Entity List
		date = new Date();
		createEntitySynonymList(entitySynonymFile);
		//System.out.println("   Create Entity Synonym List (" + (((new Date()).getTime() - date.getTime())/1000) + "s)");
		
		//-- Calculate Cluster, Sequence and Term Score
		date = new Date();
		calculateScorePerSequence();
		//System.out.println("   Calculate Score Per Sequence (" + (((new Date()).getTime() - date.getTime())/1000) + "s)");
		
		/* Only to estimate params */
		HashMap <String, String> filterCombination = new HashMap<String, String>();
		//for(double j = 1; j > 0; j -= 0.01) {
		/*for(int f1=0;f1<2;f1++) //-- Filter 1
			for(int f2=0;f2<2;f2++) //-- Filter 2
				for(int f3=0;f3<2;f3++) { //-- Filter 3*/ 
					
		int [][] filterOrderPermutation = {{1,2,3},{2,1,3},{2,3,1},{3,2,1},{3,1,2},{1,3,2}};
		int [] enableContextFilter = {1,1,1,1,1,1,1,1,1};
		int [] enableFilters = {0,1,0};//-- 0 TF-IDF, 1 Context, 2 Probabilistic
		//enableFilters[0] = f1; enableFilters[1] = f2; enableFilters[2] = f3;
		filterOrder = filterOrderPermutation[0];
		
		/*for(int i1=0;i1<2;i1++) //-- C3
			for(int i2=0;i2<2;i2++) //-- P3
				for(int i3=0;i3<2;i3++) //-- S3
					for(int i4=0;i4<2;i4++) //-- C2
						for(int i5=0;i5<2;i5++) //-- P2
							for(int i6=0;i6<2;i6++) //--S2
								for(int i7=0;i7<2;i7++) //--C1
									for(int i8=0;i8<2;i8++) //--P1
										for(int i9=0;i9<2;i9++) { //--S1
											
		enableContextFilter[0] = i1; enableContextFilter [1] = i2; enableContextFilter [2] = i3;
		enableContextFilter [3] = i4; enableContextFilter [4] = i5; enableContextFilter [5] = i6;
		enableContextFilter [6] = i7; enableContextFilter [7] = i8; enableContextFilter [8] = i9;*/
		
		this.enableFilters = enableFilters;
		this.enableFilterCxt = enableContextFilter;
		this.probabilityTFIDFFilter = 0.39;//0.6
		this.probabilityFilterCxtOut = 0.75;//0.6
		
		/*for(int o = 0; o < filterOrderPermutation.length; o++) {
			filterOrder = filterOrderPermutation[o];
			
		generateFilterCode();
		if(filterCombination.containsKey(filterCode))
			continue;
		else
			filterCombination.put(filterCode, filterCode);*/
		
		//-- Label the Target File
		date = new Date();
		labelFile();
		//System.out.println("   Label File (" + (((new Date()).getTime() - date.getTime())/1000) + "s)");
		
		//-- Delete below, only for test..		
		Evaluator.evaluate("", outputFilename, fileToLabel);
		
		/*if(f1+f2+f3 <= 1) break;}*/ 
		//}
		
		System.out.println("Cxt-Counter: " + counter);
	}
	
	/** Load Stop Words **/
	public void loadStopWords(String stopWordFile) {
		if(stopWord == null)
			stopWord = new RemoveStopWordsTool(stopWordFile);
	}
	
	/** Load SupportContext **/
	public void loadSupportContext(String contextSourceFile) {
		
		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(contextSourceFile,
				FileType.TRAINING, false);
		
		if(supportContextWS1 == null) {
			supportContextWS1 = new SupportContext(1, "BILOU", true);
			supportContextWS1.generateContext(inputSequenceSet);
		}
		
		if(supportContextWS2 == null) {
			supportContextWS2 = new SupportContext(2, "BILOU", true);
			supportContextWS2.generateContext(inputSequenceSet);
		}
		
		if(supportContextWS3 == null) {
			supportContextWS3 = new SupportContext(3, "BILOU", true);
			supportContextWS3.generateContext(inputSequenceSet);
		}
	}
	
	/** Load EntityList **/
	public void loadEntityList(String contextSourceFile) {
		
		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(contextSourceFile,
				FileType.TRAINING, false);
		
		while(inputSequenceSet.hasNext())
			addTermFrequencyForSequence(inputSequenceSet.next());
		
		//-- Remove outliers (Heavy!!)
		//removeTermBelowFrequency();
		
		//-- Sort all term list by Ascendent Frequency per Sequence
		sortAllTermListByAscendentFrequencyPerSequence();
		
		//-- Calculate Term Score per Entity (and Sort Terms by Ascendent Score)
		calculateTermScorePerEntity();
		
		//-- Sort entityList by Ascendent Term Size
		sortByAscendentTermSize();
		
		calculateAllTermListScore();
		
		if(!isAjdustmentMode)
			LogCreator.writeLogScoreStatistics(fileToLabel, entityList, allTermList, entityTermSizeList);
	}
	
	protected void createEntityNameVariationReference() {
		
		ArrayList<Cluster> clusterList;
		ExtendsAccentVariabilityInTweet eXAVIT = new ExtendsAccentVariabilityInTweet();
		
		String entityName;
		ArrayList<String> entityNameVariationList;
		
		Entity newEntity;
		ArrayList<Entity> entityLink = new ArrayList<Entity>();
		
		for(Entity entity : entityList) {
			
			entityName = entity.getId();
			clusterList = clusterHandler.getClusterMap().get(entityName);
			
			entityNameVariationList = eXAVIT.generateVariationAccentToken(entityName);
			
			for(String entityNameVariation : entityNameVariationList) {
				if(!entity.equals(entityNameVariation)) {
					
					newEntity = new Entity(entityNameVariation);
					newEntity.linkEntity(entity);
					
					entityLink.add(newEntity);
					clusterHandler.addClusterList(entityNameVariation, clusterList);
				}
			}
		}
		
		entityList.addAll(entityLink);
	}	
	
	protected void createEntitySynonymList(String synonymEntityFile) {
		
		String line;
		String [] entityList;
		String entityName;
		
		ArrayList<String> entitySynonym;
		
		if(!synonymEntityFile.equals("")) {
			try {
				
				BufferedReader in = new BufferedReader (new InputStreamReader(
						new FileInputStream(synonymEntityFile), ENCODE_USED));
				
				while((line = in.readLine()) != null) {
					
					entityList = line.split(TAG_COMMA);
					entityName = entityList[0].toLowerCase();
					
					entitySynonymMap.put(entityName, new ArrayList<String>());
					entitySynonym = entitySynonymMap.get(entityName);
					
					for(int i = 1; i < entityList.length; i++)
						entitySynonym.add(entityList[i].toLowerCase());
				}
				
				in.close();
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected ArrayList<Entity> addTermFrequencyForSequence(DataSequence sequence) {
		
		final int SEQUENCE_MINIMUM_SIZE = 2;
		
		int entityIndex;
		Entity entity;
		
		ArrayList<Entity> entityListInSequence = new ArrayList<Entity>();
		ArrayList<String> termNameListInSequence = new ArrayList<String>();
		
		HashMap<String, Boolean> entityNameListInSequence = new HashMap<String, Boolean>();
		
		String [] sequenceLowerCase = Utils.convertSequenceToLowerCase(sequence, sequence.length());
		String [] sequenceWithoutStopWord = removeStopWordFromSequence(sequenceLowerCase);
		
		String encodedContext;
		
		if(sequenceWithoutStopWord.length >= SEQUENCE_MINIMUM_SIZE) {
			for(int i = 0; i < sequence.length(); i++) {
				
				entityIndex = getEntityIndex(sequence, i);
				
				//-- add context [new Context Filter]
				if(entityIndex != -1) {
					encodedContext = contextManager.getEncodedContext(sequenceLowerCase, entityIndex);
					contextManager.addContext(encodedContext);
				}
				
				if(entityIndex != -1 && !entityNameListInSequence.containsKey(sequenceLowerCase[entityIndex])) {
						
					addFrequencyEntityTerms(sequenceLowerCase[entityIndex], sequenceWithoutStopWord,
							entityListInSequence, termNameListInSequence);
					
					entityNameListInSequence.put(sequenceLowerCase[entityIndex], false);
					
					//-- shift index to optimize search for entities
					i = entityIndex;
				} else if(entityIndex == -1 && (entity = getEntity(sequenceLowerCase[i])) != null) {
					//-- add to frequency as non entity when the term is ambiguous
					entity.addToFrequencyAsNonEntity();
				}
			}
			
			//-- add sequence to cluster
			clusterHandler.addSequence(entityListInSequence, sequenceWithoutStopWord);
			
			//-- add one entity frequency per sequence
			addToEntityFrequencyPerSequence(entityListInSequence, termNameListInSequence);
			
			//-- set similarity between entities
			setSimilarityBetweenEntities(entityListInSequence);
		}
		
		return(entityListInSequence);
	}
	
	protected int getEntityIndex(DataSequence sequence, int startPos) {
		
		int entityIndex = -1;
		
		for(int i = startPos; i < sequence.length(); i++){
			if(isEntity(sequence.y(i))) {
				entityIndex = i;
				break;
			}
		}
		
		return(entityIndex);
	}
	
	public String [] removeStopWordFromSequence(String [] sequence) {
		
		ArrayList<String> cleanSequence = new ArrayList<String>();
		String term;
		
		for(int i = 0; i < sequence.length; i++) {
			
			term = stopWord.removeStopWord(sequence[i].trim());
			
			if(!(term.equals(TAG_BLANK) || term.equals(TAG_SPACE)) && canAddTerm(sequence[i], termMinimumSize))
				cleanSequence.add(sequence[i]);
		}
		
		return((String[])cleanSequence.toArray(new String[cleanSequence.size()]));		
	}
	
	protected void addFrequencyEntityTerms(String entityName, String [] termBag, 
			ArrayList<Entity> entityList, ArrayList<String>termNameList) {
		
		Entity entity = getEntity(entityName);
		
		if(entity == null) {
			
			this.entityList.add(new Entity(entityName));
			entity = this.entityList.get(this.entityList.size() - 1);
			
			entityList.add(entity);
			
		} else if(!entityList.contains(entity)){
			entityList.add(entity);
		}
		
		for(int i = 0; i < termBag.length; i++) {
			entity.addFrequency(termBag[i]);
			
			//-- Add term to termList only if the term is not in the list
			if(!termNameList.contains(termBag[i]))
				termNameList.add(termBag[i]);
		}		
	}
	
	public static boolean canAddTerm(String term, int termMinimumSize) {
		
		boolean canAddTerm = true;
		
		//-- If term have lesser the minimum size ignore term
		if(term.length() < termMinimumSize) {
			canAddTerm = false;
		} else {
			try {
				Double.parseDouble(term.trim());
				canAddTerm = false;
			}catch(NumberFormatException e) {}
		}
		
		return(canAddTerm);
	}
	
	public Entity getEntity(String entityName) {
		
		Entity entity = null;
		
		for(Entity candidateEntity : entityList) {
			if(candidateEntity.isTermMatching(entityName)) {
				entity = candidateEntity;
				break;
			}
		}
		
		return(entity);
	}
	
	protected void addToEntityFrequencyPerSequence(ArrayList<Entity> entityList, ArrayList<String> termNameList) {
		
		Term term;
		
		for(Entity entity : entityList) {
			entity.addFrequencyPerSequence();
			
			for(String termName : termNameList) {
				//-- Need to be the term from the entity
				term = entity.getTerm(termName);
				term.addFrequencyPerSequence();
			}
		}
	}
	
	protected void addToTermFrequencyPerSequence(ArrayList<Term> termList) {
		for(Term term: termList)
			term.addFrequencyPerSequence();
	}
	
	protected void calculateTermScorePerEntity() {
		calculateTermScorePerEntity(entityList);
	}
	
	protected void calculateTermScorePerEntity(ArrayList<Entity> entityList) {
		
		entityTermSizeList = new Integer[entityList.size()];
		int index = 0;
		
		for(Entity entity : entityList) {
			entity.calculateTermScore();
			entity.sortByAscendentTermScore();
			entityTermSizeList[index++] = entity.getTermList().size();
		}
	}
	
	public void sortByAscendentTermSize() {
		QuickSort.sort(entityTermSizeList, entityList);
	}
	
	//-- Check BILOU only (Entity[0-Beginning,1-Inside,2-Last,4-UnitToken] / 3-Outside)
	public static boolean isEntity(int label) {
		return(label > -1 && label < 5 && label != 3);
	}
	
	protected void removeTermBelowFrequency() {
		
		ArrayList<Term> cleanTermList;
		ArrayList<Term> termList;
		Term candidateTerm;
		
		for(Entity entity : entityList) {
			
			cleanTermList = new ArrayList<Term>();
			
			for(Term term : entity.getTermList()) {
				
				termList = entity.getTermList();
				
				if((term.getFrequencyPerSequence() >= minimumTermFrequencyPerSequence ||
						((double)termList.get(termList.size()-1).getFrequencyPerSequence())/minimumTermFrequencyPerSequence <= 0.5 )) {
					
					cleanTermList.add(term);
					candidateTerm = containsTermInAllTermList(term.getId());
					
					if(candidateTerm != null) {
						candidateTerm.setFrequency(candidateTerm.getFrequency() + term.getFrequency());
						candidateTerm.setFrequencyPerSequence(candidateTerm.getFrequencyPerSequence() + term.getFrequencyPerSequence());
						candidateTerm.setSequenceTotalNumber(candidateTerm.getSequenceTotalNumber() + entity.getFrequency());
					} else {
						allTermList.add(new Term(term.getId()));
						candidateTerm = allTermList.get(allTermList.size() - 1);
						candidateTerm.setFrequency(term.getFrequency());
						candidateTerm.setFrequencyPerSequence(term.getFrequencyPerSequence());
						candidateTerm.setSequenceTotalNumber(entity.getFrequency());
					}
				}
			}
			
			entity.setTermList(cleanTermList);
		}
	}
	
	protected void setSimilarityBetweenEntities(ArrayList<Entity> entityList) {
		
		for(int i = 0; i < entityList.size(); i++) {
			for(int j = 0; j < entityList.size(); j++) {
				if(i != j) {
					entityList.get(i).setSimilarEntity(entityList.get(j));
				}
			}
		}
	}
	
	protected void calculateAllTermListScore() {
		for(Term term : allTermList) {
			term.calculateScore();
		}
	}
	
	protected void sortAllTermListByAscendentFrequencyPerSequence() {
		
		Integer [] frequencyPerSequenceList = new Integer[allTermList.size()];
		int index = 0;
		
		for(Term term : allTermList)
			frequencyPerSequenceList[index++] = term.getFrequencyPerSequence();
		
		QuickSort.sort(frequencyPerSequenceList, allTermList);
		
	}
	
	/************************************************************************************************************************
	 * 
	 * 
	 * Phase II: Calculate Score per Sentence based in each Entity
	 *
	 * 
	 *************************************************************************************************************************/
	
	public void calculateScorePerSequence() {
		
		Entity entity;
		String [] sequenceWithoutStopWord;
		
		double traditionalScore;
		double clusterScore;
		
		Iterator <Entry<String, ArrayList<Cluster>>> clusterListIterator = clusterHandler.getClusterMap().entrySet().iterator();
		ArrayList<Cluster> clusterList;
		
		while(clusterListIterator.hasNext()) {
			
			clusterList = clusterListIterator.next().getValue();
			
			entity = clusterList.get(0).getEntity();
			entity.calculateProbabilityToBeEntity();
			
			for(Cluster cluster : clusterList) {
				
				//-- Calculate the term Score
				cluster.calculateTermScore();
				
				//-- Start score measures
				cluster.startScoreMeasures();
				
				for(Sequence sequence : cluster.getSequenceList()) {
					
					//-- calculate sequence score (in cluster)
					sequenceWithoutStopWord = sequence.getTermNameList();
					clusterScore = sequence.calculateScore(entity);
					cluster.addScore(clusterScore);
					
					//-- calculate traditional score
					traditionalScore = calculateTermsScore(entity, sequenceWithoutStopWord);
					entity.addSequenceScore(traditionalScore);
					
					/*if(clusterScore != traditionalScore)
						System.out.println("Cst:" +  clusterScore + " Tsc:" + traditionalScore);*/
					
					//-- add relative term position to entity
					addTermPosition(entity, sequenceWithoutStopWord);
				}
				
				//-- Calculate Average Cluster Score
				cluster.calculateAverageScore();
			}

			//-- Calculate the average and standard deviation score
			calculateAverageAndStandardDeviationScore(entity);
		
			//-- Calculate the Average Term Position
			calculateAverageTermPosition(entity);
		}
		
		//-- Calculate the scores of general clusters
		calculateGeneralClusterScorePerSequence();
		
		if(!isAjdustmentMode) {
			//-- Write statistical score log
			LogCreator.writeLogStatisticalScore(fileToLabel, entityList);
			
			//-- Write cluster score log
			LogCreator.writeLogClusterScore(fileToLabel, clusterHandler);
		}
	}
	
	public void calculateGeneralClusterScorePerSequence() {
		
		double clusterScore;
		
		for(Cluster cluster : clusterHandler.getGeneralClusterList()) {
			
			//-- Calculate the term Score
			cluster.calculateTermScore();
			
			//-- Start score measures
			cluster.startScoreMeasures();
			
			for(Sequence sequence : cluster.getSequenceList()) {
				
				//-- calculate sequence score (in cluster)
				clusterScore = sequence.calculateGeneralScore();
				cluster.addScore(clusterScore);
			}
			
			//-- Calculate Average Cluster Score
			cluster.calculateAverageScore();
		}
	}
	
	public void updateClusterScore(ArrayList<Entity> entityList) {
		
		ArrayList<Cluster> clusterList;
		double clusterScore;
		
		//String [] sequenceWithoutStopWord;
		
		for(Entity entity : entityList) {
		
			clusterList = clusterHandler.getClusterMap().get(entity.getId());
			
			for(Cluster cluster : clusterList) {
				
				//-- Calculate the term Score
				cluster.calculateTermScore();
				
				//-- Start score measures
				cluster.startScoreMeasures();
				
				for(Sequence sequence : cluster.getSequenceList()) {
					
					//-- calculate sequence score (in cluster)
					//sequenceWithoutStopWord = sequence.getTermNameList();
					clusterScore = sequence.calculateScore(entity);
					cluster.addScore(clusterScore);
					
					//-- add relative term position to entity
					//addTermPosition(entity, sequenceWithoutStopWord);
				}
				
				//-- Calculate Average Cluster Score
				cluster.calculateAverageScore();
			}
		}
		
		calculateGeneralClusterScorePerSequence();
	}
	
	public void updateEntityScorePerSequenceTraditional(DataSequence sequence, ArrayList<Entity> entityList) {
		
		String [] sequenceLowerCase;
		String [] sequenceWithoutStopWord;
		
		int entityIndex;
		
		Entity entity;
		double score;

		sequenceLowerCase = Utils.convertSequenceToLowerCase(sequence, sequence.length());
		sequenceWithoutStopWord = removeStopWordFromSequence(sequenceLowerCase);//Analysis if need to not consider entity term itself
		
		for(int i = 0; i < sequence.length(); i++) {
			
			entity = getEntity(sequenceLowerCase[i]);
			
			if(entity != null) {
				
				entityIndex = getEntityIndex(sequence, i);
				
				if(entityIndex != -1) {
						
					entity = getEntity(sequenceLowerCase[entityIndex]);
					score = calculateTermsScore(entity, sequenceWithoutStopWord);
					
					entity.addSequenceScore(score);
					
					//-- shift index to optimize search for entities
					i = entityIndex;
				} else { //-- add frequency when the term is ambiguous
					entity.addToFrequencyAsNonEntity();
				}
			}
		}
		
		//-- add relative term position to entity and update entity probability
		for(Entity entityToUpdate : entityList) {
			addTermPosition(entityToUpdate, sequenceWithoutStopWord);
			entityToUpdate.calculateProbabilityToBeEntity();
		}
		
		//-- Calculate the average and standard deviation score
		calculateAverageAndStandardDeviationScore(entityList);
		
		//-- Calculate the Average Term Position
		calculateAverageTermPosition(entityList);
		
		//-- Calculate cluster score per entity
		clusterHandler.updateClusterScorePerEntity(entityList);
	}
	
	protected void addTermPosition(Entity entity, String [] sequenceWithoutStopWord) {
		
		int lastEntityIndex = -1;
		int entityIndex = getNextEntityIndexInSequence(sequenceWithoutStopWord, entity.getId(), 0);
		int entityPos = -1;
		Term term;
		
		while(entityIndex != -1 && lastEntityIndex != entityIndex) {
			
			for(int i = 0; i < sequenceWithoutStopWord.length; i++) {
				if(sequenceWithoutStopWord[i].equals(entity.getId()) && entityIndex < i) {
					entityPos = i;
					break;
				}
			}
			
			for(int i = 0; entityPos != -1 && i < sequenceWithoutStopWord.length; i++) {
				if(i != entityPos) {
					
					term = entity.getTerm(sequenceWithoutStopWord[i]);
					
					if(i < entityPos)
						term.addLeftPosition(entityPos - i);
					else
						term.addRightPosition(i - entityPos);
				}
			}
			
			lastEntityIndex = entityIndex;
			entityIndex = getNextEntityIndexInSequence(sequenceWithoutStopWord, entity.getId(), entityPos+1);
		}
	}
	
	protected int getNextEntityIndexInSequence(String [] sequence, String entityName, int startPosition) {
		
		int entityIndex = -1;
		
		for(int i = startPosition; i < sequence.length; i++) {
			if(sequence[i].equals(entityName)) {
				entityIndex = i;
				break;
			}
		}
				
		return(entityIndex);
	}
	
	protected void calculateAverageTermPosition(Entity entity) {
		for(Term term : entity.getTermList()) {
			term.calculateAveragePosition();
		}
	}
	
	protected void calculateAverageTermPosition(ArrayList<Entity> entityList) {
		for(Entity entity : entityList)
			calculateAverageTermPosition(entity);
	}
	
	protected void calculateAverageAndStandardDeviationScore() {
		calculateAverageAndStandardDeviationScore(entityList);
	}
	
	protected void calculateAverageAndStandardDeviationScore(ArrayList<Entity> entityList) {
		for(Entity entity : entityList)
			calculateAverageAndStandardDeviationScore(entity);
	}
	
	protected void calculateAverageAndStandardDeviationScore(Entity entity) {
		
		double standardDeviation;
		double average;
			
		standardDeviation = 0;
		average = entity.getAverageScore()/entity.getScoreSequenceList().size();
		
		entity.setAverageScore(average);
		
		for(Double score : entity.getScoreSequenceList())
				standardDeviation += Math.pow(score - average, 2);
		
		standardDeviation /= entity.getScoreSequenceList().size();
		standardDeviation = Math.sqrt(standardDeviation);
		entity.setStardardDeviationScore(standardDeviation);
		
		//-- Additional Calculation - Probability to be Entity
		entity.calculateProbabilityToBeEntity();
	}
	
	protected Term containsTermInAllTermList(String termName) {
		
		Term term = null;
		
		for(Term candidateTerm : allTermList)
			if(Utils.isTermMatching(candidateTerm.getId(), termName, Term.MINIMUM_RELAX_MATCHING_LENGTH, Term.EDIT_DISTANCE_ACCEPTABLE)) {
				term = candidateTerm;
				break;
			}
				
		return(term);
	}
	
	/************************************************************************************************************************
	 * 
	 * 
	 * Phase III: Label File
	 *
	 * 
	 *************************************************************************************************************************/
	
	public void labelFile() {
		
		Writer out = null;
		Writer logOutScoreSequence = null;
		
		ArrayList<Double> sequenceScore = new ArrayList<Double>();
		
		//-- For feedback
		ArrayList<Entity> entityListToUpdateCluster = new ArrayList<Entity>();
		ArrayList<Entity> entityListInSequence;
		ArrayList<DataSequence> postPonedSequenceList = new ArrayList<DataSequence>();
		
		int sequenceNumber = 0;
		int currentNumberSequenceAdded = 0;
		
		boolean hasCandidateEntity = false;
		boolean isLastSequence = false;
		
		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(fileToLabel,
				FileType.TRAINING, false);
		
		DataSequence sequence;
		
		try {
		
			/** TEST TF-IDF Threshould **/
			//System.out.print(probabilityTFIDFFilter + "\t");
			//String fileToLabelAlt = fileToLabel.substring(0, fileToLabel.lastIndexOf(".")) + "-Thres(" + probabilityTFIDFFilter + ")" + fileToLabel.substring(fileToLabel.lastIndexOf("."));
			//System.out.print(probabilityFilterCxtOut + "\t");
			//String fileToLabelAlt = fileToLabel.substring(0, fileToLabel.lastIndexOf(".")) + "-Thres(" + probabilityFilterCxtOut + ")" + fileToLabel.substring(fileToLabel.lastIndexOf("."));
			/** TEST Context Filter **/
			//generateFilterContextCode();
			//System.out.print(filterCode + "\t");
			//String fileToLabelAlt = fileToLabel.substring(0, fileToLabel.lastIndexOf(".")) + "-FltCxt(" + filterCode + ")" + fileToLabel.substring(fileToLabel.lastIndexOf("."));
			/** TEST Filters **/
			generateFilterCode();
			System.out.print(filterCode + "\t");
			String fileToLabelAlt = fileToLabel.substring(0, fileToLabel.lastIndexOf(".")) + "-Flt(" + filterCode + ")" + fileToLabel.substring(fileToLabel.lastIndexOf("."));
			
			outputFilename = LogCreator.generateOutputFilename(fileToLabelAlt, ACRONYM_OUTPUT_FILENAME);
			out = new OutputStreamWriter(new FileOutputStream(outputFilename), ENCODE_USED);
				
			if(!isAjdustmentMode)
				logOutScoreSequence = new OutputStreamWriter(new FileOutputStream(
						LogCreator.generateOutputFilename(fileToLabelAlt, ACRONYM_SCORE_OUTPUT_SEQUENCE)), ENCODE_USED);
			
			while(inputSequenceSet.hasNext()) {
				
				//-- Label Sequence
				sequence = inputSequenceSet.next();
				hasCandidateEntity = labelSequence(sequence, sequenceScore);
				
				//-- Feedback <Prototype!>
				if(USE_FEEDBACK) {
					
					//-- Add term frequency
					entityListInSequence = addTermFrequencyForSequence(sequence);
					
					//-- update entity and postPoned list
					currentNumberSequenceAdded = updateLists(sequence, entityListInSequence, entityListToUpdateCluster,
							postPonedSequenceList, hasCandidateEntity, currentNumberSequenceAdded);
					
					//-- verify if it is not the last sequence
					isLastSequence = !inputSequenceSet.hasNext();
					
					//-- execute feedback to the model
					executeFeedback(sequence, postPonedSequenceList, entityListToUpdateCluster, sequenceNumber++, 
							currentNumberSequenceAdded, isLastSequence);
				} else {
					//-- Write sequence to output filename
					writeSequence(out, sequence);
				}
				
				if(!isAjdustmentMode) {
					//-- Write output log for score
					LogCreator.writeSequenceScore(logOutScoreSequence, sequence, sequenceScore);
				}
			}
			
			out.flush();
			out.close();
				
			if(!isAjdustmentMode) {
			
				logOutScoreSequence.flush();
				logOutScoreSequence.close();
			
				LogCreator.writeParams(fileToLabelAlt, USE_SMOOTH_PARAM, termMinimumSize, 
						minimumTermFrequencyPerSequence, standardDeviationParcel);
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected boolean labelSequence(DataSequence sequence, ArrayList<Double> sequenceScore) {
		
		double score;
		boolean hasCandidateEntity = false;
		
		String [] sequenceLowerCase = Utils.convertSequenceToLowerCase(sequence, sequence.length());
		String [] sequenceWithoutStopWord = removeStopWordFromSequence(sequenceLowerCase);
		
		sequenceScore = new ArrayList<Double>();
		
		for(int i = 0; i < sequence.length(); i++) {
			score = setLabel(sequence, sequenceLowerCase, i, sequenceWithoutStopWord);
			
			if(sequenceScore != null)
				sequenceScore.add(score);
			
			if(!hasCandidateEntity && getEntity((String)sequence.x(i)) != null) 
				hasCandidateEntity = true;
		}
		
		return(hasCandidateEntity);
	}
	
	protected int updateLists(DataSequence sequence,
			ArrayList<Entity> entityListInSequence,
			ArrayList<Entity> entityListToUpdateCluster, 
			ArrayList<DataSequence> postPonedSequenceList,
			boolean hasCandidateEntity,
			int currentNumberSequenceAdded) {
		
		if(entityListInSequence.size() > 0) {
			for(Entity entity : entityListInSequence) {
				if(!entityListToUpdateCluster.contains(entity))
					entityListToUpdateCluster.add(entity);
			}
			
			currentNumberSequenceAdded++;
			
			if(supportContextWS1 != null)
				supportContextWS1.extractContextFromSequence(sequence);
			if(supportContextWS2 != null)
				supportContextWS2.extractContextFromSequence(sequence);
			if(supportContextWS3 != null)
				supportContextWS3.extractContextFromSequence(sequence);
			
		} else if(hasCandidateEntity){
			postPonedSequenceList.add(sequence);
		}
		
		return(currentNumberSequenceAdded);
	}
	
	protected void executeFeedback(DataSequence sequence, ArrayList<DataSequence> postPonedSequenceList,
			ArrayList<Entity> entityList, int sequenceNumber, int currentNumberSequenceAdded, boolean isLastSequence) {
		
		boolean checkResults = isLastSequence;
		
		if(sequenceNumber % DECAY_RATIO == 1 || isLastSequence) {
			if(sequenceNumber % (DECAY_RATIO * 5) == 1)
				checkResults |= true;
			
			updateModelStatistics(postPonedSequenceList, entityList, sequenceNumber, 
					currentNumberSequenceAdded, isLastSequence, checkResults);
		}
	}
	
	protected void updateModelStatistics(ArrayList<DataSequence> postPonedSequenceList, 
			ArrayList<Entity> entityList, int sequenceNumber, int sequenceAdded, 
			boolean isLastSequence, boolean checkResults) {
		
		if(entityList.size() > 0) {
			
			calculateTermScorePerEntity(entityList);
			
			updateClusterScore(entityList);
			
			entityList = new ArrayList<Entity>();
		}
			
		/* Check Partial Update */
		System.out.print("\nUpdate " + sequenceNumber + " added(" + sequenceAdded + ")Seqs");
		
		if(checkResults)
			checkPartialResults();
		
		//-- Update PostPonedSequence
		if(isLastSequence) {
			/*if(supportContextWS1 != null)
				supportContextWS1.updateContext();
			if(supportContextWS2 != null)
				supportContextWS2.updateContext();
			if(supportContextWS3 != null)
				supportContextWS3.updateContext();
			checkPartialResults();*/
			updatePostPonedSequence(postPonedSequenceList);
		}
	}
	
	protected void updatePostPonedSequence(ArrayList<DataSequence> postPonedSequenceList) {
		
		int postPonedSequenceAdded = -1;
		ArrayList<Entity> entityList;
		
		System.out.println("\nAdding Postponed sequences\n");
		
		while(postPonedSequenceAdded != 0) {
			
			entityList = new ArrayList<Entity>();
			
			postPonedSequenceAdded = checkPostponedSequenceList(postPonedSequenceList, entityList);
			
			calculateTermScorePerEntity(entityList);
			updateClusterScore(entityList);
			
			System.out.println("Update added(" + postPonedSequenceAdded + ")PostSeqs - Remain(" + postPonedSequenceList.size() + ")");
		}
		
		checkPartialResults();
	}
	
	protected void checkPartialResults() {
			
		String [] sequenceLowerCase;
		String [] sequenceWithoutStopWord;
		
		SequenceSet inputSequenceSet =  HandlingSequenceSet.transformFileInSequenceSet(fileToLabel,
				FileType.TRAINING, false);
		
		DataSequence sequence;
		
		Writer out = null;
		
		try {
			
			outputFilename = LogCreator.generateOutputFilename(fileToLabel, ACRONYM_OUTPUT_FILENAME + "-PartialResult-");
			out = new OutputStreamWriter(new FileOutputStream(outputFilename), ENCODE_USED);
			
			while(inputSequenceSet.hasNext()) {
				
				sequence = inputSequenceSet.next();
				sequenceLowerCase = Utils.convertSequenceToLowerCase(sequence, sequence.length());
				sequenceWithoutStopWord = removeStopWordFromSequence(sequenceLowerCase);				
				
				for(int i = 0; i < sequence.length(); i++)
					setLabel(sequence, sequenceLowerCase, i, sequenceWithoutStopWord);
				
				//-- Write sequence to output filename
				writeSequence(out, sequence);					
			}
			
			out.flush();
			out.close();
			
			CRFExecutor crf = new CRFExecutor("./samples/bcs2010.conf");
			crf.getStatistics("", outputFilename, fileToLabel);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected int checkPostponedSequenceList(ArrayList<DataSequence> postPonedSequenceList, ArrayList<Entity> entityList) {
		
		ArrayList<DataSequence> removeSequenceList = new ArrayList<DataSequence>();
		ArrayList<Entity> entityListInSequence = null;
		
		int postPonedSequenceAdded = 0;
		
		for(DataSequence postPonedSequence : postPonedSequenceList) {
			
			labelSequence(postPonedSequence, null);
			entityListInSequence = addTermFrequencyForSequence(postPonedSequence);
			
			if(entityListInSequence.size() > 0) {
				
				//-- Add sequence used to removeSequenceList
				removeSequenceList.add(postPonedSequence);
				postPonedSequenceAdded++;
				
				//-- Add entity to recalculate the cluster related
				entityList.addAll(entityListInSequence);
			}
		}
		
		//-- Remove sequenced added to clusters
		for(DataSequence postPonedSequence : removeSequenceList)
			postPonedSequenceList.remove(postPonedSequence);
		
		return(postPonedSequenceAdded);
	}
	
	protected double setLabel(DataSequence sequence,
			String [] sequenceLowerCase, int index, String [] entityTerms) {
		
		double sequenceScore = getSequenceClusterScore(sequenceLowerCase, index, entityTerms);
		//double sequenceScore = getSequenceMaxScore(sequenceLowerCase, index, entityTerms);
		
		int standardEntityLabel = 4;
		int standardOutsideLabel = 3;		
		
		if(sequenceScore > 0)
			sequence.set_y(index, standardEntityLabel);
		else
			sequence.set_y(index, standardOutsideLabel);
		
		/*if(index == sequence.length() - 1)
			entityLinker.executeLinker(sequence, sequenceLowerCase);*/
		
		return(sequenceScore);
	}
	
	public int getCandidateLabel(String [] sequenceLowerCase, int index, String [] entityTerms) {
		
		int candidateLabel;
		
		double sequenceScore = getSequenceClusterScore(sequenceLowerCase, index, entityTerms);
		//double sequenceScore = getSequenceMaxScore(sequenceLowerCase, index, entityTerms);
		
		int standardEntityLabel = 4;
		int standardOutsideLabel = 3;		
		
		if(sequenceScore > 0) {
			candidateLabel = standardEntityLabel;
		}else {
			candidateLabel = standardOutsideLabel;
		}
		
		return(candidateLabel);
	}
	
	protected double getSequenceMaxScore(String [] sequenceLowerCase, int index, String [] entityTerms) {
		
		final int MINIMUM_ENTITY_LENGTH = 2;
		double maximumSequenceScore = -1;
		double threshould = 0;
		double result;
		
		String entityName = sequenceLowerCase[index];
		Entity entity = getEntity(entityName);
		
		if(entity != null) {
			
			threshould = entity.getMinScore();// - (standardDeviationParcel * entity.getStardardDeviationScore());
			maximumSequenceScore = calculateTermsScore(entity, entityTerms);
			
			//if(maximumSequenceScore < threshould && entityName.length() > MINIMUM_ENTITY_LENGTH) {
			if(entityName.length() > MINIMUM_ENTITY_LENGTH) {
				/*maximumSequenceScore = filterContext(sequenceLowerCase, index, entityName, entity,
						entityTerms, maximumSequenceScore, threshould);*/
			} else if(entity.getProbabilityToBeEntity() < probabilityTFIDFFilter) {
				maximumSequenceScore = -1;
			}
			
			/*if(maximumSequenceScore - threshould >= 0) {
				System.out.println(entityName + " Sc: " + (maximumSequenceScore - threshould));
			} else
				maximumSequenceScore = -1;*/
		}	
		
		return((threshould >= 0)? maximumSequenceScore - threshould : -1);
	}
	
	protected double filterContext(String [] sequenceLowerCase, int index, String entityName,
			Entity entity, String [] entityTerms, double maximumSequenceScore, double threshould) {
			
			ContextToken context = getEquivalentContextInSequence(sequenceLowerCase, index, entityName,
					entity, entityTerms, maximumSequenceScore);
			
			if(context != null)// || entity.getProbabilityToBeEntity() > probabilityFilterCxtOut)
				maximumSequenceScore = threshould;
			else
				maximumSequenceScore = -1;
		
		return(maximumSequenceScore);
	}
	
	protected ContextToken getEquivalentContextInSequence(String [] sequenceLowerCase, int index, String entityName,
			Entity entity, String [] entityTerms, double maximumSequenceScore) {
		
		ContextToken context = null;
		filterCode = "";
		
		if(supportContextWS3 != null) {
			
			if(enableFilterCxt[0] == 1 && context == null) {
				context = supportContextWS3.existContextInSequenceContextHashMap(sequenceLowerCase, index);
				
				if(context != null) {
					windowSize = 3;
					contextType = ContextType.PrefixSuffix;
				}
			}
				
			
			if(enableFilterCxt[1] == 1 && context == null) {
				context = supportContextWS3.existPrefixInSequenceRestrictedPrefixContextHashMap(sequenceLowerCase, index);
				
				if(context != null) {
					windowSize = 3;
					contextType = ContextType.Prefix;
				}
			}
			
			if(enableFilterCxt[2] == 1 && context == null) {
				context = supportContextWS3.existSuffixInSequenceRestrictedSuffixContextHashMap(sequenceLowerCase, index);
				
				if(context != null) {
					windowSize = 3;
					contextType = ContextType.Suffix;
				}
			}
			
		}
		
		if(supportContextWS2 != null) {
			
			if(enableFilterCxt[3] == 1 && context == null) {
				context = supportContextWS2.existContextInSequenceContextHashMap(sequenceLowerCase, index);
				
				if(context != null) {
					windowSize = 2;
					contextType = ContextType.PrefixSuffix;
				}
			}
			
			if(enableFilterCxt[4] == 1 && context == null) {
				context = supportContextWS2.existPrefixInSequenceRestrictedPrefixContextHashMap(sequenceLowerCase, index);
				
				if(context != null) {
					windowSize = 2;
					contextType = ContextType.Prefix;
				}
			}
			
			if(enableFilterCxt[5] == 1 && context == null) {
				context = supportContextWS2.existSuffixInSequenceRestrictedSuffixContextHashMap(sequenceLowerCase, index);
				
				if(context != null) {
					windowSize = 2;
					contextType = ContextType.Suffix;
				}
			}
			
		}
		
		//if(context == null && supportContextWS1 != null) {
		if(supportContextWS1 != null) {
			
			if(enableFilterCxt[6] == 1 && context == null) {
				context = supportContextWS1.existContextInSequenceContextHashMap(sequenceLowerCase, index);
				
				if(context != null) {
					windowSize = 1;
					contextType = ContextType.PrefixSuffix;
				}
				
			}
			
			if(enableFilterCxt[7] == 1 && context == null) {
				context = supportContextWS1.existPrefixInSequenceRestrictedPrefixContextHashMap(sequenceLowerCase, index);
				
				if(context != null) {
					windowSize = 1;
					contextType = ContextType.Prefix;
				}
			}
			
			if(enableFilterCxt[8] == 1 && context == null) {
				context = supportContextWS1.existSuffixInSequenceFullRestrictedSuffixContextHashMap(sequenceLowerCase, index);
				
				if(context != null) {
					windowSize = 1;
					contextType = ContextType.Suffix;
				}
			}
			
		}
		
		/* ORIGINAL: if(context != null && entity.getProbabilityToBeEntity() < probabilityFilterCxt)
			context = null;*/
		
		return(context);
	}
	
	protected int filterByContext(String [] sequenceLowerCase, int index) {
		
		int result = -1;
		String encodedContext = contextManager.getEncodedContext(sequenceLowerCase, index);
		
		for(int windowSize = 3; windowSize > 0; windowSize--) {
			for(int contextTypeIndex = 0; contextTypeIndex < ContextManager.ContextType.values().length; contextTypeIndex++) {
				if(contextManager.existContext(encodedContext, null, ContextManager.ContextType.values()[contextTypeIndex], windowSize)) {
					result = 1;
					break;
				}
			}
			
			if(result > 0)
				break;
		}
		
		return(result);
	}
	
	protected void generateFilterContextCode() {
		
		filterCode = "";
		
		if(supportContextWS3 != null) {
			
			if(enableFilterCxt[0] == 1)
				filterCode += "C3";
			
			if(enableFilterCxt[1] == 1)
				filterCode += ((filterCode.length() > 0)?"+":"")+"P3";
			
			if(enableFilterCxt[2] == 1)
				filterCode += ((filterCode.length() > 0)?"+":"")+"S3";
		}
		
		if(supportContextWS2 != null) {
			
			if(enableFilterCxt[3] == 1)
				filterCode += ((filterCode.length() > 0)?"+":"")+"C2";
			
			if(enableFilterCxt[4] == 1)
				filterCode += ((filterCode.length() > 0)?"+":"")+"P2";
			
			if(enableFilterCxt[5] == 1)
				filterCode += ((filterCode.length() > 0)?"+":"")+"S2";
		}
	
		if(supportContextWS1 != null) {
			
			if(enableFilterCxt[6] == 1)
				filterCode += ((filterCode.length() > 0)?"+":"")+"C1";
			
			if(enableFilterCxt[7] == 1)
				filterCode += ((filterCode.length() > 0)?"+":"")+"P1";
			
			if(enableFilterCxt[8] == 1)
				filterCode += ((filterCode.length() > 0)?"+":"")+"S1";
		}
	}
	
	protected void generateFilterCode() {
		
		filterCode = "";
		int order = 1;
		
		while(order < 4) {
			if(filterOrder[0] == order && enableFilters[0] == 1)
				filterCode += ((filterCode.length() > 0)?"+":"")+"TF-IDF";
			if(filterOrder[1] == order && enableFilters[1] == 1)
				filterCode += ((filterCode.length() > 0)?"+":"")+"Cxt";
			if(filterOrder[2] == order && enableFilters[2] == 1)
				filterCode += ((filterCode.length() > 0)?"+":"")+"Prob";
			order++;
		}
	}
	
	protected double getSequenceClusterScore(String [] sequenceLowerCase, int index, String [] candidateTermList) {
		
		double result = -1;
		
		String entityName = sequenceLowerCase[index];
		Entity entity = getEntity(entityName);
		
		//If don't have the entity, then get synonym
		if(entity == null) entity = getEntity(getEntitySynonymName(entityName));
		
		Iterator<Entry<Cluster, Boolean>> ite = null;
		ArrayList<Cluster> clusterList = clusterHandler.getClusterMap().get(entityName);
		ArrayList<Cluster> clusterCandidateList;
		Cluster candidateCluster = null;
		
		//-- Enable Random Filter
		//enableFilters[0] = (new Random(new Random().nextLong())).nextInt() % 2;
		//enableFilters[1] = (new Random(new Random().nextLong())).nextInt() % 2;
		//enableFilters[2] = (new Random(new Random().nextLong())).nextInt() % 2;
		
		//-- Enable Random Order
		/*boolean orderSet [] = {false, false, false};
		for(int i = 0; i < 3; i++) {
			do {
				filterOrder[i] = 1 + ((new Random()).nextInt(3));
			}while(orderSet[filterOrder[i]-1]);
			orderSet[filterOrder[i]-1] = true; 
		}*/
		
		//-- TEST
		/*if(entity != null)
			result = 1;*/
		
		int order = 1;
		while(order < 4) {
		
			if(filterOrder[0] == order && enableFilters[0] == 1 && result <= 0 && entity != null) {//-- TF-IDF Filter 
				result = getClusterScore(clusterList, entity, candidateTermList);
				
				/** Try entity synonym **/
				if(entity != null && result <= 0) {
					result = calculateSynonymEntityScore(entity.getId(), candidateTermList);
					if(result > 0)
						break;
				} else if(result > 0)
					break;
			}
			
			/** *NEW* Context Filter **/
			if(filterOrder[1] == order && enableFilters[1] == 1) {
				if(result <= 0){// && entity != null) {
					result = filterByContext(sequenceLowerCase, index);
					if(result > 0)
					counter++;
				}
			}
			
			/** Context Filter **/
			/*if(filterOrder[1] == order && enableFilters[1] == 1) { //-- Context Filter
				if(result <= 0 && entity != null) {
					result = filterContext(sequenceLowerCase, index, entityName, entity,
							candidateTermList, 0, 1);
							
					if(result > 0 && contextManager.trimEncodedContext(contextManager.getEncodedContext(sequenceLowerCase, index),contextType, windowSize).length() > 0) {
						counter++;
						System.out.println("\"" + contextManager.trimEncodedContext(contextManager.getEncodedContext(sequenceLowerCase, index),contextType, windowSize) + "\" (" + contextType.name() + " " + windowSize + ")");
						break;
					}
				}
			}*/
			
			if(filterOrder[2] == order && enableFilters[2] == 1) { //-- Probabilistic Filter
				if(result <= 0 && entity != null && entity.getProbabilityToBeEntity() >= probabilityFilterCxtOut) {
					result = 1;
					break;
				}
			}
			
			order++;
		}
		
		/** Try to look in other entity clusters **/
		/*if(entity != null && result < 0) {
			
			ite = entity.getSimilarClusterMap().entrySet().iterator();
			
			while(ite.hasNext() && result <= 0) {
				
				candidateCluster = ite.next().getKey();
				
				double clusterSimilarity = candidateCluster.calculateSequenceSimilarity(candidateTermList);
				
				if(clusterSimilarity >= 0.8 && clusterSimilarity <= 1) {
					clusterCandidateList = new ArrayList<Cluster>();
					clusterCandidateList.add(candidateCluster);
					
					result = getClusterScore(clusterCandidateList, entity, candidateTermList);
				}
			}
		}*/
		
		return(result);
	}
	
	protected double getClusterScore(ArrayList<Cluster> clusterList, Entity entity, String [] candidateTermList) {
		
		double maximumSequenceScore = -1;//-1
		double threshould = 0;//0
		
		if(clusterList != null) {
			for(Cluster cluster : clusterList) {
				threshould = cluster.getAverageScore() * probabilityTFIDFFilter;
				maximumSequenceScore = calculateClusterScore(cluster, entity, candidateTermList);
				
				if(maximumSequenceScore > threshould) {
					/*System.out.println("ClusterID:" + cluster.getId() + 
							" sim:" + cluster.calculateSequenceGeneralSimilarity(candidateTermList) + " " +
							entity.getId() + " P:" + entity.getProbabilityToBeEntity() + " Sc:" 
							+ maximumSequenceScore + " Thres:" + threshould + 
							" candTermSize:" + candidateTermList.length);*/
					
					break;
				}
			}
		}
		
		/*if(clusterList != null && maximumSequenceScore <= threshould && entity.getProbabilityToBeEntity() >= 0.9) {
			System.out.println(entity.getId() + 
					" P:" + entity.getProbabilityToBeEntity() + 
					" Sc:" + maximumSequenceScore);
		threshould = 0;
			maximumSequenceScore = 1;
		}*/
		
		/*int numberClusterAccepted = 0;
		double sim;
		if(clusterList != null && maximumSequenceScore <= threshould & entity.getProbabilityToBeEntity() > 0.7) {
			for(Cluster cluster : clusterHandler.getGeneralClusterList()) {
				
				sim = cluster.calculateSequenceGeneralSimilarity(candidateTermList);
				
				threshould = cluster.getMinimumScore();
				maximumSequenceScore = calculateClusterScore(cluster, entity, candidateTermList);
				
				if(maximumSequenceScore > threshould && sim >= 1) {
					/*System.out.println("ClusterID: " + cluster.getId() + " sim:" + cluster.calculateSequenceGeneralSimilarity(candidateTermList) + " " +
							entity.getId() + " P:" + entity.getProbabilityToBeEntity() + " Sc: " 
							+ maximumSequenceScore + " Thres: " + threshould);*/
					/*numberClusterAccepted++;
					break;
				}
			}
		}*/
		
		return(maximumSequenceScore - threshould);
	}
	
	protected double calculateSynonymEntityScore(String entityName, String [] candidateTermList) {
		
		Entity entity;
		ArrayList<Cluster> clusterList;
		ArrayList<String> synonymList = entitySynonymMap.get(entityName);
		
		double result = -1;
		
		if(synonymList != null) {
			for(String synonymEntityName : entitySynonymMap.get(entityName)) {
				
				entity = getEntity(synonymEntityName);
				clusterList = clusterHandler.getClusterMap().get(synonymEntityName);
				
				result = getClusterScore(clusterList, entity, candidateTermList);
				
				if(result >= 0)
					break;
			}
		}
		
		return(result);
	}
	
	protected String getEntitySynonymName(String entityName) {
		
		Entity entity = null;
		String synonymName = "";
		ArrayList<String> synonymList = entitySynonymMap.get(entityName);
		
		if(synonymList != null) {
			for(String synonymEntityName : synonymList) {
				
				entity = getEntity(synonymEntityName);
				
				if(entity != null)
					break;					
			}
			
			synonymName = (entity != null)? entity.getId() : "";
		}
		
		return(synonymName);
	}
	
	protected double calculateTermsScore(Entity entity, String [] candidateTermList) {
		
		int termNumber = 0;
		double termsScore = 0;
		
		/** Traditional Score **/
		termsScore = calculateStandardScore(entity, candidateTermList);
		
		/** Electric Field Score **/
		//termsScore = calculateElectricFieldScore(entity, candidateTermList);
	
		//System.out.println(termsScore/termNumber);
		
		return(((!USE_SMOOTH_PARAM)? termsScore : ((termNumber > 0)?termsScore/termNumber:0)));
		//return((termNumber > 0)?termsScore/termNumber:0);
		//return(termsScore/termNumber);
		//return(((double)termNumber)/candidadeTermList.length);
	}
	
	protected double calculateStandardScore(Entity entity, String [] candidateTermList) {
		
		double termsScore = 0;
		int termNumber = 0;
		
		Term term;
		
		for(int i = 0; i < candidateTermList.length; i++){
			term = entity.getTerm(candidateTermList[i]);
			
			if(term != null && !term.getId().equals(entity.getId())) {
				termsScore += term.getScore();
				termNumber++;
			}
		}
		
		return(((!USE_SMOOTH_PARAM)? termsScore : ((termNumber > 0)?termsScore/termNumber : 0)));
	}
	
	protected double calculateClusterScore(Cluster cluster, Entity entity, String [] candidateTermList) {
		
		double termsScore = 0;
		Term term;
		
		for(int i = 0; i < candidateTermList.length; i++) {
			
			term = cluster.getTerm(candidateTermList[i]);
			
			if(term != null  && !term.getId().equals(entity.getId()))
				termsScore += term.getScore();
		}
			
		return(termsScore);
	}
	
	protected double calculateElectricFieldScore(Entity entity, String [] candidateTermList) {
		
		Term term;
		
		double termsScore = 0;
		
		int entityPos = -1;
		int windowSize = 7;
		int startPosition;
		int endPosition;
		
		for(int i = 0; i < candidateTermList.length; i++) {
			if(entity.getId().equals(candidateTermList[i])) {
				entityPos = i;
				break;
			}
		}
		
		if(entityPos != -1) {
			
			int j = 1;
			startPosition = ((entityPos - windowSize > 0)? (entityPos - windowSize) : 0);
			endPosition = entityPos;
			
			for(int i = startPosition; i < endPosition; i++) {	
				
				term = entity.getTerm(candidateTermList[i]);
				
				if(term != null){// && term.getAverageLeftPosition()-i != 0) {
					//termsScore += term.getScore();
					//termsScore += term.getScore()* term.getLeftPositionScore(entityPos-i);
					//termsScore += Math.pow(term.getScore(), j++);
					//termsScore += term.getScore()/Math.pow(term.getAverageLeftPosition()-i, 2);
					//termsScore += term.getScore()/(term.getAverageLeftPosition()-i);
					//System.out.println("L: " +(term.getAverageLeftPosition()-i));
					termsScore +=  term.getScore()/Math.pow(endPosition-i, 2);
					//termsScore += term.getScore()/Math.pow(term.getAverageLeftPosition()-i, 2);
					//termsScore += term.getScore()/Math.abs(term.getAverageLeftPosition()-i) * ((entityPos-i >= 0)?1:-1);
				}
			}
			
			j = windowSize;
			startPosition = entityPos + 1;
			endPosition = startPosition + windowSize;
			
			for(int i = startPosition; i < candidateTermList.length && i < endPosition; i++) {	
				
				term = entity.getTerm(candidateTermList[i]);
				
				if(term != null){// && i-term.getAverageRightPosition() != 0) {
					//termsScore += term.getScore();
					//termsScore += term.getScore() * term.getRightPositionScore(i-entityPos);
					//termsScore += Math.pow(term.getScore(), j--);
					//termsScore += term.getScore()/Math.pow(i-term.getAverageRightPosition(), 2);
					//termsScore += term.getScore()/(i-term.getAverageRightPosition());
					//System.out.println("R: " + (i-term.getAverageRightPosition()));
					termsScore += term.getScore()/Math.pow(i-startPosition+1, 2);
					//termsScore += term.getScore()/Math.pow(i-term.getAverageRightPosition(), 2);
					//termsScore += term.getScore()/Math.abs(i-term.getAverageRightPosition()) * ((i-entityPos >= 0)?1:-1);
				}
			}
		} else {
			termsScore = calculateStandardScore(entity, candidateTermList);
		}
		
		return(termsScore);
	}
	
	protected int quantityTermsFound(Entity entity, String [] candidateTermList) {
		
		int quantityTermsFound = 0;
		Term term;
		
		for(int i = 0; i < candidateTermList.length; i++){
			term = entity.getTerm(candidateTermList[i]);
			
			if(term != null)
				quantityTermsFound++;
		}
		
		return(quantityTermsFound);
	}
	
	protected void writeSequence(Writer out, DataSequence sequence) throws IOException {
		for(int i = 0; i < sequence.length(); i++)
			out.write(sequence.x(i) + DELIMITER_LABEL + LabelMap.getLabelNameBILOU(sequence.y(i)) + "\n");
		
		out.write("\n");
	}
	
	public void readArtificialIntelligenceInterpreterObject(String filename, ArtificialIntelligenceInterpreter target) throws IOException, ClassNotFoundException {
    	
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		ArtificialIntelligenceInterpreter aII = (ArtificialIntelligenceInterpreter) in.readObject();
		cloneArtificialIntelligenceInterpreter(target, aII);
		
		in.close();
    }

	private void cloneArtificialIntelligenceInterpreter(ArtificialIntelligenceInterpreter target, ArtificialIntelligenceInterpreter clone) {
		
		target.allTermList = clone.allTermList;
		target.contextSourceFile = clone.contextSourceFile;
		target.entityList = clone.entityList;
		target.entityTermSizeList = clone.entityTermSizeList;
		target.fileToLabel = clone.fileToLabel;
		target.minimumTermFrequencyPerSequence = clone.minimumTermFrequencyPerSequence;
		target.outputFilename = clone.outputFilename;
		target.standardDeviationParcel = clone.standardDeviationParcel;
		target.stopWord = clone.stopWord;
		target.stopWordFile = clone.stopWordFile;
		target.supportContextWS1 = clone.supportContextWS1;
		target.supportContextWS2 = clone.supportContextWS2;
		target.supportContextWS3 = clone.supportContextWS3;
		target.termMinimumSize = clone.termMinimumSize;
	}
    
    public void writeArtificialIntelligenceInterpreterObject(String filename) throws IOException {
    	
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

		out.writeObject(this);
		out.flush();
		out.close();
    }
    
    protected void printFileToLabelStatus(String printHeader, String [] sequenceLowerCase, 
    		Entity entity, String entityName, String [] entityTerms, double maximumSequenceScore) {
    	
    	System.out.print(printHeader + " (" + entityName + ")" + " [min:" + Utils.formatDecimalNumber(entity.getMinScore()));
		System.out.print(", max:" + Utils.formatDecimalNumber(maximumSequenceScore) + " " + ", len:" + quantityTermsFound(entity, entityTerms));
		System.out.print(", pEnt:" + Utils.formatDecimalNumber(100 * entity.getProbabilityToBeEntity()) + "%] {");
		
		for(int i = 0; i < sequenceLowerCase.length; i++)
			System.out.print(sequenceLowerCase[i] + " ");
		System.out.println("}");
    }
    
    /**
     * Gets and Sets
     **/
	public String getOutputFilename() {
		return(outputFilename);
	}

	public int getTermMinimumSize() {
		return termMinimumSize;
	}

	public void setTermMinimumSize(int termMinimumSize) {
		this.termMinimumSize = termMinimumSize;
	}

	public double getStandardDeviationParcel() {
		return standardDeviationParcel;
	}

	public void setStandardDeviationParcel(double standardDeviationParcel) {
		this.standardDeviationParcel = standardDeviationParcel;
	}

	public int getMinimumTermFrequencyPerSequence() {
		return minimumTermFrequencyPerSequence;
	}

	public void setMinimumTermFrequencyPerSequence(int minimumTermFrequencyPerSequence) {
		this.minimumTermFrequencyPerSequence = minimumTermFrequencyPerSequence;
	}
	
	public void setEntityEditDistance(int editDistance) {
		Entity.EDIT_DISTANCE_ACCEPTABLE = editDistance;
	}
	
	public void setTermEditDistance(int editDistance) {
		Term.EDIT_DISTANCE_ACCEPTABLE = editDistance;
	}

	public double getProbabilityFilterCxt() {
		return probabilityFilterCxt;
	}

	public void setProbabilityFilterCxt(double probabilityFilterCxt) {
		this.probabilityFilterCxt = probabilityFilterCxt;
	}

	public double getProbabilityFilterOut() {
		return probabilityFilterOut;
	}

	public void setProbabilityFilterOut(double probabilityFilterOut) {
		this.probabilityFilterOut = probabilityFilterOut;
	}

	public double getProbabilityFilterCxtOut() {
		return probabilityFilterCxtOut;
	}

	public void setProbabilityFilterCxtOut(double probabilityFilterCxtOut) {
		this.probabilityFilterCxtOut = probabilityFilterCxtOut;
	}

	public String getStopWordFile() {
		return stopWordFile;
	}

	public void setStopWordFile(String stopWordFile) {
		this.stopWordFile = stopWordFile;
	}

	public String getContextSourceFile() {
		return contextSourceFile;
	}

	public void setContextSourceFile(String contextSourceFile) {
		this.contextSourceFile = contextSourceFile;
	}

	public String getFileToLabel() {
		return fileToLabel;
	}

	public void setFileToLabel(String fileToLabel) {
		this.fileToLabel = fileToLabel;
	}

	public RemoveStopWordsTool getStopWord() {
		return stopWord;
	}

	public void setStopWord(RemoveStopWordsTool stopWord) {
		this.stopWord = stopWord;
	}

	public SupportContext getSupportContextWS3() {
		return supportContextWS3;
	}

	public void setSupportContextWS3(SupportContext supportContextWS3) {
		this.supportContextWS3 = supportContextWS3;
	}

	public SupportContext getSupportContextWS2() {
		return supportContextWS2;
	}

	public void setSupportContextWS2(SupportContext supportContextWS2) {
		this.supportContextWS2 = supportContextWS2;
	}

	public SupportContext getSupportContextWS1() {
		return supportContextWS1;
	}

	public void setSupportContextWS1(SupportContext supportContextWS1) {
		this.supportContextWS1 = supportContextWS1;
	}

	public static boolean isAjdustmentMode() {
		return isAjdustmentMode;
	}

	public static void setAjdustmentMode(boolean isAjdustmentMode) {
		ArtificialIntelligenceInterpreter.isAjdustmentMode = isAjdustmentMode;
	}

	public static boolean isProbabilisticFilter() {
		return isProbabilisticFilter;
	}

	public static void setProbabilisticFilter(boolean isProbabilisticFilter) {
		ArtificialIntelligenceInterpreter.isProbabilisticFilter = isProbabilisticFilter;
	}

}
