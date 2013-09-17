package lbd.NewModels.Context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class ContextManager implements Serializable {
	
	private static final long serialVersionUID = 5269891862034895722L;

	public static enum ContextType {PrefixSuffix, Prefix, Suffix};
	
	public final static String DELIMITER_SPLIT = "\\|";
	public final static String DELIMITER_TERM = "|";
	public final static String DELIMITER_AFFIX = "&";
	protected final static String EMPTY = "";
	
	protected HashMap<String, HashMap<String,Context>> contextEntityMap;
	protected HashMap<String, Context> contextMap;
	protected double probabilityThreshould;
	
	public ContextManager() {
		
		contextEntityMap = new HashMap<String, HashMap<String,Context>>();
		contextMap = new HashMap<String,Context>();
		
		probabilityThreshould = 1;
	}

	//-- Not Implemented Yet..
	public void calculateSequence() {

	}
	
	public static String getEncodedContext(String [] sequenceLowerCase, int entityIndex) {
		
		String encodedContext = "";
		
		for(int i = 0; i < sequenceLowerCase.length; i++) {
			if(sequenceLowerCase[i].trim().length() > 0) {
				if(i < entityIndex) {
					encodedContext += (i != 0 && encodedContext.length() > 0)? DELIMITER_TERM : "";
					encodedContext += sequenceLowerCase[i];
				} else if (i > entityIndex) {
					encodedContext += (i > entityIndex+1 && !encodedContext.endsWith(DELIMITER_AFFIX))? DELIMITER_TERM : "";
					encodedContext += sequenceLowerCase[i];
				} else if(i == entityIndex){
					encodedContext += DELIMITER_AFFIX;
				}
			}
		}
		
		return(encodedContext.trim());
	}
	
	public boolean existContext(String encodedContext, String entityValue, 
			ContextType contextType,  int windowSize) {
		
		boolean existContext = false;
		String encodedContextTrimmed = trimEncodedContext(encodedContext, contextType, windowSize);
		
		Context context;
		Iterator<Entry<String, Context>> ite;
		
		if(entityValue == null || entityValue.equals(EMPTY)) {
			ite = contextMap.entrySet().iterator();
		} else {
			ite = contextEntityMap.get(entityValue).entrySet().iterator();
		}
		
		while(ite.hasNext() && !existContext) {
			context = ite.next().getValue();
			//System.out.println(context.getEncodedContext(contextType, windowSize) + " " + contextType + " " + windowSize);
			if(context.getEncodedContext(contextType, windowSize).equals(encodedContextTrimmed) && encodedContextTrimmed.length() > 0) {
				existContext = true;
				System.out.println("\"" + encodedContextTrimmed + "\" (" +  contextType + " " + windowSize + ")");
				
			}
		}
		
		return(existContext);
	}
	
	public static String trimEncodedContext(String encodedContext, ContextType contextType, int windowSize) {
		
		String trimmedEncodedContext = "";
		
		String [] afix = encodedContext.split(DELIMITER_AFFIX);
		String [] prefix = ((afix.length >= 1 && !encodedContext.startsWith(DELIMITER_AFFIX) && afix[0].length() > 0))?afix[0].split(DELIMITER_SPLIT):new String[0];
		String [] suffix = ((afix.length == 1 && encodedContext.startsWith(DELIMITER_AFFIX) && afix[0].length() > 0) || (afix.length > 1 && afix[1].length() > 0))?afix[1].split(DELIMITER_SPLIT):new String[0];
		
		boolean enablePrefixSuffixTrim = prefix.length >= windowSize && suffix.length >= windowSize && contextType.name().equals(ContextType.PrefixSuffix.name());
		boolean enablePrefixTrim = prefix.length >= windowSize && contextType.name().equals(ContextType.Prefix.name());
		boolean enableSuffixTrim = suffix.length >= windowSize && contextType.name().equals(ContextType.Suffix.name());
		
		if(enablePrefixSuffixTrim || enablePrefixTrim || enableSuffixTrim) {
			
			if(enablePrefixSuffixTrim || enablePrefixTrim) {
				for(int i = prefix.length-1; i > 0 && (prefix.length - windowSize <= i); i--)
					trimmedEncodedContext = prefix[i] + ((i != prefix.length-1)?DELIMITER_TERM:"") + trimmedEncodedContext;
			}
			
			trimmedEncodedContext += DELIMITER_AFFIX;
			
			if(enablePrefixSuffixTrim || enableSuffixTrim) {
				for(int i = 0; i < suffix.length && i < windowSize; i++)
					trimmedEncodedContext += ((i != 0 && i < suffix.length-1)?DELIMITER_TERM:"") + suffix[i];
			}
		}
		
		return(trimmedEncodedContext);
	}
	
	//-- Not Implemented Yet..
	public boolean isContextReliable() {
		return(true);
	}
	
	//-- Not Implemented Yet..
	public double getContextProbability() {
		return(1.0);
	}
	
	public void addContext(String encodedContext) {
		contextMap.put(encodedContext, new Context());
		Context context = contextMap.get(encodedContext);
		context.addEncodedContext(encodedContext);
	}

}
