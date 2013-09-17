package iitb.Model;
import gnu.trove.TIntHashSet;
import iitb.CRF.DataIter;
import iitb.CRF.DataSequence;
import iitb.CRF.Feature;
import iitb.CRF.FeatureGeneratorNested;
import iitb.CRF.SegmentDataSequence;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import lbd.AutoTagger.SelectByContext;
import lbd.AutoTagger.SelectByPOSTag;
import lbd.CRF.LabelMap;
import lbd.FSNER.ArtificialIntelligenceInterpreter;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Utils.CommonEnum.Flexibility;
import lbd.Model.Dictionary;
import lbd.Model.SupportContext;
import lbd.Model.TwitterSymbol;
import lbd.Model.VerbalSuffixHashMap;
import lbd.NewModels.Affix.AffixManager;
import lbd.NewModels.BrownHierarquicalCluster.BrownCluster;
import lbd.NewModels.BrownHierarquicalCluster.BrownClusterEtzioni;
import lbd.NewModels.BrownHierarquicalCluster.JCluster;
import lbd.NewModels.Context.ContextAnalysis;
import lbd.NewModels.Context.ContextAnalysisRelaxed;
import lbd.NewModels.FeatureFilters.Affix;
import lbd.NewModels.FeatureFilters.AffixComponent;
import lbd.NewModels.FeatureFilters.CapitalizedTerms;
import lbd.NewModels.FeatureFilters.Context;
import lbd.NewModels.FeatureFilters.ContextComponent;
import lbd.NewModels.FeatureFilters.EntityProbability;
import lbd.NewModels.FeatureFilters.EntityProbabilityComponent;
import lbd.NewModels.FeatureFilters.SingleTermDictionary3;
import lbd.NewModels.FeatureFilters.SingleTermDictionary3Component;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;
import lbd.NewModels.Token.NGram;
import lbd.SummirizedPattern.SummarizedPattern;

/**
 * The FeatureGenerator is an aggregator over all these different
 * feature types. You can inherit from the FeatureGenImpl class and
 * after calling one of the constructors that does not make a call to
 * (addFeatures()) you can then implement your own addFeatures
 * class. There you will typically add the EdgeFeatures feature first
 * and then the rest.  So, for example if you wanted to add some
 * parameter for each label (like a prior), you can create a new
 * FeatureTypes class that will create as many featureids as the
 * number of labels. You will have to create a new class that is
 * derived from FeatureGenImpl and just have a different
 * implementation of the addFeatures subroutine. The rest will be
 * handled by the parent class.
 * This class  is responsible for converting the
 * string-ids that the FeatureTypes assign to their features into
 * distinct numbers. It has a inner class called FeatureMap that will
 * make one pass over the training data and create the map of
 * featurenames->integer id and as a side effect count the number of
 * features.
 *
 * @author Sunita Sarawagi
 * */

public class FeatureGenImpl implements FeatureGeneratorNested {

	public Vector features;
	transient Iterator featureIter;
	protected FeatureTypes currentFeatureType;
	protected FeatureImpl featureToReturn, feature;
	public Model model;
	int numFeatureTypes=0;
	int totalFeatures;
	boolean _fixedTransitions=true;
	public boolean generateOnlyXFeatures=false;
	public boolean addOnlyTrainFeatures=true;
	TIntHashSet retainedFeatureTypes=new TIntHashSet(); // all features of this type are retained.

	transient DataSequence data;
	int cposEnd;
	int cposStart;
	WordsInTrain dict;

	//TODO: Dictionary
	Dictionary dictionary;
	String dirDictionary = Parameters.Directory.dictionary;
	String filenameDictionary = dirDictionary + "MSM13/MISC/";

	String dirBrownCluster = "./samples/data/bcs2010/BrownCluster/";
	String filenameBrownClusterAddress = dirBrownCluster + "brownBllipClusters";//"Twitter-100K-Ptbr-BHC";//"Twitter-100K-Ptbr-BHC"
	String filenameBrownClusterEtzioniAddress = dirBrownCluster + "60K_clusters.txt";
	String filenameJClusterAddress = dirBrownCluster + "";

	BrownCluster brownCluster;
	BrownClusterEtzioni brownClusterEtzioni;
	JCluster jCluster;

	NGram nGram;

	ArrayList<ContextAnalysis> contextAnalysisList;
	ContextAnalysis contextAnalysis;
	AffixManager affixManager;
	SummarizedPattern summarizedPattern;
	TwitterSymbol twitterSymbol;
	ContextAnalysisRelaxed contextAnalysisRelaxed;

	//Create by @DMZ
	private ArrayList<SupportContext> supportContextList;
	private ArrayList<SelectByPOSTag> selectByPOSTagList;

	private SupportContext supportContext;
	private SupportContext supportContext2;
	private String supportContextFilename;
	//private String supportContextFilename2;
	private Writer logOut;
	private VerbalSuffixHashMap verbalSuffixHashMap;
	private SelectByPOSTag selByPOSTag;
	private SelectByContext selByCxt;
	private ArtificialIntelligenceInterpreter aII;

	public int numberPOSTagFeaturesPreTest;
	public int numberPOSTagFeaturePosTest;

	public int maxContextWindowSize = 4;
	private String trainingFilenameAddress;
	private String testFilenameAddress;

	/** Feature as Fiters FSF **/
	protected EntityProbabilityComponent entityProbabilityComponent;
	protected ArrayList<AffixComponent> affixComponentList;
	protected ArrayList<ContextComponent> contextComponentList;
	protected SingleTermDictionary3Component singleTermDictionary3Component;


	public void addFeature(FeatureTypes fType) {
		addFeature(fType,false);
	}
	public void addFeature(FeatureTypes fType, boolean retainThis) {
		features.add(fType);
		if (retainThis) {
			retainedFeatureTypes.add(fType.getTypeId()+1);
		}
		if (!fType.fixedTransitionFeatures()) {
			_fixedTransitions = false;
		}
	}
	public void setDict(WordsInTrain d) {
		dict = d;
	}
	public WordsInTrain getDict(){
		if (dict == null) {
			dict = new WordsInTrain();
		}
		return dict;
	}

	protected void addFeatures() {

		//Context Feature by @DMZ
		boolean useDMZFeatures = true;
		if(useDMZFeatures) {

			/** Brown Cluster **/
			/*brownCluster = new BrownCluster();
    		brownCluster.loadBrownHierarquicalCluster(filenameBrownClusterAddress);
    		NewFeatureTypes.setBrownCluster(brownCluster);
    		for(int i = 8; i <= 18; i++) addFeature(new BrownClusterFeature(this,brownCluster,i));*/

			/** Brown Cluster Etzioni Version **/
			/*brownClusterEtzioni = new BrownClusterEtzioni();
    		brownClusterEtzioni.loadBrownCluster(filenameBrownClusterEtzioniAddress);
    		for(int i = 8; i <= 18; i++) addFeature(new BrownClusterEtzioniFeature(this, brownClusterEtzioni, i));*/

			/** JCluster **/
			/*jCluster = new JCluster();
    		jCluster.loadJCluster(filenameJClusterAddress);
    		for(int i = 8; i <= 18; i++) addFeature(new JClusterFeature(this, jCluster, i));*/

			/** Summarized Pattern **/
			/*summarizedPattern = new SummarizedPattern();
    		NewFeatureTypes.setSummarizedPattern(summarizedPattern);
    		addFeature(new SummarizedPatternFeature(this, summarizedPattern));*/

			/** AII Dictionary**/
			/*File folder = new File("./samples/data/bcs2010/AutoTagger/Dictionary/");
    		File[] listOfFiles = folder.listFiles();

    		String dictionaryFilenameAddress;

    		for (int i = 0; i < listOfFiles.length; i++) {

    			dictionaryFilenameAddress = "./samples/data/bcs2010/AutoTagger/Dictionary/" + listOfFiles[i].getName();

    			if (listOfFiles[i].isFile())
    				addFeature(new SimpleDictionaryFeature(this, dictionaryFilenameAddress));
    		}*/

			/** Dictionary **/
			//dictionary = new Dictionary(0.1, 3, DictionaryMatchingType.JaroWinkler); //-- (0.4) Jaro-Winkler, (0.9) SoftTFIDF
			//dictionary.loadDictionary(filenameDictionary);
			//addFeature(new DictionaryFeature(this, dictionary));
			//addFeature(new DictionaryUniqueFeature(this, dictionary)); //-- Good for a Complete Dictionary

			//Dictionary dictionaryBHC12 = new lbd.Model.Dictionary(3, brownCluster, 12);
			//dictionaryBHC12.loadDictionary(filenameDictionary);
			//addFeature(new DictionaryUniqueFeature(this, dictionaryBHC12));

			/*Dictionary dictionary = new Dictionary(0.1, 3, DictionaryMatchingType.JaroWinkler);
    		dictionary.loadDictionary(filenameDictionary);
    		addFeature(new DictionaryFeature(this, dictionary));*/

			/*Dictionary dictionarySumPtr = new Dictionary(3, summarizedPattern);
    		dictionarySumPtr.loadDictionary(filenameDictionary);
    		addFeature(new DictionaryFeature(this, dictionarySumPtr));

    		Dictionary dictionaryBHC7 = new Dictionary(3, brownCluster, 7);
    		dictionaryBHC7.loadDictionary(filenameDictionary);
    		addFeature(new DictionaryFeature(this, dictionaryBHC7));

    		/** TwitterSymbol Feature **/
			//twitterSymbol = new TwitterSymbol();
			//addFeature(new TwitterSymbolFeature(this, twitterSymbol));

			/** Affix Manager (Old) **/
			/*affixManager = new AffixManager();
    		NewFeatureTypes.setAffixManager(affixManager);
    		addFeature(new TokenAffixFeature(this, affixManager, AffixType.PrefixSize2));
    		addFeature(new TokenAffixFeature(this, affixManager, AffixType.PrefixSize3));
    		addFeature(new TokenAffixFeature(this, affixManager, AffixType.SuffixSize2));
    		addFeature(new TokenAffixFeature(this, affixManager, AffixType.SuffixSize3));
    		addFeature(new TokenAffixFeature(this, affixManager, AffixType.SuffixSize4));*/

			/** Affix (New) **/


			/** NGram **/
			//nGram = new NGram();
			//for(int i = 0; i < 16; i++) for(int j = i+4; j <= 16; j++) addFeature(new NGramFeature(this, nGram, i, j));
			/*addFeature(new NGramFeature(this, nGram, 0, 4));
    		addFeature(new NGramFeature(this, nGram, 0, 8));
    		addFeature(new NGramFeature(this, nGram, 0, 12));
    		addFeature(new NGramFeature(this, nGram, 4, 16));
    		addFeature(new NGramFeature(this, nGram, 8, 16));
    		addFeature(new NGramFeature(this, nGram, 12, 16));
    		addFeature(new NGramFeature(this, nGram, 4, 8));
    		addFeature(new NGramFeature(this, nGram, 3, 6));
    		addFeature(new NGramFeature(this, nGram, 2, 6));*/

			/** Spell Feature **/
			//addFeature(new CapitalizationFeature(this));

			/** Context Analysis Relaxed **/
			//contextAnalysisRelaxed = new ContextAnalysisRelaxed();
			//addFeature(new ContextAnalysisRelaxedFeature(this, contextAnalysisRelaxed));

			/** Context Prefix Size **/
			//addFeature(new ContextPrefixSizeFeature(this));

			/** MultiContext **/
			/*contextAnalysisList = new ArrayList<ContextAnalysis>();
    		contextAnalysisList.add(new ContextAnalysis(1, 5, AffixType.None));
    		contextAnalysisList.add(new ContextAnalysis(1, 5, AffixType.PrefixSize2));
    		contextAnalysisList.add(new ContextAnalysis(1, 5, AffixType.PrefixSize3));
    		contextAnalysisList.add(new ContextAnalysis(1, 5, AffixType.SuffixSize1));
    		contextAnalysisList.add(new ContextAnalysis(1, 5, AffixType.SuffixSize2));
    		contextAnalysisList.add(new ContextAnalysis(1, 5, AffixType.SuffixSize3));
    		contextAnalysisList.add(new ContextAnalysis(1, 5, AffixType.SuffixSize4));
    		contextAnalysisList.add(new ContextAnalysis(1, 5, AffixType.None, brownCluster));
    		contextAnalysisList.add(new ContextAnalysis(1, 5, AffixType.PrefixSize2, brownCluster));
    		contextAnalysisList.add(new ContextAnalysis(1, 5, AffixType.PrefixSize3, brownCluster));
    		contextAnalysisList.add(new ContextAnalysis(1, 5, AffixType.SuffixSize1, brownCluster));
    		contextAnalysisList.add(new ContextAnalysis(1, 5, AffixType.SuffixSize2, brownCluster));
    		contextAnalysisList.add(new ContextAnalysis(1, 5, AffixType.SuffixSize3, brownCluster));
    		contextAnalysisList.add(new ContextAnalysis(1, 5, AffixType.SuffixSize4, brownCluster));
    		NewFeatureTypes.setContextAnalysisList(contextAnalysisList);

    		/** Context **/
			/*for(int windowSize = 5; windowSize > 1; windowSize--) {
				for(int cxtTypeIndex = 0; cxtTypeIndex < ContextType.values().length; cxtTypeIndex++) {
					addFeature(new ContextAnalysisFeature(this, contextAnalysisList.get(0),
							ContextType.values()[0], windowSize, AffixType.None));
				}
			}*/

			/* contextAnalysis = new ContextAnalysis(0.9, 4); //-- 0.9
    		//addFeature(new MultiContextFeature(this,contextAnalysis)); */

			/** Label iCxt Feature**/
			//addFeature(new LabeliCxtFeature(this));

			/** Outbreak Context Feature **/
			//supportContextList = new ArrayList<SupportContext>();
			//selectByPOSTagList = new ArrayList<SelectByPOSTag>();

			/*for(int i = 1; i <= maxContextWindowSize; i++) {

    		   supportContextList.add(new SupportContext(i, "BILOU", true, logOut));
    		   addFeature(new ContextFeature(this, supportContextList.get(i-1), i));

    		   //selectByPOSTagList.add(new SelectByPOSTag(supportContextList.get(i-2), 0));
    	 	   //addFeature(new POSTagPTBRFeature(this, selectByPOSTagList.get(i-2), 1f));
    	   }*/

			//String stopWordFile = "./samples/data/bcs2010/AutoTagger/Dictionary/PortugueseStopWords-Tweet.dic";
			//aII = new ArtificialIntelligenceInterpreter(stopWordFile, "", "");
			//addFeature(new AIIFeature(this, aII, 1f));

			//supportContext = new SupportContext(3, "BILOU", true, logOut);
			//addFeature(new ContextFeature(this, supportContext, 1f));//1f
			//addFeature(new InputContextFeature(this, supportContext, 1f));//0.5f
			//addFeature(new OutputContextFeature(this, supportContext, 1f));
			//addFeature(new ContextZeroFeature(this, supportContext, 1f));//2f
			//addFeature(new ContextMostValuableTokensFeature(this, supportContext, 1f, 0.5));
			//addFeature(new ContextWithoutPOSTagToken(this, supportContext, 1f));

			/** Select by Context **/
			//String inputCxt = "samples/data/bcs2010/Twitter-33850(34000)BILOU-NLN-LA(411.0)-PProc-R1-POSTag.tagged";
			//selByCxt = new SelectByContext(inputCxt, 3, true, 0);
			//selByCxt = new SelectByContext(supportContext, 0); //0.3
			//addFeature(new ContextFeature(this, selByCxt, 1f));//1f

			/** Select by POSTag **/
			//String input = "samples/data/bcs2010/2K-Player-MANUAL-BILOU-PProc-CV1-CRF-REWE-POSTag.tagged";
			//selByPOSTag = new SelectByPOSTag(input, 2, true, 0);
			//selByPOSTag = new SelectByPOSTag(supportContext, 0);
			//selByPOSTag = new SelectByPOSTag(2, false, 0); //0.2
			//addFeature(new POSTagPTBRFeature(this, selByPOSTag, 1f));

			//addFeature(new StatePositionFeature(this, 1f));

			/** Grammatical Features **/
			/*addFeature(new GrammaticalClassFeatureAdverb(this, 1f));
	 	   addFeature(new GrammaticalClassFeatureArticle(this, 1f));
	 	   addFeature(new GrammaticalClassFeatureConjunction(this, 1f));
	 	   addFeature(new GrammaticalClassFeatureInterjection(this, 1f));
	 	   addFeature(new GrammaticalClassFeatureNumeral(this, 1f));
	 	   addFeature(new GrammaticalClassFeaturePreposition(this, 1f));
	 	   addFeature(new GrammaticalClassFeaturePronoun(this, 1f));*/
			//addFeature(new VerbTenseFeature(this, supportContext, 1f));
			//addFeature(new VerbalSuffixFeature(this, 1f));
			//addFeature(new GrammaticalNumeralNounFeature(this, 1f));

			//addFeature(new TwitterSymbolsFeature(this, 1f));
			//addFeature(new StateTransitionFeature(this, supportContext.isTest));

			//supportContext2 = new SupportContext(3, "BILOU", true, logOut);
			//addFeature(new ContextFeature(this, supportContext2, 1f));
			//addFeature(new InputContextFeature(this, supportContext2, 0.5f));
			//addFeature(new OutputContextFeature(this, supportContext2, 0.5f));
			//addFeature(new ContextZeroFeature(this, supportContext2, 2f));

			/** Feature Filters like FSF  - like all 5 filters **/
			entityProbabilityComponent = new EntityProbabilityComponent();
			singleTermDictionary3Component = new SingleTermDictionary3Component();
			affixComponentList = new ArrayList<AffixComponent>();
			contextComponentList = new ArrayList<ContextComponent>();

			/** Entity Probability **/
			addFeature(new EntityProbability(this, entityProbabilityComponent));

			/** Affix **/
			for(int affixTypeIndex = 0; affixTypeIndex < AffixComponent.AffixType.values().length;  affixTypeIndex++) {
				for(int affixSize = 3; affixSize >= 1; affixSize--) {
					affixComponentList.add(new AffixComponent(AffixComponent.AffixType.values()[affixTypeIndex],affixSize));
					addFeature(new Affix(this, affixComponentList.get(affixComponentList.size()-1)));
				}
			}

			/** Dictionary **/
			int dictionaryListNumber = SingleTermDictionary3Component.getDictionaryListNumber();

			for(int i = 0; i < dictionaryListNumber; i++) {
				addFeature(new SingleTermDictionary3(this,i, singleTermDictionary3Component));
			}

			/** Context **/
			for(int flexibilityIndex = Flexibility.values().length-3; flexibilityIndex >= 0; flexibilityIndex--) {
				for(int windowSize = 3; windowSize > 2; windowSize--) {
					for(int contextTypeIndex = 1; contextTypeIndex < lbd.FSNER.Filter.Component.Context.ContextType.values().length; contextTypeIndex++) {
						if(flexibilityIndex > 1 || windowSize > 2) {
							contextComponentList.add(new ContextComponent(lbd.FSNER.Filter.Component.Context.
									ContextType.values()[contextTypeIndex], windowSize, Flexibility.values()[flexibilityIndex]));
							addFeature(new Context(this, contextComponentList.get(contextComponentList.size()-1)));
						}
					}
				}
			}

			/** Capitalized **/
			addFeature(new CapitalizedTerms(this));
		}

		boolean useOriginalFeatures = false;
		if(useOriginalFeatures) {

			addFeature(new EdgeFeatures(this));
			addFeature(new StartFeatures(this));
			addFeature(new EndFeatures(this));

			dict = new WordsInTrain();
			NewFeatureTypes.setWordsInTrain(dict);
			//addFeature(new UnknownFeature(this,dict));

			/** New Ones **/
			//addFeature(new WordScoreFeatures(this, dict));
			/** Finish New Ones **/

			/**
	       //addFeature(new KnownInOtherState(this, dict));
	       //addFeature(new KernelFeaturesForLongEntity(model,new WordFeatures(model, dict)));
			 **/

			addFeature(new WordFeatures(this, dict, 1f));
			//addFeature(new WordAltFeatures(this, dict, 1f));
			//addFeature(new FeatureTypesEachLabel(this,new ConcatRegexFeatures(this,0,0)));//
			//addFeature(new ConcatRegexFeatures(this,0,0));
		}
	}
	protected FeatureTypes getFeature(int i) {
		return (FeatureTypes)features.elementAt(i);
	}
	protected boolean keepFeature(DataSequence seq, FeatureImpl f) {
		if ((retainedFeatureTypes != null) && (retainedFeatureTypes.contains(currentFeatureType.getTypeId()+1))) {
			return true;
		}
		return retainFeature(seq,f);
	}
	protected boolean retainFeature(DataSequence seq, FeatureImpl f) {
		return ((seq.y(cposEnd) == f.y())
				&& ((cposStart == 0) || (f.yprev() < 0) || (seq.y(cposStart-1) == f.yprev())));
	}
	boolean featureCollectMode = false;

	class FeatureMap implements Serializable {
		Hashtable strToInt = new Hashtable();
		FeatureIdentifier idToName[];
		FeatureMap(){
			featureCollectMode = true;
		}
		public int getId(FeatureImpl f) {
			int id = getId(f.identifier());
			if ((id < 0) && featureCollectMode && (!addOnlyTrainFeatures || keepFeature(data,f))) {
				return add(f);
			}
			return id;
		}
		private int getId(Object key) {
			if (strToInt.get(key) != null) {
				return ((Integer)strToInt.get(key)).intValue();
			}
			return -1;
		}
		public int add(FeatureImpl feature) {
			int newId = strToInt.size();
			strToInt.put(feature.identifier().clone(), new Integer(newId));

			//@DMZDebug
			if(((String)feature.strId.name).indexOf("POSTag") != -1 && selByPOSTag != null) {
				if(!selByPOSTag.getSupportContext().isTest) {
					numberPOSTagFeaturesPreTest++;
				} else {
					numberPOSTagFeaturePosTest++;
					System.out.println("# Test PosTag: " + numberPOSTagFeaturePosTest);
				}
			}

			return newId;
		}
		void freezeFeatures() {
			//	    System.out.println(strToInt.size());
			featureCollectMode = false;
			idToName = new FeatureIdentifier[strToInt.size()];
			for (Enumeration e = strToInt.keys() ; e.hasMoreElements() ;) {
				Object key = e.nextElement();
				idToName[getId(key)] = (FeatureIdentifier)key;
			}
			totalFeatures = strToInt.size();
		}
		public int collectFeatureIdentifiers(DataIter trainData, int maxMem) throws Exception {
			for (trainData.startScan(); trainData.hasNext();) {
				DataSequence seq = trainData.next();
				addTrainRecord(seq);
			}
			freezeFeatures();
			return strToInt.size();
		}
		public void write(PrintWriter out) throws IOException {
			out.println("******* Features ************");
			out.println(strToInt.size());
			for (Enumeration e = strToInt.keys() ; e.hasMoreElements() ;) {
				Object key = e.nextElement();
				out.println(key + " " + ((Integer)strToInt.get(key)).intValue());
			}
		}
		public int read(BufferedReader in) throws IOException {
			in.readLine();
			int len = Integer.parseInt(in.readLine());
			String line;
			for(int l = 0; (l < len) && ((line=in.readLine())!=null); l++) {
				StringTokenizer entry = new StringTokenizer(line," ");
				FeatureIdentifier key = new FeatureIdentifier(entry.nextToken());
				int pos = Integer.parseInt(entry.nextToken());
				strToInt.put(key,new Integer(pos));
			}
			freezeFeatures();

			return strToInt.size();
		}
		public FeatureIdentifier getIdentifier(int id) {return idToName[id];}
		public String getName(int id) {return idToName[id].toString();}
	};

	FeatureMap featureMap;
	static Model getModel(String modelSpecs, int numLabels) throws Exception {
		// create model..
		return Model.getNewModel(numLabels,modelSpecs);
	}
	public FeatureGenImpl(String modelSpecs, int numLabels) throws Exception {
		this(modelSpecs,numLabels,true);
	}
	public FeatureGenImpl(String modelSpecs, int numLabels, Writer out) throws Exception {
		this(modelSpecs,numLabels,true);
		logOut = out;
	}
	public FeatureGenImpl(String modelSpecs, int numLabels, boolean addFeatureNow) throws Exception {
		this(getModel(modelSpecs,numLabels),numLabels,addFeatureNow);
	}
	public FeatureGenImpl(String modelSpecs, int numLabels, boolean addFeatureNow, Writer out) throws Exception {
		this(getModel(modelSpecs,numLabels),numLabels,addFeatureNow);
		logOut = out;
	}
	public FeatureGenImpl(Model m, int numLabels, boolean addFeatureNow) throws Exception {
		model = m;
		features = new Vector();
		featureToReturn = new FeatureImpl();
		feature = new FeatureImpl();
		featureMap = new FeatureMap();
		if (addFeatureNow) {
			addFeatures();
		}
	}
	public FeatureGenImpl(Model m, int numLabels, boolean addFeatureNow, Writer out) throws Exception {
		this(m, numLabels, addFeatureNow);
		logOut = out;
	}

	public boolean stateMappings(DataIter trainData) throws Exception {
		if (model.numStates() == model.numberOfLabels()) {
			return false;
		}
		for (trainData.startScan(); trainData.hasNext();) {
			DataSequence seq = trainData.next();
			if (seq instanceof SegmentDataSequence) {
				model.stateMappings((SegmentDataSequence)seq);
			} else {
				model.stateMappings(seq);
			}
		}
		return true;
	}
	public boolean mapStatesToLabels(DataSequence data) {
		if (model.numStates() == model.numberOfLabels()) {
			return false;
		}
		if (data instanceof SegmentDataSequence) {
			model.mapStatesToLabels((SegmentDataSequence)data);
		} else {
			for (int i = 0; i < data.length(); i++) {
				data.set_y(i, label(data.y(i)));
			}
		}
		return true;
	}
	@Override
	public int maxMemory() {return 1;}
	public boolean train(DataIter trainData) throws Exception {
		return train(trainData,true);
	}
	public boolean train(DataIter trainData, boolean cachedLabels) throws Exception {
		return train(trainData,cachedLabels,true);
	}
	public boolean labelMappingNeeded() {return model.numStates() != model.numberOfLabels();}
	public boolean train(DataIter trainData, boolean cachedLabels, boolean collectIds) throws Exception {
		// map the y-values in the training set.
		boolean labelsMapped = false;
		if (cachedLabels) {
			labelsMapped = stateMappings(trainData);
		}
		if (dict != null) {
			dict.train(trainData,model.numStates());
		}
		boolean requiresTraining = false;
		for (int f = 0; f < features.size(); f++) {
			if (getFeature(f).requiresTraining()) {
				requiresTraining = true;
				break;
			}
		}
		if (requiresTraining) {
			for (trainData.startScan(); trainData.hasNext();) {
				DataSequence seq = trainData.next();
				for (int l = 0; l < seq.length(); l++) {
					// train each featuretype.
					for (int f = 0; f < features.size(); f++) {
						getFeature(f).train(seq,l);
					}
				}
			}
		}

		//Dictionary & ContextAnalysis add by @DMZ
		if(dictionary != null || contextAnalysis != null || affixManager != null || contextAnalysisList != null || summarizedPattern != null ||
				twitterSymbol != null || brownCluster != null || contextAnalysisRelaxed != null || nGram != null) {
			for (trainData.startScan(); trainData.hasNext();) {
				DataSequence seq = trainData.next();
				for (int l = 0; l < seq.length(); l++) {
					if(seq.y(l) != 3) {
						//if(dictionary != null)dictionary.addTerm((String)seq.x(l));
					}
					if(affixManager != null) {
						affixManager.addToken((String)seq.x(l));
					}
					if(twitterSymbol != null) {
						twitterSymbol.addTwitterSymbol((String)seq.x(l));
					}
					if(nGram != null) {
						nGram.addToken((String)seq.x(l));
					}
				}
				if(contextAnalysisRelaxed != null) {
					contextAnalysisRelaxed.addContext(seq);
				}
				if(contextAnalysis != null) {
					contextAnalysis.addContext(seq);
				}
				if(contextAnalysisList != null) {
					for(ContextAnalysis contextAnalysisItem : contextAnalysisList) {
						contextAnalysisItem.addContext(seq);
					}
				}
				if(summarizedPattern != null) {
					summarizedPattern.addSequence(seq);
				}
				NewFeatureTypes.addOOVSupportContext(seq);
			}
			if(affixManager != null) {
				affixManager.removeAffixBelowThreshold();
			}
		}

		//SupportContext add by @DMZ
		if(supportContext != null) {
			//int pos = trainingFilenameAddress.indexOf("Twitter-2000-Player-MANUAL-LABELED-BILOU-CV") + "Twitter-2000-Player-MANUAL-LABELED-BILOU-CV".length();
			//SequenceSet input = HandlingSequenceSet.transformFileInSequenceSet("./samples/data/bcs2010/Twitter-2000-Player-MANUAL+AUTO-LABELED-BILOU-CV"+trainingFilenameAddress.substring(pos, pos+1)+"-CRF-RUS-POSTag-REWE-RRT-RRL-RRWS.tagged",
			int pos = trainingFilenameAddress.indexOf("Twitter-2000-Player-MANUAL+AUTO-LABELED-BILOU-CV") + "Twitter-2000-Player-MANUAL+AUTO-LABELED-BILOU-CV".length();
			/*SequenceSet input = HandlingSequenceSet.transformFileInSequenceSet("./samples/data/bcs2010/Twitter-2000-Player-MANUAL-LABELED-BILOU-CV"+trainingFilenameAddress.substring(pos, pos+1)+"-CRF-POSTag-REWE-RRT-RRL-RRWS.tagged",
					FileType.TRAINING, false);*/
			supportContext.generateContext(trainData);
		} if(supportContext2 != null) {
			supportContext2.generateContext(trainData);
		} if(selByPOSTag != null) {

			//int pos = trainingFilenameAddress.indexOf("Twitter-2000-Player-MANUAL-LABELED-BILOU-CV") + "Twitter-2000-Player-MANUAL-LABELED-BILOU-CV".length();
			//SequenceSet input = HandlingSequenceSet.transformFileInSequenceSet("./samples/data/bcs2010/Twitter-2000-Player-MANUAL+AUTO-LABELED-BILOU-CV"+trainingFilenameAddress.substring(pos, pos+1)+"-CRF-RUS-POSTag-REWE-RRT-RRL-RRWS.tagged",

			int pos = trainingFilenameAddress.indexOf("Twitter-2000-Player-MANUAL+AUTO-LABELED-BILOU-CV") + "Twitter-2000-Player-MANUAL+AUTO-LABELED-BILOU-CV".length();
			/*SequenceSet input = HandlingSequenceSet.transformFileInSequenceSet("./samples/data/bcs2010/Twitter-2000-Player-MANUAL-LABELED-BILOU-CV"+trainingFilenameAddress.substring(pos, pos+1)+"-CRF-POSTag-REWE-RRT-RRL-RRWS.tagged",
					FileType.TRAINING, false);*/

			selByPOSTag.loadSupportContext(trainData);
			selByPOSTag.analyzePOSTagContext(trainingFilenameAddress, trainingFilenameAddress);
		} if(supportContextList != null) {
			for(SupportContext sCxt : supportContextList) {
				sCxt.generateContext(trainData);
			}

		} if(selByCxt != null) {
			selByCxt.analyzeContext(trainingFilenameAddress, trainingFilenameAddress);
		} if(selectByPOSTagList != null) {

			String inputCxt = "./samples/data/bcs2010/Twitter-33850(34000)BILOU-NLN-LA(411.0)-PProc-R1-POSTag.tagged";

			for(SelectByPOSTag selByPOSTag : selectByPOSTagList) {
				selByPOSTag.analyzePOSTagContext(inputCxt, inputCxt);
			}
		}

		/** Artificial Intelligence Interpreter @DMZ **/
		if(aII != null) {

			aII.setContextSourceFile(trainingFilenameAddress);
			aII.setFileToLabel(trainingFilenameAddress);

			String stopWordFile = aII.getStopWordFile();
			String contextSourceFile = aII.getContextSourceFile();

			aII.loadStopWords(stopWordFile);
			aII.loadEntityList(contextSourceFile);
			aII.calculateScorePerSequence();
		}

		/** Features as Filter FSF **/
		if(entityProbabilityComponent != null) {

			singleTermDictionary3Component.initialize();

			for (trainData.startScan(); trainData.hasNext();) {

				DataSequence sequence = trainData.next();

				for(int i = 0; i < sequence.length(); i++) {
					if(sequence.y(i) != 3 && sequence.y(i) < 5) {//it is entity for BILOU encoding;

						//-- Add to term component
						entityProbabilityComponent.addTerm((String)sequence.x(i));

						//-- Add to affix components
						for(int e = 0; e < affixComponentList.size(); e++) {
							affixComponentList.get(e).addAffixes((String)sequence.x(i));
						}
						//-- Add to context components
						for(int e = 0; e < contextComponentList.size(); e++) {
							contextComponentList.get(e).addAsContext(sequence, i);
						}
					}
				}
			}
		}

		if (collectIds) {
			totalFeatures = featureMap.collectFeatureIdentifiers(trainData,maxMemory());
		}
		return labelsMapped;
	};
	/**
	 * @param seq
	 */
	public void addTrainRecord(DataSequence seq) {
		for (int l = 0; l < seq.length(); l++) {
			for (startScanFeaturesAt(seq,l); hasNext(); ) {
				next();
			}
		}
	}
	public void printStats() {
		System.out.println("Num states " + model.numStates());
		System.out.println("Num edges " + model.numEdges());
		if (dict != null) {
			System.out.println("Num words in dictionary " + dict.dictionaryLength());
		}
		System.out.println("Num features " + numFeatures());
	}
	protected FeatureImpl nextNoId() {
		feature.copy(featureToReturn);
		advance(false);
		return feature;
	}
	protected void advance() {
		advance(!featureCollectMode);
	}
	protected void advance(boolean returnWithId) {
		while (true) {
			for (;((currentFeatureType == null) || !currentFeatureType.hasNext()) && featureIter.hasNext();) {
				currentFeatureType = (FeatureTypes)featureIter.next();
			}
			if (!currentFeatureType.hasNext()) {
				break;
			}
			while (currentFeatureType.hasNext()) {
				featureToReturn.init();
				copyNextFeature(featureToReturn);

				featureToReturn.id = featureMap.getId(featureToReturn);

				if (featureToReturn.id < 0){
					continue;
				}
				if (featureValid(data, cposStart, cposEnd, featureToReturn, model)) {
					return;
				}

			}
		}
		featureToReturn.id = -1;
	}
	/**
	 * @param featureToReturn
	 */
	protected void copyNextFeature(FeatureImpl featureToReturn) {
		currentFeatureType.next(featureToReturn);
	}
	/**
	 * @param featureToReturn
	 * @param cposEnd
	 * @param cposStart
	 * @param data
	 * @return
	 */
	public static boolean featureValid(DataSequence data, int cposStart, int cposEnd, FeatureImpl featureToReturn, Model model) {
		if (((cposStart > 0) && (cposEnd < data.length()-1))
				|| (featureToReturn.y() >= model.numStates())
				|| (featureToReturn.yprev() >= model.numStates())) {
			return true;
		}
		if ((cposStart == 0) && (model.isStartState(featureToReturn.y()))
				&& ((data.length()>1) || (model.isEndState(featureToReturn.y())))) {
			return true;
		}
		if ((cposEnd == data.length()-1) && (model.isEndState(featureToReturn.y()))) {
			return true;
		}
		return false;
	}
	protected void initScanFeaturesAt(DataSequence d) {
		data = d;
		currentFeatureType = null;
		featureIter = features.iterator();
		advance();
	}
	@Override
	public void startScanFeaturesAt(DataSequence d, int prev, int p) {
		cposEnd = p;
		cposStart = prev+1;
		for (int i = 0; i < features.size(); i++) {
			getFeature(i).startScanFeaturesAt(d,prev,cposEnd);
		}
		initScanFeaturesAt(d);
	}
	@Override
	public void startScanFeaturesAt(DataSequence d, int p) {
		cposEnd = p;
		cposStart = p;
		for (int i = 0; i < features.size(); i++) {
			getFeature(i).startScanFeaturesAt(d,cposEnd);
		}
		initScanFeaturesAt(d);
	}
	@Override
	public boolean hasNext() {
		return (featureToReturn.id >= 0);
	}

	@Override
	public Feature next() {
		feature.copy(featureToReturn);
		advance();
		//      System.out.println(feature);
		return feature;
	}
	public void freezeFeatures() {
		if (featureCollectMode) {
			featureMap.freezeFeatures();
		}
	}
	@Override
	public int numFeatures() {
		return totalFeatures;
	}
	public FeatureIdentifier featureIdentifier(int id) {return featureMap.getIdentifier(id);}
	@Override
	public String featureName(int featureIndex) {
		return featureMap.getName(featureIndex);
	}
	public int numStates() {
		return model.numStates();
	}
	public int label(int stateNum) {
		return model.label(stateNum);
	}
	protected int numFeatureTypes() {
		return features.size();
	}
	public void read(String fileName) throws IOException, ClassNotFoundException {
		BufferedReader in=new BufferedReader(new FileReader(fileName));
		if (dict != null) {
			dict.read(in, model.numStates());
		}
		totalFeatures = featureMap.read(in);

		readDMZComponents(fileName);
	}

	protected void readDMZComponents(String fileName) throws IOException, ClassNotFoundException {

		//-- @DMZ
		/*if(brownCluster != null) {
        	BrownCluster.readBrownHierarquicalClusterObject("BrownCluster.bin", brownCluster);
        }*/

		for(Object f : features) {
			if(NewFeatureTypes.class.isAssignableFrom(f.getClass())) {
				((NewFeatureTypes) f).readStateFrequencyMap();
			}
		}

		if(nGram != null) {
			nGram.readNGramObject("NGram.bin", nGram);
		} if(contextAnalysisRelaxed != null) {
			contextAnalysisRelaxed.readContextAnalysisRelaxedObject("CxtRlx.bin");
		} if(twitterSymbol != null) {
			twitterSymbol.readTwitterSymbolObject("TwitterSymbol.bin", twitterSymbol);
		} if(summarizedPattern != null) {
			summarizedPattern.readSummarizedPatternObject("SummPtrn.bin", summarizedPattern);
		} if(affixManager != null) {
			affixManager.readAffixManagerObject("AffixManager.bin", affixManager);
		} if(contextAnalysisList != null) {
			int index = 0;
			for(ContextAnalysis contextAnalysisItem : contextAnalysisList) {
				contextAnalysisItem.readContextAnalysisObject("CtxAnalysisItem"+(++index)+".bin", contextAnalysisItem);
			}
			//contextAnalysisList.get(0).loadContext("");
		} if(contextAnalysis != null) {
			contextAnalysis.readContextAnalysisObject("CtxAnalysis.bin", contextAnalysis);
		} if(supportContext != null) {
			supportContextFilename = supportContext.generateSupportContextFeatureFilename(fileName);
			supportContext.readSupportContextObject(supportContextFilename, supportContext);
			supportContext.isTest = true;
		} if(supportContext2 != null) {
			supportContext2.generateSupportContextFeatureFilename(fileName);
			supportContext2.readSupportContextObject("ExpCxt2.FeatCxt", supportContext2);
			supportContext2.isTest = true;
		} if(verbalSuffixHashMap != null) {
			verbalSuffixHashMap.readVerbalSuffixObject("verbalSuffix.vs", verbalSuffixHashMap);
		} if(selByCxt != null) {
			selByCxt.readSelectByContextObject("Cxt.Feat", selByCxt);
		} if (selByPOSTag != null) {
			selByPOSTag.readSelectByPOSTagObject("POSTag.FeatPOS", selByPOSTag);
			selByPOSTag.getSupportContext().isTest = true;
		} if(supportContextList != null) {
			int i = 0;
			for(SupportContext sCxt : supportContextList) {
				sCxt.readSupportContextObject("CxtList.Feat" + (++i), sCxt);
				sCxt.isTest = true;
			}
		} if(selectByPOSTagList != null) {
			int i = 0;
			for(SelectByPOSTag selByPOSTag : selectByPOSTagList) {
				selByPOSTag.readSelectByPOSTagObject("POSTag.FeatPOS" + (++i), selByPOSTag);
				selByPOSTag.getSupportContext().isTest = true;
			}
		} if(aII != null) {
			aII.readArtificialIntelligenceInterpreterObject("AII-Config", aII);
		}

		/** Features to Filters FSF **/
		if(entityProbabilityComponent != null) {
			singleTermDictionary3Component.initialize();
			entityProbabilityComponent.read(entityProbabilityComponent);

			for(ContextComponent context : contextComponentList) {
				context.read(context);
			}
			for(AffixComponent affix : affixComponentList) {
				affix.read(affix);
			}
		}

	}

	public void write(String fileName) throws IOException {
		PrintWriter out=new PrintWriter(new FileOutputStream(fileName));
		if (dict != null) {
			dict.write(out);
		}
		featureMap.write(out);
		out.flush();
		out.close();

		writeDMZComponents(fileName);
	}

	protected void writeDMZComponents(String fileName) throws IOException {

		//-- @DMZ
		/*if(brownCluster != null) {
        	brownCluster.writeBrownHierarquicalClusterObject("BrownCluster.bin");

        	double oovPerc = ((double)BrownClusterFeature.oov)/(BrownClusterFeature.oov + BrownClusterFeature.invocab);
        	double invocabPerc = ((double)BrownClusterFeature.invocab)/(BrownClusterFeature.oov + BrownClusterFeature.invocab);

        	System.out.print("oov: " + BrownClusterFeature.oov + " (" + oovPerc + "%)");
        	System.out.println("invocabPerc: " + BrownClusterFeature.invocab + " (" + invocabPerc +"%)");

        	double oovPercEntity = ((double)BrownClusterFeature.oovEntity)/(BrownClusterFeature.oovEntity + BrownClusterFeature.invocabEntity);
        	double invocabPercEntity = ((double)BrownClusterFeature.invocabEntity)/(BrownClusterFeature.oovEntity + BrownClusterFeature.invocabEntity);

        	System.out.print("oovEntity: " + BrownClusterFeature.oovEntity + " (" + oovPercEntity + "%)");
        	System.out.println("invocabPercEntity: " + BrownClusterFeature.invocabEntity + " (" + invocabPercEntity +"%)");

        }*/

		for(Object f : features) {
			if(NewFeatureTypes.class.isAssignableFrom(f.getClass())) {
				((NewFeatureTypes) f).writeStateFrequencyMap();
			}
		}

		if(nGram != null) {
			nGram.writeNGramObject("NGram.bin");
		} if(contextAnalysisRelaxed != null) {
			contextAnalysisRelaxed.writeContextAnalysisRelaxedObject("CxtRlx.bin");
		} if(twitterSymbol != null) {
			twitterSymbol.writeTwitterSymbolObject("TwitterSymbol.bin");
		} if(summarizedPattern != null) {
			summarizedPattern.writeSummarizedPatternObject("SummPtrn.bin");
		} if(affixManager != null) {
			affixManager.writeAffixManagerObject("AffixManager.bin");
		} if(contextAnalysisList != null) {
			int index = 0;
			for(ContextAnalysis contextAnalysisItem : contextAnalysisList) {
				contextAnalysisItem.writeContextAnalysisObject("CtxAnalysisItem"+(++index)+".bin");
			}
		} if(contextAnalysis != null) {
			contextAnalysis.writeContextAnalysisObject("CtxAnalysis.bin");
		} if(supportContext != null) {
			supportContextFilename = supportContext.generateSupportContextFeatureFilename(fileName);
			supportContext.writeSupportContextObject(supportContextFilename);
		} if(supportContext2 != null) {
			supportContext2.writeSupportContextObject("ExpCxt2.FeatCxt");
		} if(verbalSuffixHashMap != null) {
			verbalSuffixHashMap.writeVerbalSuffixObject("verbalSuffix.vs");
		} if(selByCxt != null) {
			selByCxt.writeSelectByContextObject("Cxt.Feat");
		} if(selByPOSTag != null) {
			selByPOSTag.writeSelectByPOSTagObject("POSTag.FeatPOS");
		}  if(supportContextList != null) {
			int i = 0;
			for(SupportContext sCxt : supportContextList) {
				sCxt.writeSupportContextObject("CxtList.Feat" + (++i));
			}
		} if(selectByPOSTagList != null) {
			int i = 0;
			for(SelectByPOSTag selByPOSTag : selectByPOSTagList) {
				selByPOSTag.writeSelectByPOSTagObject("POSTag.FeatPOS" + (++i));
			}
		} if(aII != null) {
			aII.writeArtificialIntelligenceInterpreterObject("AII-Config");
		}

		/** Features to Filters FSF **/
		if(entityProbabilityComponent != null) {
			entityProbabilityComponent.write();

			for(ContextComponent context : contextComponentList) {
				context.write();
			}
			for(AffixComponent affix : affixComponentList) {
				affix.write();
			}
		}

		//--@DMZ
		//System.out.println("\n# Training PreTag: " + numberPOSTagFeaturesPreTest);

	}

	public void displayModel(double featureWts[]) throws IOException {
		//@DMZDebug
		//displayModel(featureWts,System.out);
	}
	public void displayModel(double featureWts[], PrintStream out) throws IOException {
		for (int fIndex = 0; fIndex < featureWts.length; fIndex++) {
			Object feature = featureIdentifier(fIndex).name;
			int classIndex = featureIdentifier(fIndex).stateId;
			int label = model.label(classIndex);
			out.println(feature + " " + LabelMap.getLabelNameBILOU(label) + " " + classIndex + " " + featureWts[fIndex]);

			//@DMZDebug
			if(logOut != null) {
				logOut.write(feature + " " + label + " " + classIndex + " " + featureWts[fIndex]+"\n");
			}
		}
		/*
         out.println("Feature types statistics");
         for (int f = 0; f < features.size(); f++) {
         getFeature(f).print(featureMap, featureWts);
         }
		 */
	}

	public SupportContext getSupportContext() {
		return(supportContext);
	}

	public boolean fixedTransitionFeatures() {
		return _fixedTransitions;
	}
	public String getTrainingFilenameAddress() {
		return trainingFilenameAddress;
	}
	public void setTrainingFilenameAddress(String trainingFilenameAddress) {
		this.trainingFilenameAddress = trainingFilenameAddress;
	}
	public String getTestFilenameAddress() {
		return testFilenameAddress;
	}
	public void setTestFilenameAddress(String testFilenameAddress) {
		this.testFilenameAddress = testFilenameAddress;
	}
};
