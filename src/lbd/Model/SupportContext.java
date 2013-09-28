package lbd.Model;

import iitb.CRF.DataIter;
import iitb.CRF.DataSequence;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import lbd.CRF.LabelMap;
import lbd.Utils.POSTaggerPTBR;
import lbd.Utils.Utils;

/**
 * SupportContext: This class aims to provide context
 *  (prefix and suffix) for each entity found in the
 *  training file. In case of composite entities, they
 *   must be construed as if was a string only.
 * @author DMZ
 */
public class SupportContext implements Serializable {

	private static final long serialVersionUID = 1L;

	//-- The TAG for the feature save name
	private static final String TAG_SUPPORT_CONTEXT = "SupCxt";

	//-- Only for @DMZDebug
	public boolean isTest;
	protected boolean isSilent;

	//-- Quantity token prefix and suffix in each side
	private int windowSize = 1;
	private static int contextID = 0;

	private boolean supportOnlyEntity;

	//-- TagSet used {IO, BIO or BILOU}
	private String tagSet;

	//-- List of contextCopyOfSupportContext
	private ArrayList<ContextToken> contextList;
	private ArrayList<ContextToken> contextListNotNormalized;
	private ArrayList<ContextToken> contextListWithSuffixOnly;
	private ArrayList<ContextToken> contextListWithPrefixOnly;
	private ArrayList<ContextToken> contextZeroList;

	private POSTaggerPTBR posTagger;
	private ArrayList<HashMap<String,Integer>> contextPosition;

	private float weight;

	private HashMap<String, ContextToken> fastAccessContextList;
	private HashMap<String, ContextToken> fastAccessPrefixContextList;
	private HashMap<String, ContextToken> fastAccessSuffixContextList;
	private HashMap<String, ContextToken> fastAccessContextZeroList;

	private HashMap<String, ContextToken> fastAccessContextListNotNormalized;
	private HashMap<String, ContextToken> fastAccessContextListConsiderMainToken;
	private HashMap<String, ContextToken> fastAccessPrefixContextListConsiderMainToken;
	private HashMap<String, ContextToken> fastAccessSuffixContextListConsiderMainToken;

	private HashMap<String, ContextToken> fastAccessContextWithoutPOSTagToken;
	private HashMap<String, ContextToken> fastAccessPrefixPosTagList;

	public static final String START_LEFT_KEY = "&";
	public static final String START_RIGHT_KEY = "#";
	public static final String DIVISER = ";";

	private Writer logOut;

	private String inputFilenameAddress;

	boolean dontAddUnsureContext = true;

	/**
	 * SupportContext (Constructor)
	 * @param windowSize Quantity token prefix and suffix in each side
	 * @param tagSet the code name of tag set (IO, BIO or BILOU)
	 * @param supportOnlyEntites If the contexts created only considerer entities
	 */
	public SupportContext(int windowSize, String tagSet, boolean supportOnlyEntites) {
		this(windowSize, tagSet);
		this.supportOnlyEntity = supportOnlyEntites;
	}

	public SupportContext(int windowSize, String tagSet, boolean supportOnlyEntites, Writer out) {
		this(windowSize, tagSet);
		this.supportOnlyEntity = supportOnlyEntites;
		logOut = out;
	}

	/**
	 * SupportContext (Constructor)
	 * @param windowSize Quantity token prefix and suffix in each side
	 * @param tagSet the code name of tag set (IO, BIO or BILOU)
	 */
	public SupportContext(int windowSize, String tagSet) {
		this(tagSet);
		this.windowSize = windowSize;
	}

	/**
	 * SupportContext (Constructor)
	 * @param tagSet the code name of tag set (IO, BIO or BILOU)
	 */
	public SupportContext(String tagSet) {

		contextList = new ArrayList<ContextToken> ();
		contextListNotNormalized = new ArrayList<ContextToken>();
		contextListWithSuffixOnly = new ArrayList<ContextToken>();
		contextListWithPrefixOnly = new ArrayList<ContextToken>();
		contextZeroList = new ArrayList<ContextToken> ();

		posTagger = new POSTaggerPTBR();
		contextPosition = new ArrayList<HashMap<String,Integer>>();

		fastAccessContextList = new HashMap<String, ContextToken>();
		fastAccessPrefixContextList = new HashMap<String, ContextToken>();
		fastAccessSuffixContextList = new HashMap<String, ContextToken>();
		fastAccessContextZeroList = new HashMap<String, ContextToken>();

		fastAccessContextListNotNormalized = new HashMap<String, ContextToken>();
		fastAccessContextListConsiderMainToken = new HashMap<String, ContextToken>();
		fastAccessPrefixContextListConsiderMainToken = new HashMap<String, ContextToken>();
		fastAccessSuffixContextListConsiderMainToken = new HashMap<String, ContextToken>();

		fastAccessContextWithoutPOSTagToken = new HashMap<String, ContextToken>();
		fastAccessPrefixPosTagList = new HashMap<String, ContextToken>();

		this.tagSet = tagSet;
		weight = 1;
	}

	/**
	 * generateContext: Generate the context of the tokens existed
	 * in training file. The context consists of tokens
	 * before the entity (prefix) and after (suffix).
	 * The number of prefix and suffix is determined
	 * by the window size (param).
	 * @param trainData The training file
	 */
	public void generateContext(DataIter trainData) {

		//@DMZDebug
		Utils.printlnLog("Generate Context (Normalized, w=" + windowSize + "):", logOut);

		//-- Extract all the context of the training file
		extractContext(trainData);

		//-- Normalize only the token, the context is size = 0
		normalizeContextZero();

		//-- Normalize the normal context (context, prefix and suffix list)
		normalizeContext();

		//-- Prepare efficient access by hashMap
		prepareEfficientContextAccess();
		prepareEfficientContextZeroAccess();

		Utils.printlnLog("Finished Generate Context ("+ (contextList.size() + contextZeroList.size()) +" cxts)\n", logOut);
		if(logOut != null) {
			try { logOut.flush(); } catch (IOException e) { e.printStackTrace();	}
		}
	}

	public void updateContext() {

		//@DMZDebug
		Utils.printlnLog("Generate Context (Normalized, w=" + windowSize + "):", logOut);

		//-- Normalize only the token, the context is size = 0
		normalizeContextZero();

		//-- Normalize the normal context (context, prefix and suffix list)
		normalizeContext();

		//-- Prepare efficient access by hashMap
		prepareEfficientContextAccess();
		prepareEfficientContextZeroAccess();

		Utils.printlnLog("Finished Generate Context ("+ (contextList.size() + contextZeroList.size()) +" cxts)\n", logOut);
		if(logOut != null) {
			try { logOut.flush(); } catch (IOException e) { e.printStackTrace();	}
		}
	}

	private void normalizeContext() {

		Utils.printLog("\tNormalizing Context List [", logOut);
		Date startTime = new Date();

		//-- Normalize the contextList
		normalizeContextList(contextListNotNormalized, contextList);

		//@DMZDebug
		Utils.printLog("] (" + (int)(((new Date()).getTime() - startTime.getTime())/1000.0)  + "s)", logOut);
		Utils.printLog(" {" + contextList.size()  + " ctxs, ", logOut);
		Utils.printlnLog(contextListWithPrefixOnly.size() + " border cxts}", logOut);

	}

	private void normalizeContextZero() {

		Utils.printLog("\tNormalizing Context Zero List [", logOut);
		Date startTime = new Date();

		//-- Normalize the contextZeroList
		normalizeContextZeroList();

		//@DMZDebug
		Utils.printLog("] (" + (int)(((new Date()).getTime() - startTime.getTime())/1000.0)  + "s)", logOut);
		Utils.printlnLog(" {" + contextZeroList.size() + " ctxs}", logOut);

	}

	private void prepareEfficientContextAccess() {

		for(ContextToken context : contextList) {

			String prefixPOSTag = generatePOSTagKeyFromContext(context);

			fastAccessContextList.put(context.getKey(), context);

			if(context.getPrefixSize() > 0) {
				fastAccessPrefixContextList.put(START_LEFT_KEY + context.getAllPrefix() + START_RIGHT_KEY, context);
			}

			if(context.getSuffixSize() > 0) {
				fastAccessSuffixContextList.put(START_LEFT_KEY + START_RIGHT_KEY + context.getAllSuffix(), context);
			}

			if(context.getPrefixSize() > 0 && !fastAccessPrefixPosTagList.containsKey(prefixPOSTag)) {
				fastAccessPrefixPosTagList.put(prefixPOSTag, context);
			}

			/** ContextMostValuableTokens **/
			HashMap<String, Integer> contextMap = new HashMap<String, Integer>();
			for(int i = 0; i < context.getPrefixSize(); i++) {
				if(!posTagger.isTermPOSTag(context.getPrefixIndexValue(i))) {
					contextMap.put(context.getPrefixIndexValue(i), context.getContextTokenID());
				}
			}

			for(int i = 0; i < context.getSuffixSize(); i++) {
				if(!posTagger.isTermPOSTag(context.getSuffixIndexValue(i))) {
					contextMap.put(context.getSuffixIndexValue(i), context.getContextTokenID());
				}
			}

			contextPosition.add(contextMap);

			/** ContextWithoutPOSTagToken **/
			fastAccessContextWithoutPOSTagToken.put(generateContextWithoutPOSTagTokenKey(context), context);

		}

		for(ContextToken contextNotNormalized : contextListNotNormalized) {

			fastAccessContextListNotNormalized.put(contextNotNormalized.getKey(), contextNotNormalized);

			String prefix = START_LEFT_KEY + contextNotNormalized.getAllPrefix();
			String suffix = START_RIGHT_KEY + contextNotNormalized.getAllSuffix();
			String mainToken = contextNotNormalized.getTokenValue();

			fastAccessContextListConsiderMainToken.put(prefix + START_RIGHT_KEY + mainToken + suffix, contextNotNormalized);

			if(contextNotNormalized.getPrefixSize() > 0) {
				fastAccessPrefixContextListConsiderMainToken.put(prefix + START_RIGHT_KEY + mainToken + START_RIGHT_KEY, contextNotNormalized);
			}

			if(contextNotNormalized.getSuffixSize() > 0) {
				fastAccessSuffixContextListConsiderMainToken.put(START_LEFT_KEY + START_RIGHT_KEY + mainToken + suffix, contextNotNormalized);
			}

		}
	}

	private void prepareEfficientContextZeroAccess() {

		for(ContextToken context : contextList) {
			fastAccessContextZeroList.put(context.getTokenValue(), context);
		}
	}

	/**
	 * extractContext():
	 * @param trainData
	 */
	private void extractContext(DataIter trainData) {

		Utils.printLog("\tExtracting Context [", logOut);
		Date startTime = new Date();
		int count = 0;

		for (trainData.startScan(); trainData.hasNext();) {

			//@DMZDebug
			if(++count%100 == 0) {
				Utils.printLog(".", logOut);
			}

			DataSequence seq = trainData.next();
			String[] sequenceList = Utils.convertSequenceToLowerCase(seq, seq.length());

			for (int l = 0; l < seq.length(); l++) {

				//See TagSet in AutoTagger to know better (Beginning, Inside, Last or UnitToken)
				//-- Less then five, because of the POSTag
				if(!supportOnlyEntity || (seq.y(l) != 3 && seq.y(l) < 5)) {
					l = extractContextAroundToken(sequenceList, seq, l);
				}
			}
		}

		//-- Add the context border only after to normalize in some way
		//contextList.addAll(contextListWithPrefixOnly);
		//contextList.addAll(contextListWithSuffixOnly);

		//@DMZDebug
		Utils.printLog("] (" + (int)(((new Date()).getTime() - startTime.getTime())/1000.0)  + "s)", logOut);
		Utils.printLog(" {" + contextListNotNormalized.size()  + " ctxs, ", logOut);
		Utils.printlnLog(2 * contextListWithPrefixOnly.size() + " border cxts}", logOut);
	}

	public void extractContextFromSequence(DataSequence sequence) {

		Utils.printLog("\tExtracting Context [", logOut);
		Date startTime = new Date();
		int count = 0;

		//@DMZDebug
		if(++count%100 == 0) {
			Utils.printLog(".", logOut);
		}

		String[] sequenceLowerCase = Utils.convertSequenceToLowerCase(sequence, sequence.length());

		for (int l = 0; l < sequence.length(); l++) {

			//See TagSet in AutoTagger to know better (Beginning, Inside, Last or UnitToken)
			//-- Less then five, because of the POSTag
			if(!supportOnlyEntity || (sequence.y(l) != 3 && sequence.y(l) < 5)) {
				l = extractContextAroundToken(sequenceLowerCase, sequence, l);
			}
		}

		//-- Add the context border only after to normalize in some way
		//contextList.addAll(contextListWithPrefixOnly);
		//contextList.addAll(contextListWithSuffixOnly);

		//@DMZDebug
		Utils.printLog("] (" + (int)(((new Date()).getTime() - startTime.getTime())/1000.0)  + "s)", logOut);
		Utils.printLog(" {" + contextListNotNormalized.size()  + " ctxs, ", logOut);
		Utils.printlnLog(2 * contextListWithPrefixOnly.size() + " border cxts}", logOut);
	}

	/**
	 * extractContext: Extract the context around the entity
	 *    and assign the prefix and suffix.
	 * @param seq Sequence to extract the context
	 * @param index Position of start of entity
	 * @return The next sequence token
	 */
	private int extractContextAroundToken(String[] sequenceList, DataSequence seq, int index) {

		ContextToken context = new ContextToken(sequenceList[index]);
		int nextTokenIndex;
		int totalOutsideToken = 0;

		//boolean hasPrefix = false;
		//boolean hasSuffix = false;

		String allPrefix = "";
		String allSuffix = "";

		//Add prefix
		for(int i = index - 1; i >= 0 && i >= (index - windowSize); i--) {
			context.addPrefix(new Token(sequenceList[i], seq.y(i)));

			allPrefix += sequenceList[i];
			if(seq.y(i) == 3)
			{
				totalOutsideToken++;
				//hasPrefix = true;
			}
		}

		//Get index of last token of the same entity
		//nextTokenIndex = getLastIndexEntity(seq, index); (Original, changed in 08.10.11e
		nextTokenIndex = index; //-- only to test, consider the entity part as context

		//Add suffix
		for(int i = nextTokenIndex + 1; i < seq.length() && i <= index + windowSize; i++) {
			context.addSuffix(new Token(sequenceList[i], seq.y(i)));

			allSuffix += sequenceList[i];
			if(seq.y(i) == 3)
			{
				totalOutsideToken++;
				//hasSuffix = true;
			}
		}

		if(!existContext(context, contextListNotNormalized)) {
			context.setContextTokenID(contextID);
			context.getToken().setState(seq.y(index));
			context.setKey(START_LEFT_KEY + allPrefix + START_RIGHT_KEY + allSuffix);
			context.setAllPrefix(allPrefix);
			context.setAllSuffix(allSuffix);
			context.setNumberOutsideTokens(totalOutsideToken);

			//if(hasPrefix && hasSuffix)
			contextListNotNormalized.add(context);
			/*else if (hasPrefix)
				contextListWithPrefixOnly.add(context);
			else
				contextListWithSuffixOnly.add(context);*/

			contextID++;
		}

		return (nextTokenIndex);
	}

	/**
	 * normalizeContextList(): Normalize the list
	 * of context removing repeated context and
	 * given the most probable state for a cluster
	 * of similar contexts.
	 */
	private void normalizeContextList(ArrayList<ContextToken> contextListNotNormalized,
			ArrayList<ContextToken> contextListToBeNormalized) {

		//@DMZDebug
		int count = 0;

		ArrayList<ContextToken> similarContextList = new ArrayList<ContextToken>();

		for(ContextToken contextToken : contextListNotNormalized) {

			//@DMZDebug
			if(++count%1000 == 0 && !isSilent) {
				System.out.print(".");
			}

			if(!existContextHashKey(contextToken, contextListToBeNormalized, false)) {
				contextListToBeNormalized.add(contextToken);
			} else {
				similarContextList.add(contextToken);
			}

		}

		defineMostProbableState(contextListToBeNormalized, similarContextList);
	}

	private void normalizeContextZeroList() {

		//@DMZDebug
		int count = 0;

		ArrayList<ContextToken> normalizedContextZeroList = new ArrayList<ContextToken>();
		ArrayList<ContextToken> similarContextList = new ArrayList<ContextToken>();

		//-- Add all tokens to contextZeroList
		addElementsToContextZeroList();

		for(ContextToken contextToken : contextZeroList) {

			//@DMZDebug
			if(++count%1000 == 0 && !isSilent) {
				System.out.print(".");
			}

			if(!existContext(contextToken, normalizedContextZeroList, true)) {
				normalizedContextZeroList.add(contextToken);
			} else {
				similarContextList.add(contextToken);
			}

		}

		defineMostProbableStateForContextZero(normalizedContextZeroList, similarContextList);

		contextZeroList = normalizedContextZeroList;
	}

	private void addElementsToContextZeroList() {

		for(ContextToken contextNotNormalized : contextListNotNormalized) {
			contextZeroList.add(new ContextToken(contextNotNormalized.getToken(), contextID++));
		}
	}

	/**
	 * defineMostProbableState(): Define the
	 * state more probable given a list of
	 * similar context.
	 * @param contextListToAnalyze The context list to analyze
	 */
	private void defineMostProbableState(ArrayList<ContextToken> contextListToAnalyze, ArrayList<ContextToken> contextList) {

		int mostProbableState;
		ArrayList<ContextToken> similarContextList;

		ArrayList<ContextToken> notSureToAddAsContext = new ArrayList<ContextToken>();

		for(ContextToken contextToken : contextListToAnalyze) {

			//System.out.print("\nT:" + contextToken.getTokenValue() + "{");

			similarContextList = getSimilarContextToken(contextToken, contextList);
			similarContextList.add(contextToken);

			mostProbableState = calculateMostProbableState(contextToken, similarContextList, notSureToAddAsContext);

			contextToken.getToken().setState(mostProbableState);

			//contextToken.setWeight(similarContextList.size());
			//System.out.println("Log " + contextToken.getWeight());
		}

		if(dontAddUnsureContext) {
			for(ContextToken contextToRemove : notSureToAddAsContext) {
				contextListToAnalyze.remove(contextToRemove);
			}
		}

	}

	private void defineMostProbableStateForContextZero(ArrayList<ContextToken> contextListToAnalyze, ArrayList<ContextToken> contextList) {

		int mostProbableState;
		ArrayList<ContextToken> similarContextList;
		ArrayList<ContextToken> notSureToAddAsContext = new ArrayList<ContextToken>();

		for(ContextToken contextToken : contextListToAnalyze) {

			similarContextList = getSimilarContextTokenZero(contextToken, contextList);
			similarContextList.add(contextToken);

			mostProbableState = calculateMostProbableState(contextToken, similarContextList, notSureToAddAsContext);
			contextToken.getToken().setState(mostProbableState);

			//contextToken.setWeight(similarContextList.size());
			//System.out.println(contextToken.getTokenValue() + " Log " + contextToken.getWeight());
		}
	}

	/**
	 * calculateMostProbableState(): Calculate
	 * the most probable state for a list of similar context.
	 * @param similarContextList The list of similar context
	 * @return The state most probable
	 */
	private int calculateMostProbableState(ContextToken context, ArrayList<ContextToken> similarContextList,
			ArrayList<ContextToken> notSureToAddAsContext) {

		final int NUMBER_STATES = LabelMap.POSTagPTBR.values().length;
		int [] stateArray = new int [NUMBER_STATES];
		int currentState = -1;
		int mostProbableStateNumber = 0;
		boolean isAmbiguos = false;

		for(ContextToken contextToken : similarContextList) {

			if(currentState != contextToken.getToken().getState() && currentState != -1) {
				isAmbiguos = true;
			}

			currentState = contextToken.getToken().getState();
			stateArray[currentState]++;
			//System.out.print(contextToken.getTokenValue() + "(" + contextToken.getToken().getState() + "), ");

			/** Extra All Possible States in TrainingFile **/
			if(!context.getToken().hasState(currentState)) {
				context.getToken().setStateList(currentState);
			}

		}

		//-- The last contextToken is the context added to be normalized
		if(isAmbiguos) {
			notSureToAddAsContext.add(similarContextList.get(similarContextList.size()-1));
		}

		//System.out.print("} TotalStates[");

		//@DMZDebug
		//for(int i = 0; i < NUMBER_STATES-1; i++)
		//System.out.print(stateArray[i] + ",");

		//System.out.print(stateArray[NUMBER_STATES-1]+ "]");

		for(int i = 0; i < NUMBER_STATES - 1; i++) {
			if(stateArray[mostProbableStateNumber] < stateArray[i+1]) {
				mostProbableStateNumber = i+1;
			}
		}

		return(mostProbableStateNumber);
	}

	/**
	 * existContextZeroInSequence():
	 * @param token
	 * @return
	 */
	public ContextToken existContextZeroInSequence(String token) {

		ContextToken contextZero = null;

		for(ContextToken context : contextZeroList) {
			if(context.getTokenValue().equals(token)) {
				contextZero = context;
				break;
			}
		}

		return(contextZero);
	}

	//@Fast --> existContextZeroInSequence(String token)
	public ContextToken existContextZeroInSequenceContextZeroHashMap(String token) {

		ContextToken contextZero = fastAccessContextZeroList.get(token);
		return(contextZero);
	}

	/**
	 * existContext(): Check if exist context
	 * already added in contextList. This method
	 * simply verify if the token was added
	 * before by checking the prefix, suffix and
	 * the current token analyzed in the contextList.
	 * @param tokenToAnalyze
	 * @return True if exist in the contextToken list, and No otherwise
	 */
	private boolean existContext(ContextToken tokenToAnalyze, ArrayList<ContextToken> contextList) {
		return (existContext(tokenToAnalyze, contextList, true));//-- normal is true
	}

	/**
	 * existContext(): Check if exist context
	 * already added in contextList. This method
	 * simply verify if the token was added
	 * before by checking the prefix, suffix and
	 * the current token analyzed in the contextList.
	 * @param tokenToAnalyze
	 * @param contextList ContextToken list to analyze
	 * @param considerMainToken If true the method consider the main token (the observed token)
	 * and comparer if the main token matches with the tokenValue of the context analyzed before compare
	 * the prefix and suffix. Otherwise, if not, the method only compare the prefix and suffix.
	 * @return True if exist in the contextToken list, and No otherwise
	 */
	private boolean existContext(ContextToken tokenToAnalyze, ArrayList<ContextToken> contextList , boolean considerMainToken) {

		boolean existContextInContextList = false;

		for(ContextToken token : contextList) {

			if(!considerMainToken || token.getTokenValue().equals(tokenToAnalyze.getTokenValue())) {

				existContextInContextList = (tokenToAnalyze.getPrefixSize() == token.getPrefixSize());

				if(existContextInContextList) {
					for(int i = 0; i < tokenToAnalyze.getPrefixSize(); i++) {
						if(!(token.getPrefix(i).getValue()).equals(tokenToAnalyze.getPrefix(i).getValue())) {
							existContextInContextList = false;
							break;
						}
					}
				}

				existContextInContextList &= tokenToAnalyze.getSuffixSize() == token.getSuffixSize();

				if(existContextInContextList) {
					for(int i = 0; i < tokenToAnalyze.getSuffixSize(); i++) {
						if(!(token.getSuffix(i).getValue()).equals(tokenToAnalyze.getSuffix(i).getValue())) {
							existContextInContextList = false;
							break;
						}
					}

					//-- Code optimization
					if(existContextInContextList) {
						return(existContextInContextList);
					}
				}
			}
		}

		return(existContextInContextList);
	}

	private boolean existContextHashKey(ContextToken tokenToAnalyze, ArrayList<ContextToken> contextList , boolean considerMainToken) {
		boolean existContextInContextList = false;

		for(ContextToken token : contextList) {
			if((!considerMainToken || token.getTokenValue().equals(tokenToAnalyze.getTokenValue())) &&
					token.getKey().equals(tokenToAnalyze.getKey())) {
				existContextInContextList = true;
				break;
			}
		}

		return(existContextInContextList);
	}

	//@Fast --> existContext(ContextToken tokenToAnalyze, ArrayList<ContextToken> contextList , boolean considerMainToken)
	private boolean existContextInContextHashMap(ContextToken tokenToAnalyze, boolean considerMainToken) {

		boolean existContextInContextList = false;
		ContextToken context = fastAccessContextList.get(tokenToAnalyze.getKey());

		if(context != null &&
				(!considerMainToken || context.getTokenValue().equals(tokenToAnalyze.getTokenValue()))) {
			existContextInContextList = true;
		}

		return(existContextInContextList);
	}

	//@Fast --> existContext(ContextToken tokenToAnalyze, ArrayList<ContextToken> contextList , boolean considerMainToken)
	private boolean existContextInContextHashMap(ContextToken tokenToAnalyze, boolean considerMainToken,
			HashMap<String, ContextToken> fastAccessContextList) {

		boolean existContextInContextList = false;
		ContextToken context = fastAccessContextList.get(tokenToAnalyze.getKey());

		if(context != null &&
				(!considerMainToken || context.getTokenValue().equals(tokenToAnalyze.getTokenValue()))) {
			existContextInContextList = true;
		}

		return(existContextInContextList);
	}

	public ContextToken existContextInSequencePrefixExtended(String[] seq, int index) {

		ContextToken context = existContextInSequence(seq, index);

		if(context == null) {
			context = existPrefixInSequence(seq, index);
		}

		return(context);
	}

	//@Fast --> existContextInSequencePrefixExtended(String[] seq, int index)
	public ContextToken existContextInSequencePrefixExtendedHashMap(String[] seq, int index) {

		ContextToken context = existContextInSequence(seq, index);

		if(context == null) {
			context = existPrefixInSequencePrefixContextHashMap(seq, index);
		}

		return(context);
	}

	public ContextToken existContextInSequenceFullyExtended(String[] seq, int index) {

		ContextToken context = existContextInSequencePrefixExtended(seq, index);

		if(context == null) {
			context = existSuffixInSequence(seq, index);
		}

		return(context);
	}

	//@Fast --> existContextInSequenceFullyExtended(String[] seq, int index)
	public ContextToken existContextInSequenceFullyExtendedHashMap(String[] seq, int index) {

		ContextToken context = existContextInSequencePrefixExtended(seq, index);

		if(context == null) {
			context = existSuffixInSequenceSuffixContextHashMap(seq, index);
		}

		return(context);
	}

	// -1 false, otherwise true
	public ContextToken existContextInSequence(String[] seq, int index) {

		ContextToken contextTokenToReturn = null;

		//Considerando só para BILOU por enquanto; 3 = Outside
		//if(seq.y(index) != 3) {
		for(ContextToken contextToken : contextList) {
			if(isContextEquals(seq, index, contextToken)) {
				contextTokenToReturn = contextToken;
				break;
			}
		}
		//}
		return(contextTokenToReturn);
	}

	//@Fast --> existContextInSequence(String[] seq, int index)
	public ContextToken existContextInSequenceContextHashMap(String[] seq, int index) {

		//Considerando só para BILOU por enquanto; 3 = Outside
		//if(seq.y(index) != 3) {

		String keyWithMainToken = generateKeyFromSequence(seq, index, true);
		ContextToken contextTokenToReturn = fastAccessContextListConsiderMainToken.get(keyWithMainToken);

		if(contextTokenToReturn == null) {
			String key = generateKeyFromSequence(seq, index, false);
			contextTokenToReturn = fastAccessContextList.get(key);
		}

		//}
		return(contextTokenToReturn);
	}

	public String generateContextWithoutPOSTagTokenKey(ContextToken context) {
		String key = "";

		for(int i = 0; i < context.getPrefixSize(); i++) {
			if(!posTagger.isTermPOSTag(context.getPrefixIndexValue(i))) {
				key += context.getPrefixIndexValue(i) + DIVISER;
			}
		}

		for(int i = 0; i < context.getSuffixSize(); i++) {
			if(!posTagger.isTermPOSTag(context.getSuffixIndexValue(i))) {
				key += context.getSuffixIndexValue(i) + DIVISER;
			}
		}

		return(key);
	}

	public String generateSequenceWithoutPOSTagTokenKey(String[] sequence, int index) {

		String key = "";

		for(int i = index - 1; i >= 0 && i >= (index - windowSize); i--) {
			if(!posTagger.isTermPOSTag(sequence[i])) {
				key += sequence[i] + DIVISER;
			}
		}

		for(int i = index + 1; i < sequence.length && i <= index + windowSize; i++) {
			if(!posTagger.isTermPOSTag(sequence[i])) {
				key += sequence[i] + DIVISER;
			}
		}

		return(key);
	}

	public String generateKeyFromSequence(String [] sequenceList, int index, boolean considerMainToken) {
		String prefix = START_LEFT_KEY;
		String suffix = START_RIGHT_KEY;

		//Add prefix
		for(int i = index - 1; i >= 0 && i >= (index - windowSize); i--) {
			prefix += sequenceList[i];
		}

		if(considerMainToken) {
			prefix += START_RIGHT_KEY + sequenceList[index];
		}

		//Add suffix
		for(int i = index + 1; i < sequenceList.length && i <= index + windowSize; i++) {
			suffix += sequenceList[i];
		}

		return(prefix + suffix);
	}

	public String generateLeftPrefixKeyFromSequence(String [] sequenceList, int index, boolean considerMainToken) {
		String prefix = START_LEFT_KEY;

		//Add prefix
		for(int i = index - 1; i >= 0 && i >= (index - windowSize); i--) {
			prefix += sequenceList[i];
		}

		return(prefix + START_RIGHT_KEY + ((considerMainToken)? sequenceList[index] + START_RIGHT_KEY:""));
	}

	public String generateLeftPrefixPOSTagKeyFromSequence(DataSequence sequence, int index) {
		String prefix = START_LEFT_KEY;

		//Add prefix
		for(int i = index - 1; i >= 0 && i >= (index - windowSize); i--)
		{
			if(!isTest ||(sequence.y(i) != 0 && sequence.y(i) != 1 && sequence.y(i) != 2 && sequence.y(i) != 4)) {
				prefix += sequence.y(i) + START_LEFT_KEY;
			}
			else {
				prefix += 3 + START_LEFT_KEY; //Outside if is test file and is entity
			}
		}

		return(prefix + START_RIGHT_KEY);
	}

	public String generatePOSTagKeyFromSequence(DataSequence sequence, int index) {

		String prefix = START_LEFT_KEY;
		String suffix = START_RIGHT_KEY;

		//Add prefix
		for(int i = index - 1; i >= 0 && i >= (index - windowSize); i--)
		{
			if(!isTest || (sequence.y(i) != 0 && sequence.y(i) != 1 && sequence.y(i) != 2 && sequence.y(i) != 4)) {
				prefix += sequence.y(i) + START_LEFT_KEY;
			}
			else {
				prefix += 3 + START_LEFT_KEY; //Outside if is test file and is entity
			}
		}

		//Add suffix
		for(int i = index + 1; i < sequence.length() && i <= index + windowSize; i++)
		{
			if(!isTest || (sequence.y(i) != 0 && sequence.y(i) != 1 && sequence.y(i) != 2 && sequence.y(i) != 4)) {
				suffix += sequence.y(i) + START_RIGHT_KEY;
			}
			else {
				suffix += 3 + START_RIGHT_KEY; //Outside if is test file and is entity
			}
		}

		return(prefix + suffix);
	}

	private String generateLeftPrefixPOSTagKeyFromContext(ContextToken context) {
		String prefix = START_LEFT_KEY;
		int contextPrefixState = -1;

		//Add prefix
		for(int i = 0; i < context.getPrefixSize() && i < windowSize; i++) {

			contextPrefixState = context.getPrefix(i).getState();

			if(!isTest || (contextPrefixState != 0 && contextPrefixState != 1 && contextPrefixState != 2 && contextPrefixState != 4)) {
				prefix += context.getPrefix(i).getState() + START_LEFT_KEY;
			} else {
				prefix += 3 + START_LEFT_KEY;
			}
		}

		return(prefix + START_RIGHT_KEY);
	}

	private String generatePOSTagKeyFromContext(ContextToken context) {
		String prefix = START_LEFT_KEY;
		String suffix = START_RIGHT_KEY;
		int contextPrefixState = -1;
		int contextSuffixState = -1;

		//Add prefix
		for(int i = 0; i < context.getPrefixSize() && i < windowSize; i++) {

			contextPrefixState = context.getPrefix(i).getState();

			if(!isTest || (contextPrefixState != 0 && contextPrefixState != 1 && contextPrefixState != 2 && contextPrefixState != 4)) {
				prefix += context.getPrefix(i).getState() + START_LEFT_KEY;
			} else {
				prefix += 3 + START_LEFT_KEY;
			}
		}

		for(int i = 0; i < context.getSuffixSize() && i < windowSize; i++) {

			contextSuffixState = context.getSuffix(i).getState();

			if(!isTest || (contextSuffixState != 0 && contextSuffixState != 1 && contextSuffixState != 2 && contextSuffixState != 4)) {
				suffix += context.getSuffix(i).getState() + START_RIGHT_KEY;
			} else {
				suffix += 3 + START_RIGHT_KEY;
			}
		}

		return(prefix + suffix);
	}

	public String generateRightKeyFromSequence(String [] sequenceList, int index, boolean considerMainToken) {
		String suffix = START_RIGHT_KEY;

		//Add suffix
		for(int i = index + 1; i < sequenceList.length && i <= index + windowSize; i++) {
			suffix += sequenceList[i];
		}

		return(START_LEFT_KEY + ((considerMainToken)? START_RIGHT_KEY + sequenceList[index]:"") + suffix);
	}

	public ContextToken existContextInSequenceRestricted(String[] seq, int index) {

		ContextToken contextTokenToReturn = null;

		//Considerando só para BILOU por enquanto; 3 = Outside
		//if(seq.y(index) != 3) {
		for(ContextToken contextToken : contextList) {
			if(contextToken.getPrefixSize() > 0 && contextToken.getSuffixSize() > 0 && isContextEquals(seq, index, contextToken)) {
				contextTokenToReturn = contextToken;
				break;
			}
		}
		//}
		return(contextTokenToReturn);
	}

	public ContextToken existPrefixInSequence(String[] seq, int index) {
		ContextToken context = null;

		for(ContextToken contextToken : contextList) {
			if(isPrefixEquals(seq, index, contextToken)) {
				context = contextToken;
				break;
			}
		}

		return(context);
	}

	//@Fast --> existPrefixInSequence(String[] seq, int index)
	public ContextToken existPrefixInSequencePrefixContextHashMap(String[] seq, int index) {

		ContextToken context = fastAccessPrefixContextList.get(generateLeftPrefixKeyFromSequence(seq, index, false));

		return(context);
	}

	public ContextToken existPrefixInSequenceRestricted(String[] seq, int index) {
		ContextToken context = null;

		for(ContextToken contextToken : contextList) {
			if(contextToken.getPrefixSize() > 0 && contextToken.getSuffixSize() >= 0 && isPrefixEquals(seq, index, contextToken)) {
				context = contextToken;
				break;
			}
		}

		return(context);
	}

	//@Fast --> existPrefixInSequenceRestricted(String[] seq, int index)
	public ContextToken existPrefixInSequenceRestrictedPrefixContextHashMap(String[] seq, int index) {


		ContextToken context = fastAccessPrefixContextListConsiderMainToken.get(generateLeftPrefixKeyFromSequence(seq, index, true));

		if(context == null) {
			context = fastAccessPrefixContextList.get(generateLeftPrefixKeyFromSequence(seq, index, false));
		}

		return(context);
	}

	public ContextToken existPrefixInSequenceFullRestrictedPrefixContextHashMap(String[] seq, int index) {


		ContextToken context = fastAccessPrefixContextListConsiderMainToken.get(generateLeftPrefixKeyFromSequence(seq, index, true));

		return(context);
	}

	public ContextToken existSuffixInSequence(String[] seq, int index) {
		ContextToken context = null;

		for(ContextToken contextToken : contextList) {
			if(isSuffixEquals(seq, index, contextToken)) {
				context = contextToken;
				break;
			}
		}

		return(context);
	}

	//@Fast --> existSuffixInSequence(String[] seq, int index)
	public ContextToken existSuffixInSequenceSuffixContextHashMap(String[] seq, int index) {
		ContextToken context = fastAccessSuffixContextList.get(generateRightKeyFromSequence(seq, index, false));
		return(context);
	}

	public ContextToken existSuffixInSequenceRestricted(String[] seq, int index) {
		ContextToken context = null;

		for(ContextToken contextToken : contextList) {
			if(contextToken.getPrefixSize() >= 0 && contextToken.getSuffixSize() > 0 && isSuffixEquals(seq, index, contextToken)) {
				context = contextToken;
				break;
			}
		}

		return(context);
	}

	//@Fast --> existSuffixInSequenceRestricted(String[] seq, int index)
	public ContextToken existSuffixInSequenceRestrictedSuffixContextHashMap(String[] seq, int index) {

		ContextToken context = fastAccessSuffixContextListConsiderMainToken.get(generateRightKeyFromSequence(seq, index, true));

		if(context == null) {
			context = fastAccessSuffixContextList.get(generateRightKeyFromSequence(seq, index, false));
		}

		return(context);
	}

	public ContextToken existSuffixInSequenceFullRestrictedSuffixContextHashMap(String[] seq, int index) {

		ContextToken context = fastAccessSuffixContextListConsiderMainToken.get(generateRightKeyFromSequence(seq, index, true));

		return(context);
	}


	public boolean isContextEquals(ContextToken cTA, ContextToken cTB) {
		return(isPrefixEquals(cTA, cTB) && isSuffixEquals(cTA, cTB));
	}

	private boolean isContextEquals(String[] seq, int index, ContextToken contextToken) {

		return(isPrefixEquals(seq, index, contextToken) && isSuffixEquals(seq, index, contextToken));
	}

	public boolean isPrefixEquals (ContextToken cTA, ContextToken cTB) {

		boolean isPrefixEquals = (cTA.getPrefixSize() == cTB.getPrefixSize());

		if(isPrefixEquals) {
			for(int i = 0; i < cTA.getPrefixSize(); i++) {
				if(!cTA.getPrefix(i).getValue().equals(cTB.getPrefix(i).getValue())) {
					isPrefixEquals = false;
					break;
				}
			}
		}

		return(isPrefixEquals);
	}

	private boolean isPrefixEquals(String[] seq, int index, ContextToken contextToken) {

		String prefix = "";
		int prefixSize = contextToken.getPrefixSize();
		boolean isPrefixEquals = (index >= prefixSize); //&&
		//((contextToken.getPrefixSize() == 0 && index == 0) || (contextToken.getPrefixSize() > 0));

		if(isPrefixEquals) {
			for(int i = 0; i < prefixSize; i++) {

				prefix = seq[index - (1 + i)];

				if(!prefix.equals(contextToken.getPrefixIndexValue(i))) {
					isPrefixEquals = false;
					break;
				}
			}
		}

		return(isPrefixEquals);
	}

	public boolean isSuffixEquals(ContextToken cTA, ContextToken cTB) {

		boolean isSuffixEquals = (cTA.getSuffixSize() == cTB.getSuffixSize());

		if(isSuffixEquals) {
			for(int i = 0; i < cTA.getSuffixSize(); i++) {
				if(!cTA.getSuffix(i).getValue().equals(cTB.getSuffix(i).getValue())) {
					isSuffixEquals = false;
					break;
				}
			}
		}

		return(isSuffixEquals);
	}

	private boolean isSuffixEquals(String[] seq, int index, ContextToken contextToken) {

		String suffix = "";
		int suffixSize = contextToken.getSuffixSize();
		boolean isSuffixEquals = (((seq.length - 1) - index) >= suffixSize);// &&
		//((suffixSize == 0 && index + 1 == seq.length) || (suffixSize > 0));

		if(isSuffixEquals) {
			for(int i = 0; i < suffixSize; i++) {

				suffix = seq[index + (1 + i)];

				if(!suffix.equals(contextToken.getSuffixIndexValue(i)))  {
					isSuffixEquals = false;
					break;
				}
			}
		}

		return(isSuffixEquals);
	}

	/**
	 * getLastTokenEntityIndex: Get index of last token of the same entity
	 * 	(e.g. Vasco/0 da/1 Gama/2 return index 2).
	 * @param seq Sequence to extract the context
	 * @param index Position of start of entity
	 * @return The next sequence token
	 */
	private int getLastIndexEntity(DataSequence seq, int index) {

		int nextTokenIndex = index;

		while(nextTokenIndex + 1 < seq.length() && isNextTokenPartOfEntity(seq, nextTokenIndex)) {
			nextTokenIndex++;
		}

		return nextTokenIndex;
	}

	public ContextToken getContextToken(int index) {
		return(contextList.get(index));
	}

	public ContextToken getContextTokenByID(int id) {
		for(int i = 0; i < contextList.size(); i++) {
			if(contextList.get(i).getContextTokenID() == id) {
				return(contextList.get(i));
			}
		}

		return(null);
	}

	public HashMap<String, ContextToken> getFastAccessContextList() {
		return fastAccessContextList;
	}

	public HashMap<String, ContextToken> getFastAccessPrefixContextList() {
		return fastAccessPrefixContextList;
	}

	public HashMap<String, ContextToken> getFastAccessSuffixContextList() {
		return fastAccessSuffixContextList;
	}

	public HashMap<String, ContextToken> getFastAccessContextListNotNormalized() {
		return fastAccessContextListNotNormalized;
	}

	public ArrayList<ContextToken> getContextListNotNormalized() {
		return contextListNotNormalized;
	}

	public HashMap<String, ContextToken> getFastAccessPrefixPosTagList() {
		return fastAccessPrefixPosTagList;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	/**
	 * isNextTokenPartOfEntity: Verify if the current token
	 *    is the part of entity.
	 * @param seq Sequence to extract the context
	 * @param index Position of start of entity
	 * @return Answer true if the token is part and false otherwise
	 */
	private boolean isNextTokenPartOfEntity(DataSequence seq, int index) {
		boolean answer = false;

		if(tagSet.equals("IO")) {
			answer = (seq.y(index + 1) == 0);
		} else if(tagSet.equals("BIO")) {
			answer = (seq.y(index + 1) == 1);
		} else if(tagSet.equals("BILOU")) {
			answer = (seq.y(index + 1) == 1 || seq.y(index + 1) == 2);
		}

		return(answer);
	}

	/**
	 * getSimilarContextToken(): Create a list of
	 * similar contextToken through the given contextToken.
	 * @param contextTokenToFind The context token given to find
	 * the others similar
	 * @param contextList The context list that will be analyzed
	 * @return A list of similar contextToken
	 */
	public ArrayList<ContextToken> getSimilarContextToken(ContextToken contextTokenToFind, ArrayList<ContextToken> contextList) {

		ArrayList<ContextToken> similarContextList = new ArrayList<ContextToken>();

		for(ContextToken contextToken : contextList) {
			if(isContextEquals(contextTokenToFind, contextToken) && !contextTokenToFind.equals(contextToken)) {
				similarContextList.add(contextToken);
			}
		}

		return(similarContextList);
	}

	public ArrayList<ContextToken> getSimilarContextTokenZero(ContextToken contextTokenToFind, ArrayList<ContextToken> contextList) {

		ArrayList<ContextToken> similarContextList = new ArrayList<ContextToken>();

		for(ContextToken contextToken : contextList) {
			if(contextTokenToFind.getTokenValue().equals(contextToken.getTokenValue())) {
				similarContextList.add(contextToken);
			}
		}

		return(similarContextList);
	}

	public void removeContextToken(ContextToken contextToRemove) {
		contextList.remove(contextToRemove);
	}

	public ArrayList<ContextToken> getDifferentSequenceWithSameContextToken(ContextToken contextToken) {

		ArrayList<ContextToken> contextTokenList = new ArrayList<ContextToken>();

		for(ContextToken contextTokenB : contextList) {
		}

		return(contextTokenList);
	}

	/**
	 * findContextTokenByID(): Find
	 * contextToken object by given ID.
	 * @param contextID The ID of contextToken
	 * @return ContextToken object if found or null otherwise
	 */
	public ContextToken findContextTokenByID(int contextID) {

		ContextToken contextToFind = null;

		for(ContextToken contextToken : contextList) {
			if(contextToken.getContextTokenID() == contextID) {
				contextToFind = contextToken;
				break;
			}
		}

		return(contextToFind);
	}

	public int findContextIndex(ContextToken contextTokenToFind) {

		int index = -1;

		for(int i = 0; i < 0; i++) {
			if(isContextEquals(contextTokenToFind, contextList.get(i))) {
				index = i;
				break;
			}
		}

		return(index);
	}

	public int isContextMostValuableWords(String[] sequence, int index, double threshould) {

		int idContext = -1;
		double correctHits = 0;
		ArrayList<String> termList = getTermsNotPOSTag(sequence, index);

		for(HashMap<String, Integer> contextMap : contextPosition) {

			correctHits = 0;
			idContext = -1;

			for(String term : termList) {
				if(contextMap.containsKey(term)) {
					correctHits++;
					idContext = contextMap.get(term);
				}
			}

			if(correctHits/contextMap.size() >= threshould) {
				break;
			}
		}

		return(idContext);
	}

	protected ArrayList<String> getTermsNotPOSTag(String [] sequence, int index) {

		ArrayList<String> termList = new ArrayList<String> ();
		int startIndex = (index - windowSize > 0)? index - windowSize : 0;
		int endIndex = (index + windowSize < sequence.length)? index + windowSize : sequence.length - 1;

		for(int i = startIndex; i <= endIndex; i++) {
			if(!posTagger.isTermPOSTag(sequence[i]) && i != index) {
				termList.add(sequence[i]);
			}
		}

		return(termList);
	}

	public HashMap<String, ContextToken> getFastAccessContextWithoutPOSTagToken() {
		return(fastAccessContextWithoutPOSTagToken);
	}

	/**
	 * getContextList(): Receive the
	 * context list created thought
	 * the training file.
	 * @return Return the context list
	 */
	public ArrayList<ContextToken> getContextList() {
		return contextList;
	}

	public int getContextListSize() {
		return contextList.size();
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public String getInputFilenameAddress() {
		return inputFilenameAddress;
	}

	public void setInputFilenameAddress(String inputFilenameAddress) {
		this.inputFilenameAddress = inputFilenameAddress;
	}

	public ArrayList<HashMap<String, Integer>> getContextPosition() {
		return(contextPosition);
	}

	/**
	 * readSupportContextObject(): Read SupportContext
	 * serializable object from a input file
	 * @param filename The name of the inputfile
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void readSupportContextObject(String filename, SupportContext target) throws IOException, ClassNotFoundException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		SupportContext supportContext = (SupportContext) in.readObject();
		cloneSupportContext(target, supportContext);

		in.close();
	}

	/**
	 * cloneSupportContext(): Clone support
	 * context object to not lose the
	 * reference.
	 * @param target The object to receive the cloned attributes
	 * @param clone The object to be cloned
	 */
	private void cloneSupportContext(SupportContext target, SupportContext clone) {

		target.windowSize = clone.windowSize;
		target.contextID = clone.contextID;
		target.tagSet = clone.tagSet;

		target.contextList = clone.contextList;
		target.contextListWithPrefixOnly = clone.contextListWithPrefixOnly;
		target.contextListWithSuffixOnly = clone.contextListWithSuffixOnly;
		target.contextZeroList = clone.contextZeroList;

		target.contextPosition = clone.contextPosition;
		target.fastAccessContextWithoutPOSTagToken = clone.fastAccessContextWithoutPOSTagToken;

		target.fastAccessContextList = clone.fastAccessContextList;
		target.fastAccessPrefixContextList = clone.fastAccessPrefixContextList;
		target.fastAccessSuffixContextList = clone.fastAccessSuffixContextList;
		target.fastAccessContextZeroList = clone.fastAccessContextZeroList;

		target.fastAccessContextListConsiderMainToken = clone.fastAccessContextListConsiderMainToken;
		target.fastAccessPrefixContextListConsiderMainToken = clone.fastAccessPrefixContextListConsiderMainToken;
		target.fastAccessSuffixContextListConsiderMainToken = clone.fastAccessSuffixContextListConsiderMainToken;
	}

	/**
	 * writeSupportContextObject(): Serializable
	 * method to write the object in the output file
	 * @param filename The output filename.
	 * @throws IOException
	 */
	public void writeSupportContextObject(String filename) throws IOException {

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));

		out.writeObject(this);
		out.flush();
		out.close();
	}

	/**
	 * generateSupportContextFeatureFilename(): Generate
	 * the feature filename for the supportContext.
	 * @param filename The standard name for the
	 * feature filename
	 * @return The support context feature filename
	 */
	public String generateSupportContextFeatureFilename(String filename) {

		inputFilenameAddress = filename;

		int endIndexFilename = filename.lastIndexOf(".");

		String supportContextFilename = filename.substring(0, endIndexFilename);
		supportContextFilename += "-" + TAG_SUPPORT_CONTEXT;
		supportContextFilename += filename.substring(endIndexFilename);

		return(supportContextFilename);
	}

	public void setSilent() {
		isSilent = true;
	}

	public void removeSilent() {
		isSilent = false;
	}

}
