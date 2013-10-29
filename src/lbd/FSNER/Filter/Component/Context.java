package lbd.FSNER.Filter.Component;

import java.io.Serializable;
import java.util.HashMap;

import lbd.FSNER.Utils.CommonEnum.Flexibility;
import lbd.FSNER.Utils.MapId;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public class Context implements Serializable{

	private static final long serialVersionUID = 1L;

	protected static int globalId;

	protected static final String DELIMITER_TERM = "|";
	protected static final String DELIMITER_AFFIX = "&";

	public static enum ContextType {AllContext, Prefix, Suffix};

	protected HashMap<String, MapId<String, Integer>> contextMap;
	protected HashMap<String, Object> entityMap;

	protected int maxWindowSize;

	public Context(int maxWindowSize, Flexibility contextFlexibity) {

		globalId = 0;

		contextMap = new HashMap<String, MapId<String, Integer>>();
		entityMap = new HashMap<String, Object>();

		this.maxWindowSize = maxWindowSize;
	}

	public void addAsContext(ISequence sequenceLabel, int index) {

		String prefixValue = Symbol.EMPTY;
		String suffixValue = Symbol.EMPTY;
		String mainTerm = sequenceLabel.getToken(index);

		int prefixSize = 0;
		int suffixSize = 0;

		int prefixIndex;
		int suffixIndex;

		for(int i = 1; i <= maxWindowSize; i++) {

			prefixIndex= index - i;
			suffixIndex = index + i;

			if(prefixIndex > -1 && !sequenceLabel.getToken(prefixIndex).isEmpty()) {
				prefixValue = sequenceLabel.getToken(prefixIndex) + DELIMITER_TERM + prefixValue;
				prefixSize++;

				addToContextMap(prefixValue + DELIMITER_AFFIX, mainTerm);
				//contextMap.put(prefixValue + DELIMITER_AFFIX, ++globalId);
				//System.out.println(prefixValue + DELIMITER_AFFIX + " id(" + globalId + ") Sz:" + prefixSize);
			}

			if(suffixIndex < sequenceLabel.length() && !sequenceLabel.getToken(suffixIndex).isEmpty()) {
				suffixValue = suffixValue + DELIMITER_TERM + sequenceLabel.getToken(suffixIndex);
				suffixSize++;

				addToContextMap(DELIMITER_AFFIX + suffixValue, mainTerm);
				//contextMap.put(DELIMITER_AFFIX + suffixValue, ++globalId);
				//System.out.println(suffixValue + DELIMITER_AFFIX + " id(" + globalId + ") Sz:" + suffixSize);
			}

			if(prefixSize == suffixSize) {
				addToContextMap(prefixValue + DELIMITER_AFFIX + suffixValue, mainTerm);
				//contextMap.put(prefixValue + DELIMITER_AFFIX + suffixValue, ++globalId);
				//System.out.println(prefixValue + DELIMITER_AFFIX + suffixValue + " id(" + globalId + ")");
			}
		}
	}

	protected void addToContextMap(String contextValue, String term) {
		if(!term.isEmpty() && contextValue.length() > DELIMITER_AFFIX.length()) {
			if(!contextMap.containsKey(contextValue)) {
				contextMap.put(contextValue, new MapId<String, Integer>(++globalId));
			}

			MapId<String, Integer> contextMainTermMap = contextMap.get(contextValue);
			contextMainTermMap.put(term, ++globalId);
			entityMap.put(term, null);
		}
	}

	public int getContextId(ISequence sequenceLabel, int index, ContextType contextType,
			int windowSize, Flexibility contextFlexibility) {

		int id = -1;
		String contextValue = generateContext(sequenceLabel, index, contextType, windowSize);
		String mainTerm = sequenceLabel.getToken(index);

		contextMap.get(contextValue);

		if(contextMap.containsKey(contextValue)) {
			if(contextFlexibility == Flexibility.Total) {
				id = contextMap.get(contextValue).getId();
			} else if(contextFlexibility == Flexibility.Partial &&
					entityMap.containsKey(mainTerm)) {
				id = contextMap.get(contextValue).getId();
			} else if(contextFlexibility == Flexibility.Restrict &&
					contextMap.get(contextValue).containsKey(mainTerm)) {
				id = contextMap.get(contextValue).get(mainTerm);
			}
		}

		return(id);
	}

	protected String generateContext(ISequence sequenceLabel, int index, ContextType contextType, int windowSize) {

		String contextValue = Symbol.EMPTY;
		String prefixValue = Symbol.EMPTY;
		String suffixValue = Symbol.EMPTY;

		int prefixIndex;
		int suffixIndex;

		int prefixSize = 0;
		int suffixSize = 0;

		for(int i = 1; i <= windowSize; i++) {

			prefixIndex = index - i;
			suffixIndex = index + i;

			if(contextType != ContextType.Suffix && prefixIndex > -1 && !sequenceLabel.getToken(prefixIndex).isEmpty()) {
				prefixValue = sequenceLabel.getToken(prefixIndex) + DELIMITER_TERM + prefixValue;
				prefixSize++;
			}

			if(contextType != ContextType.Prefix && suffixIndex < sequenceLabel.length() && !sequenceLabel.getToken(suffixIndex).isEmpty()) {
				suffixValue = suffixValue + DELIMITER_TERM + sequenceLabel.getToken(suffixIndex);
				suffixSize++;
			}
		}

		contextValue = prefixValue + DELIMITER_AFFIX + suffixValue;
		if((contextType == ContextType.Prefix && prefixSize < windowSize) ||
				(contextType == ContextType.Suffix && suffixSize < windowSize) ||
				(contextType == ContextType.AllContext && (prefixSize < windowSize || suffixSize < windowSize))) {
			contextValue = Symbol.EMPTY;
		}

		//System.out.println(contextValue + " cxt:" + contextType.name()+" ws:" + windowSize);

		return(contextValue);
	}

	@Override
	public String toString() {
		return("Context.Ws(" + maxWindowSize + "): " + contextMap.size());
	}

}
