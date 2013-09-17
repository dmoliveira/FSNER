package lbd.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class ContextToken_Original implements Serializable{
	
	//The token can be composed by more than one token
	private int contextTokenID;
	private Token token;
	private ArrayList<Token> prefix; //Context prefix
	private ArrayList<Token> suffix; //Context suffix
	
	public ContextToken_Original(String value, Double weight) {
		token = new Token(value, weight);
		
		prefix = new ArrayList<Token> ();
		suffix = new ArrayList<Token> ();
	}
	
	public ContextToken_Original(String value) {
		token = new Token(value);
		
		prefix = new ArrayList<Token> ();
		suffix = new ArrayList<Token> ();
	}
	
	public ContextToken_Original(Token token) {
		this.token = token;
		
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
	
	public ArrayList<Token> getSuffix() {
		return suffix; 
	}
	
	public Token getSuffix(int index) {
		return suffix.get(index);
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
	
}
