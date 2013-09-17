package iitb.Model;

import iitb.CRF.*;
import java.util.*;
import java.io.*;

/**
 * 
 * @author Sunita Sarawagi
 * 
 */

public class WordFeatures extends FeatureTypes {
	
	int stateId;
	int statePos;
	
	Object token;
	int tokenId;
	
	WordsInTrain dict;
	int _numWordStatePairs;
	protected float weight; 
	
	public static int RARE_THRESHOLD = 0;
	
	public WordFeatures(FeatureGenImpl m, WordsInTrain d, float weight) {
		super(m);
		dict = d;
		this.weight = weight;
	}

	public WordFeatures(FeatureGenImpl m, WordsInTrain d) {
		super(m);
		dict = d;
		weight = 1;
	}

	private void nextStateId() {
		stateId = dict.nextStateWithWord(token, stateId);
		statePos++;
	}

	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
		stateId = -1;
		if (dict.count(data.x(pos)) > RARE_THRESHOLD) {
			token = (data.x(pos));
			tokenId = dict.getIndex(token);
			statePos = -1;
			nextStateId();
			return true;
		}
		return false;
	}

	public boolean hasNext() {
		return (stateId != -1);
	}

	public void next(FeatureImpl f) {
		if (featureCollectMode())
			setFeatureIdentifier(tokenId * model.numStates() + stateId,
					stateId, "W_" + token, f);
		else
			setFeatureIdentifier(tokenId * model.numStates() + stateId,
					stateId, token, f);
		f.yend = stateId;
		f.ystart = -1;
		f.val = 1 * weight;
		nextStateId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see iitb.Model.FeatureTypes#maxFeatureId()
	 */
	public int maxFeatureId() {
		return dict.dictionaryLength() * model.numStates();
	}
};
