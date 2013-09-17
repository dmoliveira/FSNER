package lbd.Model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import iitb.CRF.DataIter;
import iitb.CRF.DataSequence;

/**
 * SupportContext: This class aims to provide context
 *  (prefix and suffix) for each entity found in the 
 *  training file. In case of composite entities, they
 *   must be construed as if was a string only.
 * @author DMZ
 */
public class SupportContext_Original implements Serializable {
	
	//-- The TAG for the feature save name
	private static final String TAG_SUPPORT_CONTEXT = "SupCxt";
	
	//-- Quantity token prefix and suffix in each side
	private int windowSize = 1;
	private int contextID = 0;
	
	//-- TagSet used {IO, BIO or BILOU}
	private String tagSet;
	
	//-- List of contextCopyOfSupportContext
	private ArrayList<ContextToken> contextList;
	private ArrayList<ContextToken> outsideList;
	
	/**
	 * SupportContext (Constructor)
	 * @param windowSize Quantity token prefix and suffix in each side
	 * @param tagSet the code name of tag set (IO, BIO or BILOU)
	 */
	public SupportContext_Original(int windowSize, String tagSet) {
		contextList = new ArrayList<ContextToken> ();
		outsideList = new ArrayList<ContextToken> ();
		this.tagSet = tagSet;
	}
	
   /**
    * SupportContext (Constructor)
	* @param tagSet the code name of tag set (IO, BIO or BILOU)
	*/
	public SupportContext_Original(String tagSet) {
		contextList = new ArrayList<ContextToken> ();
		outsideList = new ArrayList<ContextToken> ();
		this.tagSet = tagSet;
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
		System.out.println("Generate Context (Normalized, w=1):");
		System.out.print("\tExtracting Context [");
		int count = 0;
		Date startTime = new Date();
		
		for (trainData.startScan(); trainData.hasNext();) {
			
			//@DMZDebug
			if(++count%100 == 0)
				System.out.print(".");
			
		    DataSequence seq = trainData.next();
		    
		    for (int l = 0; l < seq.length(); l++) {
		    	
		    	//See TagSet in AutoTagger to know better (Inside, Beginning or UnitToken)
		    	//if(seq.y(l) != 3)   	
		    		l = extractContext(seq, l);
		    	//else if(seq.y(l) == 3) //Outside
		    		//extractOutside((String)seq.x(l));
		    }
		}
		
		//@DMZDebug
		System.out.println("] (" + (int)(((new Date()).getTime() - startTime.getTime())/1000.0)  + "s)");
		System.out.print("\tNormalizing Context List [");
		startTime = new Date();
		
		//-- Normalize the contextList	
    	normalizeContextList();
    	
    	//@DMZDebug
		System.out.println("] (" + (int)(((new Date()).getTime() - startTime.getTime())/1000.0)  + "s)");
    	System.out.println("Finished Generate Context\n");
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
		for(int i = 0; i < outsideList.size(); i++)
			if(token.toLowerCase().equals(outsideList.get(i).getTokenValue().toLowerCase()))
				return(outsideList.get(i));
		
		return(null);
	}
	
	public ContextToken getOutsideToken(int index) {
		return(outsideList.get(index));
	}
	
	public ContextToken getOutsideTokenByID(int id) {
		for(int i = 0; i < outsideList.size(); i++) {
			if(outsideList.get(i).getContextTokenID() == id)
				return(outsideList.get(i));
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
	private int extractContext(DataSequence seq, int index) {
		
		ContextToken token = new ContextToken((String)seq.x(index));
		int nextTokenIndex;		
		
		//Add prefix
		//for(int i = ((index - windowSize >= 0)? index - windowSize : 0); i < index; i++) {
		for(int i = index - 1; i >= 0 && i >= (index - windowSize); i--) {
			token.addPrefix(new Token((String)seq.x(i), seq.y(i)));
		}
		
		//Get index of last token of the same entity
		nextTokenIndex = getLastIndexEntity(seq, index);
		
		//Add suffix
		for(int i = nextTokenIndex + 1; i < seq.length() && i <= index + windowSize; i++) {
			token.addSuffix(new Token((String)seq.x(i), seq.y(i)));
		}
		
		if(!existContext(token)) {			
			token.setContextTokenID(contextID);
			token.getToken().setState(seq.y(index));
			contextList.add(token);
			
			contextID ++;
		}
		
		/*String out = "\nPrefix: ";
		for(int i = 0; i < token.getPrefixSize(); i++)
			out += token.getPrefix(i).getValue() + ", ";
		
		out += "<" + token.getTokenValue() + "> Suffix: ";
		
		for(int i = 0; i < token.getSuffixSize(); i ++)
			out += token.getSuffix(i).getValue() + ", ";
		
		System.out.println(out);*/			
		
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
		
		ArrayList<ContextToken> newContextList = new ArrayList<ContextToken>();
		
		for(ContextToken contextToken : contextList) {
			
			//@DMZDebug
			if(++count%1000 == 0)
				System.out.print(".");
			
			if(!existContext(contextToken, newContextList, false))
				newContextList.add(contextToken);
		}
		
		defineMostProbableState(newContextList);
		
		contextList = newContextList;
		
		//-- to colect some points that don't is reached more
		System.gc();
		
	}
	
	/**
	 * defineMostProbableState(): Define the
	 * state more probable given a list of
	 * similar context.
	 * @param contextListToAnalyze The context list to analyze
	 */
	private void defineMostProbableState(ArrayList<ContextToken> contextListToAnalyze) {
		
		int mostProbableState;
		ArrayList<ContextToken> similarContextList;
		
		for(ContextToken contextToken : contextListToAnalyze) {
			similarContextList = getSimilarContextToken(contextToken);
			mostProbableState = calculateMostProbableState(similarContextList);
			contextToken.getToken().setState(mostProbableState);
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
		
		boolean answer = false;
		
		int prefixCount = 0;
		int suffixCount = 0;
		
		String tokenValueToAnalyze = tokenToAnalyze.getTokenValue().toLowerCase();
		
		String prefixToAnalyzeValue;
		String prefixValue;
		String suffixToAnalyzeValue;
		String suffixValue;
		
		for(ContextToken token : contextList) {

			if(!considerMainToken || token.getTokenValue().toLowerCase().equals(tokenValueToAnalyze)) {
			
				prefixCount = 0;
				suffixCount = 0;
				
				for(int i = 0; i < tokenToAnalyze.getPrefixSize() && i < token.getPrefixSize(); i++) {
					
					prefixValue = token.getPrefix(i).getValue().toLowerCase();
					prefixToAnalyzeValue = tokenToAnalyze.getPrefix(i).getValue().toLowerCase();
					
					if((prefixValue).equals(prefixToAnalyzeValue))
						prefixCount++;
				}
				
				for(int i = 0; i < tokenToAnalyze.getSuffixSize() && i < token.getSuffixSize(); i++) {
					
					suffixValue = token.getSuffix(i).getValue().toLowerCase();
					suffixToAnalyzeValue = tokenToAnalyze.getSuffix(i).getValue().toLowerCase();
					
					if((suffixValue).equals(suffixToAnalyzeValue))
						suffixCount++;
				}
				
				if(prefixCount == token.getPrefixSize() && suffixCount == token.getSuffixSize()) {
					answer = true;
					break;
				}
			}
		}
		
		return(answer);
	}
	
	// -1 false, otherwise true
	public int existContextInSequence(DataSequence seq, int index) {
		
		int answer = -1;
		//boolean answer = false;
		boolean isPrefixEquals = false;
		boolean isSuffixEquals = false;
		
		//Considerando só para BILOU por enquanto; 3 == Outside
		//if(seq.y(index) != 3) {
			for(ContextToken contextToken : contextList) {
				//if((contextToken.getTokenValue().toLowerCase()).equals(((String)seq.x(index)).toLowerCase())) {
					
					isPrefixEquals = isPrefixEquals(seq, index, contextToken);
					isSuffixEquals = isSuffixEquals(seq, index, contextToken);
					
					if(isPrefixEquals && isSuffixEquals) {
						answer = contextToken.getContextTokenID();						
						return(answer);
						//answer = true;
						//break;
					}
				//}
			//}
		}
		
		ContextToken t = getOutsideToken((String)seq.x(index));
		return((t != null)?t.getContextTokenID():-1);
	}
	
	private boolean isContextEquals(ContextToken cTA, ContextToken cTB) {
		return(isPrefixEquals(cTA, cTB) && isSuffixEquals(cTA, cTB));
	}
	
	private boolean isContextEquals(DataSequence seq, int index, ContextToken contextToken) {
		return(isPrefixEquals(seq, index, contextToken) && isSuffixEquals(seq, index, contextToken));
	}
	
	private boolean isPrefixEquals (ContextToken cTA, ContextToken cTB) {
		
		boolean answer = (cTA.getPrefixSize() == cTB.getPrefixSize());
		
		for(int i = 0; i < cTA.getPrefixSize() && answer; i++)
			if(!cTA.getPrefix(i).getValue().toLowerCase().equals(cTB.getPrefix(i).getValue().toLowerCase()))
				answer = false;
		
		return(answer);
	}
	
	private boolean isPrefixEquals(DataSequence seq, int index, ContextToken contextToken) {
		
		boolean answer = false;
		int prefixCorrectCount = 0;
		String prefix = "";
		
		for(int i = 0; i < contextToken.getPrefixSize(); i++) {
			
			if(index - (1 + i) >= 0)
				prefix = ((String)seq.x(index - (1 + i) )).toLowerCase();
			
			if(prefix.equals(contextToken.getPrefix(i).getValue().toLowerCase()))
				prefixCorrectCount++;
		}
		
		if(prefixCorrectCount == contextToken.getPrefixSize())
			answer = true;
		
		return(answer);
	}
	
	private boolean isSuffixEquals(ContextToken cTA, ContextToken cTB) {
		
		boolean answer = (cTA.getSuffixSize() == cTB.getSuffixSize());
		
		for(int i = 0; i < cTA.getSuffixSize() && answer; i++)
			if(!cTA.getSuffix(i).getValue().toLowerCase().equals(cTB.getSuffix(i).getValue().toLowerCase()))
				answer = false;
		
		return(answer);
	}
	
	private boolean isSuffixEquals(DataSequence seq, int index, ContextToken contextToken) {
		
		boolean answer = false;
		int suffixCorrectCount = 0;
		String suffix = "";
		
		for(int i = 0; i < contextToken.getSuffixSize(); i++) {
			
			if(index + (1 + i) < seq.length())
				suffix = ((String)seq.x(index + (1 + i))).toLowerCase();
			
			if(suffix.equals(contextToken.getSuffix(i).getValue().toLowerCase()))
				suffixCorrectCount++;
		}
		
		if(suffixCorrectCount == contextToken.getSuffixSize())
			answer = true;
		
		return(answer);
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
	 * @return A list of similar contextToken
	 */
	public ArrayList<ContextToken> getSimilarContextToken(ContextToken contextTokenToFind) {
		
		ArrayList<ContextToken> similarContextList = new ArrayList<ContextToken>();
		
		for(ContextToken contextToken : contextList) {
			if(isContextEquals(contextTokenToFind, contextToken))
				similarContextList.add(contextToken);
		}
		
		return(similarContextList);
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
	
	/**
	 * getContextList(): Receive the
	 * context list created thought 
	 * the training file.
	 * @return Return the context list
	 */
	public ArrayList<ContextToken> getContextList() {
		return contextList;
	}
	
	/**
	 * readSupportContextObject(): Read SupportContext
	 * serializable object from a input file
	 * @param filename The name of the inputfile
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void readSupportContextObject(String filename, SupportContext_Original target) throws IOException, ClassNotFoundException {
    	
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		SupportContext_Original supportContext = (SupportContext_Original) in.readObject();
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
	private void cloneSupportContext(SupportContext_Original target, SupportContext_Original clone) {
		
		target.windowSize = clone.windowSize;
		target.contextID = clone.contextID;
		target.contextList = clone.contextList;
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
