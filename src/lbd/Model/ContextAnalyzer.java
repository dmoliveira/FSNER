package lbd.Model;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lbd.CRF.HandlingSequenceSet;
import lbd.CRF.SequenceSet;
import lbd.CRF.HandlingSequenceSet.FileType;

public class ContextAnalyzer {

	private SupportContext supportContext;

	// -- ISO-8859-1 accepts accents
	private final String ENCODE_USED = "ISO-8859-1";
	private final String FILE_EXTENSION = ".log";
	private final String TAG_CONTEXT_ANALYZED = "(CxtAnalyzed)";

	private String inputFileAddress;
	private String outputFileAddress;
	private String tagSet;

	private enum ContextType {
		PREFIX, SUFFIX, ALL
	};

	private HashMap<String, Integer> overallFrequency;

	/**
	 * ContextAnalyzer (Constructor):
	 * 
	 * @param tagSet
	 */
	public ContextAnalyzer(String tagSet) {

		this.tagSet = tagSet;
		supportContext = new SupportContext(this.tagSet);
	}

	/**
	 * loadContext():
	 */
	private void loadContext() {

		SequenceSet trainingSequenceSet = HandlingSequenceSet
				.transformFileInSequenceSet(inputFileAddress,
						FileType.TRAINING, false);

		supportContext.generateContext(trainingSequenceSet);
	}

	/**
	 * analyzeContext():
	 * 
	 * @param inputFile
	 */
	public void analyzeContext(String inputFile) {

		this.inputFileAddress = inputFile;
		outputFileAddress = generateOutputFileAddress(inputFile);

		loadContext();
		generateContextStatistics();

	}

	/**
	 * generateContextStatistics():
	 */
	private void generateContextStatistics() {

		String entityName;

		HashMap<String, String> entityContextAnalyzed = new HashMap<String, String>();
		ArrayList<HashMap<String, Integer>> frequencyList = new ArrayList<HashMap<String, Integer>>();

		try {

			Writer out = new OutputStreamWriter(new FileOutputStream(
					outputFileAddress), ENCODE_USED);

			for (ContextToken contextToken : supportContext.getContextList()) {

				entityName = contextToken.getTokenValue().toLowerCase();

				if (!entityContextAnalyzed.containsValue(entityName)) {

					entityContextAnalyzed.put(entityName, entityName);
					frequencyList = executeContextFrequencyCalculation(entityName);
					writeContextEntityAnalyzed(out, entityName, frequencyList);

				}
			}

			out.flush();
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * executeContextFrequencyCalculation():
	 * 
	 * @param entityName
	 * @return
	 */
	private ArrayList<HashMap<String, Integer>> executeContextFrequencyCalculation(
			String entityName) {

		ArrayList<HashMap<String, Integer>> frequencyList = new ArrayList<HashMap<String, Integer>>();

		HashMap<String, Integer> prefixFrequency;
		HashMap<String, Integer> suffixFrequency;
		overallFrequency = new HashMap<String, Integer>();

		prefixFrequency = calculateContextFrequency(entityName,
				ContextType.PREFIX);
		suffixFrequency = calculateContextFrequency(entityName,
				ContextType.SUFFIX);

		frequencyList.add(prefixFrequency);
		frequencyList.add(suffixFrequency);
		frequencyList.add(overallFrequency);

		return (frequencyList);
	}

	/**
	 * calculateContextFrequency():
	 * 
	 * @param contextEntity
	 *            The entity what the contexto will be analysed
	 * @param contextType
	 *            Type of context analysed (Prefix or Suffix)
	 * @return The hashmap with the frequency
	 */
	private HashMap<String, Integer> calculateContextFrequency(
			String contextEntity, ContextType contextType) {

		HashMap<String, Integer> contextFrequency = new HashMap<String, Integer>();
		ArrayList<Token> context;

		for (ContextToken contextToken : supportContext.getContextList()) {

			if ((contextToken.getTokenValue().toLowerCase())
					.equals(contextEntity.toLowerCase())) {

				context = (contextType == ContextType.PREFIX) ? contextToken
						.getPrefix() : contextToken.getSuffix();

				for (Token token : context) {
					addFrequency(token, contextFrequency);
					addFrequency(token, overallFrequency);
				}
			}
		}

		return (contextFrequency);

	}

	/**
	 * addFrequency():
	 * 
	 * @param token
	 * @param contextFrequency
	 */
	private void addFrequency(Token token,
			HashMap<String, Integer> contextFrequency) {

		// -- Initial frequency
		int frequency = 1;

		if (contextFrequency.containsKey(token.getValue().toLowerCase())) {

			frequency += contextFrequency.get(token.getValue().toLowerCase());
			contextFrequency.put(token.getValue().toLowerCase(), frequency);

		} else {
			contextFrequency.put(token.getValue().toLowerCase(), frequency);
		}
	}

	private void writeContextEntityAnalyzed(Writer out, String entityName,
			ArrayList<HashMap<String, Integer>> frequencyList) throws IOException {
			
		int count;
		int totalWordFrequency;
		int contextTypeCount = -1;
		
		String [] contextType = {"iCxt", "oCxt", "Total"};
		String contextTypeHeaderLine = ""; 
		String lineToWrite = "";
		
		System.out.println("** Writing context related with " + entityName.toUpperCase());
		
		out.write(entityName + ":\n");
		
		for(HashMap<String, Integer> frequency : frequencyList) {
			
			count = 0;
			totalWordFrequency = 0;
			contextTypeCount++;		
			lineToWrite = "";
			
			for (Map.Entry<String, Integer> entry : frequency.entrySet()) {
				
				lineToWrite += "{" + entry.getValue() + "," + entry.getKey() + "}";
			    count++;
			    totalWordFrequency += entry.getValue();
			}
			
			contextTypeHeaderLine = "\t" + contextType[contextTypeCount];
			contextTypeHeaderLine += "(" + count + "," + totalWordFrequency +"){";
			
			out.write(contextTypeHeaderLine);
			out.write(lineToWrite);
			out.write("}\n");
		}
		
		out.write("\n");
	}
	
	/**
	 * generateOutputFileAddress():
	 * @param inputFileAddress
	 * @return
	 */
	private String generateOutputFileAddress(String inputFileAddress) {
		
		int endInputFileAddressIndex = inputFileAddress.lastIndexOf(".");
		String outputFileAddress = inputFileAddress.substring(0, endInputFileAddressIndex);
		outputFileAddress += TAG_CONTEXT_ANALYZED + FILE_EXTENSION;
		
		return(outputFileAddress);
	}
	
	/**
	 * main():
	 * @param args
	 */
	public static void main(String [] args) {
		
		String inputFileAddress= "./samples/data/bcs2010/Twitter-33850(34000)BILOU-ND-Bsc-R1.tagged";
		String tagSet = "BILOU";
		
		ContextAnalyzer contextAnalyzer = new ContextAnalyzer(tagSet);
		contextAnalyzer.analyzeContext(inputFileAddress);
		
	}

}
