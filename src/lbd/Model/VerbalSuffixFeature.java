package lbd.Model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import lbd.Utils.Utils;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

public class VerbalSuffixFeature extends FeatureTypes {

	private static final long serialVersionUID = 1L;
	
	/*
	 * Sources for verbal suffixes most used in PT-BR:
	 * 1. http://www.pciconcursos.com.br/aulas/portugues/sufixos-verbais
	 * 2. http://mastercyber.hd1.com.br/portuguesOs_principais_sufixos_verbais.htm
	 */
	private final String [] verbalSuffixArray = {"ar", "ear", "ejar", "entar",
		"ficar", "fazer", "icar", "ilhar", "inhar", "iscar", "itar", "izar", "ecer", "escer"};
	
	private final String ACRONYM = "VerbalSufix.";
	
	private String token;
	private int currentState;
	private int idFeature;
	private float weight;
	boolean hasFeature;
	
	private DataSequence sequence;
	private String [] sequenceList;

	public VerbalSuffixFeature(FeatureGenImpl fgen, float weight) {
		super(fgen);
		
		this.weight = weight;		
	}
	
	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
		
		hasFeature = false;
		currentState = -1;
		
		if(sequence != data) {
			sequence = data;
			sequenceList = Utils.convertSequenceToLowerCase(data, data.length());
		}
		
		if((idFeature = existSuffixInToken(sequenceList[pos])) != -1) {
			hasFeature = true;
			advance();
		}
		
		return hasFeature;
	}

	@Override
	public boolean hasNext() {
		return (hasFeature && currentState < model.numStates());
	}
	
	protected void advance() {
		currentState++;
	}

	@Override
	public void next(FeatureImpl f) {
		
		setFeatureIdentifier(idFeature * model.numStates() + currentState, currentState, ACRONYM + "(" + token + ")",f);
		
		f.yend = currentState;
		f.ystart = -1;
		f.val = 1 * weight;
		
		advance();
	}
	
	private int existSuffixInToken(String token) {
		
		int existSuffix = -1;
		
		for(int i = 0; i < verbalSuffixArray.length; i++) {
			
			if(verbalSuffixArray[i].lastIndexOf(token) > 0) {
				existSuffix = i;
				break;
			}
		}
		
		return (existSuffix);
	}
}
