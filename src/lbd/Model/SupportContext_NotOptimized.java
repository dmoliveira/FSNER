package lbd.Model;

import iitb.CRF.DataIter;
import iitb.CRF.DataSequence;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import lbd.Utils.Utils;

/**
 * SupportContext: This class aims to provide context
 *  (prefix and suffix) for each entity found in the
 *  training file. In case of composite entities, they
 *   must be construed as if was a string only.
 * @author DMZ
 */
public class SupportContext_NotOptimized implements Serializable {

	private static final long serialVersionUID = 1L;

	//-- The TAG for the feature save name
	private static final String TAG_SUPPORT_CONTEXT = "SupCxt";

	//-- Only for @DMZDebug
	public boolean isTest;

	//-- Quantity token prefix and suffix in each side
	private int windowSize = 1;
	private static int contextID = 0;

	private boolean supportOnlyEntity;

	//-- TagSet used {IO, BIO or BILOU}
	private String tagSet;

	//-- List of contextCopyOfSupportContext
	private ArrayList<ContextToken> contextList;
	private ArrayList<ContextToken> contextListWithSuffixOnly;
	private ArrayList<ContextToken> contextListWithPrefixOnly;
	private ArrayList<ContextToken> contextZeroList;
	private ArrayList<ContextToken> outsideList;

	private float weight;

	private HashMap<String, ContextToken> fastAccessContextList;
	private final String START_LEFT_KEY = "l";
	private final String START_RIGHT_KEY = "r";

	/**
	 * SupportContext (Constructor)
	 * @param windowSize Quantity token prefix and suffix in each side
	 * @param tagSet the code name of tag set (IO, BIO or BILOU)
	 * @param supportOnlyEntites If the contexts created only considerer entities
	 */
	public SupportContext_NotOptimized(int windowSize, String tagSet, boolean supportOnlyEntites) {
		this(windowSize, tagSet);
		this.supportOnlyEntity = supportOnlyEntites;
	}

	/**
	 * SupportContext (Constructor)
	 * @param windowSize Quantity token prefix and suffix in each side
	 * @param tagSet the code name of tag set (IO, BIO or BILOU)
	 */
	public SupportContext_NotOptimized(int windowSize, String tagSet) {
		contextList = new ArrayList<ContextToken> ();
		contextListWithSuffixOnly = new ArrayList<ContextToken>();
		contextListWithPrefixOnly = new ArrayList<ContextToken>();
		contextZeroList = new ArrayList<ContextToken> ();
		fastAccessContextList = new HashMap<String, ContextToken>();
		outsideList = new ArrayList<ContextToken> ();
		this.tagSet = tagSet;
		this.windowSize = windowSize;
		weight = 1;
	}

	/**
	 * SupportContext (Constructor)
	 * @param tagSet the code name of tag set (IO, BIO or BILOU)
	 */
	public SupportContext_NotOptimized(String tagSet) {
		contextList = new ArrayList<ContextToken> ();
		contextListWithSuffixOnly = new ArrayList<ContextToken>();
		contextListWithPrefixOnly = new ArrayList<ContextToken>();
		contextZeroList = new ArrayList<ContextToken> ();
		fastAccessContextList = new HashMap<String, ContextToken>();
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
		System.out.println("Generate Context (Normalized, w=" + windowSize + "):");
		System.out.print("\tExtracting Context [");
		int count = 0;
		Date startTime = new Date();

		for (trainData.startScan(); trainData.hasNext();) {

			//@DMZDebug
			if(++count%100 == 0) {
				System.out.print(".");
			}

			DataSequence seq = trainData.next();
			String[] sequenceList = Utils.convertSequenceToLowerCase(seq, seq.length());

			for (int l = 0; l < seq.length(); l++) {

				//See TagSet in AutoTagger to know better (Inside, Beginning or UnitToken)
				if(!supportOnlyEntity || seq.y(l) != 3)
				{
					l = extractContext(sequenceList, seq, l);
					//else if(seq.y(l) == 3) //Outside
					//extractOutside((String)seq.x(l));
				}
			}
		}

		//ArrayList<ContextToken>x = new ArrayList<ContextToken>();
		//x.addAll(contextListWithoutSuffix);
		//x.addAll(contextList);
		//x.addAll(contextListWithoutPrefix);
		//contextList = x;
		contextList.addAll(contextListWithPrefixOnly);
		//addOnlyPrefix();
		contextList.addAll(contextListWithSuffixOnly);

		//@DMZDebug
		System.out.print("] (" + (int)(((new Date()).getTime() - startTime.getTime())/1000.0)  + "s)");
		System.out.print(" {" + contextList.size()  + " ctxs, ");
		System.out.println(contextListWithPrefixOnly.size() + " border cxts}");
		System.out.print("\tNormalizing Context List [");
		startTime = new Date();

		//-- Normalize the contextList
		normalizeContextList();

		//@DMZDebug
		System.out.print("] (" + (int)(((new Date()).getTime() - startTime.getTime())/1000.0)  + "s)");
		System.out.print(" {" + contextList.size()  + " ctxs, ");
		System.out.println(contextListWithPrefixOnly.size() + " border cxts}");

		System.out.print("\tNormalizing Context Zero List [");
		startTime = new Date();

		//-- Normalize the contextZeroList
		normalizeContextZeroList();

		//@DMZDebug
		System.out.print("] (" + (int)(((new Date()).getTime() - startTime.getTime())/1000.0)  + "s)");
		System.out.println(" {" + contextZeroList.size() + " ctxs}");
		System.out.println("Finished Generate Context ("+ (contextList.size() + contextZeroList.size()) +" cxts)\n");
	}

	private void prepareEfficientContextAccess() {

		String key;

		for(ContextToken context : contextList) {

			key = START_LEFT_KEY + context.getAllPrefix();
			key += START_RIGHT_KEY + context.getAllSuffix();

			fastAccessContextList.put(key, context);
		}

	}

	/**
	 * extractOutside: Put the outside token in outsideList
	 * to help to improve the results from ContextFeature.
	 * @param token The outside token
	 */
	public void extractOutside(String token) {
		ContextToken contextToken = new ContextToken(token);

		if(getOutsideToken(token) == null) {
			contextToken.setContextTokenID(contextID);
			outsideList.add(contextToken);

			contextID++;
		}
	}

	/**
	 * getOutsideToken: Search for the outside token
	 * in the outsideList. If is not found outside token
	 * then is returned null.
	 * @param token The token value that will be searched
	 * @return The ContextToken of outside token or null
	 */
	public ContextToken getOutsideToken(String token) {
		for(int i = 0; i < outsideList.size(); i++) {
			if(token.toLowerCase().equals(outsideList.get(i).getTokenValue().toLowerCase())) {
				return(outsideList.get(i));
			}
		}

		return(null);
	}

	public ContextToken getOutsideToken(int index) {
		return(outsideList.get(index));
	}

	public ContextToken getOutsideTokenByID(int id) {
		for(int i = 0; i < outsideList.size(); i++) {
			if(outsideList.get(i).getContextTokenID() == id) {
				return(outsideList.get(i));
			}
		}

		return(null);
	}

	/**
	 * extractContext: Extract the context around the entity
	 *    and assign the prefix and suffix.
	 * @param seq Sequence to extract the context
	 * @param index Position of start of entity
	 * @return The next sequence token
	 */
	private int extractContext(String[] sequenceList, DataSequence seq, int index) {

		ContextToken token = new ContextToken(sequenceList[index]);
		int nextTokenIndex;

		boolean hasPrefix = false;
		boolean hasSuffix = false;

		//Add prefix
		for(int i = index - 1; i >= 0 && i >= (index - windowSize); i--) {
			token.addPrefix(new Token(sequenceList[i], seq.y(i)));
			hasPrefix = true;
		}

		//Get index of last token of the same entity
		//nextTokenIndex = getLastIndexEntity(seq, index); (Original, changed in 08.10.11e
		nextTokenIndex = index; //-- only to test, consider the entity part as context

		//Add suffix
		for(int i = nextTokenIndex + 1; i < seq.length() && i <= index + windowSize; i++) {
			token.addSuffix(new Token(sequenceList[i], seq.y(i)));
			hasSuffix = true;
		}

		if(!existContext(token)) {
			token.setContextTokenID(contextID);
			token.getToken().setState(seq.y(index));

			if(hasPrefix && hasSuffix) {
				contextList.add(token);
			} else if (hasPrefix) {
				contextListWithPrefixOnly.add(token);
			} else {
				contextListWithSuffixOnly.add(token);
			}

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
	private void normalizeContextList() {

		//@DMZDebug
		int count = 0;

		ArrayList<ContextToken> normalizedContextList = new ArrayList<ContextToken>();
		ArrayList<ContextToken> similarContextList = new ArrayList<ContextToken>();

		for(ContextToken contextToken : contextList) {

			//@DMZDebug
			if(++count%1000 == 0) {
				System.out.print(".");
			}

			if(!existContext(contextToken, normalizedContextList, false)) {
				normalizedContextList.add(contextToken);
			} else {
				similarContextList.add(contextToken);
			}

		}

		defineMostProbableState(normalizedContextList, similarContextList);

		contextList = normalizedContextList;
	}

	private void normalizeContextZeroList() {

		//@DMZDebug
		int count = 0;

		ArrayList<ContextToken> normalizedContextZeroList = new ArrayList<ContextToken>();
		ArrayList<ContextToken> similarContextList = new ArrayList<ContextToken>();

		addElementsToContextZeroList();

		for(ContextToken contextToken : contextZeroList) {

			//@DMZDebug
			if(++count%1000 == 0) {
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
		for(ContextToken context : contextList) {
			contextZeroList.add(new ContextToken(context.getToken(), ++contextID));
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

		for(ContextToken contextToken : similarContextList) {
			stateArray[contextToken.getToken().getState()]++;
		}

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
		return (existContext(tokenToAnalyze, this.contextList, true));
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
					if(existContextInContextList) {
						return(existContextInContextList);
					}
				}
			}
		}

		return(existContextInContextList);
	}

	//@Fast
	/*public boolean existContext(ContextToken tokenToAnalyze, ArrayList<ContextToken> contextList , boolean considerMainToken) {

		boolean existContextInContextList = false;

		String key = START_LEFT_KEY + tokenToAnalyze.getAllPrefixValue();
		key += START_RIGHT_KEY + tokenToAnalyze.getAllSuffixValue();

		ContextToken context = fastAccessContextList.get(key);


			if(context != null && (!considerMainToken || tokenToAnalyze.getTokenValue().equals(tokenToAnalyze.getTokenValue())))
				existContextInContextList = true;

		return(existContextInContextList);
	}*/

	public ContextToken existContextInSequencePrefixExtended(String[] seq, int index) {

		ContextToken context = existContextInSequence(seq, index);

		if(context == null) {
			context = existPrefixInSequence(seq, index);
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

	//@Fast
	/*public ContextToken existContextInSequence(String[] seq, int index) {

		//Considerando só para BILOU por enquanto; 3 = Outside
		//if(seq.y(index) != 3) {
		String key = generateKeyFromSequence(seq, index);
		ContextToken contextTokenToReturn = fastAccessContextList.get(key);

		//}
		return(contextTokenToReturn);
	}*/

	private String generateKeyFromSequence(String [] sequenceList, int index) {
		String prefix = START_LEFT_KEY;
		String suffix = START_RIGHT_KEY;

		//Add prefix
		for(int i = index - 1; i >= 0 && i >= (index - windowSize); i--) {
			prefix += sequenceList[i];
		}

		//Add suffix
		for(int i = index + 1; i < sequenceList.length && i <= index + windowSize; i++) {
			suffix += sequenceList[i];
		}

		return(prefix + suffix);
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
			if(isContextEquals(contextTokenToFind, contextToken)) {
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
	public void readSupportContextObject(String filename, SupportContext_NotOptimized target) throws IOException, ClassNotFoundException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		SupportContext_NotOptimized supportContext = (SupportContext_NotOptimized) in.readObject();
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
	private void cloneSupportContext(SupportContext_NotOptimized target, SupportContext_NotOptimized clone) {

		target.windowSize = clone.windowSize;
		target.contextID = clone.contextID;
		target.contextList = clone.contextList;
		target.contextListWithPrefixOnly = clone.contextListWithPrefixOnly;
		target.contextListWithSuffixOnly = clone.contextListWithSuffixOnly;
		target.contextZeroList = clone.contextZeroList;
		target.fastAccessContextList = clone.fastAccessContextList;
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
