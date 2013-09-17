package lbd.Model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import lbd.NewModels.NewTypeFeatures.NewFeatureTypes;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Utils.Symbol;

public class SimpleDictionaryFeature extends NewFeatureTypes{

	private static final long serialVersionUID = 1L;
	protected static final String DICTIONARY_UNIVERSAL_DIRECTORY = "./samples/data/bcs2010/AutoTagger/Dictionary/EtzDictionaries/";
	protected final int WINDOW_SIZE = 6;
	
	protected static HashMap<String, HashMap<String, Object>> dictionary;

	public SimpleDictionaryFeature(FeatureGenImpl fgen, String dictionaryFilenameAddress) {
		super(fgen);
		
		this.featureName = "Dictionary";
		this.featureType = FeatureType.Dictionary;
		proccessSequenceType = ProccessSequenceType.Plain;
		
		dictionary = new HashMap<String, HashMap<String, Object>>();
		loadDictionary(dictionaryFilenameAddress);
	}
	
	protected void loadDictionary(String dictionaryFilenameAddress) {
		
		try {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(dictionaryFilenameAddress), Parameters.dataEncoding));
			
			String entry;
			String [] entryElement;
			
			String entryPreprocessed = Symbol.EMPTY;
			
			while((entry = in.readLine()) != null) {
					
				entryElement = entry.split(Symbol.SPACE);
				entryPreprocessed = Symbol.EMPTY;
				
				//-- Preprocess entry
				for(int i = 0; i < entryElement.length; i++)
					entryPreprocessed +=  entryElement[i] + Symbol.SPACE;
				
				if(entryPreprocessed.length() > 0) {
					
					entryPreprocessed = entryPreprocessed.substring(0, entryPreprocessed.length()-1);
					
					if(!dictionary.containsKey(entryElement[0]))
						dictionary.put(entryElement[0], new HashMap<String, Object>());
					
					dictionary.get(entryElement[0]).put(entryPreprocessed, null);
				}
			}
			
			in.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	protected int startFeature(DataSequence data, int pos) {
		
		int id = -1;
		
		String [] termsWindowSizeList = generateTermsWindowSize(data, pos);

		HashMap<String, Object> entryDictionaryMap = dictionary.get(data.x(pos));
		
		if(entryDictionaryMap != null) {
			for(int j = termsWindowSizeList.length - 1; j >= 0; j--) {
				
				if(termsWindowSizeList[j] == null)
					continue;
				
				if(entryDictionaryMap.containsKey(termsWindowSizeList[j])) {
					id = 1;
					break;
				}
			}
		}
		
		return(id);
		
	}
	
	protected String[] generateTermsWindowSize(DataSequence data, int pos) {
		
		String term;
		String [] termsWindowSizeList = new String[WINDOW_SIZE + 1];
		
		for(int i = 0; i < termsWindowSizeList.length; i++) {
			
			termsWindowSizeList[i] = Symbol.EMPTY;
			
			for(int j = pos; j < pos + i + 1 && j < pos + termsWindowSizeList.length && j < data.length(); j++) {
				
				term = (String)data.x(j);
				
				if(term.length() > 0)
					termsWindowSizeList[i] += ((j != pos && !((String)data.x(j)).isEmpty())? 
							Symbol.SPACE : Symbol.EMPTY) + ((String)data.x(j));
			}
			
			if(i > 0 && termsWindowSizeList[i - 1].equals(termsWindowSizeList[i])) {
				termsWindowSizeList[i] = null;
				break;
			}
		}
		
		return(termsWindowSizeList);
	}
}
