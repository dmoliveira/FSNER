package lbd.Model;

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

import lbd.Utils.Utils;
import iitb.CRF.DataIter;
import iitb.CRF.DataSequence;

/**
 * SupportContext: This class aims to provide context
 *  (prefix and suffix) for each entity found in the 
 *  training file. In case of composite entities, they
 *   must be construed as if was a string only.
 * @author DMZ
 */
public class SupportContext_Optimized implements Serializable {
	
	private static final long serialVersionUID = -1153030366689839065L;

	//-- The TAG for the feature save name
	private static final String TAG_SUPPORT_CONTEXT = "SupCxt";
	
	//-- Only for @DMZDebug
	public boolean isTest;
	
	//-- Quantity token prefix and suffix in each side
	private int windowSize = 1;
	private int contextID = 0;
	
	private boolean supportOnlyEntity;
	
	//-- TagSet used {IO, BIO or BILOU}
	private String tagSet;
	
	//-- List of contextCopyOfSupportContext
	private ArrayList<ContextToken> contextList;
	private ArrayList<ContextToken> contextListNotNormalized;
	private ArrayList<ContextToken> contextListWithSuffixOnly;
	private ArrayList<ContextToken> contextListWithPrefixOnly;
	private ArrayList<ContextToken> contextZeroList;
	private ArrayList<ContextToken> outsideList;
	
	private float weight;
	
	private HashMap<String, ContextToken> fastAccessContextList;
	private HashMap<String, ContextToken> fastAccessContextListNotNormalized;
	private HashMap<String, ContextToken> fastAccessPrefixContextList;
	private HashMap<String, ContextToken> fastAccessSuffixContextList;
	private HashMap<String, ContextToken> fastAccessContextZeroList;
	public static final String START_LEFT_KEY = "_";
	public static final String START_RIGHT_KEY = "_";
	
	private Writer logOut;
	
	/**
	 * SupportContext (Constructor)
	 * @param windowSize Quantity token prefix and suffix in each side
	 * @param tagSet the code name of tag set (IO, BIO or BILOU)
	 * @param supportOnlyEntites If the contexts created only considerer entities
	 */
	public SupportContext_Optimized(int windowSize, String tagSet, boolean supportOnlyEntites) {
		this(windowSize, tagSet);
		this.supportOnlyEntity = supportOnlyEntites;
	}
	
	public SupportContext_Optimized(int windowSize, String tagSet, boolean supportOnlyEntites, Writer out) {
		this(windowSize, tagSet);
		this.supportOnlyEntity = supportOnlyEntites;
		logOut = out;
	}
	
	/**
	 * SupportContext (Constructor)
	 * @param windowSize Quantity token prefix and suffix in each side
	 * @param tagSet the code name of tag set (IO, BIO or BILOU)
	 */
	public SupportContext_Optimized(int windowSize, String tagSet) {
		this(tagSet);
		this.windowSize = windowSize;
	}
	
   /**
    * SupportContext (Constructor)
	* @param tagSet the code name of tag set (IO, BIO or BILOU)
	*/
	public SupportContext_Optimized(String tagSet) {
		contextList = new ArrayList<ContextToken> ();
		contextListWithSuffixOnly = new ArrayList<ContextToken>();
		contextListWithPrefixOnly = new ArrayList<ContextToken>();
		contextZeroList = new ArrayList<ContextToken> ();
		fastAccessContextList = new HashMap<String, ContextToken>();
		fastAccessPrefixContextList = new HashMap<String, ContextToken>();
		fastAccessSuffixContextList = new HashMap<String, ContextToken>();
		fastAccessContextZeroList = new HashMap<String, ContextToken>();
		outsideList = new ArrayList<ContextToken> ();
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
    	prepareEfficientContextZeroAccess();
		
    	//-- Normalize the normal context (context, prefix and suffix list)
		normalizeContext();
		prepareEfficientContextAccess();
    	
    	Utils.printlnLog("Finished Generate Context ("+ (contextList.size() + contextZeroList.size()) +" cxts)\n", logOut);    	
    	if(logOut != null) try { logOut.flush(); } catch (IOException e) { e.printStackTrace();	}
	}
	
	private void normalizeContext() {
		
		Utils.printLog("\tNormalizing Context List [", logOut);
		Date startTime = new Date();
		
		//-- Normalize the contextList	
    	normalizeContextList(contextList);
    	
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
			fastAccessContextList.put(context.getKey(), context);
			
			if(context.getPrefixSize() > 0)
				fastAccessPrefixContextList.put(START_LEFT_KEY + context.getAllPrefix() + START_RIGHT_KEY, context);
			
			if(context.getSuffixSize() > 0)
				fastAccessSuffixContextList.put(START_LEFT_KEY + START_RIGHT_KEY + context.getAllSuffix(), context);
		}
		
		for(ContextToken contextNotNormalized : contextListNotNormalized)
			fastAccessContextListNotNormalized.put(contextNotNormalized.getKey(), contextNotNormalized);
	}
	
	private void prepareEfficientContextZeroAccess() {
		
		for(ContextToken context : contextList) 			
			fastAccessContextZeroList.put(context.getTokenValue(), context);
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
			if(++count%100 == 0)
				Utils.printLog(".", logOut);
			
		    DataSequence seq = trainData.next();
		    String[] sequenceList = Utils.convertSequenceToLowerCase(seq, seq.length());
		    
		    for (int l = 0; l < seq.length(); l++) {
		    	
		    	//See TagSet in AutoTagger to know better (Inside, Beginning or UnitToken)
		    	if(!supportOnlyEntity || seq.y(l) != 3)   	
		    		l = extractContextAroundToken(sequenceList, seq, l);
		    }
		}
		
		//-- Add the context border only after to normalize in some way
		//contextList.addAll(contextListWithPrefixOnly);
		//contextList.addAll(contextListWithSuffixOnly);
		
		//@DMZDebug
		Utils.printLog("] (" + (int)(((new Date()).getTime() - startTime.getTime())/1000.0)  + "s)", logOut);
		Utils.printLog(" {" + contextList.size()  + " ctxs, ", logOut);
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
		
		//boolean hasPrefix = false;
		//boolean hasSuffix = false;
		
		String allPrefix = "";
		String allSuffix = "";
		
		//Add prefix
		for(int i = index - 1; i >= 0 && i >= (index - windowSize); i--) {
			context.addPrefix(new Token(sequenceList[i], seq.y(i)));
			
			allPrefix += sequenceList[i];
			//hasPrefix = true;
		}
		
		//Get index of last token of the same entity
		//nextTokenIndex = getLastIndexEntity(seq, index); (Original, changed in 08.10.11e
		nextTokenIndex = index; //-- only to test, consider the entity part as context
		
		//Add suffix
		for(int i = nextTokenIndex + 1; i < seq.length() && i <= index + windowSize; i++) {
			context.addSuffix(new Token(sequenceList[i], seq.y(i)));
			
			allSuffix += sequenceList[i];
			//hasSuffix = true;
		}
		
		if(!existContext(context)) {			
			context.setContextTokenID(contextID);
			context.getToken().setState(seq.y(index));
			context.setKey(START_LEFT_KEY + allPrefix + START_RIGHT_KEY + allSuffix);
			context.setAllPrefix(allPrefix);
			context.setAllSuffix(allSuffix);
			
			//if(hasPrefix && hasSuffix)
				contextList.add(context);
			/*else if (hasPrefix)
				contextListWithPrefixOnly.add(context);
			else
				contextListWithSuffixOnly.add(context);*/
			
			contextID ++;
		}		
		
		return (nextTokenIndex);
	}
	
	/**
	 * normalizeContextList(): Normalize the list
	 * of context removing repeated context and
	 * given the most probable state for a cluster
	 * of similar contexts.
	 */
	private void normalizeContextList(ArrayList<ContextToken> contextList) {
		
		//@DMZDebug
		int count = 0;
		
		ArrayList<ContextToken> normalizedContextList = new ArrayList<ContextToken>();
		ArrayList<ContextToken> similarContextList = new ArrayList<ContextToken>();
		
		for(ContextToken contextToken : contextList) {
			
			//@DMZDebug
			if(++count%1000 == 0)
				System.out.print(".");
			
			if(!existContextHashKey(contextToken, normalizedContextList, false))
				normalizedContextList.add(contextToken);
			else
				similarContextList.add(contextToken);
				
		}
		
		defineMostProbableState(normalizedContextList, similarContextList);
		
		contextListNotNormalized = contextList;
		contextList = normalizedContextList;
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
			if(++count%1000 == 0)
				System.out.print(".");
			
			if(!existContext(contextToken, normalizedContextZeroList, true))
				normalizedContextZeroList.add(contextToken);
			else
				similarContextList.add(contextToken);
				
		}
		
		defineMostProbableStateForContextZero(normalizedContextZeroList, similarContextList);
		
		contextZeroList = normalizedContextZeroList;
	}
	
	private void addElementsToContextZeroList() {
		
		for(ContextToken context : contextList)
			contextZeroList.add(new ContextToken(context.getToken(), ++contextID));
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
		
		for(ContextToken contextToken : contextListToAnalyze) {
			
			similarContextList = getSimilarContextToken(contextToken, contextList);
			similarContextList.add(contextToken);
			
			mostProbableState = calculateMostProbableState(similarContextList);
			contextToken.getToken().setState(mostProbableState);
			
			//contextToken.setWeight(similarContextList.size());
			//System.out.println("Log " + contextToken.getWeight());
		}
		
	}
	
	private void defineMostProbableStateForContextZero(ArrayList<ContextToken> contextListToAnalyze, ArrayList<ContextToken> contextList) {
		
		int mostProbableState;
		ArrayList<ContextToken> similarContextList;
		
		for(ContextToken contextToken : contextListToAnalyze) {
			
			similarContextList = getSimilarContextTokenZero(contextToken, contextList);
			similarContextList.add(contextToken);
			
			mostProbableState = calculateMostProbableState(similarContextList);
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
	private int calculateMostProbableState(ArrayList<ContextToken> similarContextList) {
		
		final int NUMBER_STATES = 5; //-- Considering BILOU
		int [] stateArray = new int [NUMBER_STATES];
		int mostProbableStateNumber = 0;
		
		for(ContextToken contextToken : similarContextList)
			stateArray[contextToken.getToken().getState()]++;
		
		for(int i = 0; i < NUMBER_STATES - 1; i++)
			if(stateArray[mostProbableStateNumber] < stateArray[i+1])
				mostProbableStateNumber = i+1;
		
		return(mostProbableStateNumber);
	}	
	
	/**
	 * existContextZeroInSequence():
	 * @param token
	 * @return
	 */
	public ContextToken existContextZeroInSequence(String token) {
		
		ContextToken contextZero = null;
		
		for(ContextToken context : contextZeroList)
			if(context.getTokenValue().equals(token)) {
				contextZero = context;
				break;
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
	private boolean existContext(ContextToken tokenToAnalyze) {
		return (existContext(tokenToAnalyze, this.contextList, true));//-- normal is true
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
				
				if(existContextInContextList && tokenToAnalyze.getSuffixSize() == token.getSuffixSize()) {
					for(int i = 0; i < tokenToAnalyze.getSuffixSize(); i++) {
						if(!(token.getSuffix(i).getValue()).equals(tokenToAnalyze.getSuffix(i).getValue())) {
							existContextInContextList = false;
							break;
						}
					}
					
					//-- Code optimization
					if(existContextInContextList)
						return(existContextInContextList);
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
		
		if(context == null)
			context = existPrefixInSequence(seq, index);
		
		return(context);
	}
	
	//@Fast --> existContextInSequencePrefixExtended(String[] seq, int index)
	public ContextToken existContextInSequencePrefixExtendedHashMap(String[] seq, int index) {
		
		ContextToken context = existContextInSequence(seq, index);
		
		if(context == null)
			context = existPrefixInSequencePrefixContextHashMap(seq, index);
		
		return(context);
	}
	
	public ContextToken existContextInSequenceFullyExtended(String[] seq, int index) {
		
		ContextToken context = existContextInSequencePrefixExtended(seq, index);
		
		if(context == null)
			context = existSuffixInSequence(seq, index);	
			
		return(context);
	}
	
	//@Fast --> existContextInSequenceFullyExtended(String[] seq, int index)
	public ContextToken existContextInSequenceFullyExtendedHashMap(String[] seq, int index) {
		
		ContextToken context = existContextInSequencePrefixExtended(seq, index);
		
		if(context == null)
			context = existSuffixInSequenceSuffixContextHashMap(seq, index);	
			
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
		String key = generateKeyFromSequence(seq, index); 
		ContextToken contextTokenToReturn = fastAccessContextList.get(key);

		//}
		return(contextTokenToReturn);
	}
	
	public String generateKeyFromSequence(String [] sequenceList, int index) {
		String prefix = START_LEFT_KEY;
		String suffix = START_RIGHT_KEY;
		
		//Add prefix
		for(int i = index - 1; i >= 0 && i >= (index - windowSize); i--)
			prefix += sequenceList[i];
		
		//Add suffix
		for(int i = index + 1; i < sequenceList.length && i <= index + windowSize; i++)
			suffix += sequenceList[i];
		
		return(prefix + suffix);
	}
	
	private String generateLeftPrefixKeyFromSequence(String [] sequenceList, int index) {
		String prefix = START_LEFT_KEY;
		
		//Add prefix
		for(int i = index - 1; i >= 0 && i >= (index - windowSize); i--)
			prefix += sequenceList[i];
		
		return(prefix + START_RIGHT_KEY);
	}
	
	private String generateRightKeyFromSequence(String [] sequenceList, int index) {
		String suffix = START_LEFT_KEY + START_RIGHT_KEY;
		
		//Add suffix
		for(int i = index + 1; i < sequenceList.length && i <= index + windowSize; i++)
			suffix += sequenceList[i];
		
		return(suffix);
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
		
		ContextToken context = fastAccessPrefixContextList.get(generateLeftPrefixKeyFromSequence(seq, index));		
		
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
		
		ContextToken context = fastAccessPrefixContextList.get(generateLeftPrefixKeyFromSequence(seq, index));					
		
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
		ContextToken context = fastAccessSuffixContextList.get(generateRightKeyFromSequence(seq, index));		
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
		ContextToken context = fastAccessSuffixContextList.get(generateRightKeyFromSequence(seq, index));		
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
			for(int i = 0; i < cTA.getPrefixSize(); i++)
				if(!cTA.getPrefix(i).getValue().equals(cTB.getPrefix(i).getValue())) {
					isPrefixEquals = false;
					break;
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
			for(int i = 0; i < cTA.getSuffixSize(); i++)
				if(!cTA.getSuffix(i).getValue().equals(cTB.getSuffix(i).getValue())) {
					isSuffixEquals = false;
					break;
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
			
		while(nextTokenIndex + 1 < seq.length() && isNextTokenPartOfEntity(seq, nextTokenIndex))
			nextTokenIndex++;
		
		return nextTokenIndex;
	}
	
	public ContextToken getContextToken(int index) {
		return(contextList.get(index));
	}
	
	public ContextToken getContextTokenByID(int id) {
		for(int i = 0; i < contextList.size(); i++)
			if(contextList.get(i).getContextTokenID() == id)
				return(contextList.get(i));
		
		return(null);
	}
	
	public HashMap<String, ContextToken> getFastAccessContextList() {
		return fastAccessContextList;
	}

	public HashMap<String, ContextToken> getFastAccessContextListNotNormalized() {
		return fastAccessContextListNotNormalized;
	}

	public ArrayList<ContextToken> getContextListNotNormalized() {
		return contextListNotNormalized;
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
		
		if(tagSet.equals("IO"))
			answer = (seq.y(index + 1) == 0);
		else if(tagSet.equals("BIO"))
			answer = (seq.y(index + 1) == 1);
		else if(tagSet.equals("BILOU"))
			answer = (seq.y(index + 1) == 1 || seq.y(index + 1) == 2);
		
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
			if(isContextEquals(contextTokenToFind, contextToken))
				similarContextList.add(contextToken);
		}
		
		return(similarContextList);
	}
	
	public ArrayList<ContextToken> getSimilarContextTokenZero(ContextToken contextTokenToFind, ArrayList<ContextToken> contextList) {
		
		ArrayList<ContextToken> similarContextList = new ArrayList<ContextToken>();
		
		for(ContextToken contextToken : contextList) {
			if(contextTokenToFind.getTokenValue().equals(contextToken.getTokenValue()))
				similarContextList.add(contextToken);
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
		
		for(ContextToken contextToken : contextList)
			if(contextToken.getContextTokenID() == contextID) {
				contextToFind = contextToken;
				break;
			}

		return(contextToFind);
	}
	
	public int findContextIndex(ContextToken contextTokenToFind) {
		
		int index = -1;
		
		for(int i = 0; i < 0; i++)
			if(isContextEquals(contextTokenToFind, contextList.get(i))) {
				index = i;
				break;
			}
		
		return(index);
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

	/**
	 * readSupportContextObject(): Read SupportContext
	 * serializable object from a input file
	 * @param filename The name of the inputfile
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void readSupportContextObject(String filename, SupportContext_Optimized target) throws IOException, ClassNotFoundException {
    	
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		SupportContext_Optimized supportContext = (SupportContext_Optimized) in.readObject();
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
	private void cloneSupportContext(SupportContext_Optimized target, SupportContext_Optimized clone) {
		
		target.windowSize = clone.windowSize;
		target.contextID = clone.contextID;
		target.contextList = clone.contextList;
		target.contextListWithPrefixOnly = clone.contextListWithPrefixOnly;
		target.contextListWithSuffixOnly = clone.contextListWithSuffixOnly;
		target.contextZeroList = clone.contextZeroList;
		target.fastAccessContextList = clone.fastAccessContextList;
		target.fastAccessPrefixContextList = clone.fastAccessPrefixContextList;
		target.fastAccessSuffixContextList = clone.fastAccessSuffixContextList;
		target.fastAccessContextZeroList = clone.fastAccessContextZeroList;
		target.outsideList = clone.outsideList;
		target.tagSet = clone.tagSet;
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
		
		int endIndexFilename = filename.lastIndexOf(".");
		
		String supportContextFilename = filename.substring(0, endIndexFilename);
		supportContextFilename += "-" + TAG_SUPPORT_CONTEXT;
		supportContextFilename += filename.substring(endIndexFilename);
		
		return(supportContextFilename);
	}

}
