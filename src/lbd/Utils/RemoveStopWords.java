package lbd.Utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class RemoveStopWords implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private final String ENCODE_USED = "ISO-8859-1";
	
	protected HashMap<String, Boolean> stopWordMap;
	protected String stopWordInputFilenameAddress;
	
	protected final String SPACE = " ";
	
	public RemoveStopWords(String stopWordInputFilenameAddress) {
		
		stopWordMap = new HashMap<String, Boolean>();
		this.stopWordInputFilenameAddress = stopWordInputFilenameAddress;
		
		loadStopWords(stopWordInputFilenameAddress);
	}
	
	protected void loadStopWords(String stopWordInputFilenameAddress) {
		
		try {
			BufferedReader in = new BufferedReader (new InputStreamReader(
					new FileInputStream(stopWordInputFilenameAddress), ENCODE_USED));
			String line;
			
			while((line = in.readLine()) != null)
				stopWordMap.put(line.toLowerCase(), false);
			
			in.close();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public String removeStopWord(String sentence) {
		
		String cleanSentence = "";
		String [] sentenceElement = sentence.split(SPACE);
		
		for(int i = 0; i < sentenceElement.length; i++)
			if(!stopWordMap.containsKey(sentenceElement[i].toLowerCase()))
				cleanSentence += SPACE + sentenceElement[i];
		
		return(cleanSentence);
	}

}
