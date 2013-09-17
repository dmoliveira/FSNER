package lbd.NewModels.FeatureFilters;

import iitb.CRF.DataSequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Utils.Symbol;

public class SingleTermDictionary3Component {

	protected static final String DICTIONARY_UNIVERSAL_DIRECTORY = Parameters.Directory.dictionary + "MSM13/LOC/";
	protected final int WINDOW_SIZE = 3;

	protected static ArrayList<HashMap<String, HashMap<String, Integer>>> dictionaryList;
	protected static ArrayList<String> dictionaryNameList;

	//-- To optimize generateTermWindowSize
	protected static DataSequence currentSequence;
	protected static int currentIndex;
	protected static String [] termsWindowSizeList;

	public SingleTermDictionary3Component() {

		dictionaryList = new ArrayList<HashMap<String,HashMap<String, Integer>>>();
		dictionaryNameList = new ArrayList<String>();

	}

	public void initialize() {

		//-- Load Dictionary Name List
		loadDictionaryNameList();

		//-- Load all dictionaries from the universal directory
		loadAllDictionary();
	}

	protected void loadDictionaryNameList() {

		File folder = new File(DICTIONARY_UNIVERSAL_DIRECTORY);
		File[] listOfFiles = folder.listFiles();

		String dictionaryFilenameAddress;

		for (int i = 0; i < listOfFiles.length; i++) {

			dictionaryFilenameAddress = DICTIONARY_UNIVERSAL_DIRECTORY + listOfFiles[i].getName();

			if (listOfFiles[i].isFile()) {
				dictionaryNameList.add(dictionaryFilenameAddress);
			}
		}
	}

	protected void loadAllDictionary() {
		for(String dictionaryFilenameAddress : dictionaryNameList) {
			loadDictionary(dictionaryFilenameAddress);
		}
	}

	protected void loadDictionary(String dictionaryFilenameAddress) {

		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(dictionaryFilenameAddress), Parameters.dataEncoding));

			dictionaryList.add(new HashMap<String, HashMap<String, Integer>> ());
			HashMap<String, HashMap<String, Integer>> dictionaryMap = dictionaryList.get(dictionaryList.size() - 1);

			String entry;
			String [] entryElement;

			int entryId = 0;

			String entryPreprocessed = Symbol.EMPTY;

			while((entry = in.readLine()) != null) {

				entryElement = entry.split(Symbol.SPACE);
				entryPreprocessed = entry;

				if(entryPreprocessed.length() > 0 && entryElement.length > 0) {

					if(!dictionaryMap.containsKey(entryElement[0])) {
						dictionaryMap.put(entryElement[0], new HashMap<String, Integer>());
					}

					dictionaryMap.get(entryElement[0]).put(entryPreprocessed, ++entryId);
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

	protected int getSequenceInstanceIdSub(int dictionaryNameIndex, DataSequence sequence, int index) {

		int id = -1;

		if(currentSequence != sequence || currentIndex != index) {
			termsWindowSizeList = generateTermsWindowSize(sequence, index);

			currentSequence = sequence;
			currentIndex = index;
		}

		HashMap<String, Integer> entryDictionaryMap = dictionaryList.get(
				dictionaryNameIndex).get(sequence.x(index));

		if(entryDictionaryMap != null) {
			for(int j = termsWindowSizeList.length - 1; j >= 0; j--) {

				if(termsWindowSizeList[j] == null) {
					continue;
				}

				if(entryDictionaryMap.containsKey(termsWindowSizeList[j])) {
					id = entryDictionaryMap.get(termsWindowSizeList[j]);
					break;
				}
			}
		}

		return (id);
	}

	protected String[] generateTermsWindowSize(DataSequence sequence, int index) {

		String term;
		String [] termsWindowSizeList = new String[WINDOW_SIZE + 1];

		for(int i = 0; i < termsWindowSizeList.length; i++) {

			termsWindowSizeList[i] = Symbol.EMPTY;

			for(int j = index; j < index + i + 1 && j < index + termsWindowSizeList.length && j < sequence.length(); j++) {

				term = (String)sequence.x(j);

				if(term.length() > 0) {
					termsWindowSizeList[i] += ((j != index && !((String)sequence.x(j)).isEmpty())?
							Symbol.SPACE : Symbol.EMPTY) + (String)sequence.x(j);
				}
			}

			if(i > 0 && termsWindowSizeList[i - 1].equals(termsWindowSizeList[i])) {
				termsWindowSizeList[i] = null;
				break;
			}
		}

		return(termsWindowSizeList);
	}

	public static int getDictionaryListNumber() {

		File folder = new File(DICTIONARY_UNIVERSAL_DIRECTORY);
		File[] listOfFiles = folder.listFiles();

		int dictionaryNumberList = 0;

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				dictionaryNumberList++;
			}
		}

		return(dictionaryNumberList);
	}

}
