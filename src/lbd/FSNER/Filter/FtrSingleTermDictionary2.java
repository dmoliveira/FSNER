package lbd.FSNER.Filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import lbd.FSNER.Component.SequenceLabel;
import lbd.FSNER.Configuration.Parameters;
import lbd.FSNER.Model.AbstractDataPreprocessor;
import lbd.FSNER.Model.AbstractFilter;
import lbd.FSNER.Model.AbstractFilterScoreCalculatorModel;
import lbd.FSNER.Utils.ClassName;
import lbd.FSNER.Utils.Symbol;
import lbd.data.handler.ISequence;

public class FtrSingleTermDictionary2 extends AbstractFilter{

	private static final long serialVersionUID = 1L;

	protected static final String DICTIONARY_UNIVERSAL_DIRECTORY = "./samples/data/bcs2010/AutoTagger/Dictionary/";

	protected static ArrayList<HashMap<String, Object>> dictionaryList;
	protected static ArrayList<String> dictionaryNameList;
	protected static HashMap<String, Boolean> dictionaryLoadMap;
	protected int dictionaryNameIndex;

	protected AbstractDataPreprocessor dataProcessor;

	public FtrSingleTermDictionary2(int preprocessingTypeNameIndex,
			AbstractFilterScoreCalculatorModel scoreCalculator, AbstractDataPreprocessor dataProcessor, int dictionaryNameIndex) {

		super(ClassName.getSingleName(FtrSingleTermDictionary2.class.getName()),
				preprocessingTypeNameIndex, scoreCalculator);

		if(dictionaryList == null) {
			dictionaryList = new ArrayList<HashMap<String,Object>>();
			dictionaryNameList = new ArrayList<String>();
			dictionaryLoadMap = new HashMap<String, Boolean>();
		}

		this.dataProcessor = dataProcessor;
		this.dictionaryNameIndex = dictionaryNameIndex;
	}

	@Override
	public void initialize() {
		if(dictionaryList.size() == 0) {
			//-- Load Dictionary Name List
			loadDictionaryNameList();

			//-- Load all dictionaries from the universal directory
			loadAllDictionary();
		}

		mActivityName += Symbol.SQUARE_BRACKET_LEFT + dictionaryNameList.get(dictionaryNameIndex).substring(
				dictionaryNameList.get(dictionaryNameIndex).lastIndexOf(Symbol.SLASH) + 1) + Symbol.SQUARE_BRACKET_RIGHT;
	}

	protected void loadDictionaryNameList() {

		File folder = new File(DICTIONARY_UNIVERSAL_DIRECTORY);
		File[] listOfFiles = folder.listFiles();

		String dictionaryFilenameAddress;

		for (int i = 0; i < listOfFiles.length; i++) {

			dictionaryFilenameAddress = DICTIONARY_UNIVERSAL_DIRECTORY + listOfFiles[i].getName();

			if (listOfFiles[i].isFile() && !dictionaryLoadMap.containsKey(dictionaryFilenameAddress)) {
				dictionaryNameList.add(dictionaryFilenameAddress);
				dictionaryLoadMap.put(dictionaryFilenameAddress, false);
				//System.out.println("--" + listOfFiles[i].getName());
			}
		}
	}

	protected void loadAllDictionary() {

		for(String dictionaryFilenameAddress : dictionaryNameList) {
			if(!dictionaryLoadMap.get(dictionaryFilenameAddress)) {
				loadDictionary(dictionaryFilenameAddress);
				dictionaryLoadMap.put(dictionaryFilenameAddress, true);
			}
		}
	}

	protected void loadDictionary(String dictionaryFilenameAddress) {

		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(dictionaryFilenameAddress), Parameters.DataHandler.mDataEncoding));

			dictionaryList.add(new HashMap<String, Object> ());

			String entry;
			String [] entryElement;
			String entryPreprocessed;

			HashMap<String, Object> dictionaryMap = dictionaryList.get(dictionaryList.size() - 1);

			while((entry = in.readLine()) != null) {

				entryElement = entry.split(Symbol.SPACE);

				for(int i = 0; i < entryElement.length; i++) {

					entryPreprocessed = dataProcessor.preprocessingTerm(entryElement[i], -1).getTerm();
					dictionaryMap.put(entryPreprocessed, null);
					//System.out.println(entryPreprocessed);
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
	public void loadActionBeforeSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionBeforeSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadTermSequence(SequenceLabel sequenceLabelProcessed, int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceIteration(
			SequenceLabel sequenceLabelProcessed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadActionAfterSequenceSetIteration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjust(SequenceLabel sequenceProcessedLabel) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSequenceInstanceIdSub(ISequence pSequence,
			SequenceLabel sequenceLabelProcessed, int index) {

		String id = Symbol.EMPTY;
		HashMap<String, Object> dictionaryMap;

		for(int i = 0; i < dictionaryNameList.size(); i++) {

			dictionaryMap = dictionaryList.get(i);

			if(dictionaryMap.containsKey(sequenceLabelProcessed.getTerm(index))) {
				id = "id:" + this.mId + ".dic:" + dictionaryNameList.get(i);
				break;
			}
		}

		return (id);
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
