package lbd.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class ContextToken implements Serializable {
	
	//The token can be composed by more than one token
	protected int contextTokenID;
	protected Token token;
	protected ArrayList<Token> prefix; //Context prefix
	protected ArrayList<Token> suffix; //Context suffix
	
	protected String key;
	protected String allPrefix;
	protected String allSuffix;
	
	//-- Statistical Values
	protected transient int frequency;
	protected transient int correctHits;
	protected transient int wrongHits;
	
	protected int numberOutsideTokens;
	
	protected transient double percentageCorrectHits;
	protected transient double percentageWrongHits;
	
	public ContextToken(String value, Double weight) {
		token = new Token(value, weight);
		
		prefix = new ArrayList<Token> ();
		suffix = new ArrayList<Token> ();
	}
	
	public ContextToken(String value) {
		token = new Token(value);
		
		prefix = new ArrayList<Token> ();
		suffix = new ArrayList<Token> ();
	}
	
	public ContextToken(Token token, int contextID) {
		this.token = token;
		this.contextTokenID = contextID;
		
		prefix = new ArrayList<Token> ();
		suffix = new ArrayList<Token> ();
	}
	
	public void addPrefix(Token prefixToken) {
		prefix.add(prefixToken);
	}
	
	public void addSuffix(Token suffixToken) {
		suffix.add(suffixToken);
	}
	
	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}
	
	public String getTokenValue() {
		return token.getValue();
	}
	
	public void setTokenValue(String value) {
		token.setValue(value);
	}

	public double getWeight() {
		return token.getWeight();
	}

	public void setWeight(double weight) {
		token.setWeight(weight);
	}
	
	public ArrayList<Token> getPrefix() {
		return prefix;
	}
	
	public Token getPrefix(int index) {
		return prefix.get(index);
	}
	
	public String getPrefixIndexValue(int index) {
		return prefix.get(index).getValue();
	}
	
	public ArrayList<Token> getSuffix() {
		return suffix; 
	}
	
	public Token getSuffix(int index) {
		return suffix.get(index);
	}
	
	public String getSuffixIndexValue(int index) {
		return suffix.get(index).getValue();
	}
	
	public void setPrefix(ArrayList<Token> prefix) {
		this.prefix = prefix;
	}

	public void setSuffix(ArrayList<Token> suffix) {
		this.suffix = suffix;
	}

	public void setPrefix(Token token, int index) {
		prefix.set(index, token);
	}
	
	public void setSuffix(Token token, int index) {
		suffix.set(index, token);
	}
	
	public int getPrefixSize() {
		return prefix.size();
	}
	
	public int getSuffixSize() {
		return suffix.size();
	}

	public int getContextTokenID() {
		return contextTokenID;
	}

	public void setContextTokenID(int contextTokenID) {
		this.contextTokenID = contextTokenID;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getAllPrefix() {
		return allPrefix;
	}
	
	public String getAllPrefixInOrder() {
		String prefix = "";
		
		for(int i = this.prefix.size()-1; i >= 0; i--)
			prefix += this.prefix.get(i).getValue() + " ";
		
		if(prefix.length() > 0)
			prefix = prefix.substring(0, prefix.length()-1);
		
		return(prefix);
	}

	public void setAllPrefix(String allPrefix) {
		this.allPrefix = allPrefix;
	}

	public String getAllSuffix() {
		return allSuffix;
	}
	
	public String getAllSuffixInOrder() {
		String suffix = "";
		
		for(int i = this.suffix.size()-1; i >= 0; i--)
			suffix += this.suffix.get(i).getValue() + " ";
		
		if(suffix.length() > 0)
			suffix = suffix.substring(0, suffix.length()-1);
		
		return(suffix);
	}

	public void setAllSuffix(String allSuffix) {
		this.allSuffix = allSuffix;
	}
	
	@Override
	public String toString() {
		String contextStatus = "Cxt("+contextTokenID+"):" + token.getValue();
		contextStatus += " Px:{ ";
		for(int i = 0; i < prefix.size(); i++) contextStatus += prefix.get(i).getValue() + " ";
		contextStatus += "} Sx:{ ";
		for(int i = 0; i < suffix.size(); i++) contextStatus += suffix.get(i).getValue() + " ";
		contextStatus += "}";
		
		return(contextStatus);
	}
	
	//-- Statistical values
	public void addFrequency() {
		frequency++;
	}
	
	public void addCorrectHit() {
		correctHits++;
	}
	
	public void addWrongHit() {
		wrongHits++;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getCorrectHits() {
		return correctHits;
	}

	public void setCorrectHits(int correctHits) {
		this.correctHits = correctHits;
	}

	public int getWrongHits() {
		return wrongHits;
	}

	public void setWrongHits(int wrongHits) {
		this.wrongHits = wrongHits;
	}

	public double getPercentageCorrectHits() {
		return percentageCorrectHits;
	}

	public void setPercentageCorrectHits(double percentageCorrectHits) {
		this.percentageCorrectHits = percentageCorrectHits;
	}

	public double getPercentageWrongHits() {
		return percentageWrongHits;
	}

	public void setPercentageWrongHits(double percentageWrongHits) {
		this.percentageWrongHits = percentageWrongHits;
	}

	public int getNumberOutsideTokens() {
		return numberOutsideTokens;
	}

	public void setNumberOutsideTokens(int numberOutsideTokens) {
		this.numberOutsideTokens = numberOutsideTokens;
	}
}
