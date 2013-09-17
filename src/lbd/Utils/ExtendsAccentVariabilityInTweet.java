package lbd.Utils;

import java.util.ArrayList;

import lbd.Model.ContextToken;
import lbd.Model.SupportContext;

public class ExtendsAccentVariabilityInTweet {

	public class AccentCharacter {
		
		private int index;
		private int classIndex;
		private int accentInTokenIndex;
		char accent;
		
		public AccentCharacter(char accent, int accentInTokenIndex, int classIndex, int index) {
			
			this.accent = accent;
			
			this.accentInTokenIndex = accentInTokenIndex;
			this.classIndex = classIndex;
			this.index = index;
		}
		
		public int getAccentInTokenIndex() {
			return accentInTokenIndex;
		}
		public void setAccentInTokenIndex(int accentInTokenIndex) {
			this.accentInTokenIndex = accentInTokenIndex;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		public int getClassIndex() {
			return classIndex;
		}
		public void setClassIndex(int classIndex) {
			this.classIndex = classIndex;
		}
		public char getAccent() {
			return accent;
		}
		public void setAccent(char accent) {
			this.accent = accent;
		}
	}
	
	private static final char [] correspondentAccentArray = {'a', 'e', 'i', 'o', 'u', 'A', 'E', 'I', 'O', 'U', 'c', 'C'};
	private static final char [][] accentArray = {{'à', 'è', 'ì', 'ò', 'ù', 'À', 'È', 'Ì', 'Ò', 'Ù'},
			{'á', 'é', 'í', 'ó', 'ú', 'Á', 'É', 'Í', 'Ó', 'Ú'},
			{'â', 'ê', 'î', 'ô', 'û', 'Â', 'Ê', 'Î', 'Ô', 'Û'},
			{'ã', 'ẽ', 'ĩ', 'õ', 'ũ', 'Ã', 'Ẽ', 'Ĩ', 'Õ', 'Ũ'},
			{'ä', 'ë', 'ï', 'ö', 'ü', 'Ä', 'Ë', 'Ï', 'Ö', 'Ü'},
			{'ç', 'Ç'}};
	
	
	public ArrayList<String> generateVariationAccentToken(String token) {
		
		ArrayList<AccentCharacter> accentList = getTokenAccents(token);
		ArrayList<String> tokenAccentVariationList = generateVariationAccentToken(token, accentList);
		
		return(tokenAccentVariationList);
	}
	
	public ArrayList<String> generateVariationAccentToken(String token, ArrayList<AccentCharacter> accentList) {
		
		ArrayList<String> accentTokenVariationList = new ArrayList<String>();
		accentTokenVariationList.add(token);
		
		int currentLength;
		String tokenObserved;
		String tokenModified;
		
		for(AccentCharacter accent : accentList) {
			
			currentLength = accentTokenVariationList.size();
			
			for(int i = 0; i < currentLength; i++) {
				
				tokenObserved = accentTokenVariationList.get(i);
				tokenModified = modifyTokenAccent(tokenObserved, accent);
				
				accentTokenVariationList.add(tokenModified);
			}
		}
		
		return(accentTokenVariationList);
	}
	
	public String modifyTokenAccent(String token, AccentCharacter accent) {
		
		String tokenModified = "";
		
		if(accent.getAccentInTokenIndex() > 0) {
			tokenModified += token.substring(0, accent.getAccentInTokenIndex());
			tokenModified += getCorrepondentNoAccentChar(accent.getClassIndex(), accent.getIndex());
			
			if(accent.getAccentInTokenIndex() < token.length() - 1)
				tokenModified += token.substring(accent.getAccentInTokenIndex() + 1);
		} else {
			tokenModified += getCorrepondentNoAccentChar(accent.getClassIndex(), accent.getIndex());
			
			if(accent.getAccentInTokenIndex() < token.length() - 1)
				tokenModified += token.substring(accent.getAccentInTokenIndex() + 1);
		}
		
		return(tokenModified);
	}
	
	public ArrayList<AccentCharacter> getTokenAccents(String token) {
		
		ArrayList<AccentCharacter> accentList = new ArrayList<AccentCharacter>();
		
		for(int i = 0; i < token.length(); i++)
			findTokenAccent(token.charAt(i), i, accentList);
		
		return(accentList);
	}
	
	public void findTokenAccent(char tokenChar, int tokenIndex, ArrayList<AccentCharacter> accentList) {
		
		boolean foundAccentInChar = false;
		
		for(int j = 0; !foundAccentInChar && j < accentArray.length; j++) {
			for(int k = 0; k < accentArray[j].length; k++) {
				if(tokenChar == accentArray[j][k]) {
					accentList.add(new AccentCharacter(tokenChar, tokenIndex, j, k));
					foundAccentInChar = true;
					
					break;
				}
			}
		}
	}
	
	protected char getCorrepondentNoAccentChar(int accentLineIndex, int accentColumnIndex) {
		
		//-- The first option maps to vogals char
		if(accentLineIndex < accentArray.length - 1)
			return(correspondentAccentArray[accentColumnIndex]);
		else // -- The second option maps to cedilla case
			return(correspondentAccentArray[(accentArray[accentLineIndex - 1].length) + accentColumnIndex]);
	}
	
	/**
	 * generateSequenceAccentVariation(): Generate
	 * every possible sequence with any non-accentuation of the token.
	 * 
	 * Obs: This method not create token with accents, only omit
	 * one accent per time doing every possible combination.
	 * @param supportContext
	 * @param sequenceList
	 * @param pos
	 * @return
	 */
	public String [][] generateSequenceAccentVariation(SupportContext supportContext,
			String [] sequenceList, int pos) {
		
		ArrayList<String> contextTokenList = new ArrayList<String> ();
		ArrayList<ArrayList<AccentCharacter>> accentMultList = new ArrayList<ArrayList<AccentCharacter>>();
		ArrayList<Integer> tokenPositionInSequenceList = new ArrayList<Integer>();
		ArrayList<ArrayList<String>> sequenceTokenAccentVariationList;
		String [][] sequenceVariationArray = null;
		
		//-- Generate all accents for each token intern to the window around the token observed
		generateAccentMultList(supportContext, contextTokenList, 
				tokenPositionInSequenceList, accentMultList, sequenceList, pos);
		
		//-- Generate all possible tokens with accent
		sequenceTokenAccentVariationList = generateTokenAccentVariationMultList(
				contextTokenList, accentMultList);
		
		//-- Verify if the sequence has tokens with accent
		if(sequenceTokenAccentVariationList.size() > 0) {
			
			//-- Generate all possible sequences with token accent variation
			sequenceVariationArray = generateTokenSequenceAccentVariation(sequenceList, 
					tokenPositionInSequenceList, sequenceTokenAccentVariationList);	
		}
	
		return(sequenceVariationArray);
	}
	
	/**
	 * generateAccentMultList: It generates a multiple list
	 * of accent variation for each token inside the window stipulated
	 * by the support context (except by the main token - token in the position pos).
	 * Also the tokens are stored in the contextTokenList and the 
	 * token position in the sequence is stored in the tokenPositionInSequenceList.
	 * @param supportContext
	 * @param contextTokenList
	 * @param tokenPositionInSequenceList
	 * @param accentMultList
	 * @param sequenceList
	 * @param pos
	 */
	private void generateAccentMultList(SupportContext supportContext, 
			ArrayList<String> contextTokenList, ArrayList<Integer> tokenPositionInSequenceList, 
			ArrayList<ArrayList<AccentCharacter>> accentMultList, String[] sequenceList, int pos) {
		
		int windowSize = supportContext.getWindowSize();
		int sequenceLength = sequenceList.length;
		
		ArrayList<AccentCharacter> accentList = null;
		
		//-- Generate all accents for each token intern to the window around the token observed
		for(int i = pos - windowSize; i >= 0 && i < sequenceLength && i <= pos + windowSize; i++) {
			
			if(i != pos) {
				
				//-- Tokens that will be used to be replaced in the sentence variation
				contextTokenList.add(sequenceList[i]);
				
				//-- Store the token position to improve the performance
				tokenPositionInSequenceList.add(i);
				
				//-- Get each accent of the token observed 
				accentList = getTokenAccents(sequenceList[i]);
				
				//-- Add to a multList to be looked after
				accentMultList.add(accentList);
			}
		}
	}
	
	/**
	 * generateTokenAccentVariationMultList: Generate all
	 * variations possible for the tokens with accents
	 * in the contextTokenList.
	 * @param contextTokenList
	 * @param accentMultList
	 * @return
	 */
	private ArrayList<ArrayList<String>> generateTokenAccentVariationMultList(
			ArrayList<String> contextTokenList, ArrayList<ArrayList<AccentCharacter>> accentMultList) {
		
		ArrayList<ArrayList<String>> sequenceTokenAccentVariationList = new ArrayList<ArrayList<String>>();
		
		for(int i = 0; i < accentMultList.size(); i++) {
			if(accentMultList.get(i).size() > 0)
				sequenceTokenAccentVariationList.add(generateVariationAccentToken(contextTokenList.get(i),
						accentMultList.get(i)));
		}
		
		return(sequenceTokenAccentVariationList);
	}
	
	/**
	 * 
	 * @param sequenceList
	 * @param sequenceVariationNumber
	 * @param sequenceTokenAccentVariationList
	 * @param tokenPositionInSequenceList
	 */
	private String [][] generateTokenSequenceAccentVariation(String[] sequenceList, 
			ArrayList<Integer> tokenPositionInSequenceList, 
			ArrayList<ArrayList<String>> sequenceTokenAccentVariationList) {
		
		int sequenceVariationArrayIndex = 0;
		int currentSequenceVariationSize = 0;
		int totalNumberOfSequenceVariation = calculateNumberTokenAccentVariation(sequenceTokenAccentVariationList);
		
		String [] sequence;
		String [][] sequenceVariationArray = new String [totalNumberOfSequenceVariation][sequenceList.length];
		
		ArrayList<String []> variationSequenceList = new ArrayList<String[]>();
		variationSequenceList.add(sequenceList);
		
		for(int contextIndex = 0; contextIndex < sequenceTokenAccentVariationList.size(); contextIndex++) {
			
			currentSequenceVariationSize = variationSequenceList.size();
			
			for(int sequenceVariationListIndex = 0; sequenceVariationListIndex < currentSequenceVariationSize; sequenceVariationListIndex++) {
			
				//-- It starts from 1, because 0 is the original position of the sequence with accents
				for(int tokenVariationIndex = 1; tokenVariationIndex < sequenceTokenAccentVariationList.get(contextIndex).size(); tokenVariationIndex++) {
				
					sequence = variationSequenceList.get(sequenceVariationListIndex).clone();
					
					sequence[tokenPositionInSequenceList.get(contextIndex)] = sequenceTokenAccentVariationList.get(contextIndex).get(tokenVariationIndex);
					
					variationSequenceList.add(sequence);
					sequenceVariationArray[sequenceVariationArrayIndex] = sequence;
					sequenceVariationArrayIndex++;
					
					//-- @DMZDebug Print the sequence generated
					//printSequenceGenerated(sequence, contextIndex, sequenceVariationListIndex, tokenVariationIndex);
				}
			}
		}
		
		return(sequenceVariationArray);
	}
	
	/**
	 * calculateNumberTokenAccentVariation: Calculate the total 
	 * number of accent tokens can be varied.
	 * @param sequenceTokenAccentVariationList
	 * @return
	 */
	private int calculateNumberTokenAccentVariation(ArrayList<ArrayList<String>> sequenceTokenAccentVariationList) {
		
		int numberTokenAccentVariation = 1;
		
		for(int i = 0; i < sequenceTokenAccentVariationList.size(); i++)
			numberTokenAccentVariation *= sequenceTokenAccentVariationList.get(i).size();
		
		return(numberTokenAccentVariation);
		
	}
	
	/**
	 * printSequenceGenerated: Print in form of debug
	 * the sequence generated after the accent modification.
	 * @param sequenceList
	 * @param contextIndex
	 * @param variationSequenceListIndex
	 * @param indexTokenVariation
	 */
	private void printSequenceGenerated(String [] sequenceList, int contextIndex,
			int variationSequenceListIndex, int indexTokenVariation) {
		
		String debugMessage = "";
		
		for(int i = 0; i < sequenceList.length; i++)
			debugMessage += sequenceList[i] + " ";
		
		debugMessage += ", cxtIdx: " + contextIndex;
		debugMessage += ", idxVarSeqLst: " + variationSequenceListIndex;
		debugMessage += ", idxTknVar: " + indexTokenVariation;
		
		System.out.println(debugMessage);
	}

}
