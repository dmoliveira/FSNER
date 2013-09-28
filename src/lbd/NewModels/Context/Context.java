package lbd.NewModels.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.NewModels.Context.ContextManager.ContextType;

public class Context implements Serializable {

	private static final long serialVersionUID = 1L;

	protected ArrayList<String> entityList;
	protected ArrayList<HashMap<Integer, ContextUnit>> contextTypeList;

	public Context() {

		contextTypeList = new ArrayList<HashMap<Integer, ContextUnit>>(); //-- Integer = WindowSize
		entityList = new ArrayList<String>();

		for(int i = 0; i < ContextType.values().length; i++) {
			contextTypeList.add(new HashMap<Integer, ContextUnit>());
		}
	}

	public void addEncodedContext(String encodedContext) {

		String [] encodedContextAffix = encodedContext.split(ContextManager.DELIMITER_AFFIX);

		int indexPrefix = 0;
		boolean hasPrefix = !encodedContext.startsWith(ContextManager.DELIMITER_AFFIX) && encodedContextAffix[indexPrefix].length() > 0;

		int indexSuffix = (hasPrefix)? 1 : 0;
		boolean hasSuffix = hasPrefix && !encodedContext.endsWith(ContextManager.DELIMITER_AFFIX);
		hasSuffix |= !hasPrefix && encodedContextAffix.length > indexSuffix && encodedContextAffix[indexSuffix].length() > 0;

		String [] prefix = (hasPrefix)?encodedContextAffix[indexPrefix].split(ContextManager.DELIMITER_SPLIT) : new String [0];
		String [] suffix = (hasSuffix)?encodedContextAffix[indexSuffix].split(ContextManager.DELIMITER_SPLIT) : new String [0];

		int prefixSize = prefix.length;
		int suffixSize = suffix.length;

		String currentEncodedPrefix = "";
		String currentEncodedSuffix = "";
		String currentEncodedContext = "";

		int iterationSize = (prefixSize > suffixSize)? prefixSize : suffixSize;

		for(int i = 0; i < iterationSize; i++) {
			if(prefixSize > i && suffixSize > i) {

				currentEncodedPrefix = prefix[prefixSize - (i+1)] + ((i != 0)?ContextManager.DELIMITER_TERM:"") + currentEncodedPrefix;
				currentEncodedSuffix += ((i != 0)?ContextManager.DELIMITER_TERM:"") + suffix[i];

				currentEncodedContext = currentEncodedPrefix + ContextManager.DELIMITER_AFFIX + currentEncodedSuffix;
				contextTypeList.get(ContextType.PrefixSuffix.ordinal()).put(i+1, new ContextUnit(currentEncodedContext));

				currentEncodedContext = currentEncodedPrefix + ContextManager.DELIMITER_AFFIX;
				contextTypeList.get(ContextType.Prefix.ordinal()).put(i+1, new ContextUnit(currentEncodedContext));

				currentEncodedContext = ContextManager.DELIMITER_AFFIX + currentEncodedSuffix;
				contextTypeList.get(ContextType.Suffix.ordinal()).put(i+1, new ContextUnit(currentEncodedContext));

			} else if(prefixSize > i) {

				currentEncodedPrefix = prefix[prefixSize - (i+1)] + ((i != 0)?ContextManager.DELIMITER_TERM:"") + currentEncodedPrefix;

				currentEncodedContext = currentEncodedPrefix + ContextManager.DELIMITER_AFFIX;
				contextTypeList.get(ContextType.Prefix.ordinal()).put(i+1, new ContextUnit(currentEncodedContext));

			} else if(suffixSize > i){

				currentEncodedSuffix += ((i != 0)?ContextManager.DELIMITER_TERM:"") + suffix[i];

				currentEncodedContext = ContextManager.DELIMITER_AFFIX + currentEncodedSuffix;
				contextTypeList.get(ContextType.Suffix.ordinal()).put(i+1, new ContextUnit(currentEncodedContext));
			}
		}

		//System.out.println(encodedContext + " $$ " + currentEncodedPrefix + " $$ " + encodedCurrentContext);
	}

	public String getEncodedContext(ContextType contextType, int windowSize) {

		int contextTypeIndex = contextType.ordinal();
		String encodedContext = (contextTypeList.get(contextTypeIndex).get(windowSize) != null)?
				contextTypeList.get(contextTypeIndex).get(windowSize).getContextID() : "";

				return(encodedContext);
	}

	public void addEntityName(String term) {
		if(!entityList.contains(term)) {
			entityList.add(term);
		}
	}

	public ArrayList<String> getEntityList() {
		return(entityList);
	}

}
